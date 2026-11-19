# spendwise-shared

Parent POM (BOM) and shared libraries for the SpendWise microservices.

| Artifact | What it is |
|---|---|
| `com.spendwise:spendwise-parent` | Parent POM: Java 21, Spring Boot / Spring Cloud / gRPC / Avro / Confluent versions, plugin management |
| `com.spendwise:spendwise-common` | Cross-cutting auto-configuration: correlation IDs, tracing, security (resource server, user context), Kafka error handling, idempotency guard, exception handling, PII masking |
| `com.spendwise:spendwise-events` | Avro contracts of every Kafka event and the classes generated from them |
| `com.spendwise:spendwise-grpc-contracts` | Protobuf contracts of every internal gRPC API and the stubs generated from them (budget-service serves, analytics-service calls) |

All four are released together under one version and published to GitHub Packages.

## Using it in a service repo

```xml
<parent>
    <groupId>com.spendwise</groupId>
    <artifactId>spendwise-parent</artifactId>
    <version>1.1.0</version>
    <relativePath/>
</parent>
```

Reading from GitHub Packages needs a token, even for public packages: a classic PAT with
`read:packages`, in a `<server>` with id `github` in `~/.m2/settings.xml`.

## Contract gates

Producers, consumers, servers and clients of a contract live in different repositories and upgrade at
different times, so CI refuses a change that would break one still on an older release:

| Contract | Gate | Rule |
|---|---|---|
| Avro events (`spendwise-events`) | `ReleasedSchemaCompatibilityTest` in `mvn verify` | Every schema stays `FULL_TRANSITIVE` compatible with every version under `spendwise-events/src/test/resources/released/` (the Schema Registry's own setting) |
| gRPC (`spendwise-grpc-contracts`) | `proto-breaking` job, `buf breaking` | No `FILE`-level breaking change against the last release tag |

## Releasing

1. Set the new version: `./mvnw versions:set -DnewVersion=1.2.0 -DgenerateBackupPoms=false`
   and update `spendwise-common.version`, `spendwise-events.version` and
   `spendwise-grpc-contracts.version` in `pom.xml` to match.
2. Record the schemas being released:
   `mkdir spendwise-events/src/test/resources/released/1.2.0 && cp spendwise-events/src/main/avro/*.avsc spendwise-events/src/test/resources/released/1.2.0/`
3. Commit, then tag and push: `git tag -a v1.2.0 -m "spendwise-shared 1.2.0" && git push origin v1.2.0`
4. The `release` workflow checks the tag equals the POM version and runs `mvn deploy`.

Released versions are immutable. Versioning is SemVer: a breaking change in `spendwise-common`,
or a contract change the gates above refuse, is a new major version.
