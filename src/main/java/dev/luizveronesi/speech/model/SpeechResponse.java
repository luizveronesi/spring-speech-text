package dev.luizveronesi.speech.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SpeechResponse {

	private String uid;

	private List<Sentence> sentences;

	// dev.luizveronesi.speech.model.Transcribe or
	// com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
	private Object result;
}
