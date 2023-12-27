package dev.luizveronesi.speech.service.strategy;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClient;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.Media;
import com.amazonaws.services.transcribe.model.Settings;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.TranscriptionJob;
import com.amazonaws.services.transcribe.model.TranscriptionJobStatus;
import com.fasterxml.jackson.core.type.TypeReference;

import dev.luizveronesi.speech.model.Sentence;
import dev.luizveronesi.speech.model.SpeechJobRequest;
import dev.luizveronesi.speech.model.SpeechRequest;
import dev.luizveronesi.speech.model.SpeechResponse;
import dev.luizveronesi.speech.model.SpeechType;
import dev.luizveronesi.speech.model.Transcribe;
import dev.luizveronesi.speech.utils.JsonUtil;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AwsTranscribeStrategy implements SpeechStrategy {

	@Value("${spring.cloud.aws.region.static:}")
	private String awsAccountRegion;

	@Value("${spring.cloud.aws.credentials.access-key:}")
	private String awsCredentialKey;

	@Value("${spring.cloud.aws.credentials.secret-key:}")
	private String awsCredentialSecret;

	@Value("${spring.cloud.aws.transcribe.endpoint:''}")
	private String awsAccountEndpoint;

	@Value("${spring.cloud.aws.transcribe.bucket:''}")
	private String awsBucket;

	private final S3Template s3Template;

	@Async
	public SpeechResponse extract(SpeechRequest request) {
		// first the file must be uploaded to S3
		String uriFile;
		try {
			uriFile = this.upload(request.getFile());
		} catch (Exception e) {
			throw new RuntimeException("error uploading file: " + ExceptionUtils.getStackTrace(e));
		}

		// create an uid with the number of participants, so the request does not need
		// send it again for getting the results
		var uid = UUID.randomUUID().toString() + "_" + request.getNumParticipants();

		Media media = new Media();
		media.setMediaFileUri(uriFile);

		var transRequest = new StartTranscriptionJobRequest();
		transRequest.setTranscriptionJobName(uid);
		transRequest.withMedia(media);

		if (request.getLanguage() != null)
			transRequest.withLanguageCode(request.getLanguage());

		if (request.getMediaFormat() != null)
			transRequest.withMediaFormat(request.getMediaFormat().toLowerCase());

		var settings = new Settings();
		if (request.getNumParticipants() != null)
			settings.setMaxSpeakerLabels(request.getNumParticipants());

		settings.setShowSpeakerLabels(Boolean.TRUE);
		transRequest.setSettings(settings);

		this.getClient().startTranscriptionJob(transRequest);

		return SpeechResponse.builder()
				.uid(uid)
				.build();
	}

	public SpeechResponse getResults(SpeechJobRequest request) {
		var client = this.getClient();
		TranscriptionJob transcriptionJob = this.getTranscriptionJob(client, request.getId());

		if (this.isTranscribeJobFailed(transcriptionJob))
			throw new RuntimeException("Error in AWS processing " + transcriptionJob.getFailureReason());

		if (this.isTranscribeJobCompleted(transcriptionJob)) {
			try {
				var transcribe = this.convertJob(transcriptionJob);
				return SpeechResponse.builder()
						.uid(request.getId())
						.result(transcribe)
						.sentences(this.convertSentences(transcribe))
						.build();
			} catch (Exception e) {
				throw new RuntimeException("Error processing AWS response: " + e.getMessage());
			}
		}

		return SpeechResponse.builder()
				.uid(request.getId())
				.build();
	}

	private Transcribe convertJob(TranscriptionJob transcriptionJob) throws Exception {
		String transcriptContent;
		var transcriptUri = transcriptionJob.getTranscript().getTranscriptFileUri();
		try {
			URL url = new URL(transcriptUri);
			URLConnection connection = url.openConnection();
			transcriptContent = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("error processing transcription from uri: " + transcriptUri + e.getMessage());
		}

		return JsonUtil.deserialize(
				transcriptContent,
				new TypeReference<Transcribe>() {
				});
	}

	private List<Sentence> convertSentences(Transcribe transcribe) {
		List<Transcribe.TranscriptionItem> items = transcribe.getResults().getItems();
		List<Sentence> sentences = new ArrayList<>();
		for (Transcribe.TranscriptionSegment segment : transcribe.getResults().getTranscription().getSegments()) {
			StringJoiner sb = new StringJoiner(" ");
			for (Transcribe.TranscriptionItem item : segment.getItems()) {
				Optional<Transcribe.TranscriptionItem> optional = items.stream().filter(
						(t -> !StringUtils.isEmpty(t.getStartTime()) && !StringUtils.isEmpty(t.getEndTime())
								&& t.getStartTime().equals(item.getStartTime())
								&& t.getEndTime().equals(item.getEndTime())))
						.findFirst();

				if (optional.isPresent()) {
					Transcribe.TranscriptionItem token = optional.get();
					if (token.getAlternatives() != null && token.getAlternatives().size() > 0) {
						sb.add(token.getAlternatives().get(0).getContent());
					}
				}
			}

			sentences.add(Sentence.builder()
					.start(segment.getStartTime())
					.end(segment.getEndTime())
					.value(sb.toString())
					.participant(segment.getSpeakerLabel())
					.build());
		}
		return sentences;
	}

	private Boolean isTranscribeJobCompleted(TranscriptionJob transcriptionJob) {
		return transcriptionJob.getTranscriptionJobStatus().equals(TranscriptionJobStatus.COMPLETED.name());
	}

	private Boolean isTranscribeJobFailed(TranscriptionJob transcriptionJob) {
		return transcriptionJob.getTranscriptionJobStatus().equals(TranscriptionJobStatus.FAILED.name());
	}

	private TranscriptionJob getTranscriptionJob(AmazonTranscribe client, String uid) {
		GetTranscriptionJobRequest jobRequest = new GetTranscriptionJobRequest();
		jobRequest.setTranscriptionJobName(uid);
		return client.getTranscriptionJob(jobRequest).getTranscriptionJob();
	}

	private AmazonTranscribe getClient() {
		return AmazonTranscribeClient.builder()
				.withCredentials(this.getCredentialsProvider())
				.withRegion(awsAccountRegion)
				.build();
	}

	private AWSStaticCredentialsProvider getCredentialsProvider() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(awsCredentialKey, awsCredentialSecret);
		return new AWSStaticCredentialsProvider(credentials);
	}

	private String upload(MultipartFile file) throws Exception {
		var resource = s3Template.upload(awsBucket,
				file.getOriginalFilename(),
				file.getInputStream());

		System.out.println(resource.getURI());

		return resource.getURI().toString();
	}

	@Override
	public SpeechType getStrategyName() {
		return SpeechType.AWS;
	}
}