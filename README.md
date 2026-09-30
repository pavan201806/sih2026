# RakshaVaani (रक्षावाणी)
### Offline-First Multilingual Mesh Voice Communication for Disaster Relief

[![Build APK](https://github.com/pavan201806/sih2026/actions/workflows/build-apk.yml/badge.svg)](https://github.com/pavan201806/sih2026/actions/workflows/build-apk.yml)
[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26–35)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Licence](https://img.shields.io/badge/Licence-GPL--3.0_/_Apache--2.0-blue.svg)](LICENSES.md)

---

## 1. Executive Summary

During natural disasters (floods, cyclones, earthquakes, and landslides), commercial telecom infrastructure (cellular towers, fiber backhauls, and power grids) suffers total collapse within hours. First responders and stranded civilians face two critical bottlenecks:

1. **Zero Connectivity:** Standard VoIP and messaging applications fail completely without internet or active SIM cards.
2. **Language Barriers:** Rescue teams deployed across different Indian states often cannot comprehend regional dialects or scripts under high-stress emergency conditions.

**RakshaVaani** is a resilient, 100% offline emergency communication system designed for entry-tier Android handsets. It transforms commodity smartphones into an autonomous voice-and-data mesh network without cell towers, Wi-Fi routers, or internet access.

By pairing **on-device neural Speech-to-Text (STT)**, **compact wire-frame encoding (< 52 bytes per sentence)**, **AES-256-GCM mesh transport**, and **offline Text-to-Speech (TTS) synthesis**, RakshaVaani allows a responder to speak in their native tongue and have it spoken aloud on peer handsets in the recipient's chosen regional language.

---

## 2. System Architecture & End-to-End Pipeline

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           SENDER HANDSET (Phone A)                             │
│                                                                                 │
│   [ Micro ] ──► [ AudioCapture ] ──► [ AI4Bharat IndicConformer ]              │
│   16 kHz PCM     100 ms chunks        Streaming ASR (int8 ONNX)                 │
│                                                    │                            │
│                                                    ▼                            │
│   [ AES-256-GCM ] ◄── [ FrameCodec ] ◄── [ Clause Segmenter ]                  │
│    Sealed Payload      < 52 B frame       Semantic Text Chunking                │
└──────────────────────────────────────┬──────────────────────────────────────────┘
                                       │
                     OFFLINE MULTI-RADIO AD-HOC MESH
           (Bluetooth RFCOMM · BLE 5.0 Broadcast · Wi-Fi Direct P2P)
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          RELAY NODE (Phone B - Optional)                        │
│                                                                                 │
│   [ Incoming Frame ] ──► [ Deduplication ] ──► [ TTL Decrement ] ──► [ Re-TX ] │
│                           Seen-Set Cache        TTL > 0              Jittered   │
└──────────────────────────────────────┬──────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          RECEIVER HANDSET (Phone C)                             │
│                                                                                 │
│   [ FrameCodec ] ──► [ AES-256-GCM ] ──► [ TextNormaliser ]                     │
│    De-framing         Verified Decrypt    Numerals & Lexicon Mapping            │
│                                                    │                            │
│                                                    ▼                            │
│   [ Loudspeaker ] ◄── [ AudioSink ] ◄─── [ Piper / VITS TTS ]                  │
│    Emergency Voice     AudioTrack Stream   On-Device Neural Synthesis           │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Transmission Levels
1. **Level 1 (Direct Speech Frame):** The speaker's voice is transcribed into text by an on-device int8 quantized neural model, packaged into an ultra-compact binary packet (~30–50 bytes), sealed with AES-256-GCM, and transmitted over ad-hoc radios.
2. **Level 2 (Emergency Alert):** High-priority alerts bypass transmit queues, trigger instant non-interruptible playback at maximum volume on receiving handsets, and flash the screen.
3. **Level 3 (Template Code - 1 Byte):** When acoustic models are not installed or in severe RF degradation, 1-byte pre-compiled emergency operational codes (e.g., *"Medical assistance required urgently"*) are transmitted and synthesized locally in any of the 10 supported regional languages.

---

## 3. Key Capabilities

* **100% Offline by Design:** No cellular data, no internet permission, no cloud servers, and no SIM card required.
* **Multilingual Coverage:** Architected for 10 Scheduled Indian languages:
  * **Hindi (हिन्दी)**
  * **Telugu (తెలుగు)**
  * **Tamil (தமிழ்)**
  * **Marathi (मराठी)**
  * **Bengali (বাংলা)**
  * **Gujarati (ગુજરાતી)**
  * **Kannada (ಕನ್ನಡ)**
  * **Malayalam (മലയാളം)**
  * **Odia (ଓଡ଼ିଆ)**
  * **English**
* **Multi-Radio Mesh Network:**
  * **Bluetooth RFCOMM (Classic):** Auto-dialing and connection establishment across bonded handsets.
  * **BLE 5.0 Extended Advertising:** Connectionless, zero-handshake radio broadcasts (`OnAirBuffer`).
  * **Wi-Fi Direct P2P:** Autonomous group-owner negotiation without opening Android system settings.
  * **UDP Subnet Broadcast:** High-throughput local network datagrams across Wi-Fi hotspots.
* **Store-and-Forward Mesh Relaying:**
  * 3-bit TTL (up to 7 relay hops).
  * Cyclic sequence tracking with a 512-entry sliding seen-set cache to eliminate packet storms.
  * Randomized transmission jitter (20–100 ms) preventing RF packet collisions.
* **Security & Authenticity:**
  * AES-256-GCM authenticated encryption.
  * 12-byte unencrypted frame header authenticated as Additional Authenticated Data (AAD).
  * Monotonic epoch counters mitigating replay attacks.
* **Tactical Emergency Features:**
  * **3-Second Lock Gate:** Prevents accidental pocket triggers under adverse conditions.
  * **DND-Bypassing Siren:** Maximum volume emergency acoustic alarm for immediate awareness.
  * **Acoustic Direction Finder (`LOCATE`):** Measures radio RSSI and emits acoustic cadence feedback to locate trapped team members.
  * **Sunlight-Optimised UI:** High-contrast monochrome & spectrum design system strictly adhering to WCAG AAA contrast ratios with full Indic script line-box padding.

---

## 4. Multi-Module Project Structure

The project is structured into 8 modular Kotlin/Android modules for strict separation of concerns:

```text
sih2026/
├── app/                  # Main Android UI (Jetpack Compose), Foreground Services, Platform Bindings
├── core-proto/           # Binary frame codecs, AES-256-GCM cipher, Mesh router, Template catalog
├── core-audio/           # 16 kHz PCM AudioRecord capture, Ring buffers, AudioTrack sink
├── core-asr/             # Sherpa-ONNX streaming IndicConformer speech recognition interface
├── core-tts/             # Sherpa-ONNX VITS speech synthesis, text normalisation, Hindi numerals
├── core-link/            # Radio link implementations (Bluetooth RFCOMM, BLE Broadcast, Wi-Fi Direct)
├── core-models/          # Emergency lexicons, install manifests, SHA-256 hash verifiers
├── bench/                # On-device latency tracing, Real-Time Factor (RTF) measurement harness
├── gradle/               # Version catalog (libs.versions.toml) and Gradle wrapper
├── models/               # Manifest definitions, token dictionaries, and normalization rules
└── tools/                # Build-time packaging scripts and sherpa-onnx dependency fetcher
```

---

## 5. Technology Stack

* **Operating System:** Android 8.0 (API Level 26) through Android 15 (API Level 35)
* **Architecture:** Kotlin Multiplatform / Modular Android (Kotlin 2.0.21)
* **User Interface:** Jetpack Compose with custom design tokens (`Tokens.kt`, `Palette.kt`)
* **Concurrency:** Kotlin Coroutines & Asynchronous Flow
* **Inference Engine:** Sherpa-ONNX 1.10.38 with ONNX Runtime backend
* **Acoustic Models:** AI4Bharat IndicConformer (int8 dynamic quantization, ~189 MB per language)
* **Voice Synthesis Models:** Piper VITS / Mimic 3 CMU Indic (~63 MB per language)
* **Phonemisation:** Embedded `espeak-ng` binary tables

---

## 6. Building and Installation

### A. Automated Cloud Build (GitHub Actions)

The repository includes an automated GitHub Actions CI workflow configured to build and upload the debug APK without requiring a local Android SDK setup:

1. Push your changes to the `main` branch or trigger **Workflow Dispatch** under the **Actions** tab.
2. The workflow automatically provisions JDK 17, downloads the Android SDK, fetches the `sherpa-onnx` native AAR, compiles `:app:assembleDebug`, and uploads the artifact.
3. Download `app-debug.apk` directly from the GitHub Actions run artifacts.

### B. Local Build Instructions

#### Prerequisites:
* **JDK:** OpenJDK 17 (Temurin recommended)
* **Android SDK:** Platforms for API 35 with Build Tools 35.0.0
* **NDK:** ABI filter set to `arm64-v8a`

#### Steps:
```bash
# 1. Clone the repository
git clone https://github.com/pavan201806/sih2026.git
cd sih2026

# 2. Fetch the native Sherpa-ONNX binary library
chmod +x tools/fetch_sherpa.sh
./tools/fetch_sherpa.sh

# 3. Build the debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 7. Live Demonstration Runbook (Hackathon 2-Handset Setup)

1. **Setup:** Install `app-debug.apk` on two Android handsets (Phone A and Phone B).
2. **Bluetooth Bonding:** Pair both phones once in Android Bluetooth settings.
3. **Launch & Unlock:** Open **RakshaVaani** on both handsets; hold the center circle for 3 seconds to unlock.
4. **AI Model Setup Demonstration:**
   - Tap **☰ (Settings)** in the top bar.
   - Tap **AI Model Setup** to view the offline language model catalog.
   - Tap **Download** on English, Hindi, and Telugu STT / TTS cards to demonstrate simulated offline model preparation.
   - Tap **Continue to RakshaVaani** to return to the active operating screen.
5. **Transmitting Speech:**
   - On **Phone A** (set to Hindi): Hold the central **HOLD TO TALK** button and speak an emergency message (e.g., *"चिकित्सा सहायता चाहिए"*).
   - Release the button to transmit.
6. **Receiving & Playback:**
   - **Phone B** (set to Telugu) receives the encrypted packet over Bluetooth / Wi-Fi mesh.
   - The message renders in Telugu (*"వైద్య సహాయం కావాలి"*) and speaks aloud automatically.
7. **Emergency Alert:**
   - Tap the red **ALERT** flank on Phone A.
   - Phone B receives the alert frame, flashing the screen and sounding the emergency alert at full volume.

---

## 8. Licences & Legal Notices

This project combines open-source libraries and permissive models:

* **RakshaVaani Codebase:** Licensed under [GPL-3.0](LICENSES.md).
* **Sherpa-ONNX & ONNX Runtime:** [Apache-2.0](https://github.com/k2-fsa/sherpa-onnx) & [MIT](https://github.com/microsoft/onnxruntime).
* **AI4Bharat IndicConformer:** [Apache-2.0](https://github.com/AI4Bharat).
* **Piper TTS Voices:** [MIT](https://github.com/rhasspy/piper).
* **espeak-ng Phonemisation Data:** [GPL-3.0](LICENSES.md) (embedded in native JNI binary).

Full license texts, copyright notices, and third-party attribution are preserved in [`LICENSES.md`](LICENSES.md) and inside the in-app **About** screen.
