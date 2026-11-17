# spendwise-shared

Parent POM (BOM) and shared libraries for the SpendWise microservices.

| Artifact | What it is |
|---|---|
| `com.spendwise:spendwise-parent` | Parent POM: Java 21, Spring Boot / Spring Cloud / gRPC / Avro / Confluent versions, plugin management |
| `com.spendwise:spendwise-common` | Cross-cutting auto-configuration: correlation IDs, tracing, security (resource server, user context), Kafka error handling, idempotency guard, exception handling, PII masking |
| `com.spendwise:spendwise-events` | Avro contracts of every Kafka event and the classes generated from them |

All three are released together under one version and published to GitHub Packages.

## Using it in a service repo

```xml
<parent>
    <groupId>com.spendwise</groupId>
    <artifactId>spendwise-parent</artifactId>
    <version>1.0.0</version>
    <relativePath/>
</parent>
```

Reading from GitHub Packages needs a token, even for public packages: a classic PAT with
`read:packages`, in a `<server>` with id `github` in `~/.m2/settings.xml`.

## Releasing

1. Set the new version: `./mvnw versions:set -DnewVersion=1.1.0 -DgenerateBackupPoms=false`
   and update `spendwise-common.version` / `spendwise-events.version` in `pom.xml` to match.
2. Commit, then tag and push: `git tag -a v1.1.0 -m "spendwise-shared 1.1.0" && git push origin v1.1.0`
3. The `release` workflow checks the tag equals the POM version and runs `mvn deploy`.

Released versions are immutable. Versioning is SemVer: a breaking change in `spendwise-common`
or an incompatible Avro schema change is a new major version.
