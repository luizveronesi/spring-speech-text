package dev.luizveronesi.speech.service.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;

import dev.luizveronesi.speech.model.Sentence;
import dev.luizveronesi.speech.model.SpeechJobRequest;
import dev.luizveronesi.speech.model.SpeechRequest;
import dev.luizveronesi.speech.model.SpeechResponse;
import dev.luizveronesi.speech.model.SpeechType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AzureSpeechServicesStrategy implements SpeechStrategy {

    @Value("${spring.cloud.azure.speech.services.subscription-key:}")
    private String subscriptionKey;

    @Value("${spring.cloud.azure.speech.services.region:}")
    private String region;

    public SpeechResponse getResults(SpeechJobRequest request) {
        System.out.println("not implemented");
        return null;
    }

    public SpeechResponse extract(SpeechRequest request) {
        List<SpeechRecognitionResult> results;

        try {
            results = this.getResults(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return SpeechResponse.builder()
                .result(results)
                .sentences(this.convertSentences(results))
                .build();
    }

    private List<Sentence> convertSentences(List<SpeechRecognitionResult> results) {
        List<Sentence> sentences = new ArrayList<>();
        for (var result : results) {
            var sentence = new Sentence();
            sentence.setValue(result.getText());
            sentences.add(sentence);
        }
        return sentences;
    }

    private List<SpeechRecognitionResult> getResults(SpeechRequest request) throws Exception {
        var results = new ArrayList<SpeechRecognitionResult>();

        final Semaphore doneSemaphore = new Semaphore(0);

        var speechConfig = this.authenticate();
        var pushStream = AudioInputStream.createPushStream();
        pushStream.write(request.getFile().getBytes());

        var audioConfig = AudioConfig.fromStreamInput(pushStream);
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, request.getLanguage(), audioConfig);
        recognizer.recognized.addEventListener((s, e) -> {
            if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                results.add(e.getResult());
            } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                System.out.println("NOMATCH: Speech could not be recognized.");
            }
        });

        recognizer.canceled.addEventListener((s, e) -> {
            System.out.println("CANCELED: Reason=" + e.getReason());

            if (e.getReason() == CancellationReason.Error)
                throw new RuntimeException(
                        "CANCELED: Reason=" + e.getReason()
                                + ". ErrorCode=" + e.getErrorCode()
                                + " " + e.getErrorDetails());

            doneSemaphore.release();
        });

        recognizer.sessionStarted.addEventListener((s, e) -> {
            System.out.println("\n Session started event.");
        });

        recognizer.sessionStopped.addEventListener((s, e) -> {
            System.out.println("\n Session stopped event.");
            doneSemaphore.release();
        });

        recognizer.startContinuousRecognitionAsync().get();
        doneSemaphore.tryAcquire(30, TimeUnit.SECONDS);

        recognizer.stopContinuousRecognitionAsync().get();

        pushStream.close();
        speechConfig.close();
        audioConfig.close();
        recognizer.close();

        return results;
    }

    private SpeechConfig authenticate() {
        return SpeechConfig.fromSubscription(subscriptionKey, region);
    }

    @Override
    public SpeechType getStrategyName() {
        return SpeechType.AZURE;
    }
}
