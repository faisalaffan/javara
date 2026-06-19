# JAVARA — Temenos Transact Integration Design

> Design doc untuk modul integrasi Temenos Transact (T24) di ekosistem JAVARA

**Date:** 2026-06-20
**Status:** Approved
**Phase:** 1

---

## 1. Overview

Modul integrasi Temenos Transact adalah komponen pertama ekosistem JAVARA. Menyediakan boilerplate Spring Boot multi-module untuk koneksi ke T24 Transact via multiple integration pattern (OFS, TAFJ, IRIS, JMS), full CRUD transaksi perbankan, REST/gRPC API, audit trail, idempotensi, retry queue, dan multi-tenancy.

Target akhir: consumer cukup import starter, tambah `@EnableJavara`, set properties — langsung production-ready.

## 2. Architecture Decision

**Multi-Module Clean Architecture (Approach B)**

Dipilih karena:
- Isolasi adapter per integration pattern — extensible tanpa refactor core
- Cocok sebagai boilerplate dan Spring Boot starter
- Consumer hanya depend module yang dipakai
- Clean boundary antara domain logic dan infrastruktur T24

## 3. Module Breakdown

```
javara/
├── javara-core/           ← Domain model, port interfaces, value objects, exceptions
├── javara-ofs/            ← T24 OFS adapter (XML/JSON)
├── javara-tafj/           ← TAFJ REST/SOAP adapter
├── javara-iris/           ← IRIS integration adapter
├── javara-jms/            ← JMS/MQ direct adapter
├── javara-app/            ← Spring Boot executable, DB config, API controllers
├── javara-client/         ← Feign/RestTemplate consumer SDK
├── javara-starter/        ← Spring Boot autoconfig starter
└── javara-bom/            ← Bill of Materials POM
```

### 3.1 `javara-core`
- T24 entity model: `T24Customer`, `T24Account`, `T24FundTransfer`, `T24PaymentOrder`, `T24TellerTransaction`
- Port interfaces: `CustomerPort`, `TransactionPort`, `EnquiryPort`, `SystemPort`
- OFS message envelope (XML/JSON generic builder)
- Value objects: `AccountNumber`, `CustomerId`, `T24Reference`, `Amount`, `IdempotencyKey`
- Exception hierarchy (lihat Section 7)
- Zero framework dependency. Pure Java.

### 3.2 `javara-ofs`
- HTTP client ke T24 OFS endpoint (Spring WebClient + Apache HttpClient)
- OFS request builder: domain model → T24 OFS XML/JSON envelope
- OFS response parser: T24 TOF/Narrative response → domain model
- Auth handler: Basic, JWT, custom HTTP header
- Config: `javara.t24.ofs.*`

### 3.3 `javara-tafj`
- REST client ke TAFJ-exposed REST services
- SOAP client (Spring WebServiceTemplate)
- WSDL binding atau dynamic SOAP call (configurable)
- Config: `javara.t24.tafj.*`

### 3.4 `javara-iris`
- IRIS event listener dan publisher
- Message serializer/deserializer (IRIS canonical format)
- Retry, dead-letter queue integration
- Config: `javara.t24.iris.*`

### 3.5 `javara-jms`
- JMS connection factory (CachingConnectionFactory)
- Message converter: domain model ↔ T24 JMS message format
- Connection pooling
- Config: `javara.t24.jms.*`

### 3.6 `javara-app`
- Spring Boot entry point (`@EnableJavara`)
- REST controllers + gRPC service implementations
- Flyway migration + PostgreSQL schema
- Idempotency registry, retry queue scheduler
- Audit trail writer (async)
- OAuth2 Resource Server / API Key security
- Micrometer + OpenTelemetry observability
- Actuator health endpoints

### 3.7 `javara-client` (Consumer SDK)
- Feign interfaces pre-configured untuk semua endpoint REST
- Resilience4j retry + circuit breaker built-in
- DTO classes matching API contract
- Package: `id.co.javara.client.*`

### 3.8 `javara-starter`
- `spring.factories` auto-configuration
- `@EnableJavara` annotation — inject semua beans
- Property defaults (`javara.t24.*`)
- Actuator health check auto-include

## 4. Data Flow

```
Consumer App
    │
    ▼
┌─────────────────────────┐
│  javara-client (SDK)    │  ← Feign client, auto-retry, circuit breaker
└───────────┬─────────────┘
            │ REST / gRPC
            ▼
┌─────────────────────────┐
│  javara-app              │
│  ┌─────────────────────┐│
│  │ Controller/gRPC      ││  ← Validate DTO, authenticate
│  │ ↓                   ││
│  │ Service Layer       ││  ← Orchestrate, idempotency check, audit
│  │ ↓                   ││
│  │ Port Interface      ││  ← Core abstractions
│  └─────────┬───────────┘│
│  ┌─────────▼───────────┐│
│  │ Adapter Resolver    ││  ← Strategy: select adapter by config/header
│  └─────────┬───────────┘│
└────────────┼─────────────┘
             │
    ┌────────┼────────┬──────────┐
    ▼        ▼        ▼          ▼
┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
│ OFS  │ │ TAFJ │ │ IRIS │ │ JMS  │
└──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘
   │        │        │        │
   ▼        ▼        ▼        ▼
┌─────────────────────────────────┐
│       T24 Transact              │
└─────────────────────────────────┘
```

### Request lifecycle
1. Consumer panggil REST/gRPC (langsung atau via SDK)
2. Controller validasi DTO + auth check
3. Service cek `idempotency_registry` — kalau key exists, return cached response
4. Service panggil port interface (`TransactionPort.fundTransfer(...)`)
5. Adapter Resolver pilih adapter berdasarkan `javara.t24.default-adapter` atau header `X-T24-Adapter`
6. Adapter bangun request format-spesifik, kirim ke T24, parse response → domain model
7. Service simpan audit trail (async), cache idempotency response, return ke consumer

### Error flow
- T24 business error → `T24BusinessException` → 422 + error detail
- Timeout → `T24TimeoutException` → retry 3x → exhausted → 504 + masuk retry_queue
- Adapter unavailable → fallback ke secondary adapter → jika semua down → 503

## 5. API Surface

### 5.1 REST

Base path: `/api/v1/t24`

**Customer Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/customers/{customerId}` | CIF inquiry by ID |
| `GET` | `/customers/search?name=&idType=&idNumber=` | Customer search |
| `POST` | `/customers` | Create customer |
| `PUT` | `/customers/{customerId}` | Update customer |
| `GET` | `/customers/{customerId}/accounts` | List accounts by customer |
| `POST` | `/customers/{customerId}/accounts` | Open new account |

**Transaction Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/transactions/fund-transfer` | Fund transfer posting |
| `POST` | `/transactions/teller` | Teller transaction posting |
| `POST` | `/transactions/payment-order` | Payment order posting |
| `POST` | `/transactions/multi-commit` | Multi-record commit |
| `GET` | `/transactions/{transactionId}` | Transaction inquiry |
| `GET` | `/transactions/{transactionId}/status` | Status inquiry |

**System Endpoints**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/health/t24` | T24 connectivity health |
| `GET` | `/adapter/status` | Active adapter info |
| `GET` | `/retry-queue/pending` | Pending retry count |

### 5.2 Standard Request/Response

Request:
```json
{
  "idempotencyKey": "uuid-required",
  "channel": "INTERNET_BANKING",
  "debitAccount": "12345-678-1-USD",
  "creditAccount": "98765-432-1-USD",
  "amount": 250000.00,
  "currency": "USD",
  "paymentDetails": "Invoice payment INV-2026"
}
```

Success response:
```json
{
  "transactionId": "FT260620ABCDEF",
  "status": "COMPLETED",
  "t24Reference": "T24/260620/000123",
  "postedAt": "2026-06-20T10:30:00Z"
}
```

Error response:
```json
{
  "errorCode": "T24_POSTING_ERROR",
  "message": "Account 12345-678-1-USD is dormant",
  "t24ErrorCode": "EB-AC.DORMANT",
  "requestId": "req-uuid"
}
```

### 5.3 gRPC

Protobuf services:
- `CustomerService` — `GetCustomer`, `SearchCustomers`, `CreateCustomer`
- `TransactionService` — `PostFundTransfer`, `PostTellerTransaction`, `PostMultiCommit`, `GetTransactionStatus`

REST dan gRPC share service layer. Proto file di `javara-app/src/main/proto/`.

## 6. Database Design (App DB)

PostgreSQL 16. Schema: `javara_t24`.

### Tables

| Table | Purpose |
|-------|---------|
| `idempotency_registry` | Idempotency key → response cache, prevent double-post |
| `retry_queue` | Failed transactions queued for retry |
| `audit_log` | Immutable audit trail, all requests |
| `customer_cache` | Local CIF cache (TTL 5 min) |
| `transaction_log` | Successful transaction summary |
| `adapter_health_event` | Circuit breaker state change history |
| `multi_commit_batch` | Multi-record operation tracking |
| `enrichment_rule` | Per-channel enrichment config |

### Key columns

**`idempotency_registry`**: `idempotency_key (PK)`, `response_payload (JSONB)`, `http_status`, `created_at`, `expires_at`. TTL 24h.

**`retry_queue`**: `id (UUID PK)`, `original_request (JSONB)`, `adapter`, `attempts`, `max_attempts`, `last_error`, `next_retry_at`, `status (ENUM)`. Scheduler `@Scheduled(fixedDelay=30000)`.

**`audit_log`**: Event type, adapter, request URL/payload (masked), response, duration, error code, correlation ID. Partitioned monthly, 90-day retention.

### Migration

Flyway scripts in `javara-app/src/main/resources/db/migration/`:

```
V1__init_idempotency_registry.sql
V2__init_retry_queue.sql
V3__init_audit_log.sql
V4__init_customer_cache.sql
V5__init_transaction_log.sql
V6__init_enrichment_rule.sql
```

## 7. Error Handling & Resilience

### Exception Hierarchy

```
JavaraException (sealed base)
├── T24ConnectionException           ← Network, DNS, firewall
│   └── T24AuthException             ← Invalid/expired credentials
├── T24ResponseException             ← T24 returned error (EB-xxx)
│   ├── T24BusinessException         ← Business rule: dormant, NSF, limit
│   ├── T24ValidationException       ← Invalid field, version mismatch
│   └── T24SystemException           ← T24 internal error, OFS agent down
├── T24TimeoutException              ← Request exceeded timeout
├── IdempotencyViolationException    ← Same key, different payload
└── AdapterUnavailableException      ← Adapter not configured/healthy
```

### Resilience Stack (Resilience4j)

```
Request → Circuit Breaker → Retry → TimeLimiter → Adapter
```

Per-adapter config:

```yaml
javara.t24.resilience:
  retry:
    max-attempts: 3
    backoff: exponential
    max-delay: 2s
  circuit-breaker:
    failure-rate-threshold: 50%
    wait-duration-in-open: 30s
    sliding-window-size: 20
    permitted-calls-in-half-open: 5
  time-limiter:
    ofs: 30s
    tafj: 60s
    iris: 15s
    jms: 45s
```

### Adapter Fallback Chain

```yaml
javara.t24.adapters: [ofs, tafj]   # ofs primary, tafj fallback
```

Circuit open pada adapter primary → auto route ke secondary. Semua down → 503 + retry queue.

### Retry Queue

Failed transactions land in `retry_queue`. Scheduler processes batch every 30s. Exponential backoff between attempts. Max 5 attempts. Exhausted → webhook alert.

## 8. Security & Multi-Tenancy

### Authentication

Two modes, configurable:
- `oauth2`: Spring Security OAuth2 Resource Server, JWT validation via JWKS URI
- `apikey`: Custom `X-API-Key` header filter, key hash in DB

### Authorization (RBAC)

Tables: `role`, `permission`, `role_permission`, `user_role`.

Permission granularity: `t24:customer:read`, `t24:customer:write`, `t24:transaction:ft`, `t24:transaction:teller`, `t24:transaction:po`, `t24:transaction:multi-commit`, `t24:enquiry:all`.

### Multi-Tenancy

Two levels:

**Channel** (header `X-Channel-Code`): `INTERNET_BANKING`, `MOBILE_BANKING`, `TELLER`, `ATM`, `BATCH`. Each has its own enrichment rules and rate limits.

**Tenant** (header `X-Tenant-Id`, optional): Multi-bank/entity deployment. Maps to T24 Company concept. Per-tenant data source and connection pool config.

```yaml
javara.t24.multi-tenancy:
  enabled: false
  default-tenant: "BNI01"
  tenants:
    - id: "BNI01"
      t24-company: "ID0010001"
      ofs-endpoint: "https://t24-bni01.internal:9443"
```

### Rate Limiting

Per-channel, per-operation. Configurable in `application.yml`:

```yaml
javara.t24.rate-limit.channels:
  INTERNET_BANKING:
    fund-transfer: 100/s
    customer-inquiry: 500/s
  BATCH:
    fund-transfer: unlimited
```

### Data Protection
- PII masking on audit_log fields (`idNumber`, `accountNumber` → `12345****678`)
- No PII in INFO+ log levels
- gRPC: TLS mutual auth
- All T24 connections: HTTPS/TLS

## 9. Testing Strategy

### Pyramid

| Layer | Tool | Scope |
|-------|------|-------|
| Unit | JUnit 5, AssertJ | Domain logic, message builders/parsers, exception handling |
| Integration | Spring Boot Test, MockMvc, WireMock, Testcontainers | REST controllers, DB, adapter integration, retry/idempotency behavior |
| E2E | Testcontainers (PostgreSQL + WireMock sandbox) | Full flow happy + sad path per adapter |
| Contract | Spring Cloud Contract, protobuf lint | REST + gRPC contract stability |

### Coverage Targets

| Module | Target |
|--------|--------|
| `javara-core` | ≥ 90% |
| `javara-ofs` | ≥ 85% |
| `javara-tafj` | ≥ 85% |
| `javara-iris` | ≥ 85% |
| `javara-jms` | ≥ 85% |
| `javara-app` | ≥ 80% |
| `javara-client` | ≥ 80% |

### Key integration test scenarios
- OFS fund transfer: POST request → WireMock stubs T24 OK → verify 201 + audit_log entry
- Double POST with same idempotency key → same response, single audit entry
- T24 timeout → retry 3x → exhausted → verify retry_queue entry
- Circuit breaker: 10 failures → OPEN → next call instant failure
- Adapter fallback: OFS down → auto route to TAFJ

## 10. Tech Stack & Build

### Dependencies

| Category | Choice |
|----------|--------|
| Java | 21 LTS (Virtual Threads) |
| Framework | Spring Boot 3.4+ |
| Build | Maven 4.x multi-module |
| App DB | PostgreSQL 16 |
| Migration | Flyway |
| Cache | Caffeine (local) |
| Resilience | Resilience4j |
| Observability | Micrometer, Actuator, OpenTelemetry |
| REST API | Spring Web, SpringDoc OpenAPI 2.x |
| gRPC | grpc-server-spring-boot-starter, protobuf-maven-plugin |
| Client SDK | Spring Cloud OpenFeign |
| Testing | JUnit 5, Mockito, WireMock, Testcontainers, REST Assured |

### Maven Artifacts

| GAV | Description |
|-----|-------------|
| `id.co.javara:javara-bom:1.0.0` | Bill of Materials |
| `id.co.javara:javara-core:1.0.0` | Domain model + port interfaces |
| `id.co.javara:javara-ofs:1.0.0` | OFS adapter |
| `id.co.javara:javara-tafj:1.0.0` | TAFJ adapter |
| `id.co.javara:javara-iris:1.0.0` | IRIS adapter |
| `id.co.javara:javara-jms:1.0.0` | JMS adapter |
| `id.co.javara:javara-app:1.0.0` | Spring Boot executable |
| `id.co.javara:javara-client:1.0.0` | Consumer SDK |
| `id.co.javara:javara-starter:1.0.0` | Autoconfig starter |

### Consumer Quickstart

```xml
<dependency>
    <groupId>id.co.javara</groupId>
    <artifactId>javara-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
@SpringBootApplication
@EnableJavara
public class BankApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }
}
```

```yaml
javara:
  t24:
    ofs:
      endpoint: https://t24.internal:9443
      auth: basic
      username: ${T24_USERNAME}
      password: ${T24_PASSWORD}
    default-adapter: ofs
  security:
    auth-mode: oauth2
    jwk-set-uri: https://auth.bank.co.id/.well-known/jwks.json
```

## 11. Future: CLI `javara` (Phase 2)

```bash
javara new temenos-integration    → scaffold full multi-module project
javara add ofs                    → add OFS adapter to existing
javara add tafj                   → add TAFJ adapter
javara gen ft-endpoint            → generate fund transfer endpoint (controller + service + test)
javara gen customer-crud           → generate customer CRUD endpoint
```

---

*Generated by JAVARA brainstorming session — 2026-06-20*
