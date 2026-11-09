# S7. 한국어 오프라인 음성 (STT / 의도 해석 / TTS) 오픈소스

- 상태: 문서·저장소 조사 완료 (2026-11-09, gh/Hugging Face API로 라이선스·크기 확인). 실제 기기 정확도·지연 측정은 안 함
- 막히면: Pleos STT/TTS SDK만 사용, 음성 명령 범위 축소

## 확인한 사실

### STT
| 순위 | 이름 | 라이선스 (코드 / 모델) | 한국어 근거 | 크기 | 안드로이드 | 비고 |
|---|---|---|---|---|---|---|
| 1 | sherpa-onnx 스트리밍 Zipformer | Apache-2.0 / Apache-2.0 | `sherpa-onnx-streaming-zipformer-korean-2024-06-16` (KsponSpeech 학습) | int8 약 132MB | GitHub 릴리스 `sherpa-onnx-2.0.1.aar` (49MB) | 스트리밍, 오프라인 |
| 2 | Vosk | Apache-2.0 / Apache-2.0 | `vosk-model-small-ko-0.22` (Zeroth WER 28.1) | 82MB | Maven `com.alphacephei:vosk-android:0.3.47` | **문법(단어 목록) 제한 인식** 지원 → 고정 명령에 유리 |
| 3 | sherpa-onnx SenseVoice | Apache-2.0 / FunASR MODEL_LICENSE v1.1 | `sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2025-09-09` | 166MB | 같은 AAR | 비스트리밍 (VAD로 짧게 잘라 사용) |
| 4 | whisper.cpp | MIT / MIT | 다국어 whisper base/small | 60~190MB | JNI 직접 빌드 | 비스트리밍, 지연 위험 |
| ✗ | Moonshine | MIT / **한국어 Tiny 비상업** | Korean Tiny 26M | – | – | 마켓 앱에 사용 불가 |
| ? | Android `SpeechRecognizer` | 플랫폼 | – | – | 내장 | AAOS/Pleos 온디바이스 한국어 인식기 존재 여부 미확인 |

- 한국어 키워드 스포팅(KWS) 모델은 sherpa-onnx에 없음

### 의도 해석
- 고정 명령 15개 수준에는 **직접 만든 규칙 매처**(정규화 + 동의어 사전 + 서수 파싱)가 충분 → `feature/voice` `VoiceCommandParser`
- Vosk 문법 모드로 인식 어휘를 묶으면 매처가 더 단순해짐
- KOMORAN (Apache-2.0, 형태소 분석기)은 고정 명령에는 과함. open-korean-text는 2024-03 이후 정체
- Picovoice Rhino는 온라인 AccessKey + 독점 모델 → 제외

### TTS
| 순위 | 이름 | 라이선스 (코드 / 모델) | 한국어 근거 | 크기 | 비고 |
|---|---|---|---|---|---|
| 1 | Supertonic (sherpa-onnx) | MIT / OpenRAIL-M (상업 허용, 사용 제한 조항) | `Supertone/supertonic-2` 언어 목록에 ko | 85~129MB | sherpa int8 패키지에 한국어가 들었는지 미확인 |
| 2 | vits-mimic3-ko_KO-kss_low | Apache / CC0 | sherpa 패키지 | 67MB | 저품질, 원천 KSS 데이터셋 라이선스 미확인 |
| 3 | MeloTTS-Korean | MIT / MIT | HF `myshell-ai/MeloTTS-Korean` | 미측정 | sherpa 변환본 없음 |
| ✗ | Piper | GPL-3.0 (후속) | ko 음성 없음 | – | 제외 |

## 결정

- **주 경로는 Pleos SDK**: `TextToSpeech:3.3.1` ON_DEVICE (키 불필요, "Fully compatible", APK 증가 없음), `SpeechToText:3.3.1`
- **오픈소스 대체 경로**: STT는 Vosk small-ko + 문법 제한(82MB) 1순위, 정확도가 부족하면 sherpa-onnx 스트리밍 Zipformer(132MB). TTS는 sherpa-onnx + Supertonic(한국어 포함 확인 후)
- 그 전 단계 구현: 안드로이드 기본 `SpeechRecognizer`/`TextToSpeech` 어댑터 (`feature/voice/android`). 모든 엔진은 `SpeechInput`/`SpeechOutput` 포트 뒤에 둔다
- 의도 해석은 엔진과 무관한 순수 Kotlin 규칙 매처
- 모델 동봉 여부(APK 크기 vs 오프라인 원칙)는 앱 리뷰 크기 제한 확인 후 결정

## 위험

- 라이선스: Zipformer 학습 데이터(KsponSpeech, AI Hub) 약관, KSS 데이터셋, OpenRAIL-M 사용 제한 고지
- APK 크기: 모델 80~170MB + sherpa AAR 49MB
- 차량 SoC 지연 미측정, 에뮬레이터(x86_64)는 실차를 대표하지 못함

## 못 확인한 것

- Pleos STT ON_DEVICE 한국어 정확도, 스트리밍 여부, 음성 명령 사용 정책
- AAOS/Pleos 에뮬레이터 내장 `SpeechRecognizer`·`TextToSpeech` 엔진과 한국어 음성 데이터
- sherpa-onnx Maven Central 좌표, 한국어 hotword 편향 동작
- Supertonic sherpa 패키지의 한국어 포함 여부와 음질
- 앱 리뷰 APK 크기 상한, NVIDIA Parakeet 한국어 지원

## 근거

- `gh api repos/k2-fsa/sherpa-onnx` (릴리스 asset), Hugging Face `/api/models` (k2-fsa, Supertone, myshell-ai), alphacephei model-list.json, Moonshine LICENSE·docs, `docs/spikes/S6.md`
