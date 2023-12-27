# Spring Speech To Text

Java implementation for converting speech to text using AWS Transcribe and Azure Speech Services.

## Installation

```bash
# Clone the repository
git https://github.com/luizveronesi/spring-speech-text.git

# Navigate to the project directory
cd spring-speech-text

# Install dependencies
mvn install
```

```bash
# Docker installation
mvn clean package -f pom.xml -U
docker build . -t spring-speech-text-example:latest
docker create --name spring-speech-text-example --network your-network --ip x.x.x.x --restart unless-stopped spring-speech-text-example:latest bash
docker start spring-speech-text-example
```

## Usage

```bash
# Run the application
java -jar target/api.jar
```

Open Swagger: http://localhost:8080/swagger-ui/index.html

## Configuration

All configuration parameters must be set at file src/main/resources/application.yml.

### AWS Transcribe

This implementation uploads the file to a S3 bucket and processes the audio file in batch.

With the unique identifier for the operation, it is possible to check if the job has already been executed and get the transcribed text.

```bash
spring:
  cloud:
    aws:
      region:
        static: us-east-1
      credentials:
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}
      transcribe:
        endpoint: https://transcribe.us-east-1.amazonaws.com
        bucket: YOUR_BUCKET
```

### Azure Speech Services

Attention: this service is not working properly.

The results are completely different and the quality is worse than the same operation in AWS Transcribe.

I recommend using AWS Transcribe while this code isn't improved.

```bash
spring:
  cloud:
    azure:
      speech:
        services:
          subscription-key: ${AZURE_SPEECH_SERVICES_KEY}
          region: eastus
```

## Endpoint

### POST /

Upload an image and extract its text.

#### Request

|         Parameter |     Type      | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| ----------------: | :-----------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|            `file` | MultipartFile | The audio file itself.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|            `type` |    option     | Select the engine to extract the text from the audio file. Available options: AWS, AZURE.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|        `language` |    string     | The language code. Available: [en-IE, ar-AE, pa-IN, be-BY, te-IN, zh-TW, en-US, uk-UA, sw-KE, gu-IN, ta-IN, en-AB, ug-CN, su-ID, bn-IN, hy-AM, en-IN, sl-SI, ab-GE, zh-CN, ar-SA, eu-ES, en-ZA, gd-GB, cy-WL, uz-UZ, tl-PH, so-SO, sk-SK, rw-RW, ro-RO, pl-PL, no-NO, mt-MT, mr-IN, mn-MN, mk-MK, lv-LV, lt-LT, is-IS, hu-HU, hr-HR, ha-NG, fi-FI, et-ET, bg-BG, az-AZ, th-TH, tr-TR, ru-RU, pt-PT, nl-NL, it-IT, id-ID, fr-FR, es-ES, de-DE, sw-RW, sw-TZ, sr-RS, ps-AF, or-IN, kn-IN, ga-IE, af-ZA, wo-SN, tt-RU, sw-BI, en-NZ, ko-KR, el-GR, ba-RU, hi-IN, de-CH, vi-VN, cy-GB, ml-IN, ms-MY, he-IL, cs-CZ, ka-GE, si-LK, gl-ES, lg-IN, kab-DZ, da-DK, en-AU, zu-ZA, mhr-RU, ast-ES, pt-BR, en-WL, sw-UG, ky-KG, ckb-IQ, bs-BA, fa-IR, kk-KZ, ckb-IR, sv-SE, ja-JP, mi-NZ, ca-ES, es-US, fr-CA, en-GB]. Obs: not all languages have been tested. |
| `numParticipants` |    number     | Inform the number of different participants in the recording.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|     `mediaFormat` |    string     | Inform the media type for the upload file. Tested with mp3 and wav.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |

#### Response

|   Parameter |       Type        | Description                                                                                                                                                                                                                                                                                 |
| ----------: | :---------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|       `uid` |      string       | Unique identifier for the trnascription operation.                                                                                                                                                                                                                                          |
|   `results` |  list of objects  | The object from each engine response. If type is AWS, it is a Transcribe (dev.luizveronesi.speech.model.Transcribe). If type is Azure, it is a list of SpeechRecognitionResult (https://learn.microsoft.com/en-us/java/api/com.microsoft.cognitiveservices.speech.speechrecognitionresult). |
| `sentences` | list of sentences | List of sentence objects (dev.luizveronesi.speech.model.Sentence) with extracted text, start and end times (in seconds) and the participant.                                                                                                                                                |

## Next steps

Implement unit tests.

Improve Azure Speech Service implementation: add batch processing as in AWS, check if other formats are allowed and get duration and participant for each sentence.
