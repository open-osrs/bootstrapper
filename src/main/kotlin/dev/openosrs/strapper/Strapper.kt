package dev.openosrs.strapper

import dev.openosrs.strapper.exceptions.VersionException
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import kotlin.system.exitProcess

fun main() {
    BootstrapLoader().loadBootStrap()
    val controller = StrapController()
    StrapController.mode = when (JOptionPane.showOptionDialog(
        null,
        "Select Bootstrap mode",
        "OpenOSRS Bootstrapper",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        arrayOf("TEST", "STAGING", "LIVE"),
        null
    )) {
        0 -> "test"
        1 -> "staging"
        2 -> "live"
        else -> exitProcess(-1)
    }

    val f = JFileChooser()
    f.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    f.dialogTitle = "Select the root directory of OpenOSRS"
    if (f.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        controller.strapArtifacts(f.selectedFile)
    }
        else
            throw VersionException(Exception("Couldn't find git repo at selected directory"))

}
