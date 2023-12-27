package dev.luizveronesi.speech.controller.documentation;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import dev.luizveronesi.speech.model.SpeechJobRequest;
import dev.luizveronesi.speech.model.SpeechRequest;
import dev.luizveronesi.speech.model.SpeechResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface UploadControllerDocumentation {

        @Operation
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Upload file to transcription.", content = {
                                        @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                        }),
                        @ApiResponse(responseCode = "404", description = "Resource not found", content = {
                                        @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                        }),
                        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                                        @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                        })
        })
        ResponseEntity<SpeechResponse> upload(SpeechRequest request);

        @Operation
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Retrieves transcription from job. Only for AWS batch processing.", content = {
                                        @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                        }),
                        @ApiResponse(responseCode = "404", description = "Resource not found", content = {
                                        @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                        }),
                        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {
                                        @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                        })
        })
        ResponseEntity<SpeechResponse> getResults(SpeechJobRequest request);
}