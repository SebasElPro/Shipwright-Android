package com.dishii.soh;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class ExtractorAssetStateTest {
    private static final String CURRENT = "c7737f39734fc3a7b2888715c825b7137de7a8a1037e28122c0426ae6d78b074";
    private static final String CHANGED = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private static void check(boolean condition) {
        if (!condition) throw new AssertionError();
    }

    private static void write(File root, String name, String value) throws Exception {
        Files.write(new File(root, name).toPath(), value.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        File root = Files.createTempDirectory("soh-setup-test").toFile();
        try {
            check(ExtractorAssetState.needsCopy(root, CURRENT));
            new File(root, "assets").mkdir();
            write(root, "assets/test.yaml", "unchanged");
            check(ExtractorAssetState.needsCopy(root, CURRENT));
            write(root, ".android_support_files_version", "soh-9.3.1-torch-support-1");
            check(ExtractorAssetState.needsCopy(root, CURRENT));
            write(root, ".android_support_files_version", "soh-9.3.1-torch-support-2");
            check(!ExtractorAssetState.needsCopy(root, CURRENT)); // safe legacy adoption
            check(!ExtractorAssetState.isCurrent(root, CURRENT)); // still needs the cheap marker migration
            check(ExtractorAssetState.needsCopy(root, CHANGED)); // never adopt changed YAML
            ExtractorAssetState.markComplete(root, CURRENT);
            check(ExtractorAssetState.isCurrent(root, CURRENT));
            write(root, ".android_support_files_version", "future-shader-update");
            check(!ExtractorAssetState.needsCopy(root, CURRENT)); // shader independent
            check(ExtractorAssetState.needsCopy(root, CHANGED));
            write(root, ExtractorAssetState.MARKER, "broken");
            check(ExtractorAssetState.needsCopy(root, CURRENT));
            new File(root, ExtractorAssetState.MARKER).delete();
            new File(root, ".android_support_files_version").delete();
            check(ExtractorAssetState.needsCopy(root, CURRENT)); // interrupted setup retries
            ExtractorAssetState.markComplete(root, CURRENT);
            new File(root, "assets/test.yaml").delete();
            check(ExtractorAssetState.needsCopy(root, CURRENT)); // missing/empty tree repaired
            check(!ExtractorAssetState.isCurrent(root, CURRENT));
            check(ExtractorAssetState.needsCopy(root, null));
            try {
                ExtractorAssetState.markComplete(root, null);
                throw new AssertionError("Invalid revision accepted");
            } catch (java.io.IOException expected) { }
            System.out.println("Extractor asset version regression checks passed");
        } finally {
            try (java.util.stream.Stream<java.nio.file.Path> paths = Files.walk(root.toPath())) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
            }
        }
    }
}
