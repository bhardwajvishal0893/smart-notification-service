# Smart Notification Service 🔔

An AI-powered notification routing service that intelligently decides the best delivery channel (Email / SMS / Push) for each notification based on message priority, user preferences, and real-time context.

## Architecture

```
Client
  │
  ▼
POST /api/notifications/send
  │
  ▼
NotificationService
  ├── UserService (Redis cache lookup)
  ├── AiRoutingService (OpenAI GPT-3.5 decides channel)
  ├── NotificationRepository (PostgreSQL persist)
  └── NotificationProducer (Kafka publish)
                │
                ▼
         Kafka Topic: notification-events
         (3 partitions, partitioned by userId)
                │
                ▼
         NotificationConsumer (3 concurrent threads)
                │
                ▼
         ChannelDispatchService
         ├── EMAIL → SendGrid / AWS SES
         ├── SMS   → Twilio / AWS SNS
         └── PUSH  → Firebase FCM / APNs
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2 |
| AI Routing | Spring AI + OpenAI GPT-3.5-turbo |
| Message Queue | Apache Kafka |
| Caching | Redis |
| Database | PostgreSQL |
| Deployment | Railway |

## Key Design Decisions

**Why Kafka?**
Notification sending is fire-and-forget. The API responds immediately with `202 Accepted` and the actual delivery happens asynchronously. This decouples the API latency from the delivery latency and allows retry without blocking the caller.

**Why Redis for user caching?**
User preferences rarely change but are read on every notification. Caching in Redis avoids repeated DB hits under high notification throughput.

**Why AI routing?**
Rule-based routing (e.g., "if CRITICAL use SMS") breaks down at scale when you have thousands of user preference combinations, time zones, and message types. The LLM reasons holistically across all signals in one shot — and the routing logic stays in plain English, not nested if/else.

**Kafka partitioning by userId**
Notifications for the same user always land on the same partition, ensuring ordering guarantees (e.g., OTP before "OTP expired" message).

## API Endpoints

### Create User
```
POST /api/users
{
  "email": "user@example.com",
  "name": "John Doe",
  "phoneNumber": "+919876543210",
  "deviceToken": "firebase-token-xyz",
  "channelPreferences": ["PUSH", "EMAIL", "SMS"],
  "quietHoursStart": 22,
  "quietHoursEnd": 8
}
```

### Send Notification
```
POST /api/notifications/send
{
  "userId": 1,
  "title": "Payment Failed",
  "message": "Your payment of ₹999 failed. Please retry.",
  "priority": "CRITICAL"
}

Response (202 Accepted):
{
  "notificationId": 42,
  "status": "QUEUED",
  "channelUsed": "SMS",
  "aiRoutingReason": "CRITICAL priority payment alert; SMS chosen to ensure immediate delivery regardless of quiet hours",
  "queuedAt": "2024-05-01T14:30:00"
}
```

### Get Notification History
```
GET /api/notifications/{userId}?page=0&size=20
```

### Health Check
```
GET /actuator/health
```

## Local Setup

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker (for Kafka + Redis + PostgreSQL)

### Run with Docker Compose
```bash
# Start infrastructure
docker-compose up -d

# Run app
./mvnw spring-boot:run
```

## Deploy to Railway

1. Push this repo to GitHub
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Add these services in Railway:
   - **PostgreSQL** plugin
   - **Redis** plugin
   - **Kafka** plugin (via Upstash Kafka or RedpandaData)
4. Set environment variables:

```
DATABASE_URL=<from Railway PostgreSQL>
DATABASE_USERNAME=<from Railway>
DATABASE_PASSWORD=<from Railway>
REDIS_URL=<from Railway Redis>
KAFKA_BOOTSTRAP_SERVERS=<from Railway Kafka>
OPENAI_API_KEY=<your OpenAI key>
```

5. Railway auto-detects Dockerfile and deploys. Done.

## AI Routing Logic

The AI considers:
- **Priority** — CRITICAL always forces SMS; LOW stays on email
- **Quiet Hours** — avoids disturbing users at night for non-critical alerts
- **Channel Availability** — only picks channels the user has registered
- **User Preferences** — respects preferred channel ordering
- **Fallback** — if OpenAI fails, rule-based fallback kicks in automatically

## Scalability Notes

- **Kafka consumers** run with `concurrency=3` — scales to partition count
- **Redis TTL** set to 1 hour for user preferences
- **PostgreSQL** connection pooling via HikariCP (Spring Boot default)
- **Retry logic** — failed notifications tracked with retry count in DB
- **AI fallback** — service never fails due to OpenAI outage

## Resume Talking Points

- Designed async notification pipeline using Kafka with userId-based partitioning for ordered delivery
- Integrated Spring AI with OpenAI GPT-3.5-turbo for intelligent channel routing with rule-based fallback
- Implemented Redis caching layer for user preferences reducing DB load at high throughput
- Built with graceful degradation — AI failure silently falls back to deterministic routing
