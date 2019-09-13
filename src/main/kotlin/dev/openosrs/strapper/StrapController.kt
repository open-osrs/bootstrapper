package dev.openosrs.strapper


import com.google.gson.Gson
import dev.openosrs.strapper.uploaders.FTPUploader
import mu.KotlinLogging
import org.apache.commons.codec.digest.DigestUtils
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import java.nio.file.Files
import java.util.*
import javax.swing.JOptionPane
import kotlin.collections.HashMap


private val log = KotlinLogging.logger("StrapLogger")

class StrapController {
    companion object {
        var mode = "test"
    }

    private val bootstrapLoader = BootstrapLoader()
    private var bootstrap = bootstrapLoader.loadBootStrap()
    private val artifactsList = listOf(
        "runescape-api/build/libs", "runelite-client/build/libs", "injected-client/build/libs", "http-api/build/libs",
        "runelite-api/build/libs", "runescape-api/build/libs"
    )


    fun strapArtifacts(dir: File) {
        val f = File(dir, "runelite-client/build/resources/main/runelite.plus.properties")
        try {
            val fr = FileRepositoryBuilder().setMustExist(true).setGitDir(File(dir, "\\.git"))
                .setMustExist(true).build()
            val head = Repository.shortenRefName(fr.getRef("refs/heads/master").toString())
            JOptionPane.showMessageDialog(null, head)
            val properties = Properties()
            properties.load(f.inputStream())
            val rlVersion = properties.getProperty("runelite.version")
            val projectVersion = properties.getProperty("runelite.plus.version")
            val oldArtifacts = bootstrap.artifacts.filter { !it.name.contains("SNAPSHOT") }
            bootstrap.artifacts = oldArtifacts
            val uploader = FTPUploader(JOptionPane.showInputDialog("Enter FTP login username"), JOptionPane.showInputDialog("Enter FTP password"))
            val artifactFiles = HashMap<String, File>()
            if (uploader.connect()) {
                for (s in artifactsList) {
                    if (s.contains("runelite-client")) {
                        val fName = "${s.replace("runelite-", "").split("/")[0]}-$rlVersion.${bootstrap.client.extension}"
                        artifactFiles[fName] = File(File(dir, s), fName)
                        val newName = uploader.upload(artifactFiles[fName]!!)
                        val file = artifactFiles[fName]!!
                        val size = file.length().toString()
                        val location = "http://download.tuxfamily.org/rlplus/$mode/$newName"
                        val hash = DigestUtils.sha512Hex(file.readBytes())
                        bootstrap.artifacts = bootstrap.artifacts.plus(BootStrap.Artifact(hash, newName, location, size))
                    } else {
                        val fName = "${s.split("/")[0]}-$rlVersion.${bootstrap.client.extension}"
                        artifactFiles[fName] = File(File(dir, s), fName)
                        val newName = uploader.upload(artifactFiles[fName]!!)
                        val file = artifactFiles[fName]!!
                        val size = file.length().toString()
                        val location = "http://download.tuxfamily.org/rlplus/$mode/$newName"
                        val hash = DigestUtils.sha512Hex(file.readBytes())
                        bootstrap.artifacts = bootstrap.artifacts.plus(BootStrap.Artifact(hash, newName, location, size))
                    }
                }
            }
            bootstrap.projectVersion = projectVersion
            bootstrap.buildCommit = head.toString()
            bootstrap.client = BootStrap.Client("client", "", "jar",
                "net.runelite", "", rlVersion)
            Files.writeString(File("bootstrap.json").toPath(), Gson().newBuilder().setPrettyPrinting().create().toJson(bootstrap))
        } catch (e: RepositoryNotFoundException) {
            e.printStackTrace()
        }

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
}


