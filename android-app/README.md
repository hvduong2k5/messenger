# Android Mobile Client Configuration Guide

This guide describes how to configure, build, and run the Android Messenger client application. The mobile app is designed to work in both online (real-time chat via MQTT) and offline (local database caching and syncing queue) environments.

---

## 🏗️ Client Application Architecture

The mobile app follows the **MVVM (Model-View-ViewModel)** architectural pattern:

- **View Layer:** Activities (e.g., `LoginActivity`, `ChatDetailActivity`) and Fragments managing user interaction layouts.
- **ViewModel Layer:** Manages application state, UI logic, and fetches repository-driven LiveData streams.
- **Repository Layer:** Acts as the Single Source of Truth (SSOT). Fetches from Room DB local cache and synchronizes changes over HTTP/MQTT in the background.
- **Local Persistence (Room DB):** Keeps messages, user profiles, and sync queues offline.
- **Real-Time Service:** `MessagingService` runs as a foreground service hosting the Paho MQTT Client, keeping connections active and handling instant message payloads.

---

## 🛠️ Prerequisites

- **IDE:** Android Studio (Koala, Ladybug, or newer)
- **JDK:** Java Development Kit 17+
- **SDK Requirements:**
  - `compileSdk`: 34+
  - `minSdk`: 26+

---

## ⚙️ Local Configuration

To connect the Android application to your server, configure these target connection files:

### 1. API Base URL Setup
Open [RetrofitClient.java](file:///e:/mobile_project/android-app/app/src/main/java/com/midterm/team12345/data/remote/RetrofitClient.java) and configure the `BASE_URL` value around line 28:

```java
// Replace the IP with your server's IP address (or 10.0.2.2 for emulator)
private static final String BASE_URL = "http://192.168.1.166:8080";
```

### 2. MQTT Broker URL Setup
Open [MessagingService.java](file:///e:/mobile_project/android-app/app/src/main/java/com/midterm/team12345/data/remote/mqtt/MessagingService.java) and configure the `brokerUrl` value around line 68:

```java
// Use a public broker for testing, or your local broker IP (e.g. tcp://10.0.2.2:1883)
String brokerUrl = "tcp://broker.emqx.io:1883";
```

---

## 🚀 Building & Running

### Option A: Via Android Studio (GUI)
1. Launch Android Studio and select **Open**.
2. Navigate to the `android-app/` directory and open it as an Android project.
3. Wait for Gradle Sync and indexing to finish.
4. Select your target device (Emulator or USB Debugging physical phone).
5. Click the **Run** button (green play icon) or press `Shift + F10`.

### Option B: Via Command Line (Gradle Wrapper)
You can compile and build the debug APK directly using Gradle:

- **On Windows (PowerShell/CMD):**
  ```powershell
  .\gradlew.bat assembleDebug
  ```
- **On Linux / macOS:**
  ```bash
  ./gradlew assembleDebug
  ```

Upon success, the generated installer APK will be available in:
`android-app/app/build/outputs/apk/debug/app-debug.apk`

---

## 🛜 Offline Mode & Sync Architecture

The app is built to sustain complete loss of network connectivity without impacting user experience:

- **Local Storage Cache:** In [MessageRepositoryImpl.java](file:///e:/mobile_project/android-app/app/src/main/java/com/midterm/team12345/data/repository/MessageRepositoryImpl.java), when sending a message offline, it is instantly written into Room DB with a status of `PENDING`. The list adapter displays this message immediately on the right side.
- **Sync Queue Logging:** The failed message is serialized into JSON and stored in the local `sync_queue` table via `SyncQueueDao` with a status of `PENDING`.
- **Background Resynchronization:** When the app detects a connection state transition or starts up online, it triggers `syncPendingMessages()`. This reads all pending tasks from the local queue, sends them to the server sequentially, updates the DB state to `SENT`, and deletes the task from the sync queue.
