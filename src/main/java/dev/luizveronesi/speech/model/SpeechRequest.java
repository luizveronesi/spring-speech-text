package dev.luizveronesi.speech.model;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeechRequest {

    @NotNull
    private SpeechType type;

    @NotNull
    private MultipartFile file;

    @NotNull
    private Integer numParticipants;

    @NotNull
    private String language;

    @NotNull
    private String mediaFormat;

    public Integer getNumParticipants() {
        if (this.numParticipants == null)
            return 0;
        return this.numParticipants;
    }
}
