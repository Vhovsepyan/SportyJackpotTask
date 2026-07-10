# Jackpot Service

A Spring Boot backend that processes bets for **jackpot pool contributions** and
evaluates them for **jackpot rewards**.

Every bet contributes a slice of its stake to a jackpot pool. A bet can then be
evaluated for a win: if it wins, the whole pool is awarded and reset to its
initial value.

The service ships with two interchangeable messaging modes:

- **`mock` (default)** — no infrastructure at all. Bets are processed in-process
  through the exact same code path the Kafka consumer uses. Runs with just a JDK.
- **`kafka`** — publishes bets to a real broker and consumes them back.

---

## Quick start (mock mode — zero dependencies)

Requires **Java 25** only. The Gradle wrapper is committed, so no local Gradle
install is needed.

```bash
./gradlew bootRun
```

The app starts on **http://localhost:8080** and seeds two jackpots on startup:

| Jackpot ID         | Contribution        | Reward                                   |
|--------------------|---------------------|------------------------------------------|
| `jackpot-fixed`    | flat 5% of stake    | flat 10% win chance                      |
| `jackpot-variable` | 10% → 1% as pool grows | 1% → 100% as pool grows to 10,000     |

### Try it with curl

Place a bet (returns **202 Accepted**; the contribution is applied immediately in
mock mode):

```bash
curl -i -X POST http://localhost:8080/api/bets \
  -H "Content-Type: application/json" \
  -d '{"betId":"bet-1","userId":"user-1","jackpotId":"jackpot-fixed","amount":100.00}'
```

Evaluate that bet for a reward (returns **200 OK** with a win/lose result):

```bash
curl -i -X POST http://localhost:8080/api/bets/bet-1/evaluate
```

Example win response:

```json
{ "betId": "bet-1", "won": true, "rewardAmount": 1005.0000 }
```

Example lose response:

```json
{ "betId": "bet-1", "won": false }
```

The `jackpot-variable` pool has a low starting win chance, so it is handy for
watching the pool grow across several bets before it pays out.

### Handy URLs

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 console: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:jackpotdb`, user `sa`, empty password)

---

## Endpoints

| Method & path                     | Purpose                          | Success | Notes                                   |
|-----------------------------------|----------------------------------|---------|-----------------------------------------|
| `POST /api/bets`                  | Place a bet (contribute to pool) | 202     | Body validated; published for processing |
| `POST /api/bets/{betId}/evaluate` | Evaluate a bet for a reward      | 200     | Idempotent; 404 if the bet never contributed |

Error responses are consistent JSON: `400` (validation / malformed body),
`404` (unknown bet), `409` (concurrent update conflict), `500` (unexpected).

---

## Run tests

```bash
./gradlew test
```

Covers contribution & reward strategy maths, the strategy factories, the
contribution and reward services (Mockito), the REST controllers
(`@WebMvcTest`), the JPA repositories (`@DataJpaTest`), and a full end-to-end
flow (`@SpringBootTest`).

---

## Kafka mode (optional)

Requires **Docker**. Start a single-node Kafka (KRaft, no ZooKeeper) and run the
app on the `kafka` profile:

```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=kafka'
```

Now `POST /api/bets` publishes each bet as JSON to the `jackpot-bets` topic; a
`@KafkaListener` (consumer group `jackpot-service`) consumes it and runs the same
`ContributionService`. The reward endpoint behaves identically to mock mode.

Tear down with `docker compose down`.

### Listener configuration note

The broker runs in **combined broker + controller KRaft mode**. In that mode the
controller listener is *not* advertised, so Kafka derives its advertised address
from `listeners` — and binding it to `0.0.0.0` fails startup with:

```
advertised.listeners cannot use the nonroutable meta-address 0.0.0.0. Use a routable IP address.
```

So `docker-compose.yml` binds the broker listener to all interfaces (reachable
from the host) but the controller listener to `localhost`:

```yaml
KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://localhost:9093
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
```

---

## Design notes

### Strategy pattern for contributions and rewards

Both the contribution maths and the reward maths are pluggable behind small
interfaces:

- `ContributionStrategy#calculate(betAmount, jackpot)` — how much of a stake
  goes into the pool.
- `RewardStrategy#winChance(jackpot)` — the probability a bet wins.

Each jackpot stores a `ContributionType` / `RewardType` enum plus the tunable
parameters (percentages, decay rate, floor, pool limit), so a jackpot's
behaviour is **data-driven** rather than hard-coded.

**Adding a new type** is a closed-for-modification, open-for-extension change:

1. Add a value to the relevant enum (e.g. `ContributionType.TIERED`).
2. Add a `@Component` implementing the strategy interface and returning that enum
   from `supportedType()`.

That's it — the `ContributionStrategyFactory` / `RewardStrategyFactory` discover
all strategy beans from the Spring context and index them by their declared
type, so no factory or `switch` statement needs editing.

### Concurrency

Multiple bets can hit the same pool at once. `Jackpot` carries a JPA `@Version`
column, so pool updates use **optimistic locking**: a concurrent, stale write
fails fast with an `OptimisticLockingFailureException`, which the global handler
maps to `409 Conflict`. Money is handled with `BigDecimal` throughout.

### Idempotent reward evaluation

Reward evaluation is safe to retry. A `JackpotReward` row is keyed by `betId`
(unique); if one already exists, evaluation short-circuits and returns the
original result without rolling again or paying out twice. The win path —
persisting the reward and resetting the pool — runs inside a single transaction.

### Same code path for both modes

`MockBetPublisher` and `KafkaBetPublisher` both implement `BetPublisher`; the
controller depends only on the interface. The mock publisher calls
`ContributionService` directly, which is the identical service the Kafka consumer
invokes — so mock mode exercises real processing logic, not a stub.

---

## Tech stack

Java 25 · Spring Boot 3.5.x (Web, Data JPA, Validation, Kafka) · H2 (in-memory) ·
Lombok · springdoc-openapi · Gradle (Kotlin DSL, wrapper committed) ·
JUnit 5 · Mockito · AssertJ.
