package dev.openosrs.strapper

import dev.openosrs.strapper.controllers.StrapController
import tornadofx.launch

fun main(args: Array<String>) {
    if (args.isNotEmpty())
    {
        StrapController.mode = "nightly"
        StrapController.user = args[0]
        StrapController.p = args[1]
        BootstrapLoader().loadBootStrap()
        val controller = StrapController()

        return
    } else {

        launch<GUIApp>()

        /**
    when (StrapController.mode) {
        "nightly" -> controller.strapArtifacts(File("./"))
        else -> {
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
    } **/
    }


}
