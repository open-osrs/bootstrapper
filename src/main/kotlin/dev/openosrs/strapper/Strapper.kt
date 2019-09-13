package dev.openosrs.strapper

import dev.openosrs.strapper.exceptions.VersionException
import javax.swing.JFileChooser

fun main() {
    BootstrapLoader().loadBootStrap()
    val controller = StrapController()
    val f = JFileChooser()
    f.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    f.dialogTitle = "Select the root directory of OpenOSRS"
    if (f.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        controller.strapArtifacts(f.selectedFile)
    }
        else
            throw VersionException(Exception("Couldn't find git repo at selected directory"))

}
