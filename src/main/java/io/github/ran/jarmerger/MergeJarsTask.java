package io.github.ran.jarmerger;

import fr.stevecohen.jarmanager.JarPacker;
import fr.stevecohen.jarmanager.JarUnpacker;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class MergeJarsTask extends DefaultTask {
    @TaskAction
    void mergeJars() throws IOException {
        File jarMerger = new File(getProject().getProjectDir(), "Merged");
        File fabricTemps = new File(jarMerger, "fabric-temps");
        File forgeTemps = new File(jarMerger, "forge-temps");
        if (fabricTemps.exists()) {
            deleteDirectory(fabricTemps);
            fabricTemps.delete();
        }
        fabricTemps.mkdirs();
        if (forgeTemps.exists()) {
            deleteDirectory(forgeTemps);
            forgeTemps.delete();
        }
        forgeTemps.mkdirs();

        String jar = getProject().getName() + "-" + getProject().getVersion();

        File mergedJar = new File(jarMerger, jar);
        if (mergedJar.exists()) {
            deleteDirectory(mergedJar);
            mergedJar.delete();
        }

        if (new File(jarMerger, jar + ".jar").exists()) {
            new File(jarMerger, jar + ".jar").delete();
        }

        File forgeJar = getProject().getProjectDir().toPath().resolve("forge/build/libs/" + jar + ".jar").toFile();
        File fabricJar = getProject().getProjectDir().toPath().resolve("fabric/build/libs/" + jar + ".jar").toFile();

        JarUnpacker jarUnpacker = new JarUnpacker();
        jarUnpacker.unpack(forgeJar.getAbsolutePath(), forgeTemps.getAbsolutePath());
        jarUnpacker.unpack(fabricJar.getAbsolutePath(), fabricTemps.getAbsolutePath());

        File manifest = new File(fabricTemps, "META-INF/MANIFEST.MF");
        // Read the manifest and add "MixinConfigs: lod.mixins.json\n" at the second line
        String[] lines = FileUtils.readFileToString(manifest, StandardCharsets.UTF_8).split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i == 1) {
                sb.append("MixinConfigs: lod.mixins.json\n");
            }
            sb.append(lines[i]).append("\n");
        }
        FileUtils.writeStringToFile(manifest, sb.toString(), StandardCharsets.UTF_8);

        JarPacker packer = new JarPacker();

        FileUtils.copyDirectory(fabricTemps, forgeTemps);

        forgeTemps.renameTo(mergedJar);

//        compress(mergedJar.getAbsolutePath());
        packer.pack(mergedJar.getAbsolutePath(), new File(jarMerger, jar + ".jar").getAbsolutePath());


        Set<PosixFilePermission> perms = new HashSet<>(); // Create a list of the perms
        perms.add(PosixFilePermission.GROUP_EXECUTE);
//        perms.add(PosixFilePermission.);
        Files.setPosixFilePermissions(new File(jarMerger, jar + ".jar").toPath(), perms); // Apply the perms onto the jar


        deleteDirectory(fabricTemps);
        deleteDirectory(mergedJar);
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
