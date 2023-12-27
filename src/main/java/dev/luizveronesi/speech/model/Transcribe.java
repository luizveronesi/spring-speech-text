package dev.luizveronesi.speech.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transcribe implements Serializable {

    private String uid;
    private String jobName;
    private String accountId;
    private TranscriptionResults results;
    private String status;

    @Getter
    @Setter
    public static class TranscriptionResults implements Serializable {

        private static final long serialVersionUID = -9166409497221536870L;

        public TranscriptionResults() {
        }

        private List<TranscriptionTranscript> transcripts;

        @JsonProperty("speaker_labels")
        private Transcription transcription;

        private List<TranscriptionItem> items;
    }

    @Getter
    @Setter
    public static class TranscriptionTranscript implements Serializable {

        private static final long serialVersionUID = -5434159832375728526L;

        public TranscriptionTranscript() {
        }

        private String transcript;
    }

    @Getter
    @Setter
    public static class Transcription implements Serializable {

        private static final long serialVersionUID = -7855393065524630270L;

        public Transcription() {
        }

        private Integer speakers;
        private List<TranscriptionSegment> segments;
    }

    @Getter
    @Setter
    public static class TranscriptionSegment implements Serializable {

        private static final long serialVersionUID = 6138933281334560611L;

        public TranscriptionSegment() {
        }

        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("speaker_label")
        private String speakerLabel;

        @JsonProperty("end_time")
        private String endTime;

        private List<TranscriptionItem> items;
    }

    @Getter
    @Setter
    public static class TranscriptionItem extends TranscriptionSegment {

        private static final long serialVersionUID = -2685706222214745427L;

        public TranscriptionItem() {
        }

        private String type;
        private List<TranscriptionAlternative> alternatives;
    }

    @Getter
    @Setter
    public static class TranscriptionAlternative implements Serializable {

        private static final long serialVersionUID = -4452832070226408458L;

        public TranscriptionAlternative() {
        }

        private String confidence;
        private String content;
    }
}