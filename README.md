# Real-Time Messenger Application

[![Project Status](https://img.shields.io/badge/status-active-brightgreen.svg)]()
[![Java Version](https://img.shields.io/badge/java-17-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/spring--boot-3.x-green.svg)]()
[![Android SDK](https://img.shields.io/badge/android-34%2B-blue.svg)]()
[![MQTT Broker](https://img.shields.io/badge/mqtt-Mosquitto-purple.svg)]()
[![Database](https://img.shields.io/badge/database-PostgreSQL-blue.svg)]()

A cross-platform real-time instant messaging application built with a robust Spring Boot backend, a real-time Eclipse Mosquitto MQTT broker, and an offline-first Android mobile client.

---

## 📖 Table of Contents
1. [Overview](#-overview)
2. [Key Features](#-key-features)
3. [System Architecture](#-system-architecture)
4. [Technology Stack](#-technology-stack)
5. [Directory Layout](#-directory-layout)
6. [Quick Start Guides](#-quick-start-guides)

---

## 🌟 Overview

This project is a modern chat application that implements high-performance messaging with strict offline reliability. The architecture is designed to support:
- **Instant message transmission** with low-overhead MQTT packet transfers.
- **Robust offline experience** where all actions (sending messages, status updates) are logged into a local database and synchronized automatically once network connection is restored.
- **Rich media and signaling** support for file/image transfer and audio/video calling.

---

## ✨ Key Features

### 💬 Real-Time Messaging & Presence
- Instant messaging between users (1-to-1) and groups.
- Message delivery indicators: `Sending` ➡️ `Sent` ➡️ `Delivered` ➡️ `Read`.
- User online presence tracking and "Last seen at..." status.

### 📶 Offline-First Capabilities (Sync Queue)
- Full messaging functionality when completely offline.
- Messages are saved directly to Room DB with a `PENDING` sync state.
- A local **Sync Queue** logs unsent payloads and automatically publishes them to the server once the device goes back online.
- Null-safe UI adapter bindings prevent rendering failures during offline transitions.

### 📞 Call Signaling Integration
- REST APIs designed for establishing, answering, rejecting, and terminating voice and video calls.
- WebRTC signaling data exchange endpoint support to establish peer-to-peer media paths.

### 📁 Media Sharing & Utilities
- Upload attachments, images, and files inside chat rooms.
- Server integration with **Cloudinary** for image caching and media streaming optimization.
- Email verification support via SMTP.

---

## 🏗️ System Architecture

The following diagram illustrates the interaction flow between the Android application, the Spring Boot API, the PostgreSQL database, and the Mosquitto MQTT message broker:

```mermaid
graph TD
    subgraph Client ["Mobile Client (Android App)"]
        UI["UI (Activities & Fragments)"]
        VM["ViewModels"]
        RoomDB[("Room DB (Local Cache)")]
        Retrofit["Retrofit HTTP Client"]
        MQTTClient["MQTT Messaging Service"]
        SyncQ["Sync Queue & Workers"]
    end

    subgraph Broker ["Message Broker"]
        Mosquitto["Eclipse Mosquitto MQTT Broker"]
    end

    subgraph Server ["Backend API (Spring Boot App)"]
        Controller["Controllers & REST APIs"]
        MqttHandler["MQTT Event Handlers"]
        DBService["Database Services"]
        CloudinaryClient["Cloudinary API Client"]
        MailSender["Spring Mail Sender"]
    end

    subgraph Data ["Databases & External Storage"]
        Postgres[("PostgreSQL Database")]
        CloudStorage[("Cloudinary Storage")]
        SMTP[("SMTP Mail Server")]
    end

    %% Client internal flow
    UI <--> VM
    VM <--> RoomDB
    VM <--> Retrofit
    VM <--> MQTTClient
    SyncQ <--> RoomDB
    SyncQ <--> Retrofit

    %% Client-Server connections
    Retrofit <-->|REST API / HTTP| Controller
    MQTTClient <-->|MQTT Publish/Subscribe| Mosquitto
    Controller <-->|Publish Event| Mosquitto

    %% Backend integrations
    Controller <--> DBService
    MqttHandler <--> DBService
    DBService <--> Postgres
    Controller <--> CloudinaryClient
    CloudinaryClient <--> CloudStorage
    DBService <--> MailSender
    MailSender <--> SMTP
```

---

## 🛠️ Technology Stack

| Component | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Backend API** | Spring Boot | `3.x` | Core backend container, security context & REST endpoints |
| **Database** | PostgreSQL | `15` | Relational storage for user accounts, credentials, and persistent messages |
| **MQTT Broker** | Eclipse Mosquitto | `2.x` | Real-time message exchange and topic subscription routing |
| **Client Core** | Native Android | Java | MVVM clean architecture client |
| **Client DB** | Room DB | `2.6.x` | Local cache SQL database layer for offline capability |
| **Networking** | Retrofit & OkHttp | `2.9.x` | REST API communication, token interceptors, and logging |
| **Realtime Sync** | Paho MQTT client | `1.2.5` | Handles publish/subscribe connection from the mobile app |
| **Image Loading** | Glide | `4.16` | Asynchronous image loading, resizing, and memory caching |

---

## 📁 Directory Layout

```
messenger-develop/
├── backend-api/               # Backend codebase and configurations
│   ├── docker-compose.yml     # Docker architecture orchestrator
│   ├── .env.example           # Environment template file
│   └── messenger/             # Spring Boot Maven Project
│
├── android-app/               # Android client codebase
│   ├── app/                   # Mobile application source modules
│   └── build.gradle.kts       # Gradle project settings
│
└── README.md                  # This file
```

---

## 🚀 Quick Start Guides

To run and configure different components of the application, please head over to their respective folders and read the guides:

*   💻 **Backend Server Guide:** [backend-api/README.md](file:///e:/mobile_project/backend-api/README.md) – Step-by-step setup for running the Spring Boot API, PostgreSQL, and MQTT Broker locally or inside Docker.
*   📱 **Android Mobile App Guide:** [android-app/README.md](file:///e:/mobile_project/android-app/README.md) – Configuration steps to build the APK, configure host connection IPs, and run the app.
