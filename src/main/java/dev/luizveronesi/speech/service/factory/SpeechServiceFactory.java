package dev.luizveronesi.speech.service.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import dev.luizveronesi.speech.model.SpeechType;
import dev.luizveronesi.speech.service.strategy.SpeechStrategy;

@Service
public class SpeechServiceFactory {

    private final Map<SpeechType, SpeechStrategy> strategies = new HashMap<>();

    public SpeechServiceFactory(Set<SpeechStrategy> strategies) {
        this.createStrategyMap(strategies);
    }

    public SpeechStrategy getStrategy(SpeechType type) {
        return this.strategies.get(type);
    }

    private void createStrategyMap(Set<SpeechStrategy> strategies) {
        strategies.forEach(absenceStrategy -> this.strategies.put(absenceStrategy.getStrategyName(), absenceStrategy));
    }
}