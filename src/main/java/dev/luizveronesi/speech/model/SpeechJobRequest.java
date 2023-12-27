package dev.luizveronesi.speech.model;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeechJobRequest {

    @NotNull
    private SpeechType type;

    private String id;
}
