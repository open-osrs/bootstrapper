package dev.openosrs.strapper.uploaders

import com.google.common.io.Files
import dev.openosrs.strapper.StrapController
import mu.KotlinLogging
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.time.Instant
import kotlin.system.exitProcess

private val log = KotlinLogging.logger("FTPUploader")

class FTPUploader(private val user: String, private val password: String) {
    private val ftp = FTPClient()
    private val timeStamp = Instant.now().toEpochMilli()

    fun connect(): Boolean {
        ftp.connect("ftp.tuxfamily.org")
        ftp.login(user, password)
        ftp.bufferSize = 1024000
        ftp.sendDataSocketBufferSize = 1024000
        return ftp.changeWorkingDirectory("rlplus/rlplus-repository/${StrapController.mode}")
    }

     fun upload(file: File): String {
         ftp.reinitialize()
         log.info("Uploading ${file.absoluteFile}. . .")
         val inputStream = file.inputStream()
         val externalFileName = "${file.nameWithoutExtension}-$timeStamp.${file.extension}"
         var r = ftp.storeFile(externalFileName, inputStream)
         inputStream.close()
         log.info("Uploaded $externalFileName")
         return externalFileName
    }

    fun uploadStrap(file: File): Boolean {
        ftp.reinitialize()
        ftp.changeWorkingDirectory("rlplus/rlplus-repository")
        when (ftp.retrieveFile("bootstrap-nightly.json",
            File("bootstrap-nightly.json.bak").outputStream())) {
            false -> {
                log.error { "couldn't backup remote nightly json. it may not exist" }
                exitProcess(420)
            }
            true -> {
                log.info { "backed up nightly strap" }
                return when (ftp.deleteFile("bootstrap-nightly.json")) {
                    true -> {
                        log.info { "deleted remote nightly" }
                        deliverStrap(file)
                    }
                    false -> {
                        log.info { "couldn't delete old strap." }
                        deliverStrap(file)
                    }
                }
            }
        }
    }

    private fun deliverStrap(file: File): Boolean {
        log.info("Uploading ${file.absoluteFile}. . .")
        var r = ftp.storeFile(file.name, file.inputStream())
        file.inputStream().close()
        log.info("Uploaded new strap")
        return r
    }
}