package dev.openosrs.strapper.controllers


import com.google.gson.Gson
import dev.openosrs.strapper.Bootstrap
import dev.openosrs.strapper.BootstrapLoader
import dev.openosrs.strapper.events.NewBootstrapEvent
import dev.openosrs.strapper.views.UI
import javafx.scene.control.Tab
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import tornadofx.*
import tornadofx.Stylesheet.Companion.tab
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.swing.JOptionPane
import kotlin.collections.HashMap
import kotlin.streams.toList


class StrapController() : Controller() {
    companion object {
        var mode = ""
        var user = ""
        var p = ""
    }

    private val logger = KotlinLogging.logger("StrapLogger")

    private val uiView: UI by inject()


    lateinit var rlVersion: String
    lateinit var projectVersion: String
    private val bootstrapLoader = BootstrapLoader()

    val bootstrap = bootstrapLoader.loadBootStrap()
    private val newBootstrap = bootstrapLoader.loadBootStrap()
    var artifacts = newBootstrap.artifacts
    private val libsDir = "runelite-client/build/lib"
    private val artifactsList = listOf(
            "runescape-api/build/libs", "runelite-client/build/libs", "injected-client/build/libs", "http-api/build/libs",
            "runelite-api/build/libs", "runescape-api/build/libs"
    )


    fun buildBootstrap(dir: File) {
        newBootstrap.artifacts.size
        var client = File(File(dir, "runelite-client/build/libs"), "client-$projectVersion-${newBootstrap.client.extension}")
        var libs = Files.list((File(dir, "runelite-client/build/lib").toPath()))
        for (f in libs.toList()) {
            uiView.completion.plus((1 / newBootstrap.artifacts.size) * 100)
            uiView.progressLabel.value = "Processing dependencies"
            val file = f.toFile()
            newBootstrap.artifacts.find { artifact -> artifact.name.startsWith(file.name.split("-")[0]) }.let { artifact ->
                if (!(file.name.contains(artifact!!.version))) {
                    logger.info { artifact.toString() }
                }

                artifact.apply {
                    val oldVer = artifact.version
                    artifact.rename(file.name)
                    logger.info { "Dependency ${artifact.name} updated: $oldVer -> ${artifact.version}" }
                    val size = file.length().toString()
                    artifact.sizeProperty.value = size
                    var path = "https://github.com/runelite-extended/maven-repo/raw/master/$mode/${file.name}"
                    val hash = DigestUtils.sha256Hex(file.readBytes())
                    artifact.hashProperty.value = hash
                    newBootstrap.validationQueue.add(artifact)
                }
            }
        }
    }


    fun strapArtifacts(dir: File) {
        uiView.completion.value = .05
        logger.info { "attempting to strap artifacts from directory ${dir.name}" }
        val f = File(dir, "runelite-client/build/resources/main/runelite.plus.properties")

        try {
            val fr: FileRepository
            try {
                 fr = FileRepositoryBuilder().setMustExist(true).setGitDir(File(dir, "\\.git"))
                        .setMustExist(true).build()
            } catch(e: java.lang.Exception) {
                showErrorMessage(e)
                return
            }

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
            projectVersion = properties.getProperty("runelite.plus.version")
            logger.info { "proceeding with runelite version $rlVersion and openOSRS version $projectVersion" }
            val oldArtifacts = newBootstrap.artifacts.filter { !it.name.contains("SNAPSHOT") }
            uiView.completion.value = 0.1
            /** val uploader = when (mode) {
            "nighlty" -> FTPUploader(user, p)
            else -> FTPUploader(user, p)

            //FTPUploader(JOptionPane.showInputDialog("Enter FTP login username"),
            //JOptionPane.showInputDialog("Enter FTP password"))
            }**/
            newBootstrap.client.extension = "jar"

            val artifactFiles = HashMap<String, File>()
            if (projectVersion.isNotEmpty()) {
                logger.info { "Strapping main build artifacts" }
                for (s in artifactsList) {
                    uiView.completion.value += 0.1
                    if (s.contains("runelite-client")) {
                        val fName = "${s.replace("runelite-", "")
                                .split("/")[0]}-$rlVersion.${bootstrap.client.extension}"
                        try {
                            artifactFiles[fName] = File(File(dir, s), fName)
                        } catch (e: FileNotFoundException) {
                            showErrorMessage(e)
                        }
                        // var a = artifacts.filter { it.name == fName }.first()
                        var name = fName
                        val file = artifactFiles[fName]!!
                        var size = file.length().toString()
                        var path = "https://github.com/runelite-extended/maven-repo/raw/master/$mode/${file.name}"
                        var hash = DigestUtils.sha256Hex(file.readBytes())
                        logger.info { "name: $name \n size: $size \n path: $path \n $hash: $hash \n" }

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
                        val size = file.length().toString()
                        val path = "https://github.com/runelite-extended/maven-repo/raw/master/$mode/${file.name}"
                        val hash = DigestUtils.sha256Hex(file.readBytes())
                        logger.info { "name: $name \n size: $size \n path: $path \n $hash: $hash \n" }

                    }
                    logger.info { "found artifact $s" }
                }
                logger.info { "Searching dependencies. . ." }
                buildBootstrap(dir)
                logger.info { "building bootstrapper file. . ." }
                newBootstrap.projectVersionProperty.value = projectVersion
                newBootstrap.buildCommitProperty.value = head.toString()
                fire(NewBootstrapEvent(bootstrap))

                // bootstrap.client = Bootstrap.Client("client", "", "jar",
                //           "net.runelite", "", rlVersion)
                val file = java.nio.file.Files.writeString(File("bootstrap-nightly.json").toPath(),
                        Gson()
                                .newBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(newBootstrap))
                if (mode == "nightly") {
                    // uploader.uploadStrap(file.toFile())
                }
                if (mode != "nightly") {
                    JOptionPane.showMessageDialog(null,
                            "Bootstrapping is complete. Don't forget to PR the bootstrap file to the maven-repo")
                }
            }
        } catch (e: RepositoryNotFoundException) {
            e.printStackTrace()
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
        while (newBootstrap.validationQueue.isNotEmpty()) {
            validate(newBootstrap.validationQueue.poll())
        }
    }

    private fun validate(a: Bootstrap.Artifact): Boolean {
        val file = File(a.pathProperty.value)
        val size = file.length()
        if (size != a.sizeProperty.value.toLong()) {
            logger.error { "Unable to validate ${a.name}. has size of $size but the boostrap size is ${a.size}" }
        }
        try {
            val stream = file.toURI()
            val readAllBytes = Files.write(Paths.get(a.name), stream.toURL().readBytes())

            if (DigestUtils.sha256Hex(readAllBytes.toFile().readBytes()) != a.hash) {
                logger.error { "Hash missmatch on package ${a.name}" }
                return false
            }
        } catch (e: Exception) {
            showErrorMessage(e)
        }
        return true
    }
}


