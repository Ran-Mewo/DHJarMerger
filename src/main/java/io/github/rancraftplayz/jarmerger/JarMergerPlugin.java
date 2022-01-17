package io.github.rancraftplayz.jarmerger;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JarMergerPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create("mergeJars", MergeJarsTask.class);
    }
}
