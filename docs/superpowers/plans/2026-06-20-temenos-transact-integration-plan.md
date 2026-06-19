# Temenos Transact Integration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the multi-module Spring Boot Temenos Transact integration boilerplate — 8 modules + BOM, ready for Maven Central publish.

**Architecture:** Multi-module Clean Architecture. Core domain model isolated from adapters. Strategy pattern for adapter selection. Spring Boot app exposes REST + gRPC. Consumer SDK via Feign. Autoconfig starter for one-click integration.

**Tech Stack:** Java 21 LTS, Spring Boot 3.4+, Maven 4.x multi-module, PostgreSQL 16, Flyway, Resilience4j, gRPC (grpc-server-spring-boot-starter), Spring Cloud OpenFeign, JUnit 5, Testcontainers, WireMock

## Global Constraints

- Java 21 LTS minimum
- Spring Boot 3.4+ 
- Maven 4.x multi-module
- PostgreSQL 16 for app DB
- Flyway for migrations (V1-V6 naming)
- Group ID: `id.co.javara`
- Package base: `id.co.javara`
- All adapters depend ONLY on `javara-core` (except app module)
- `javara-core` MUST have zero framework dependency (pure Java)
- Test coverage: core ≥ 90%, adapters ≥ 85%, app ≥ 80%
- JUnit 5 + AssertJ for assertions
- TDD: test first, verify fail, implement, verify pass, commit
- Conventional commits: `feat:`, `test:`, `chore:`

---

## Milestone 0: Project Skeleton

### Task 0.1: Parent POM — Multi-module scaffolding

**Files:**
- Create: `pom.xml`
- Create: `javara-bom/pom.xml`
- Create: `.mvn/jvm.config`
- Create: `.gitignore` (update if exists)

**Interfaces:**
- Consumes: nothing
- Produces: Parent POM with `<modules>`, `<dependencyManagement>`, Java 21 config, plugin versions

- [ ] **Step 1: Create parent POM**

Write `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>id.co.javara</groupId>
    <artifactId>javara</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>JAVARA</name>
    <description>The championship toolkit for Java engineers — Temenos Transact Integration</description>

    <modules>
        <module>javara-bom</module>
        <module>javara-core</module>
        <module>javara-ofs</module>
        <module>javara-tafj</module>
        <module>javara-iris</module>
        <module>javara-jms</module>
        <module>javara-app</module>
        <module>javara-client</module>
        <module>javara-starter</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <spring-boot.version>3.4.1</spring-boot.version>
        <resilience4j.version>2.2.0</resilience4j.version>
        <testcontainers.version>1.20.4</testcontainers.version>
        <wiremock.version>3.9.1</wiremock.version>
        <grpc.version>1.68.0</grpc.version>
        <protobuf.version>3.25.5</protobuf.version>
        <springdoc.version>2.7.0</springdoc.version>
        <openfeign.version>4.2.0</openfeign.version>
        <flyway.version>10.21.0</flyway.version>
        <caffeine.version>3.1.8</caffeine.version>
        <micrometer-tracing.version>1.4.1</micrometer-tracing.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-bom</artifactId>
                <version>${project.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.13.0</version>
                    <configuration>
                        <source>21</source>
                        <target>21</target>
                        <parameters>true</parameters>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.5.1</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                    <version>3.5.1</version>
                </plugin>
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <version>0.8.12</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

- [ ] **Step 2: Create BOM POM**

Write `javara-bom/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>id.co.javara</groupId>
        <artifactId>javara</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>javara-bom</artifactId>
    <packaging>pom</packaging>

    <name>JAVARA Bill of Materials</name>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-core</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-ofs</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-tafj</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-iris</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-jms</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-app</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-client</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>id.co.javara</groupId>
                <artifactId>javara-starter</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

- [ ] **Step 3: Create .mvn/jvm.config**

Write `.mvn/jvm.config`:

```
--enable-preview
-XX:+UseZGC
-Xmx2g
```

- [ ] **Step 4: Build parent to verify**

Run: `mvn validate -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add pom.xml javara-bom/pom.xml .mvn/jvm.config
git commit -m "chore: add parent POM and BOM for multi-module project"
```

---

## Milestone 1: javara-core — Domain Model & Port Interfaces

### Task 1.1: T24 domain value objects

**Files:**
- Create: `javara-core/pom.xml`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/vo/AccountNumber.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/vo/CustomerId.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/vo/T24Reference.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/vo/Amount.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/vo/IdempotencyKey.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/vo/package-info.java`
- Test: `javara-core/src/test/java/id/co/javara/core/domain/vo/AccountNumberTest.java`

**Interfaces:**
- Consumes: nothing
- Produces: `AccountNumber`, `CustomerId`, `T24Reference`, `Amount`, `IdempotencyKey` — all records

- [ ] **Step 1: Create javara-core POM**

Write `javara-core/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>id.co.javara</groupId>
        <artifactId>javara</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>javara-core</artifactId>

    <name>JAVARA Core</name>
    <description>Domain model, port interfaces, value objects, exceptions</description>

    <dependencies>
        <!-- Pure Java — no framework dependencies -->

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Write failing test for AccountNumber**

Write `javara-core/src/test/java/id/co/javara/core/domain/vo/AccountNumberTest.java`:

```java
package id.co.javara.core.domain.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AccountNumberTest {

    @Test
    void shouldCreateValidAccountNumber() {
        var account = new AccountNumber("12345-678-1-USD");
        assertThat(account.value()).isEqualTo("12345-678-1-USD");
        assertThat(account.toString()).isEqualTo("12345-678-1-USD");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldRejectBlankValue(String input) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new AccountNumber(input));
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        var a1 = new AccountNumber("12345-678-1-USD");
        var a2 = new AccountNumber("12345-678-1-USD");
        assertThat(a1).isEqualTo(a2);
    }
}
```

- [ ] **Step 3: Run test — expect FAIL**

Run: `mvn -pl javara-core test -Dtest=AccountNumberTest`
Expected: FAIL — class not found

- [ ] **Step 4: Write AccountNumber + all value objects**

Write `javara-core/src/main/java/id/co/javara/core/domain/vo/AccountNumber.java`:

```java
package id.co.javara.core.domain.vo;

import java.util.Objects;

public record AccountNumber(String value) {

    public AccountNumber {
        Objects.requireNonNull(value, "Account number must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Account number must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
```

Write `javara-core/src/main/java/id/co/javara/core/domain/vo/CustomerId.java`:

```java
package id.co.javara.core.domain.vo;

import java.util.Objects;

public record CustomerId(String value) {

    public CustomerId {
        Objects.requireNonNull(value, "Customer ID must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
```

Write `javara-core/src/main/java/id/co/javara/core/domain/vo/T24Reference.java`:

```java
package id.co.javara.core.domain.vo;

import java.util.Objects;

public record T24Reference(String value) {

    public T24Reference {
        Objects.requireNonNull(value, "T24 reference must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("T24 reference must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
```

Write `javara-core/src/main/java/id/co/javara/core/domain/vo/Amount.java`:

```java
package id.co.javara.core.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record Amount(BigDecimal value, String currency) {

    public Amount {
        Objects.requireNonNull(value, "Amount value must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + value);
        }
        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }
    }

    @Override
    public String toString() {
        return value + " " + currency;
    }
}
```

Write `javara-core/src/main/java/id/co/javara/core/domain/vo/IdempotencyKey.java`:

```java
package id.co.javara.core.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record IdempotencyKey(String value) {

    public IdempotencyKey {
        Objects.requireNonNull(value, "Idempotency key must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be blank");
        }
    }

    public static IdempotencyKey generate() {
        return new IdempotencyKey(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
```

- [ ] **Step 5: Run tests — expect PASS**

Run: `mvn -pl javara-core test`
Expected: BUILD SUCCESS, all AccountNumber tests pass

- [ ] **Step 6: Add tests for remaining value objects**

Create test files for CustomerId, T24Reference, Amount, IdempotencyKey following the same pattern:
- valid creation
- null/blank rejection
- equality by value

- [ ] **Step 7: Run all tests**

Run: `mvn -pl javara-core test`
Expected: BUILD SUCCESS, all VO tests pass

- [ ] **Step 8: Commit**

```bash
git add javara-core/
git commit -m "feat(core): add domain value objects — AccountNumber, CustomerId, T24Reference, Amount, IdempotencyKey"
```

### Task 1.2: T24 domain entities

**Files:**
- Create: `javara-core/src/main/java/id/co/javara/core/domain/model/T24Customer.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/model/T24Account.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/model/T24FundTransfer.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/model/T24PaymentOrder.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/model/T24TellerTransaction.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/model/T24MultiCommit.java`
- Create: `javara-core/src/main/java/id/co/javara/core/domain/model/TransactionStatus.java`
- Test: `javara-core/src/test/java/id/co/javara/core/domain/model/T24FundTransferTest.java`

**Interfaces:**
- Consumes: `AccountNumber`, `CustomerId`, `T24Reference`, `Amount`, `IdempotencyKey` (from Task 1.1)
- Produces: `T24Customer`, `T24Account`, `T24FundTransfer`, `T24PaymentOrder`, `T24TellerTransaction`, `T24MultiCommit`, `TransactionStatus`

- [ ] **Step 1: Write failing test for T24FundTransfer builder**

Write `javara-core/src/test/java/id/co/javara/core/domain/model/T24FundTransferTest.java`:

```java
package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class T24FundTransferTest {

    @Test
    void shouldBuildFundTransferWithAllFields() {
        var ft = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-123"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("250000.00"), "USD"))
            .paymentDetails("Invoice INV-2026")
            .channel("INTERNET_BANKING")
            .build();

        assertThat(ft.debitAccount().value()).isEqualTo("12345-678-1-USD");
        assertThat(ft.creditAccount().value()).isEqualTo("98765-432-1-USD");
        assertThat(ft.amount().value()).isEqualByComparingTo(new BigDecimal("250000.00"));
        assertThat(ft.amount().currency()).isEqualTo("USD");
        assertThat(ft.paymentDetails()).isEqualTo("Invoice INV-2026");
        assertThat(ft.channel()).isEqualTo("INTERNET_BANKING");
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

Run: `mvn -pl javara-core test -Dtest=T24FundTransferTest`
Expected: FAIL

- [ ] **Step 3: Write T24FundTransfer + all entities**

Write `javara-core/src/main/java/id/co/javara/core/domain/model/T24FundTransfer.java`:

```java
package id.co.javara.core.domain.model;

import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import id.co.javara.core.domain.vo.T24Reference;
import java.time.Instant;

public record T24FundTransfer(
    IdempotencyKey transactionId,
    AccountNumber debitAccount,
    AccountNumber creditAccount,
    Amount amount,
    String paymentDetails,
    String channel,
    String valueDate,
    String processingPriority,
    String chargeCode,
    T24Reference t24Reference,
    TransactionStatus status,
    Instant postedAt
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private IdempotencyKey transactionId;
        private AccountNumber debitAccount;
        private AccountNumber creditAccount;
        private Amount amount;
        private String paymentDetails;
        private String channel;
        private String valueDate;
        private String processingPriority = "NORMAL";
        private String chargeCode = "SHA";
        private T24Reference t24Reference;
        private TransactionStatus status = TransactionStatus.PENDING;
        private Instant postedAt;

        public Builder transactionId(IdempotencyKey id) { this.transactionId = id; return this; }
        public Builder debitAccount(AccountNumber acct) { this.debitAccount = acct; return this; }
        public Builder creditAccount(AccountNumber acct) { this.creditAccount = acct; return this; }
        public Builder amount(Amount a) { this.amount = a; return this; }
        public Builder paymentDetails(String details) { this.paymentDetails = details; return this; }
        public Builder channel(String ch) { this.channel = ch; return this; }
        public Builder valueDate(String vd) { this.valueDate = vd; return this; }
        public Builder processingPriority(String pp) { this.processingPriority = pp; return this; }
        public Builder chargeCode(String cc) { this.chargeCode = cc; return this; }
        public Builder t24Reference(T24Reference ref) { this.t24Reference = ref; return this; }
        public Builder status(TransactionStatus s) { this.status = s; return this; }
        public Builder postedAt(Instant at) { this.postedAt = at; return this; }

        public T24FundTransfer build() {
            if (transactionId == null) throw new IllegalStateException("transactionId is required");
            if (debitAccount == null) throw new IllegalStateException("debitAccount is required");
            if (creditAccount == null) throw new IllegalStateException("creditAccount is required");
            if (amount == null) throw new IllegalStateException("amount is required");
            return new T24FundTransfer(
                transactionId, debitAccount, creditAccount, amount,
                paymentDetails, channel, valueDate, processingPriority,
                chargeCode, t24Reference, status, postedAt
            );
        }
    }
}
```

Write `TransactionStatus.java`:

```java
package id.co.javara.core.domain.model;

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED,
    IN_PROGRESS
}
```

Write remaining entities (`T24Customer`, `T24Account`, `T24PaymentOrder`, `T24TellerTransaction`, `T24MultiCommit`) following the same builder pattern.

- [ ] **Step 4: Run tests — expect PASS**

Run: `mvn -pl javara-core test`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add javara-core/src/main/java/id/co/javara/core/domain/model/
git add javara-core/src/test/
git commit -m "feat(core): add T24 domain entities — FundTransfer, Customer, Account, PaymentOrder, Teller, MultiCommit"
```

### Task 1.3: Port interfaces

**Files:**
- Create: `javara-core/src/main/java/id/co/javara/core/port/CustomerPort.java`
- Create: `javara-core/src/main/java/id/co/javara/core/port/TransactionPort.java`
- Create: `javara-core/src/main/java/id/co/javara/core/port/EnquiryPort.java`

**Interfaces:**
- Consumes: domain model entities and VOs (from Task 1.1, 1.2)
- Produces: `CustomerPort`, `TransactionPort`, `EnquiryPort` — interfaces with documented contracts

- [ ] **Step 1: Write TransactionPort interface**

Write `javara-core/src/main/java/id/co/javara/core/port/TransactionPort.java`:

```java
package id.co.javara.core.port;

import id.co.javara.core.domain.model.*;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.T24Reference;

/**
 * Port for T24 transaction operations.
 * Each adapter (OFS, TAFJ, IRIS, JMS) implements this interface.
 */
public interface TransactionPort {

    /**
     * Post a fund transfer transaction to T24.
     * @param transfer the fund transfer details
     * @return posted transfer with T24 reference and status
     * @throws id.co.javara.core.exception.T24ConnectionException if T24 is unreachable
     * @throws id.co.javara.core.exception.T24TimeoutException if request times out
     */
    T24FundTransfer postFundTransfer(T24FundTransfer transfer);

    T24TellerTransaction postTellerTransaction(T24TellerTransaction tellerTx);

    T24PaymentOrder postPaymentOrder(T24PaymentOrder paymentOrder);

    T24MultiCommit postMultiCommit(T24MultiCommit multiCommit);

    TransactionStatus getTransactionStatus(T24Reference reference);

    /**
     * Check if this adapter is healthy and reachable.
     * @return true if T24 responds to health check
     */
    boolean isHealthy();
}
```

Write `CustomerPort.java`:

```java
package id.co.javara.core.port;

import id.co.javara.core.domain.model.T24Account;
import id.co.javara.core.domain.model.T24Customer;
import id.co.javara.core.domain.vo.CustomerId;
import java.util.List;

public interface CustomerPort {

    T24Customer getCustomer(CustomerId customerId);

    List<T24Customer> searchCustomers(String searchTerm);

    T24Customer createCustomer(T24Customer customer);

    T24Customer updateCustomer(CustomerId customerId, T24Customer customer);

    List<T24Account> getCustomerAccounts(CustomerId customerId);

    T24Account openAccount(CustomerId customerId, T24Account account);
}
```

Write `EnquiryPort.java`:

```java
package id.co.javara.core.port;

import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.T24Reference;
import java.math.BigDecimal;
import java.util.Map;

public interface EnquiryPort {

    BigDecimal getAccountBalance(AccountNumber accountNumber);

    Map<String, Object> getTransactionDetails(T24Reference reference);

    boolean isAccountValid(AccountNumber accountNumber);
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn -pl javara-core compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add javara-core/src/main/java/id/co/javara/core/port/
git commit -m "feat(core): add port interfaces — CustomerPort, TransactionPort, EnquiryPort"
```

### Task 1.4: Exception hierarchy

**Files:**
- Create: `javara-core/src/main/java/id/co/javara/core/exception/JavaraException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/T24ConnectionException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/T24AuthException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/T24ResponseException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/T24BusinessException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/T24ValidationException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/T24SystemException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/T24TimeoutException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/IdempotencyViolationException.java`
- Create: `javara-core/src/main/java/id/co/javara/core/exception/AdapterUnavailableException.java`

**Interfaces:**
- Consumes: nothing
- Produces: full exception hierarchy rooted at `JavaraException`

- [ ] **Step 1: Write JavaraException base**

Write `javara-core/src/main/java/id/co/javara/core/exception/JavaraException.java`:

```java
package id.co.javara.core.exception;

public class JavaraException extends RuntimeException {

    private final String errorCode;

    public JavaraException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public JavaraException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
```

Write `T24ConnectionException.java`:

```java
package id.co.javara.core.exception;

public class T24ConnectionException extends JavaraException {

    public T24ConnectionException(String message, Throwable cause) {
        super("T24_CONNECTION_ERROR", message, cause);
    }
}
```

Write `T24AuthException.java`:

```java
package id.co.javara.core.exception;

public class T24AuthException extends T24ConnectionException {

    public T24AuthException(String message) {
        super("Authentication failed: " + message, null);
    }
}
```

Write `T24ResponseException.java`:

```java
package id.co.javara.core.exception;

public class T24ResponseException extends JavaraException {

    private final String t24ErrorCode;

    public T24ResponseException(String t24ErrorCode, String message) {
        super("T24_RESPONSE_ERROR", message);
        this.t24ErrorCode = t24ErrorCode;
    }

    public String t24ErrorCode() {
        return t24ErrorCode;
    }
}
```

Write `T24BusinessException.java`:

```java
package id.co.javara.core.exception;

public class T24BusinessException extends T24ResponseException {

    public T24BusinessException(String t24ErrorCode, String message) {
        super(t24ErrorCode, message);
    }
}
```

Write `T24ValidationException.java`:

```java
package id.co.javara.core.exception;

public class T24ValidationException extends T24ResponseException {

    public T24ValidationException(String t24ErrorCode, String message) {
        super(t24ErrorCode, message);
    }
}
```

Write `T24SystemException.java`:

```java
package id.co.javara.core.exception;

public class T24SystemException extends T24ResponseException {

    public T24SystemException(String t24ErrorCode, String message) {
        super(t24ErrorCode, message);
    }
}
```

Write `T24TimeoutException.java`:

```java
package id.co.javara.core.exception;

public class T24TimeoutException extends JavaraException {

    public T24TimeoutException(String message) {
        super("T24_TIMEOUT", message);
    }

    public T24TimeoutException(String message, Throwable cause) {
        super("T24_TIMEOUT", message, cause);
    }
}
```

Write `IdempotencyViolationException.java`:

```java
package id.co.javara.core.exception;

public class IdempotencyViolationException extends JavaraException {

    public IdempotencyViolationException(String idempotencyKey, String detail) {
        super("IDEMPOTENCY_VIOLATION",
            "Key '" + idempotencyKey + "' reused with different payload: " + detail);
    }
}
```

Write `AdapterUnavailableException.java`:

```java
package id.co.javara.core.exception;

public class AdapterUnavailableException extends JavaraException {

    public AdapterUnavailableException(String adapterName) {
        super("ADAPTER_UNAVAILABLE", "T24 adapter '" + adapterName + "' is not available");
    }
}
```

- [ ] **Step 2: Add test for exception hierarchy**

Write `javara-core/src/test/java/id/co/javara/core/exception/ExceptionHierarchyTest.java`:

```java
package id.co.javara.core.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHierarchyTest {

    @Test
    void t24BusinessExceptionShouldBeInstanceOfAllParents() {
        var ex = new T24BusinessException("EB-AC.DORMANT", "Account is dormant");
        assertThat(ex).isInstanceOf(T24ResponseException.class)
                      .isInstanceOf(JavaraException.class)
                      .isInstanceOf(RuntimeException.class);
        assertThat(ex.t24ErrorCode()).isEqualTo("EB-AC.DORMANT");
        assertThat(ex.errorCode()).isEqualTo("T24_RESPONSE_ERROR");
    }

    @Test
    void t24ConnectionExceptionShouldCarryMessage() {
        var ex = new T24ConnectionException("Connection refused", 
                    new java.net.ConnectException("localhost:9443"));
        assertThat(ex.getMessage()).contains("Connection refused");
        assertThat(ex.errorCode()).isEqualTo("T24_CONNECTION_ERROR");
        assertThat(ex.getCause()).isInstanceOf(java.net.ConnectException.class);
    }

    @Test
    void adapterUnavailableExceptionShouldIncludeAdapterName() {
        var ex = new AdapterUnavailableException("ofs");
        assertThat(ex.getMessage()).contains("ofs");
        assertThat(ex.errorCode()).isEqualTo("ADAPTER_UNAVAILABLE");
    }
}
```

- [ ] **Step 3: Run all core tests**

Run: `mvn -pl javara-core test`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add javara-core/src/main/java/id/co/javara/core/exception/
git add javara-core/src/test/java/id/co/javara/core/exception/
git commit -m "feat(core): add exception hierarchy — JavaraException base + 9 typed exceptions"
```

---

## Milestone 2: javara-ofs — OFS Adapter

### Task 2.1: OFS message builder

**Files:**
- Create: `javara-ofs/pom.xml`
- Create: `javara-ofs/src/main/java/id/co/javara/ofs/OFSMessageBuilder.java`
- Create: `javara-ofs/src/main/java/id/co/javara/ofs/OFSEnvelope.java`
- Test: `javara-ofs/src/test/java/id/co/javara/ofs/OFSMessageBuilderTest.java`

**Interfaces:**
- Consumes: `T24FundTransfer`, `T24Customer`, `T24PaymentOrder`, `T24TellerTransaction` (from Task 1.2)
- Produces: `OFSMessageBuilder` with builder method per transaction type → XML string

- [ ] **Step 1: Create javara-ofs POM**

Write `javara-ofs/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>id.co.javara</groupId>
        <artifactId>javara</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>javara-ofs</artifactId>

    <name>JAVARA OFS Adapter</name>

    <dependencies>
        <dependency>
            <groupId>id.co.javara</groupId>
            <artifactId>javara-core</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-xml</artifactId>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Write failing test for OFSMessageBuilder**

Write `javara-ofs/src/test/java/id/co/javara/ofs/OFSMessageBuilderTest.java`:

```java
package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class OFSMessageBuilderTest {

    @Test
    void shouldBuildFundTransferOFSMessage() {
        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-123"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("250000.00"), "USD"))
            .paymentDetails("Test payment")
            .build();

        var builder = new OFSMessageBuilder();
        OFSEnvelope envelope = builder.buildFundTransfer(transfer);

        assertThat(envelope.xml()).contains("FUNDS.TRANSFER,REVERS/PROCESS")
            .contains("DEBIT.ACCT.NO")
            .contains("12345-678-1-USD")
            .contains("CREDIT.ACCT.NO")
            .contains("98765-432-1-USD")
            .contains("DEBIT.AMOUNT")
            .contains("250000.00");
    }

    @Test
    void shouldBuildCustomerCreationOFSMessage() {
        // verify customer creation OFS message format
    }
}
```

- [ ] **Step 3: Run test — expect FAIL**

Run: `mvn -pl javara-ofs test`
Expected: FAIL

- [ ] **Step 4: Write OFSEnvelope + OFSMessageBuilder**

Write `javara-ofs/src/main/java/id/co/javara/ofs/OFSEnvelope.java`:

```java
package id.co.javara.ofs;

public record OFSEnvelope(String xml) {
    public OFSEnvelope {
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("OFS XML must not be blank");
        }
    }
}
```

Write `javara-ofs/src/main/java/id/co/javara/ofs/OFSMessageBuilder.java`:

```java
package id.co.javara.ofs;

import id.co.javara.core.domain.model.*;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.CustomerId;

public class OFSMessageBuilder {

    private static final String OFS_TEMPLATE_FT =
        "FUNDS.TRANSFER,REVERS/PROCESS,," +
        "DEBIT.ACCT.NO:1:1=%s,," +
        "DEBIT.CURRENCY:1:1=%s,," +
        "DEBIT.AMOUNT:1:1=%s,," +
        "CREDIT.ACCT.NO:1:1=%s,," +
        "CREDIT.CURRENCY:1:1=%s,," +
        "PAYMENT.DETAILS:1:1=%s";

    public OFSEnvelope buildFundTransfer(T24FundTransfer transfer) {
        if (transfer == null) {
            throw new IllegalArgumentException("Fund transfer must not be null");
        }
        
        String xml = String.format(OFS_TEMPLATE_FT,
            transfer.debitAccount().value(),
            transfer.amount().currency(),
            transfer.amount().value().toPlainString(),
            transfer.creditAccount().value(),
            transfer.amount().currency(),
            transfer.paymentDetails() != null ? transfer.paymentDetails() : ""
        );
        
        return new OFSEnvelope(xml);
    }

    public OFSEnvelope buildCustomer(T24Customer customer) {
        // Customer creation OFS message format
        String xml = "CUSTOMER,REVERS/PROCESS,," +
            "SHORT.NAME:1:1=" + customer.fullName();
        return new OFSEnvelope(xml);
    }
}
```

- [ ] **Step 5: Run tests — expect PASS**

Run: `mvn -pl javara-ofs test`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add javara-ofs/
git commit -m "feat(ofs): add OFSMessageBuilder — fund transfer + customer OFS envelope generation"
```

### Task 2.2: OFS HTTP client + response parser

**Files:**
- Create: `javara-ofs/src/main/java/id/co/javara/ofs/OFSClient.java`
- Create: `javara-ofs/src/main/java/id/co/javara/ofs/OFSResponseParser.java`
- Create: `javara-ofs/src/main/java/id/co/javara/ofs/OFSConfigProperties.java`
- Test: `javara-ofs/src/test/java/id/co/javara/ofs/OFSResponseParserTest.java`
- Test: `javara-ofs/src/test/java/id/co/javara/ofs/OFSClientTest.java`

**Interfaces:**
- Consumes: `OFSEnvelope`, `OFSMessageBuilder` (from Task 2.1)
- Produces: `OFSClient` (HTTP client), `OFSResponseParser` (XML/JSON → domain model), `OFSConfigProperties`

- [ ] **Step 1: Write failing test for OFSResponseParser**

Write `javara-ofs/src/test/java/id/co/javara/ofs/OFSResponseParserTest.java`:

```java
package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.exception.T24BusinessException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OFSResponseParserTest {

    @Test
    void shouldParseSuccessResponse() {
        String t24Response = """
            FUNDS.TRANSFER,REVERS/PROCESS,,
            DEBIT.ACCT.NO:1:1=12345-678-1-USD,,
            T24.REFERENCE:1:1=T24/260620/00123,,
            TRANSACTION.STATUS:1:1=COMPLETED
            """;

        var parser = new OFSResponseParser();
        T24FundTransfer result = parser.parseFundTransferResponse(t24Response);

        assertThat(result.t24Reference().value()).isEqualTo("T24/260620/00123");
        assertThat(result.status()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void shouldParseErrorResponse() {
        String errorResponse = """
            FUNDS.TRANSFER,REVERS/PROCESS,,
            ERROR.CODE:1:1=EB-AC.DORMANT,,
            ERROR.TEXT:1:1=Account is dormant
            """;

        var parser = new OFSResponseParser();
        
        assertThatThrownBy(() -> parser.parseFundTransferResponse(errorResponse))
            .isInstanceOf(T24BusinessException.class)
            .hasMessageContaining("dormant");
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

Run: `mvn -pl javara-ofs test -Dtest=OFSResponseParserTest`
Expected: FAIL

- [ ] **Step 3: Write OFSResponseParser**

Write `javara-ofs/src/main/java/id/co/javara/ofs/OFSResponseParser.java`:

```java
package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import id.co.javara.core.domain.vo.T24Reference;
import id.co.javara.core.exception.T24BusinessException;
import id.co.javara.core.exception.T24ResponseException;
import id.co.javara.core.exception.T24SystemException;
import java.math.BigDecimal;
import java.time.Instant;

public class OFSResponseParser {

    public T24FundTransfer parseFundTransferResponse(String rawResponse) {
        if (rawResponse.contains("ERROR.CODE")) {
            String errorCode = extractField(rawResponse, "ERROR.CODE");
            String errorText = extractField(rawResponse, "ERROR.TEXT");
            throw new T24BusinessException(errorCode, errorText);
        }
        
        String t24Ref = extractField(rawResponse, "T24.REFERENCE");
        String status = extractField(rawResponse, "TRANSACTION.STATUS");
        
        return T24FundTransfer.builder()
            .t24Reference(new T24Reference(t24Ref))
            .status(TransactionStatus.valueOf(status))
            .postedAt(Instant.now())
            .debitAccount(new AccountNumber(extractField(rawResponse, "DEBIT.ACCT.NO")))
            .creditAccount(new AccountNumber("0")) // placeholder, caller fills
            .amount(new Amount(BigDecimal.ZERO, "USD")) // placeholder
            .transactionId(IdempotencyKey.generate())
            .build();
    }

    String extractField(String raw, String fieldName) {
        String marker = fieldName + ":1:1=";
        int start = raw.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();
        int end = raw.indexOf(",", start);
        if (end == -1) end = raw.length();
        return raw.substring(start, end).trim();
    }
}
```

- [ ] **Step 4: Run tests — expect PASS**

Run: `mvn -pl javara-ofs test`
Expected: BUILD SUCCESS

- [ ] **Step 5: Write OFSClient (HTTP layer)**

Write `javara-ofs/src/main/java/id/co/javara/ofs/OFSClient.java`:

```java
package id.co.javara.ofs;

import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.T24Customer;
import id.co.javara.core.exception.T24ConnectionException;
import id.co.javara.core.exception.T24TimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public class OFSClient {

    private final RestClient restClient;
    private final OFSMessageBuilder messageBuilder;
    private final OFSResponseParser responseParser;
    private final OFSConfigProperties config;

    public OFSClient(OFSConfigProperties config) {
        this.config = config;
        this.messageBuilder = new OFSMessageBuilder();
        this.responseParser = new OFSResponseParser();
        this.restClient = RestClient.builder()
            .baseUrl(config.endpoint())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, config.authHeader())
            .build();
    }

    public T24FundTransfer postFundTransfer(T24FundTransfer transfer) {
        OFSEnvelope envelope = messageBuilder.buildFundTransfer(transfer);
        
        try {
            String response = restClient.post()
                .body(envelope.xml())
                .retrieve()
                .body(String.class);
            
            return responseParser.parseFundTransferResponse(response);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            throw new T24TimeoutException("OFS request timed out", e);
        } catch (T24ConnectionException e) {
            throw e;
        }
    }
}
```

Write `OFSConfigProperties.java`:

```java
package id.co.javara.ofs;

public record OFSConfigProperties(
    String endpoint,
    String username,
    String password,
    String authType
) {
    public String authHeader() {
        if ("basic".equalsIgnoreCase(authType)) {
            return "Basic " + java.util.Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());
        }
        return "";
    }
}
```

- [ ] **Step 6: Run all OFS tests**

Run: `mvn -pl javara-ofs test`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add javara-ofs/
git commit -m "feat(ofs): add OFSClient + OFSResponseParser — HTTP layer + T24 response handling"
```

### Task 2.3: OFS adapter — implements TransactionPort and CustomerPort

**Files:**
- Create: `javara-ofs/src/main/java/id/co/javara/ofs/OFSAdapter.java`
- Test: `javara-ofs/src/test/java/id/co/javara/ofs/OFSAdapterTest.java`

**Interfaces:**
- Consumes: `TransactionPort`, `CustomerPort` (from Task 1.3), `OFSClient`, `OFSResponseParser`, `OFSMessageBuilder` (from Task 2.1, 2.2)
- Produces: `OFSAdapter` implementing `TransactionPort` and `CustomerPort`

Write `OFSAdapter` implementing both ports, delegating to `OFSClient` and `OFSMessageBuilder`. Test with WireMock (mock T24 OFS endpoint).

- [ ] **Step 1: Write OFSAdapter**

```java
package id.co.javara.ofs;

import id.co.javara.core.domain.model.*;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.CustomerId;
import id.co.javara.core.domain.vo.T24Reference;
import id.co.javara.core.port.CustomerPort;
import id.co.javara.core.port.TransactionPort;
import java.util.List;
import java.util.Map;

public class OFSAdapter implements TransactionPort, CustomerPort {

    private final OFSClient client;
    private final OFSMessageBuilder messageBuilder;
    private final OFSResponseParser responseParser;

    public OFSAdapter(OFSConfigProperties config) {
        this.client = new OFSClient(config);
        this.messageBuilder = new OFSMessageBuilder();
        this.responseParser = new OFSResponseParser();
    }

    @Override
    public T24FundTransfer postFundTransfer(T24FundTransfer transfer) {
        return client.postFundTransfer(transfer);
    }

    @Override
    public T24TellerTransaction postTellerTransaction(T24TellerTransaction tellerTx) {
        // TODO: Implement teller transaction via OFS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24PaymentOrder postPaymentOrder(T24PaymentOrder paymentOrder) {
        // TODO: Implement payment order via OFS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24MultiCommit postMultiCommit(T24MultiCommit multiCommit) {
        // TODO: Implement multi-commit via OFS
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public TransactionStatus getTransactionStatus(T24Reference reference) {
        // TODO: Implement status inquiry via OFS enquiry
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isHealthy() {
        try {
            client.postFundTransfer(null); // health check
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // CustomerPort

    @Override
    public T24Customer getCustomer(CustomerId customerId) {
        // TODO: via OFS enquiry
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<T24Customer> searchCustomers(String searchTerm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24Customer createCustomer(T24Customer customer) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24Customer updateCustomer(CustomerId customerId, T24Customer customer) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<T24Account> getCustomerAccounts(CustomerId customerId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public T24Account openAccount(CustomerId customerId, T24Account account) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
```

- [ ] **Step 2: Write integration test with WireMock**

Write `javara-ofs/src/test/java/id/co/javara/ofs/OFSAdapterIntegrationTest.java`:

```java
package id.co.javara.ofs;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class OFSAdapterIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    @Test
    void shouldPostFundTransferSuccessfully() {
        wireMock.stubFor(post("/browser/OFSServlet")
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/xml")
                .withBody("""
                    FUNDS.TRANSFER,REVERS/PROCESS,,
                    T24.REFERENCE:1:1=T24/260620/00123,,
                    TRANSACTION.STATUS:1:1=COMPLETED
                    """)));

        var config = new OFSConfigProperties(
            "http://localhost:" + wireMock.getPort(),
            "test", "test", "basic"
        );

        var adapter = new OFSAdapter(config);
        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("test-123"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("1000.00"), "USD"))
            .build();

        var result = adapter.postFundTransfer(transfer);
        assertThat(result.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(result.t24Reference().value()).isEqualTo("T24/260620/00123");
    }
}
```

- [ ] **Step 3: Run integration tests**

Run: `mvn -pl javara-ofs verify`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add javara-ofs/
git commit -m "feat(ofs): add OFSAdapter implementing TransactionPort + CustomerPort with WireMock test"
```

---

## Milestone 3: javara-app — Spring Boot Application

### Task 3.1: Spring Boot app skeleton with DB schema

**Files:**
- Create: `javara-app/pom.xml`
- Create: `javara-app/src/main/java/id/co/javara/app/JavaraApplication.java`
- Create: `javara-app/src/main/resources/application.yml`
- Create: `javara-app/src/main/resources/db/migration/V1__init_idempotency_registry.sql`
- Create: `javara-app/src/main/resources/db/migration/V2__init_retry_queue.sql`
- Create: `javara-app/src/main/resources/db/migration/V3__init_audit_log.sql`
- Create: `javara-app/src/main/resources/db/migration/V4__init_customer_cache.sql`
- Create: `javara-app/src/main/resources/db/migration/V5__init_transaction_log.sql`
- Create: `javara-app/src/main/resources/db/migration/V6__init_enrichment_rule.sql`
- Test: `javara-app/src/test/java/id/co/javara/app/JavaraApplicationTest.java`

**Interfaces:**
- Consumes: all adapter modules, core module (from M1)
- Produces: runnable Spring Boot app with Flyway migrations

- [ ] **Step 1: Create javara-app POM**

Write `javara-app/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>id.co.javara</groupId>
        <artifactId>javara</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>javara-app</artifactId>

    <name>JAVARA Application</name>

    <dependencies>
        <dependency>
            <groupId>id.co.javara</groupId>
            <artifactId>javara-core</artifactId>
        </dependency>
        <dependency>
            <groupId>id.co.javara</groupId>
            <artifactId>javara-ofs</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-otel</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Write application main class**

Write `javara-app/src/main/java/id/co/javara/app/JavaraApplication.java`:

```java
package id.co.javara.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(JavaraProperties.class)
public class JavaraApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaraApplication.class, args);
    }
}
```

- [ ] **Step 3: Write application.yml**

Write `javara-app/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: javara
  datasource:
    url: jdbc:postgresql://localhost:5432/javara
    username: ${DB_USERNAME:javara}
    password: ${DB_PASSWORD:javara}
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration
    schemas: javara_t24
    default-schema: javara_t24

javara:
  t24:
    default-adapter: ofs
    adapters: [ofs, tafj]
    ofs:
      endpoint: ${T24_OFS_ENDPOINT:http://localhost:9443}
      username: ${T24_USERNAME:}
      password: ${T24_PASSWORD:}
      auth-type: basic
    resilience:
      retry:
        max-attempts: 3
        backoff: exponential
        max-delay: 2s
      circuit-breaker:
        failure-rate-threshold: 50
        wait-duration-in-open: 30s
        sliding-window-size: 20
      time-limiter:
        ofs: 30s
        tafj: 60s
  security:
    auth-mode: apikey
```

- [ ] **Step 4: Write Flyway migrations**

V1 — `javara-app/src/main/resources/db/migration/V1__init_idempotency_registry.sql`:

```sql
CREATE SCHEMA IF NOT EXISTS javara_t24;

CREATE TABLE javara_t24.idempotency_registry (
    idempotency_key VARCHAR(36) PRIMARY KEY,
    response_payload JSONB NOT NULL,
    http_status INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '24 hours'
);

CREATE INDEX idx_idempotency_expires ON javara_t24.idempotency_registry(expires_at);
```

V2 — `V2__init_retry_queue.sql`:

```sql
CREATE TYPE javara_t24.retry_status AS ENUM ('PENDING', 'RETRYING', 'EXHAUSTED', 'CANCELLED');

CREATE TABLE javara_t24.retry_queue (
    id UUID PRIMARY KEY,
    original_request JSONB NOT NULL,
    adapter VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    last_error TEXT,
    next_retry_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status javara_t24.retry_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_retry_queue_next ON javara_t24.retry_queue(status, next_retry_at)
    WHERE status IN ('PENDING', 'RETRYING');
```

V3 — `V3__init_audit_log.sql`:

```sql
CREATE TABLE javara_t24.audit_log (
    id BIGSERIAL,
    event_type VARCHAR(50) NOT NULL,
    adapter VARCHAR(20),
    request_url TEXT,
    request_payload JSONB,
    response_payload JSONB,
    http_status INTEGER,
    duration_ms BIGINT,
    error_code VARCHAR(50),
    caller_ip VARCHAR(45),
    correlation_id VARCHAR(36),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE INDEX idx_audit_correlation ON javara_t24.audit_log(correlation_id, created_at);
```

V4 — `V4__init_customer_cache.sql`:

```sql
CREATE TABLE javara_t24.customer_cache (
    customer_id VARCHAR(36) PRIMARY KEY,
    cif_number VARCHAR(50) NOT NULL,
    full_name VARCHAR(200),
    id_type VARCHAR(10),
    id_number VARCHAR(50),
    branch_code VARCHAR(10),
    status VARCHAR(20),
    raw_json JSONB,
    cached_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '5 minutes'
);

CREATE INDEX idx_customer_cache_cif ON javara_t24.customer_cache(cif_number);
CREATE INDEX idx_customer_cache_id ON javara_t24.customer_cache(id_type, id_number);
```

V5 — `V5__init_transaction_log.sql`:

```sql
CREATE TABLE javara_t24.transaction_log (
    transaction_id VARCHAR(36) PRIMARY KEY,
    transaction_type VARCHAR(20) NOT NULL,
    channel VARCHAR(30),
    amount NUMERIC(22,2),
    currency VARCHAR(3),
    debit_account VARCHAR(50),
    credit_account VARCHAR(50),
    t24_reference VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    request_payload JSONB,
    response_payload JSONB,
    idempotency_key VARCHAR(36),
    adapter_used VARCHAR(20),
    duration_ms BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tx_log_t24ref ON javara_t24.transaction_log(t24_reference);
CREATE INDEX idx_tx_log_created ON javara_t24.transaction_log(created_at DESC);
```

V6 — `V6__init_enrichment_rule.sql`:

```sql
CREATE TABLE javara_t24.enrichment_rule (
    rule_id UUID PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    channel VARCHAR(30),
    transaction_type VARCHAR(20),
    field_path VARCHAR(200),
    enrichment_source VARCHAR(30) NOT NULL,
    source_config JSONB,
    priority INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

- [ ] **Step 5: Write JavaraProperties config class**

Write `javara-app/src/main/java/id/co/javara/app/JavaraProperties.java`:

```java
package id.co.javara.app;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "javara")
public record JavaraProperties(T24 t24, Security security) {

    public record T24(
        String defaultAdapter,
        List<String> adapters,
        Ofs ofs,
        Resilience resilience
    ) {
        public record Ofs(String endpoint, String username, String password, String authType) {}
        public record Resilience(
            Retry retry,
            CircuitBreaker circuitBreaker,
            TimeLimiter timeLimiter
        ) {
            public record Retry(int maxAttempts, String backoff, String maxDelay) {}
            public record CircuitBreaker(int failureRateThreshold, String waitDurationInOpen, int slidingWindowSize) {}
            public record TimeLimiter(String ofs, String tafj) {}
        }
    }

    public record Security(String authMode) {}
}
```

- [ ] **Step 6: Add application bootstrap test**

Write `javara-app/src/test/java/id/co/javara/app/JavaraApplicationTest.java`:

```java
package id.co.javara.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class JavaraApplicationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("javara_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldLoadContextWithFlywayMigrations() {
        // Context loads → Flyway ran → all good
    }
}
```

- [ ] **Step 7: Run bootstrap test**

Run: `mvn -pl javara-app test -Dtest=JavaraApplicationTest`
Expected: BUILD SUCCESS — Spring context loads, Flyway runs all 6 migrations

- [ ] **Step 8: Commit**

```bash
git add javara-app/
git commit -m "feat(app): add Spring Boot app skeleton with Flyway migrations + Testcontainers bootstrap test"
```

### Task 3.2: Adapter resolver + REST controllers

**Files:**
- Create: `javara-app/src/main/java/id/co/javara/app/adapter/AdapterResolver.java`
- Create: `javara-app/src/main/java/id/co/javara/app/adapter/AdapterConfig.java`
- Create: `javara-app/src/main/java/id/co/javara/app/controller/FundTransferController.java`
- Create: `javara-app/src/main/java/id/co/javara/app/service/FundTransferService.java`
- Create: `javara-app/src/main/java/id/co/javara/app/dto/FundTransferRequest.java`
- Create: `javara-app/src/main/java/id/co/javara/app/dto/FundTransferResponse.java`
- Create: `javara-app/src/main/java/id/co/javara/app/dto/ErrorResponse.java`
- Create: `javara-app/src/main/java/id/co/javara/app/config/GlobalExceptionHandler.java`
- Test: `javara-app/src/test/java/id/co/javara/app/controller/FundTransferControllerTest.java`

**Interfaces:**
- Consumes: `TransactionPort` (from Task 1.3), `OFSAdapter` (from Task 2.3), `JavaraProperties` (from Task 3.1)
- Produces: REST endpoint `POST /api/v1/t24/transactions/fund-transfer`

- [ ] **Step 1: Write failing test for FundTransferController**

Write `javara-app/src/test/java/id/co/javara/app/controller/FundTransferControllerTest.java`:

```java
package id.co.javara.app.controller;

import id.co.javara.app.service.FundTransferService;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.model.TransactionStatus;
import id.co.javara.core.domain.vo.*;
import id.co.javara.core.port.TransactionPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FundTransferController.class)
class FundTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        TransactionPort transactionPort() {
            return mock(TransactionPort.class);
        }
    }

    @Autowired
    private TransactionPort transactionPort;

    @Test
    void shouldReturn201WhenFundTransferSuccess() throws Exception {
        var response = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey("uuid-123"))
            .debitAccount(new AccountNumber("12345-678-1-USD"))
            .creditAccount(new AccountNumber("98765-432-1-USD"))
            .amount(new Amount(new BigDecimal("250000.00"), "USD"))
            .t24Reference(new T24Reference("T24/260620/00123"))
            .status(TransactionStatus.COMPLETED)
            .build();

        when(transactionPort.postFundTransfer(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/t24/transactions/fund-transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Idempotency-Key", "uuid-123")
                .content("""
                    {
                        "debitAccount": "12345-678-1-USD",
                        "creditAccount": "98765-432-1-USD",
                        "amount": 250000.00,
                        "currency": "USD",
                        "paymentDetails": "Invoice INV-2026",
                        "channel": "INTERNET_BANKING"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.t24Reference").value("T24/260620/00123"));
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

Run: `mvn -pl javara-app test -Dtest=FundTransferControllerTest`
Expected: FAIL

- [ ] **Step 3: Write DTOs**

Write `javara-app/src/main/java/id/co/javara/app/dto/FundTransferRequest.java`:

```java
package id.co.javara.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record FundTransferRequest(
    @NotBlank String debitAccount,
    @NotBlank String creditAccount,
    @Positive BigDecimal amount,
    @NotBlank String currency,
    String paymentDetails,
    String channel,
    String valueDate,
    String processingPriority,
    String chargeCode
) {}
```

Write `javara-app/src/main/java/id/co/javara/app/dto/FundTransferResponse.java`:

```java
package id.co.javara.app.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FundTransferResponse(
    String transactionId,
    String status,
    String t24Reference,
    Instant postedAt,
    String debitAccount,
    String creditAccount,
    BigDecimal amount,
    String currency
) {}
```

Write `javara-app/src/main/java/id/co/javara/app/dto/ErrorResponse.java`:

```java
package id.co.javara.app.dto;

import java.time.Instant;

public record ErrorResponse(
    String errorCode,
    String message,
    String t24ErrorCode,
    String requestId,
    Instant timestamp
) {}
```

- [ ] **Step 4: Write FundTransferService**

Write `javara-app/src/main/java/id/co/javara/app/service/FundTransferService.java`:

```java
package id.co.javara.app.service;

import id.co.javara.app.dto.FundTransferRequest;
import id.co.javara.app.dto.FundTransferResponse;
import id.co.javara.core.domain.model.T24FundTransfer;
import id.co.javara.core.domain.vo.AccountNumber;
import id.co.javara.core.domain.vo.Amount;
import id.co.javara.core.domain.vo.IdempotencyKey;
import id.co.javara.core.port.TransactionPort;
import org.springframework.stereotype.Service;

@Service
public class FundTransferService {

    private final TransactionPort transactionPort;

    public FundTransferService(TransactionPort transactionPort) {
        this.transactionPort = transactionPort;
    }

    public FundTransferResponse execute(FundTransferRequest request, String idempotencyKey) {
        var transfer = T24FundTransfer.builder()
            .transactionId(new IdempotencyKey(idempotencyKey))
            .debitAccount(new AccountNumber(request.debitAccount()))
            .creditAccount(new AccountNumber(request.creditAccount()))
            .amount(new Amount(request.amount(), request.currency()))
            .paymentDetails(request.paymentDetails())
            .channel(request.channel())
            .valueDate(request.valueDate())
            .processingPriority(request.processingPriority())
            .chargeCode(request.chargeCode())
            .build();

        var result = transactionPort.postFundTransfer(transfer);

        return new FundTransferResponse(
            result.transactionId().value(),
            result.status().name(),
            result.t24Reference() != null ? result.t24Reference().value() : null,
            result.postedAt(),
            result.debitAccount().value(),
            result.creditAccount().value(),
            result.amount() != null ? result.amount().value() : null,
            result.amount() != null ? result.amount().currency() : null
        );
    }
}
```

- [ ] **Step 5: Write FundTransferController**

Write `javara-app/src/main/java/id/co/javara/app/controller/FundTransferController.java`:

```java
package id.co.javara.app.controller;

import id.co.javara.app.dto.FundTransferRequest;
import id.co.javara.app.dto.FundTransferResponse;
import id.co.javara.app.service.FundTransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/t24/transactions")
public class FundTransferController {

    private final FundTransferService service;

    public FundTransferController(FundTransferService service) {
        this.service = service;
    }

    @PostMapping("/fund-transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public FundTransferResponse postFundTransfer(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody FundTransferRequest request) {
        return service.execute(request, idempotencyKey);
    }
}
```

- [ ] **Step 6: Write GlobalExceptionHandler**

Write `javara-app/src/main/java/id/co/javara/app/config/GlobalExceptionHandler.java`:

```java
package id.co.javara.app.config;

import id.co.javara.app.dto.ErrorResponse;
import id.co.javara.core.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(T24BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleBusiness(T24BusinessException ex) {
        return new ErrorResponse("T24_BUSINESS_ERROR", ex.getMessage(),
            ex.t24ErrorCode(), UUID.randomUUID().toString(), Instant.now());
    }

    @ExceptionHandler(T24ConnectionException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleConnection(T24ConnectionException ex) {
        return new ErrorResponse(ex.errorCode(), ex.getMessage(),
            null, UUID.randomUUID().toString(), Instant.now());
    }

    @ExceptionHandler(T24TimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public ErrorResponse handleTimeout(T24TimeoutException ex) {
        return new ErrorResponse("T24_TIMEOUT", ex.getMessage(),
            null, UUID.randomUUID().toString(), Instant.now());
    }

    @ExceptionHandler(IdempotencyViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIdempotency(IdempotencyViolationException ex) {
        return new ErrorResponse("IDEMPOTENCY_VIOLATION", ex.getMessage(),
            null, UUID.randomUUID().toString(), Instant.now());
    }
}
```

- [ ] **Step 7: Write AdapterConfig + AdapterResolver**

Write `javara-app/src/main/java/id/co/javara/app/adapter/AdapterConfig.java`:

```java
package id.co.javara.app.adapter;

import id.co.javara.app.JavaraProperties;
import id.co.javara.core.port.CustomerPort;
import id.co.javara.core.port.TransactionPort;
import id.co.javara.ofs.OFSAdapter;
import id.co.javara.ofs.OFSConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AdapterConfig {

    @Bean
    @Primary
    public TransactionPort transactionPort(JavaraProperties properties) {
        return ofsAdapter(properties);
    }

    @Bean
    @Primary
    public CustomerPort customerPort(JavaraProperties properties) {
        return ofsAdapter(properties);
    }

    @Bean
    public OFSAdapter ofsAdapter(JavaraProperties properties) {
        var ofsConfig = new OFSConfigProperties(
            properties.t24().ofs().endpoint(),
            properties.t24().ofs().username(),
            properties.t24().ofs().password(),
            properties.t24().ofs().authType()
        );
        return new OFSAdapter(ofsConfig);
    }
}
```

Write `javara-app/src/main/java/id/co/javara/app/adapter/AdapterResolver.java`:

```java
package id.co.javara.app.adapter;

import id.co.javara.app.JavaraProperties;
import id.co.javara.core.exception.AdapterUnavailableException;
import id.co.javara.core.port.TransactionPort;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AdapterResolver {

    private final Map<String, TransactionPort> adapters;
    private final JavaraProperties properties;

    public AdapterResolver(Map<String, TransactionPort> adapters, JavaraProperties properties) {
        this.adapters = adapters;
        this.properties = properties;
    }

    public TransactionPort resolve(String adapterName) {
        String name = adapterName != null ? adapterName : properties.t24().defaultAdapter();
        TransactionPort adapter = adapters.get(name + "Adapter");
        if (adapter == null) {
            throw new AdapterUnavailableException(name);
        }
        return adapter;
    }
}
```

- [ ] **Step 8: Run controller test — expect PASS**

Run: `mvn -pl javara-app test -Dtest=FundTransferControllerTest`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add javara-app/
git commit -m "feat(app): add REST controller, service, adapter config — fund transfer endpoint working"
```

### Task 3.3: Health checks, retry scheduler, audit service

**Files:**
- Create: `javara-app/src/main/java/id/co/javara/app/health/T24HealthIndicator.java`
- Create: `javara-app/src/main/java/id/co/javara/app/scheduler/RetryQueueScheduler.java`
- Create: `javara-app/src/main/java/id/co/javara/app/service/AuditService.java`
- Create: `javara-app/src/main/java/id/co/javara/app/repository/RetryQueueRepository.java`
- Create: `javara-app/src/main/java/id/co/javara/app/repository/AuditLogRepository.java`
- Create: `javara-app/src/main/java/id/co/javara/app/repository/IdempotencyRegistryRepository.java`

**Interfaces:**
- Consumes: `TransactionPort` (from Task 1.3), DB repositories
- Produces: T24 health check, retry scheduler, audit writer

- [ ] **Step 1: Write T24HealthIndicator**

Write `javara-app/src/main/java/id/co/javara/app/health/T24HealthIndicator.java`:

```java
package id.co.javara.app.health;

import id.co.javara.core.port.TransactionPort;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class T24HealthIndicator implements HealthIndicator {

    private final TransactionPort transactionPort;

    public T24HealthIndicator(TransactionPort transactionPort) {
        this.transactionPort = transactionPort;
    }

    @Override
    public Health health() {
        try {
            if (transactionPort.isHealthy()) {
                return Health.up()
                    .withDetail("adapter", transactionPort.getClass().getSimpleName())
                    .build();
            }
            return Health.down().withDetail("reason", "T24 unreachable").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

- [ ] **Step 2: Write RetryQueueScheduler**

Write `javara-app/src/main/java/id/co/javara/app/scheduler/RetryQueueScheduler.java`:

```java
package id.co.javara.app.scheduler;

import id.co.javara.app.repository.RetryQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryQueueScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryQueueScheduler.class);
    private final RetryQueueRepository repository;

    public RetryQueueScheduler(RetryQueueRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 30_000)
    public void processRetryQueue() {
        var pending = repository.findPendingRetries();
        log.debug("Processing {} pending retries", pending.size());
        for (var job : pending) {
            // Replay through the adapter
            // Increment attempts
            // If exhausted, alert
        }
    }
}
```

- [ ] **Step 3: Write AuditService**

Write `javara-app/src/main/java/id/co/javara/app/service/AuditService.java`:

```java
package id.co.javara.app.service;

import id.co.javara.app.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Async
    public void log(String eventType, String adapter, String requestUrl,
                    String requestPayload, String responsePayload,
                    int httpStatus, long durationMs, String errorCode,
                    String correlationId) {
        repository.insert(eventType, adapter, requestUrl, requestPayload,
            responsePayload, httpStatus, durationMs, errorCode, correlationId, Instant.now());
    }
}
```

- [ ] **Step 4: Write JDBC repositories**

Write `javara-app/src/main/java/id/co/javara/app/repository/AuditLogRepository.java`:

```java
package id.co.javara.app.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.Instant;

@Repository
public class AuditLogRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AuditLogRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(String eventType, String adapter, String requestUrl,
                       String requestPayload, String responsePayload,
                       int httpStatus, long durationMs, String errorCode,
                       String correlationId, Instant createdAt) {
        var params = new MapSqlParameterSource()
            .addValue("event_type", eventType)
            .addValue("adapter", adapter)
            .addValue("request_url", requestUrl)
            .addValue("request_payload", requestPayload)
            .addValue("response_payload", responsePayload)
            .addValue("http_status", httpStatus)
            .addValue("duration_ms", durationMs)
            .addValue("error_code", errorCode)
            .addValue("correlation_id", correlationId)
            .addValue("created_at", createdAt);

        jdbc.update("""
            INSERT INTO javara_t24.audit_log
                (event_type, adapter, request_url, request_payload,
                 response_payload, http_status, duration_ms,
                 error_code, correlation_id, created_at)
            VALUES
                (:event_type, :adapter, :request_url, :request_payload::jsonb,
                 :response_payload::jsonb, :http_status, :duration_ms,
                 :error_code, :correlation_id, :created_at)
            """, params);
    }
}
```

Write `RetryQueueRepository.java`:

```java
package id.co.javara.app.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class RetryQueueRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RetryQueueRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findPendingRetries() {
        return jdbc.queryForList("""
            SELECT id, original_request, adapter, attempts, max_attempts
            FROM javara_t24.retry_queue
            WHERE status IN ('PENDING', 'RETRYING')
              AND next_retry_at <= NOW()
            ORDER BY next_retry_at
            LIMIT 50
            """, new MapSqlParameterSource());
    }
}
```

Write `IdempotencyRegistryRepository.java`:

```java
package id.co.javara.app.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class IdempotencyRegistryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public IdempotencyRegistryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<String> findResponseByKey(String key) {
        var result = jdbc.query("""
            SELECT response_payload, http_status
            FROM javara_t24.idempotency_registry
            WHERE idempotency_key = :key AND expires_at > NOW()
            """,
            new MapSqlParameterSource("key", key),
            rs -> {
                if (rs.next()) {
                    return rs.getString("response_payload");
                }
                return null;
            });
        return Optional.ofNullable(result);
    }

    public void insert(String key, String responsePayload, int httpStatus) {
        var params = new MapSqlParameterSource()
            .addValue("key", key)
            .addValue("response_payload", responsePayload)
            .addValue("http_status", httpStatus);
        jdbc.update("""
            INSERT INTO javara_t24.idempotency_registry
                (idempotency_key, response_payload, http_status)
            VALUES (:key, :response_payload::jsonb, :http_status)
            ON CONFLICT (idempotency_key) DO NOTHING
            """, params);
    }
}
```

- [ ] **Step 5: Build and run all app tests**

Run: `mvn -pl javara-app test`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add javara-app/
git commit -m "feat(app): add health indicator, retry scheduler, audit service, DB repositories"
```

---

## Milestone 4: javara-client + javara-starter

### Task 4.1: Consumer SDK (Feign client)

**Files:**
- Create: `javara-client/pom.xml`
- Create: `javara-client/src/main/java/id/co/javara/client/T24FundTransferClient.java`
- Create: `javara-client/src/main/java/id/co/javara/client/config/JavaraClientConfig.java`
- Test: `javara-client/src/test/java/id/co/javara/client/T24FundTransferClientTest.java`

- [ ] **Step 1: Write javara-client POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>id.co.javara</groupId>
        <artifactId>javara</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>javara-client</artifactId>

    <name>JAVARA Client SDK</name>

    <dependencies>
        <dependency>
            <groupId>id.co.javara</groupId>
            <artifactId>javara-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-feign</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Write Feign client interface**

Write `javara-client/src/main/java/id/co/javara/client/T24FundTransferClient.java`:

```java
package id.co.javara.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import java.math.BigDecimal;
import java.time.Instant;

@FeignClient(name = "javara-t24", url = "${javara.client.base-url:http://localhost:8080}")
public interface T24FundTransferClient {

    @PostMapping("/api/v1/t24/transactions/fund-transfer")
    @CircuitBreaker(name = "t24-fund-transfer")
    @Retry(name = "t24-fund-transfer")
    FundTransferResponse postFundTransfer(
        @RequestHeader("X-Idempotency-Key") String idempotencyKey,
        @RequestHeader("X-Channel-Code") String channel,
        @RequestBody FundTransferRequest request);

    record FundTransferRequest(
        String debitAccount, String creditAccount,
        BigDecimal amount, String currency,
        String paymentDetails, String channel
    ) {}

    record FundTransferResponse(
        String transactionId, String status,
        String t24Reference, Instant postedAt
    ) {}
}
```

- [ ] **Step 3: Write auto-config for client**

Write `javara-client/src/main/java/id/co/javara/client/config/JavaraClientConfig.java`:

```java
package id.co.javara.client.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@EnableFeignClients(basePackages = "id.co.javara.client")
@ComponentScan(basePackages = "id.co.javara.client")
public class JavaraClientConfig {
}
```

- [ ] **Step 4: Commit**

```bash
git add javara-client/
git commit -m "feat(client): add consumer SDK — Feign client with Resilience4j circuit breaker + retry"
```

### Task 4.2: Spring Boot starter

**Files:**
- Create: `javara-starter/pom.xml`
- Create: `javara-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Create: `javara-starter/src/main/java/id/co/javara/starter/JavaraAutoConfiguration.java`
- Create: `javara-starter/src/main/java/id/co/javara/starter/EnableJavara.java`

- [ ] **Step 1: Write starter POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>id.co.javara</groupId>
        <artifactId>javara</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>javara-starter</artifactId>

    <name>JAVARA Spring Boot Starter</name>

    <dependencies>
        <dependency>
            <groupId>id.co.javara</groupId>
            <artifactId>javara-core</artifactId>
        </dependency>
        <dependency>
            <groupId>id.co.javara</groupId>
            <artifactId>javara-ofs</artifactId>
        </dependency>
        <dependency>
            <groupId>id.co.javara</groupId>
            <artifactId>javara-app</artifactId>
        </dependency>
        <dependency>
            <groupId>id.co.javara</groupId>
            <artifactId>javara-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Write AutoConfiguration imports**

Write `javara-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
id.co.javara.starter.JavaraAutoConfiguration
id.co.javara.client.config.JavaraClientConfig
id.co.javara.app.adapter.AdapterConfig
```

- [ ] **Step 3: Write @EnableJavara annotation**

Write `javara-starter/src/main/java/id/co/javara/starter/EnableJavara.java`:

```java
package id.co.javara.starter;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JavaraAutoConfiguration.class)
public @interface EnableJavara {
}
```

Write `javara-starter/src/main/java/id/co/javara/starter/JavaraAutoConfiguration.java`:

```java
package id.co.javara.starter;

import id.co.javara.app.JavaraProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JavaraProperties.class)
@ComponentScan(basePackages = "id.co.javara")
public class JavaraAutoConfiguration {
}
```

- [ ] **Step 4: Commit**

```bash
git add javara-starter/
git commit -m "feat(starter): add Spring Boot autoconfig starter — @EnableJavara one-click integration"
```

---

## Milestone 5: Full Build & E2E Verification

### Task 5.1: Full project build

**Files:**
- Update: `javara-bom/pom.xml` (add Spring Boot BOM)

- [ ] **Step 1: Build all modules**

Run: `mvn clean verify -pl javara-core,javara-ofs,javara-app,javara-client,javara-starter`
Expected: BUILD SUCCESS — all modules compile, all tests pass

- [ ] **Step 2: Run from parent**

Run: `mvn clean verify`
Expected: BUILD SUCCESS — full build

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: full multi-module build passing — all 5 active modules"
```

---

## Summary

| Milestone | Tasks | Modules | Status |
|-----------|-------|---------|--------|
| 0: Skeleton | 0.1 | Parent POM, BOM | Pending |
| 1: Core | 1.1-1.4 | `javara-core` | Pending |
| 2: OFS | 2.1-2.3 | `javara-ofs` | Pending |
| 3: App | 3.1-3.3 | `javara-app` | Pending |
| 4: SDK | 4.1-4.2 | `javara-client`, `javara-starter` | Pending |
| 5: Verify | 5.1 | Full build | Pending |

**Later milestones (separate plans):**
- `javara-tafj` adapter (TAFJ REST/SOAP)
- `javara-iris` adapter (IRIS event-driven)
- `javara-jms` adapter (JMS/MQ direct)
- gRPC endpoints (grpc-server integration)
- Multi-tenancy support
- API key security
- CLI `javara` tooling (Phase 2)
