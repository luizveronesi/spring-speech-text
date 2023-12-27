package dev.luizveronesi.speech.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sentence implements Serializable {

    private String start;

    private String end;

    private String participant;

    private String value;

}
