package dev.openosrs.strapper.views

import dev.openosrs.strapper.Bootstrap
import dev.openosrs.strapper.controllers.StrapController
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.io.File

class UI : View("My View") {
    private val controller by inject<StrapController>()
    val progressLabel = SimpleStringProperty("Click Update to start")
    override val root = VBox()

    enum class StrapMode(val text: String) {
        TEST("test"),
        STAGING("staging"),
        NIGHTLY("nightly"),
        LIVE("live")
    }

    val modeOptions = FXCollections.observableArrayList(StrapMode.values().toList())
    val mode = SimpleObjectProperty<StrapMode>()
            .onChange {
                if (it != null) {
                    StrapController.mode = it.text
                }
            }

    var completion = SimpleDoubleProperty(0.0)


    val model: Bootstrap.ArtifactModel by inject()

    init {
        with(root) {
            hbox {
                button {
                    text = "Update"
                    setOnAction {
                        var file = with(DirectoryChooser()) {
                            initialDirectory = File("C:/users/jesse/rl")
                            title = "Choose project dir"
                            showDialog(null)
                        }
                        runAsync {
                            controller.strapArtifacts(file)
                        }
                    }
                }

                button {

                    text = "Validate"
                    setOnAction {
                        runAsync {
                            controller.validate()
                        }
                    }
                }

                combobox(values = modeOptions) {
                    bind(mode)
                }

            }

            progressindicator(completion) {

                text(progressLabel) {
                }

            }


            tableview(controller.artifacts) {
                this.enableCellEditing()
                onEditCommit { println(this) }



                column("Name", Bootstrap.Artifact::name)
                column("Version", Bootstrap.Artifact::version)
                column("Path", Bootstrap.Artifact::path)
                column("Size", Bootstrap.Artifact::size)
                column("Hash", Bootstrap.Artifact::hash)
                autosize()

                bindSelected(model)
                autosize()
                resizeColumnsToFitContent { contentColumns }
            }
            autosize()
        }


    }


}

