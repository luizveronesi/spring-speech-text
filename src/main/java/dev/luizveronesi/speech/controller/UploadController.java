package dev.luizveronesi.speech.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.luizveronesi.speech.controller.documentation.UploadControllerDocumentation;
import dev.luizveronesi.speech.model.SpeechJobRequest;
import dev.luizveronesi.speech.model.SpeechRequest;
import dev.luizveronesi.speech.model.SpeechResponse;
import dev.luizveronesi.speech.service.SpeechTextService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UploadController implements UploadControllerDocumentation {

    private final SpeechTextService uploadService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SpeechResponse> upload(@ModelAttribute SpeechRequest request) {
        return new ResponseEntity<>(uploadService.execute(request), HttpStatus.OK);
    }

    @PostMapping(value = "/results")
    public ResponseEntity<SpeechResponse> getResults(@RequestBody SpeechJobRequest request) {
        return new ResponseEntity<>(uploadService.getResults(request), HttpStatus.OK);
    }
}
