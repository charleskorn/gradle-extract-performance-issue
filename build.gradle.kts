import de.undercouch.gradle.tasks.download.Download
import java.nio.file.Files

plugins {
    id("de.undercouch.download") version "5.0.1"
}

val downloadArchive = tasks.register<Download>("downloadArchive") {
    src("https://go.dev/dl/go1.18rc1.darwin-amd64.tar.gz")
    dest(project.layout.buildDirectory.file("download/go1.18rc1.darwin-amd64.tar.gz"))
    overwrite(false)
}

val extractWithCommandLineTool = tasks.register<Exec>("extractWithCommandLineTool") {
    dependsOn(downloadArchive)

    workingDir(project.layout.buildDirectory.dir("extractWithCommandLineTool"))
    commandLine("/usr/bin/tar", "xf", downloadArchive.get().dest.absolutePath, "--strip-components=1")

    doFirst {
        Files.createDirectories(workingDir.toPath())
    }
}

val extractWithSync = tasks.register<Sync>("extractWithSync") {
    from(tarTree(downloadArchive.map{ it.dest })) {
        eachFile {
            relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
        }

        includeEmptyDirs = false
    }

    into(project.layout.buildDirectory.dir("extractWithSync"))
}

val extractWithCustomAction = tasks.register("extractWithDoLast") {
    dependsOn(downloadArchive)

    doLast {
        sync {
            from(tarTree(downloadArchive.map{ it.dest })) {
                eachFile {
                    relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                }

                includeEmptyDirs = false
            }

            into(project.layout.buildDirectory.dir("extractWithDoLast"))
        }
    }
}

tasks.register("runAll") {
    dependsOn(extractWithCommandLineTool)
    dependsOn(extractWithSync)
    dependsOn(extractWithCustomAction)
}
