# Messenger Backend API

This repository contains the backend service for the Messenger application, built on Spring Boot, PostgreSQL database, and Eclipse Mosquitto MQTT Broker.

---

## 🛠️ Technology Stack

- **Backend Framework:** Spring Boot (Java 17)
- **Database:** PostgreSQL 15
- **Message Broker:** Eclipse Mosquitto (MQTT)
- **Containerization:** Docker & Docker Compose

---

## 🚀 Getting Started with Docker Compose

Follow these steps to spin up the entire backend stack locally inside Docker containers.

### 1. Prerequisites
Ensure you have [Docker](https://www.docker.com/products/docker-desktop/) and Docker Compose installed and running.

### 2. Configure Environment Variables
Copy the template environment file to create your local active configuration:

**On Windows (PowerShell/CMD):**
```bash
copy .env.example .env
```

**On Linux / macOS:**
```bash
cp .env.example .env
```

Open the newly created `.env` file and adjust any configurations (such as JWT keys, email settings, or Cloudinary credentials) as needed.

### 3. Run the Services
Build and start all containers in detached mode:
```bash
docker-compose up --build -d
```
This command starts:
- **`postgres-db`** on port `5432`
- **`mqtt-broker`** on port `1883`
- **`messenger-api`** on port `8080` (waits for postgres and mqtt to start up)

### 4. View Logs
To monitor the Spring Boot API logs in real-time, execute:
```bash
docker-compose logs -f messenger-api
```

### 5. Stop the Services
To stop the services and clean up containers, run:
```bash
docker-compose down
```
*Note: Your PostgreSQL database data is persistent and stored in a Docker volume named `pgdata`, so database records will not be lost when stopping containers.*

---

## 💡 Important Developer Notes

### Docker Internal Networking
- In the Docker network environment, the backend API refers to the database and broker using container names:
  - **PostgreSQL Database:** `postgres-db` (e.g. `jdbc:postgresql://postgres-db:5432/...`)
  - **MQTT Broker:** `mqtt-broker` (e.g. `tcp://mqtt-broker:1883`)
- You do **not** need to modify `application.properties` directly. The environment variables specified in `docker-compose.yml` automatically override these settings.

### Android Emulator & Physical Devices Setup
- **Android Studio Emulator:** When testing the mobile app via emulator, `localhost` or `127.0.0.1` refers to the emulator's internal loopback address. 
  - Update `RetrofitClient.java` (for API calls) and `MessagingService.java` (for MQTT connection) to point to **`10.0.2.2`** instead of `localhost` (e.g., `http://10.0.2.2:8080` and `tcp://10.0.2.2:1883`).
- **Physical Test Device (via USB/Wi-Fi):** Make sure the mobile device is on the same local network (LAN) as your computer.
  - Locate your host machine's LAN IP address (e.g., `192.168.1.x`) and configure the client URLs using that IP instead.
