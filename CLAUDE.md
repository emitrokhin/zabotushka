# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Zabotushka is a Quarkus-based bot platform integrating with **Telegram** and **Max** (Russian messaging platform) 
to manage group memberships based on **Greenway** partner qualification data. Users authorize via bot, link their 
Greenway partner ID, and the system automatically adds/removes them from groups based on qualification checks.

## Build and Development Commands

### Build & Run Commands

### Build and Package
```bash
# Development mode (hot reload)
./mvnw quarkus:dev

# Build JAR
./mvnw clean package

# Build über-jar
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

### Run Tests
```bash
# Development mode (hot reload)
./mvnw quarkus:dev

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthorizedMaxUserServiceTest

# Run a specific test method
./mvnw test -Dtest=AuthorizedMaxUserServiceTest#testSaveAuthorizedUser_Success

# Run integration tests (failsafe)
mvn verify
```
### Run Application Locally
```bash
# Set required environment variables first
export BASIC_AUTH_PASSWORD=
export BASIC_AUTH_USERNAME=
export GREENWAY_ID=
export GREENWAY_PASSWORD=
export HOST=
export MAX_BOT_TOKEN=
export MAX_BOT_WEBHOOK_SECRET=some_secret
export PG_DB_NAME=zabotushka
export PG_HOST=localhost
export PG_LOGIN=user
export PG_PASSWORD=password
export TELEGRAM_ACCESS_BOT_TOKEN=
export TELEGRAM_ACCESS_BOT_WEBHOOK_ENABLED=
export TELEGRAM_ACCESS_BOT_WEBHOOK_SECRET=
export TELEGRAM_MESSAGE_BOT_TOKEN=

# Run with Maven
mvn quarkus:dev

# Or with profile
mvn quarkus:dev -Dspring-boot.run.profiles=$PROFILE_NAME
```

## Architecture

**Stack**: 
Quarkus 3.30.6, 
Java 25, 
Hibernate ORM Panache, 
PostgreSQL (H2 for tests), 
Flyway, 
Quarkus Bucket4j (rate limiting), 
MapStruct, 
OpenTelemetry.

### Multi-Platform Abstraction

The core design uses a Template Method pattern for platform-agnostic group management:
- `AbstractGroupAccessService` — defines qualification check flow; subclasses (`TelegramGroupAccessService`, `MaxGroupAccessService`) provide platform-specific kick/notification logic
- `AbstractJoinRequestService` — same pattern for handling join requests
- `PlatformGroupAccessService`, `PlatformMessageService`, `MessengerWebhookRegistrar` — interfaces for each platform capability

### Key Service Layers

| Layer | Package | Purpose |
|-------|---------|---------|
| Resources (REST) | `resource/` | Webhooks + authorization endpoints for Telegram & Max |
| Services | `service/telegram/`, `service/max/` | Platform-specific implementations |
| Greenway | `service/greenway/` | Partner auth + qualification checking |
| Scheduler | `scheduler/` | `GroupQualificationScheduler` — cron entry point (8th of month, 00:00); delegates to `GroupQualificationOrchestrator` |
| Qualification | `scheduler/qualification/` | `GroupQualificationOrchestrator` collects all `PlatformQualificationProcessor` beans via CDI `Instance<>`, runs monthly check, merges `QualificationProcessStats`; `AbstractPlatformQualificationProcessor<U>` provides Template Method for per-chatId membership iteration, orphaned-member removal, and `lastCheckedAt` update; concrete classes: `TelegramQualificationProcessor`, `MaxQualificationProcessor` |
| Clients | `client/` | REST clients: `TelegramAccessBotApi`, `TelegramMessageBotApi`, `MaxBotApi`, `MyGreenwayApi`, `MyGreenwayLoginApi` |
| Entities | `entity/` | `AuthorizedTelegramUser`, `AuthorizedMaxUser`, `UserGroupMembership` (Panache active record) |
| Config | `config/` | `TelegramChatGroupRequirements`, `MaxChatGroupRequirements` (implement `ChatGroupRequirements` interface) |

### Data Flow

1. User starts bot → `TelegramAuthorizationResource` / `MaxAuthorizationResource` guides them through Greenway credential linking
2. Webhook events (join requests for Telegram, `user_added` events for Max) trigger `TelegramJoinRequestService` / `MaxJoinRequestService`
3. `GroupQualificationScheduler` (cron: 8th of month at 00:00) delegates to `GroupQualificationOrchestrator` → iterates all `PlatformQualificationProcessor` beans → each processor iterates `UserGroupMembership` per `chatId`, calls platform service to check/remove non-qualified users, handles orphaned memberships (user no longer in authorized users table), and returns `QualificationProcessStats`
4. `UserGroupMembership` entity tracks each user's presence in each group with last-check timestamps

### Database

Flyway migrations in `src/main/resources/db/migration/`. Current schema:
- `authorized_telegram_users` — maps `telegram_id` → `greenway_id`
- `authorized_max_users` — maps `max_id` → `greenway_id`
- `user_group_memberships` — tracks `(platform_user_id, chat_id, platform)` with timestamps

- When adding new database changes:
1. Create new versioned migration file in `src/main/resources/db/migration/`
2. Test migration locally before committing
3. Never modify existing migrations

### Testing

- `@QuarkusTest` with H2 in-memory DB (Flyway disabled, schema created by Hibernate)
- `GreenwayServiceTestProfile` disables Greenway API startup
- Tests use `@Transactional` + `@AfterEach` cleanup
- REST endpoint tests use REST Assured; service tests use `@ExtendWith(MockitoExtension.class)` with Mockito + AssertJ
- **Test naming**: `methodName_ShouldExpectedBehavior_WhenCondition`
- **`@DisplayName`**: English description of the scenario
- **AssertJ**: Always use `.as("description")` on assertions to describe what is being checked:
  ```java
  assertThat(result).as("Card should exist").isEqualTo(card);
  assertThatThrownBy(() -> service.method())
          .as("Exception should be thrown when card not found")
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("...");
  ```
- **Parameterized tests**: Use `@ParameterizedTest` + `@ValueSource` for testing the same behavior across multiple input values (e.g., error codes)
- **Test fixtures**: Extract reusable object creation into private helper methods (e.g., `createMockWebhookRequest(...)`)
- **`var`**: Use for all local variables in tests
- Call verify() without times(1), if only one call expected, i.e
  ```java
  verify(service).onlyOnce(someArg);
  ```
- **Static mocking of Panache active-record methods**: Use `MockedStatic` initialized in `@BeforeEach` and closed in `@AfterEach`:
  ```java
  MockedStatic<UserGroupMembership> membershipMock;

  @BeforeEach void setUp() { membershipMock = mockStatic(UserGroupMembership.class); }
  @AfterEach void tearDown() { membershipMock.close(); }
  ```
- **Spying on Panache entity instances** to stub `persist()` / `delete()`:
  ```java
  var membership = spy(createMembership(12345L, chatId));
  doNothing().when(membership).persist();
  ```

### Security

- HTTP Basic Auth on protected endpoints (`BasicAuthIdentityProvider`)
- Telegram webhook secret verified via request header
- Max webhook secret verified via `X-Max-Bot-Api-Secret` header
- All bot tokens/credentials loaded from environment variables

### Configuration

- `src/main/resources/application.yml` — production config (PostgreSQL, rate limiter: 25 req/sec for Telegram, webhook URLs, OTLP observability, log rotation)
- `src/test/resources/application.yml` — test overrides (H2 in-memory, webhooks disabled)

### Code Style Conventions
- **Javadoc**: Use single-line `///` format for documentation comments (as seen in `AsyncConfig.java`)
- **Exceptions**: Use unnamed pattern variable when exception is not used:
  ```java
  try { ... } catch (Exception _) { ... }
  ```
- **Mappers**: Use MapStruct with shared `@Mapper(config = MapStructConfig.class)`; only add `@Mapping` for ignored targets or when source/target names differ
- **DTOs**: Records preferred for immutable DTOs when no complex mapping needed
  
