package dev.openosrs.strapper.controllers


import dev.openosrs.strapper.events.ProgressLabelUpdateEvent
import dev.openosrs.strapper.events.NewBootstrapEvent
import dev.openosrs.strapper.models.Bootstrap
import dev.openosrs.strapper.util.BootstrapLoader
import dev.openosrs.strapper.util.DependencyParser
import dev.openosrs.strapper.views.UI
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.ArrayUtils
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import tornadofx.*
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.swing.JOptionPane
import kotlin.collections.HashMap


class StrapController() : Controller() {
    companion object {
        var mode = ""
        var user = ""
        var p = ""
    }

    private val logger = KotlinLogging.logger("StrapLogger")


    private lateinit var rlVersion: String
    private lateinit var projectVersion: String
    private val bootstrapLoader = BootstrapLoader()

    private val bootstrap = bootstrapLoader.loadBootStrap()
    var newBootstrap = Bootstrap()
    var artifacts = bootstrap.artifacts
    private val artifactsList = listOf(
            "runescape-api/build/libs", "runelite-client/build/libs", "injected-client/build/libs", "http-api/build/libs",
            "runelite-api/build/libs"
    )

    private fun processDependencyURL(url: String, a: Bootstrap.Artifact) {

        FileUtils.copyURLToFile(
                URL(url),
                File(a.name),
                10000,
                10000)
        with(File(a.name)) {
            a.size = this.length().toString()
            a.hash = DigestUtils.sha256Hex(this.readBytes())
            this.delete()
        }
    }

    fun removeDuplicateDependencies() {
        val origSize = newBootstrap.artifacts.size
        val a = newBootstrap.artifacts.toSet().toList().observable()
        newBootstrap.artifacts.clear()
        newBootstrap.artifacts.addAll(a)
        fire(ProgressLabelUpdateEvent("Removed ${origSize - newBootstrap.artifacts.size} duplicate " +
                "dependencies"))
    }

    fun addStaticDependencies() {
        //bandage for static artifacts
            newBootstrap.artifacts.addAll(bootstrap.artifacts.filter { artifact ->
                artifact.path.contains("nexus.thatgamerblue.com")
                        || artifact.path.contains("natives")
            }.toList())

    }

    fun useNewestVersions() {
        for (a in newBootstrap.artifacts) {
            newBootstrap.artifacts.filtered { Bootstrap.Artifact::name.equals(a.name) }.forEach { artifact ->
                        newBootstrap.artifacts.remove(a.olderVersion(artifact))
                    }
        }
    }


    fun buildBootstrap(dir: File) {
            newBootstrap.launcherJvm11Arguments = bootstrap.launcherJvm11Arguments
        File(
            File(dir, "runelite-client/build/libs"),
            "client-$projectVersion-${newBootstrap.client.extension}"
        )

            newBootstrap.artifacts.addAll(DependencyParser(dir).artifacts)
            newBootstrap.artifacts.forEach {
                fire(ProgressLabelUpdateEvent(
                        "Processing dependency " +
                                "${newBootstrap.artifacts.indexOf(it)}/${newBootstrap.artifacts.size}"))

                processDependencyURL(it.path, it)
                Bootstrap.validationQueue.add(it)
            }
    }


    fun strapArtifacts(dir: File) {
            if (Files.exists(File("out").toPath())) {
                FileUtils.cleanDirectory(File("out"))
                FileUtils.deleteDirectory(File("out"))
            }
            logger.info {   "attempting to strap artifacts from directory ${dir.name}"}
            val f = File(dir, "runelite-client/build/resources/main/open.osrs.properties")
            val fr: FileRepository = FileRepositoryBuilder().setMustExist(true).setGitDir(File(dir, "\\.git"))
                .setMustExist(true).build()


        val head = with(RevWalk(fr))
            {
                this.parseCommit(fr.getRef("refs/heads/master").leaf.objectId).name
            }
            logger.info { "proceeding with git commit $head" }
            val properties = Properties()
            try {
                properties.load(f.inputStream())
            } catch (e: Exception) {
                showErrorMessage(e)
            }


            logger.info { "attempting to load properties resource. . ." }
            rlVersion = properties.getProperty("runelite.version")
            projectVersion = properties.getProperty("open.osrs.version")
            val minimumLauncherVersion = properties.getProperty("launcher.version")
            logger.info { "proceeding with runelite version $rlVersion and openOSRS version $projectVersion" }
            val oldArtifacts = newBootstrap.artifacts.filter { !it.name.contains("SNAPSHOT") }
            //uiView.completion.value = 0.1
            /** val uploader = when (mode) {
            "nighlty" -> FTPUploader(user, p)
            else -> FTPUploader(user, p)

            //FTPUploader(JOptionPane.showInputDialog("Enter FTP login username"),
            //JOptionPane.showInputDialog("Enter FTP password"))
            }**/
            newBootstrap.client = bootstrap.client
            newBootstrap.clientJvm9Arguments = bootstrap.clientJvm9Arguments
            newBootstrap.clientJvmArguments = bootstrap.clientJvmArguments
            newBootstrap.launcherArguments = bootstrap.launcherArguments
            newBootstrap.launcherJvm11Arguments = bootstrap.launcherJvm11Arguments
            newBootstrap.minimumLauncherVersion = minimumLauncherVersion
            newBootstrap.client.version = rlVersion
            newBootstrap.client.extension = "jar"

            newBootstrap.artifacts.clear()

            if (projectVersion.isNotEmpty()) {
                newBootstrap.projectVersionProperty.value = projectVersion
                newBootstrap.buildCommitProperty.value = head.toString()

            }
    }

    fun completeStrapping() {
            fire(NewBootstrapEvent(newBootstrap))
            log.info(newBootstrap.toJSON(JsonBuilder()).toString())
            newBootstrap.save(Path.of("out/bootstrap-$mode.json"))
            if (mode == "nightly") {
                // uploader.uploadStrap(file.toFile())
            }
            if (mode != "nightly") {
                JOptionPane.showMessageDialog(
                    null,
                    "Bootstrapping is complete. Don't forget to PR the bootstrap file to the maven-repo"
                )
            }
    }

    fun addBuildArtifacts(dir: File) {
            Files.createDirectory(Paths.get("out"))
            val artifactFiles = HashMap<String, File>()
            for (s in artifactsList) {
                val artifactRepo = "https://github.com/runelite-extended/hosting/raw/master/"
                if (s.contains("runelite-client")) {
                    val fName = "${s.replace("runelite-", "")
                        .split("/")[0]}-$rlVersion.${bootstrap.client.extension}"
                    try {
                        artifactFiles[fName] = File(File(dir, s), fName)
                    } catch (e: FileNotFoundException) {
                        showErrorMessage(e)
                    }
                    // var a = artifacts.filter { it.name == fName }.first()
                    val name = fName
                    val file = artifactFiles[fName]!!
                    Files.copy(file.toPath(), File("out", file.name).toPath())
                    var size = file.length().toString()
                    var path = "$artifactRepo$mode/${file.name}"
                    var hash = DigestUtils.sha256Hex(file.readBytes())
                    logger.info { "name: $name \n size: $size \n path: $path \n $hash: $hash \n" }
                    with(Bootstrap.Artifact()) {
                        this.name = name
                        this.size = size
                        this.path = path
                        this.hash = hash
                        this.rename(this.name)
                        newBootstrap.artifacts.add(this)
                    }

                } else {
                    val fName = "${s.split("/")[0]}-$rlVersion.${bootstrap.client.extension}"
                    logger.info { "Searching for $fName" }
                    try {
                        artifactFiles[fName] = File(File(dir, s), fName)
                    } catch (e: FileNotFoundException) {
                        showErrorMessage(e)
                        break
                    }                        // var a = artifacts.find {n -> n.name == fName }!!
                    val name = fName
                    val file = artifactFiles[fName]!!
                    Files.copy(file.toPath(), File("out", file.name).toPath())
                    val size = file.length().toString()
                    val path = "$artifactRepo$mode/${file.name}"
                    val hash = DigestUtils.sha256Hex(file.readBytes())
                    logger.info { "name: $name \n size: $size \n path: $path \n $hash: $hash \n" }
                    with(Bootstrap.Artifact()) {
                        this.name = name
                        this.size = size
                        this.path = path
                        this.hash = hash
                        this.rename(this.name)

                        newBootstrap.artifacts.add(this)
                    }
                }
                logger.info { "found artifact $s" }
            }

    }

    private fun showErrorMessage(e: Exception) {
        JOptionPane.showMessageDialog(null, "File not found: ${e.message}")
        e.printStackTrace()
    }

    fun validProjectDir(dir: File): Boolean {
        return try {
            val fr = FileRepositoryBuilder().setMustExist(true).setGitDir(File(dir, "\\.git"))
                    .setMustExist(true).build()
            val head = fr.getRef("refs/heads/master")
            true
        } catch (e: RepositoryNotFoundException) {
            false
        }
    }

    fun validate() {
        while (Bootstrap.validationQueue.isNotEmpty()) {
            fire(ProgressLabelUpdateEvent("Validating hashes: ${Bootstrap.validationQueue.size} remaining"))
            validate(Bootstrap.validationQueue.poll())
        }
    }

    private fun validate(a: Bootstrap.Artifact): Boolean {
        if (a.path.contains("github.com")) {
            return true
        }
        try {
            val file: File = if (a.name.length < 3) {
                createTempFile(a.name + "-nameWasTooShort")
            } else {
                createTempFile(a.name)
            }
            FileUtils.copyURLToFile(URL(a.path), file)
            with (file) {
                if (file.length() != a.sizeProperty.value.toLong()) {
                    logger.error { "Unable to validate ${a.name}. has size of ${file.length()} but the boostrap size is ${a.size}" }
                }
                if (DigestUtils.sha256Hex(this.readBytes()) != a.hash) {
                    logger.error { "Hash mismatch on package ${a.name}" }
                    return false
                }
            }

        } catch (e: Exception) {
            showErrorMessage(e)
        }
        return true
    }
}


