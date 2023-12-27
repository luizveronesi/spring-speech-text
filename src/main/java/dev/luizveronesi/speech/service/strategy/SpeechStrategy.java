package dev.luizveronesi.speech.service.strategy;

import dev.luizveronesi.speech.model.SpeechJobRequest;
import dev.luizveronesi.speech.model.SpeechRequest;
import dev.luizveronesi.speech.model.SpeechResponse;
import dev.luizveronesi.speech.model.SpeechType;
import dev.luizveronesi.speech.service.factory.StrategyBase;

public interface SpeechStrategy extends StrategyBase<SpeechType> {

	SpeechResponse extract(SpeechRequest request);

	SpeechResponse getResults(SpeechJobRequest request);
}
