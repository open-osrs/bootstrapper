package dev.openosrs.strapper.uploaders

import mu.KotlinLogging
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.time.Instant

private val log = KotlinLogging.logger("FTPUploader")

class FTPUploader(private val user: String, private val password: String) {
    private val ftp = FTPClient()
    private val timeStamp = Instant.now().toEpochMilli()

    fun connect(): Boolean {
        ftp.connect("ftp.tuxfamily.org")
        ftp.login(user, password)
        ftp.bufferSize = 1024000
        ftp.sendDataSocketBufferSize = 1024000
        return ftp.changeWorkingDirectory("rlplus/rlplus-repository/test")
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
}