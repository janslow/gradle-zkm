package com.jayanslow.gradle.zkm

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import static com.zelix.ZKM.run

/**
 * Task which runs ZKM
 */
class ZKMTask extends DefaultTask {
  public static String OPEN_CLASSPATH_PROPERTY_NAME = 'classpath.open'
  public static String COMPILE_CLASSPATH_PROPERTY_NAME = 'classpath.compile'
  public static String OUTPUT_DIRECTORY_PROPERTY_NAME = 'output.dir'

  private
  static void runZKM(File script, File logFile, File trimLogFile, File defaultExcludeFile, File defaultTrimExclude, File defaultDirectory, boolean verbose, boolean parseOnly, Properties extraProperties) {
    run(script.toString(), logFile?.toString(), trimLogFile?.toString(), defaultExcludeFile?.toString(), defaultTrimExclude?.toString(), defaultDirectory?.toString(), verbose, parseOnly, extraProperties)
  }

  private static File writeClasspathFile(FileCollection compileClasspath) {
    File file = File.createTempFile("compile", ".zkm")
    file.deleteOnExit()
    file << compileClasspath.getFiles().stream()
            .filter({it.exists()})
            .map({"\"${it.toString()}\""})
            .reduce({a, b -> "${a}\n\t${b}"}).get()
  }

  @InputFiles
  FileCollection openClasspath

  @InputFiles
  FileCollection compileClasspath

  @OutputDirectory
  File outputDirectory

  File script
  File defaultExcludeFile
  File defaultTrimExclude
  File defaultDirectory

  boolean verbose
  boolean parseOnly
  Map<String, String> extraProperties

  File getOrCreateLogDir() {
    def dir = project.buildDir.toPath().resolve("zkm").toFile();
    if (dir.directory) {
      return dir
    }
    return project.mkdir(dir)
  }

  @SuppressWarnings("GroovyUnusedDeclaration")
  @TaskAction
  obfuscate() {
    def logDir = getOrCreateLogDir()
    def logFilePath = logDir.toPath().resolve("zkm.log").toFile()
    def trimLogFilePath = logDir.toPath().resolve("trim.log").toFile()
    def extraProperties = getExtraProperties()
    runZKM(script, logFilePath, trimLogFilePath, defaultExcludeFile, defaultTrimExclude, defaultDirectory, verbose, parseOnly, extraProperties)
  }

  private Properties getExtraProperties() {
    def props = new Properties()
    this.extraProperties?.forEach {k, v -> props.setProperty(k, v)}
    props[OPEN_CLASSPATH_PROPERTY_NAME] = writeClasspathFile(this.openClasspath).toString()
    props[COMPILE_CLASSPATH_PROPERTY_NAME] = writeClasspathFile(this.compileClasspath).toString()
    props[OUTPUT_DIRECTORY_PROPERTY_NAME] = outputDirectory.toString()
    return props
  }
}
