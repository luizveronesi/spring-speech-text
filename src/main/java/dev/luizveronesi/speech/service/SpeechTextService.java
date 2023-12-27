package dev.luizveronesi.speech.service;

import org.springframework.stereotype.Service;

import dev.luizveronesi.speech.model.SpeechJobRequest;
import dev.luizveronesi.speech.model.SpeechRequest;
import dev.luizveronesi.speech.model.SpeechResponse;
import dev.luizveronesi.speech.service.factory.SpeechServiceFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpeechTextService {

	private final SpeechServiceFactory speechServiceFactory;

	public SpeechResponse execute(SpeechRequest request) {
		return speechServiceFactory.getStrategy(request.getType()).extract(request);
	}

	public SpeechResponse getResults(SpeechJobRequest request) {
		return speechServiceFactory.getStrategy(request.getType()).getResults(request);
	}
}