# Accounts Microservice

A Spring Boot microservice for managing bank accounts, built with Java 17, containerized with Docker, and deployed via a fully automated GitHub Actions CI/CD pipeline.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Docker](#docker)
- [CI/CD Pipeline](#cicd-pipeline)
- [Kubernetes Deployment](#kubernetes-deployment)
- [API Documentation](#api-documentation)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Build Tool | Maven 3.9 |
| Database | MySQL 8.0 (prod) / H2 (dev) |
| Container | Docker (Alpine-based multi-stage build) |
| Orchestration | Kubernetes |
| CI/CD | GitHub Actions |
| API Docs | SpringDoc OpenAPI / Swagger UI |

---

## Project Structure

```
accounts/
├── .github/workflows/ci-cd.yml   # GitHub Actions pipeline
├── Dockerfile                    # Multi-stage Docker build
├── docker-compose.yml            # Local multi-service orchestration
├── k8s/                          # Kubernetes manifests
│   ├── accounts-deployment.yaml
│   ├── mysql-deployment.yaml
│   ├── configmap.yaml
│   └── secret.yaml
├── pom.xml
└── src/
    └── main/java/com/microservices/accounts/
        ├── controller/           # REST endpoints
        ├── service/              # Business logic
        ├── repository/           # Data access (Spring Data JPA)
        ├── entity/               # JPA entities
        ├── dto/                  # Data transfer objects
        ├── mapper/               # Entity ↔ DTO mappers
        └── exception/            # Global exception handling
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- Docker & Docker Compose

### Run locally (H2 in-memory database)

```bash
./mvnw spring-boot:run
```

The service starts on **http://localhost:8080**.

### Run tests

```bash
./mvnw test
```

### Build a JAR

```bash
./mvnw clean package -DskipTests
```

---

## Docker

### Multi-Stage Dockerfile

The [`Dockerfile`](./Dockerfile) uses a **two-stage build** to keep the final image small and production-ready:

```
Stage 1 — Build   : maven:3.9-eclipse-temurin-17   (compile & package)
Stage 2 — Runtime : eclipse-temurin:17-jre-alpine  (~200 MB, JRE only)
```

**Why multi-stage?**
- Stage 1 needs Maven + JDK to compile — these are heavy tools (~600 MB).
- Stage 2 ships only the JRE and the compiled JAR — nothing extra.
- The final image is roughly **3× smaller** than a single-stage build.

#### Build and run the image manually

```bash
# Build the image
docker build -t accounts:local .

# Run a container
docker run -p 8080:8080 accounts:local
```

---

### Docker Compose

[`docker-compose.yml`](./docker-compose.yml) starts the full microservices stack locally with a single command:

```bash
docker compose up
```

| Service | Image | Port | Notes |
|---|---|---|---|
| **accounts** | `kadarsh03/accounts:v1` | 8080 | Spring profile: `mysql`; waits for MySQL health check |
| **loans** | `kadarsh03/loans:s4` | 8090 | |
| **cards** | `kadarsh03/cards:s4` | 9000 | |
| **mysql** | `mysql:8.0` | 3306 | Database: `accountsdb`; persistent volume |

All services share the `kadarsh03-network` bridge network. MySQL data is persisted in the named volume `mysql-data` so it survives container restarts.

#### Key Docker Compose features

- **Health check** — Compose waits for MySQL to be ready (`mysqladmin ping`) before starting the accounts service.
- **Memory limits** — Each microservice is capped at 700 MB to prevent runaway resource usage.
- **Named volume** — `mysql-data` keeps database files intact across `docker compose down` / `up` cycles.

```bash
# Start all services
docker compose up -d

# Tail logs
docker compose logs -f accounts

# Stop and remove containers (data volume is preserved)
docker compose down
```

---

## CI/CD Pipeline

The pipeline is defined in [`.github/workflows/ci-cd.yml`](./.github/workflows/ci-cd.yml) and runs on **GitHub Actions**.

### Triggers

| Event | Branch | Effect |
|---|---|---|
| `push` | `master` | Runs all three jobs |
| `pull_request` | `master` | Runs build-and-test only |

### Jobs

```
build-and-test  ──►  docker-build-push  ──►  deploy-to-kubernetes
```

Each job depends on the previous one succeeding. Jobs 2 and 3 only run on the `master` branch.

---

#### Job 1 — Build and Test

Runs on every push and pull request targeting `master`.

| Step | Action |
|---|---|
| Checkout code | `actions/checkout@v2` |
| Set up JDK 17 | `actions/setup-java@v4` (Temurin distribution) |
| Cache Maven dependencies | `actions/cache@v3` keyed on `pom.xml` hash |
| Build JAR | `mvn clean package -DskipTests` |
| Run tests | `mvn test` |

Dependency caching means subsequent runs skip downloading the Maven repository, significantly reducing build time.

---

#### Job 2 — Docker Build & Push

Runs only when Job 1 passes **and** the branch is `master`.

| Step | Action |
|---|---|
| Checkout code | `actions/checkout@v4` |
| Log in to Docker Hub | `docker/login-action@v3` using `DOCKER_USERNAME` / `DOCKER_TOKEN` secrets |
| Build & push image | `docker/build-push-action@v5` |

The Docker image is tagged with the **Git commit SHA** for full traceability:

```
<DOCKER_USERNAME>/accounts:<GIT_COMMIT_SHA>
```

This means every pushed image can be traced back to the exact commit that produced it.

---

#### Job 3 — Deploy to Kubernetes

Runs only when Job 2 passes **and** the branch is `master`.

The deployment step is ready for production cloud clusters (AWS EKS, GKE). For local Minikube clusters, apply the image update manually:

```bash
kubectl set image deployment/accounts-deployment \
  accounts=<DOCKER_USERNAME>/accounts:<GIT_COMMIT_SHA>

kubectl rollout status deployment/accounts-deployment
```

---

### Required GitHub Secrets

Go to **Settings → Secrets and variables → Actions** and add:

| Secret | Description |
|---|---|
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_TOKEN` | Docker Hub access token (not your password) |
| `KUBE_CONFIG` | Contents of `~/.kube/config` for the target cluster |

---

## Kubernetes Deployment

Manifests are in the [`k8s/`](./k8s/) directory.

### Resources

| Resource | File | Description |
|---|---|---|
| Deployment | `accounts-deployment.yaml` | 3 replicas, resource limits, liveness & readiness probes |
| MySQL | `mysql-deployment.yaml` | Managed MySQL instance |
| ConfigMap | `configmap.yaml` | Spring profile and environment variables |
| Secret | `secret.yaml` | MySQL credentials (base64-encoded) |

### Accounts Deployment Highlights

- **3 replicas** for high availability
- **CPU:** requests 300m / limit 500m
- **Memory:** requests 500Mi / limit 700Mi
- **Liveness probe:** `GET /actuator/health` — restarts the pod if the app hangs
- **Readiness probe:** `GET /actuator/health` — removes the pod from load balancing until it is ready

### Apply to a cluster

```bash
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/accounts-deployment.yaml
```

---

## API Documentation

Once the service is running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

The OpenAPI JSON spec is at:

```
http://localhost:8080/v3/api-docs
```

Spring Actuator health endpoint:

```
http://localhost:8080/actuator/health
```
