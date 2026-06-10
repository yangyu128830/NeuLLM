package com.neusoft.edu.neullmdev.service.classroom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ArtifactStorageService {

    private final Path baseDir;

    public ArtifactStorageService(@Value("${app.storage.artifact-dir:data/artifacts}") String artifactDir) {
        this.baseDir = Path.of(artifactDir).toAbsolutePath().normalize();
    }

    public String store(String submissionId, String fileName, byte[] bytes) throws IOException {
        Path dir = baseDir.resolve(submissionId);
        Files.createDirectories(dir);
        String safeName = fileName == null || fileName.isBlank() ? "artifact.bin"
                : fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        Path target = dir.resolve(safeName);
        Files.write(target, bytes);
        return target.toString();
    }

    public void deleteSubmissionArtifacts(String submissionId) {
        Path dir = baseDir.resolve(submissionId);
        try {
            if (Files.isDirectory(dir)) {
                try (var stream = Files.list(dir)) {
                    stream.forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
                }
                Files.deleteIfExists(dir);
            }
        } catch (IOException ignored) {
        }
    }
}
