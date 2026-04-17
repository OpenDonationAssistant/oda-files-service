# ODA Files Service
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/OpenDonationAssistant/oda-files-service)
![Sonar Tech Debt](https://img.shields.io/sonar/tech_debt/OpenDonationAssistant_oda-files-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Violations](https://img.shields.io/sonar/violations/OpenDonationAssistant_oda-files-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Tests](https://img.shields.io/sonar/tests/OpenDonationAssistant_oda-files-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Coverage](https://img.shields.io/sonar/coverage/OpenDonationAssistant_oda-files-service?server=https%3A%2F%2Fsonarcloud.io)

## Running with Docker

The Docker image is available from GitHub Container Registry:

```bash
docker pull ghcr.io/opendonationassistant/oda-files-service:latest
```

### Environment Variables

Configure the following environment variables to run the service:

| Variable | Description | Default |
|----------|-------------|---------|
| `MINIO_ENDPOINT` | MinIO server endpoint | *Required* |
| `MINIO_USERNAME` | MinIO access username | *Required* |
| `MINIO_PASSWORD` | MinIO access password | *Required* |
| `RABBITMQ_HOST` | RabbitMQ hostname | `localhost` |
| `JDBC_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost/postgres?currentSchema=files` |
| `JDBC_USER` | Database username | `postgres` |
| `JDBC_PASSWORD` | Database password | `postgres` |

### Docker Run Example

```bash
docker run -d \
  --name oda-files-service \
  -p 8080:8080 \
  -e MINIO_ENDPOINT=http://minio:9000 \
  -e MINIO_USERNAME=minioadmin \
  -e MINIO_PASSWORD=minioadmin \
  -e RABBITMQ_HOST=rabbitmq \
  -e JDBC_URL=jdbc:postgresql://postgres:5432/postgres?currentSchema=files \
  -e JDBC_USER=postgres \
  -e JDBC_PASSWORD=postgres \
  ghcr.io/opendonationassistant/oda-files-service:0.0.4
```

### Docker Compose Example

```yaml
version: '3.8'
services:
  oda-files-service:
    image: ghcr.io/opendonationassistant/oda-files-service:0.0.4
    ports:
      - "8080:8080"
    environment:
      MINIO_ENDPOINT: http://minio:9000
      MINIO_USERNAME: minioadmin
      MINIO_PASSWORD: minioadmin
      RABBITMQ_HOST: rabbitmq
      JDBC_URL: jdbc:postgresql://postgres:5432/postgres?currentSchema=files
      JDBC_USER: postgres
      JDBC_PASSWORD: postgres
    depends_on:
      - postgres
      - rabbitmq
      - minio

  postgres:
    image: postgres:16
    environment:
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "15672:15672"

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data

volumes:
  postgres_data:
  minio_data:
```
