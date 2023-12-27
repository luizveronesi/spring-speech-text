package dev.luizveronesi.speech.model;

public enum SpeechType {
	AWS, AZURE;

	public String getName() {
		return this.name();
	}
}
