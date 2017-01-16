package com.jayanslow.gradle.zkm

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.SourceSet

/**
 * Plugin which configures the ZKM task
 */
class ZKMPlugin implements Plugin<Project> {
  @Override
  void apply(final Project project) {
    project.extensions.create('zkm', ZKMPluginExtension)
    ZKMPluginExtension zkm = project.zkm
    project.task('zkm', type: ZKMTask) {
      SourceSet sourceSet = project.sourceSets.main

      dependsOn sourceSet.compileJavaTaskName
      group = BasePlugin.BUILD_GROUP
      description = 'Obfuscate main classes with ZKM.'

      openClasspath = sourceSet.output
      compileClasspath = sourceSet.compileClasspath
      outputDirectory = project.buildDir.toPath().resolve('classes/obfuscated').toFile()

      doFirst {
        script = zkm.script
      }
    }
  }
}
