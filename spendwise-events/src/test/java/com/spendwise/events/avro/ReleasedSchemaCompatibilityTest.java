package com.spendwise.events.avro;

import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 6 (polyrepo) — the build-time gate in front of the Schema Registry.
 * <p>
 * Producers and consumers of an event now live in different repositories and upgrade
 * spendwise-events at different times, so every schema in this build must stay
 * {@code FULL_TRANSITIVE} compatible — the registry's own setting — with every version
 * already released: readable by consumers still on an old release (forward), and able
 * to read events old producers wrote or that sit in a topic for replay (backward).
 * <p>
 * Each release's schemas are kept under {@code src/test/resources/released/<version>/}.
 * After publishing a release, copy {@code src/main/avro/*.avsc} into a new folder for it.
 * A contract that disappears from {@code src/main/avro} fails here too: deleting an event
 * type breaks every consumer still reading it.
 */
class ReleasedSchemaCompatibilityTest {

    private static final Path RELEASED = Path.of("src/test/resources/released");

    private static final Map<String, Schema> CURRENT = Map.of(
            "TransactionCreatedEvent.avsc", TransactionCreatedEvent.getClassSchema(),
            "TransactionReversedEvent.avsc", TransactionReversedEvent.getClassSchema(),
            "TransactionRejectedEvent.avsc", TransactionRejectedEvent.getClassSchema());

    @Test
    void atLeastOneReleaseIsRecorded() throws IOException {
        assertThat(releasedVersions()).isNotEmpty();
    }

    @TestFactory
    Stream<DynamicTest> everyContractIsFullyCompatibleWithEveryReleasedVersion() throws IOException {
        List<DynamicTest> tests = new ArrayList<>();
        for (Path version : releasedVersions()) {
            try (Stream<Path> files = Files.list(version)) {
                for (Path file : files.filter(f -> f.toString().endsWith(".avsc")).sorted().toList()) {
                    String name = file.getFileName().toString();
                    tests.add(DynamicTest.dynamicTest(version.getFileName() + "/" + name, () -> {
                        Schema current = CURRENT.get(name);
                        assertThat(current)
                                .as("%s was released in %s but is no longer in src/main/avro", name, version.getFileName())
                                .isNotNull();
                        Schema released = new Schema.Parser().parse(file.toFile());
                        assertThat(compatibility(current, released))
                                .as("backward: the current %s cannot read events written with release %s", name, version.getFileName())
                                .isEqualTo(SchemaCompatibilityType.COMPATIBLE);
                        assertThat(compatibility(released, current))
                                .as("forward: consumers on release %s cannot read %s written with the current schema", version.getFileName(), name)
                                .isEqualTo(SchemaCompatibilityType.COMPATIBLE);
                    }));
                }
            }
        }
        return tests.stream();
    }

    private static List<Path> releasedVersions() throws IOException {
        try (Stream<Path> dirs = Files.list(RELEASED)) {
            return dirs.filter(Files::isDirectory).sorted().toList();
        }
    }

    private static SchemaCompatibilityType compatibility(Schema reader, Schema writer) {
        return SchemaCompatibility.checkReaderWriterCompatibility(reader, writer).getType();
    }
}
