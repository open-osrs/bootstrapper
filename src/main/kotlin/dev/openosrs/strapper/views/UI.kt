package dev.openosrs.strapper.views

import dev.openosrs.strapper.controllers.StrapController
import dev.openosrs.strapper.events.NewBootstrapEvent
import dev.openosrs.strapper.models.Bootstrap
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TabPane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import tornadofx.*

class UI : View("OpenOSRS Bootstrapper") {
    private val controller by inject<StrapController>()
    val progressLabel = SimpleStringProperty("Click Update to start")
    override val root = VBox()

    enum class StrapMode(val text: String) {
        TEST("test"),
        STAGING("staging"),
        NIGHTLY("nightly"),
        LIVE("live")
    }

    private val modeOptions = FXCollections.observableArrayList(StrapMode.values().toList())
    private val mode = SimpleObjectProperty<StrapMode>()
            .onChange {
                if (it != null) {
                    StrapController.mode = it.text
                }
            }

    var completion = SimpleDoubleProperty(0.0)


    private val model: Bootstrap.ArtifactModel by inject()
    private val enableValidate = SimpleBooleanProperty()
    private var enableBootstrap = SimpleBooleanProperty()

    private lateinit var tabpane: TabPane


    init {
        progressLabel.value = "Select a mode to get strappin"
        title = "OpenOSRS bootstrapper"
        with(root) {
            setWindowMinSize(width, width)
            setMinSize(800.0, height)
            hbox {
                style {
                    paddingHorizontalProperty.value = 10.0
                }
                button {
                    enableWhen {
                        enableBootstrap
                    }
                    text = "New Bootstrap"
                    setOnAction {
                        val file = with(DirectoryChooser()) {
                            title = "Choose project dir"
                            showDialog(null)
                        }
                        runAsync {
                            controller.strapArtifacts(file)
                        }
                    }
                }

                val validateButton = button {
                    style {
                        spacing = Dimension(40.0, Dimension.LinearUnits.px)

                    }
                    var tooltip = tooltip {
                        text { "Validate that the artifacts were uploaded and hash is the same" }
                    }
                    text = "Validate"
                    enableWhen { enableValidate }
                    setOnAction {
                        runAsync {
                            controller.validate()
                        }
                    }
                }

                val modeCombobox = combobox(values = modeOptions) {
                    paddingHorizontalProperty.value = 100.0
                    setOnAction {
                        enableBootstrap.value = true
                    }
                    bind(mode)
                }



                progressindicator(completion) {


                }
                spacer { }
                text("Select a mode to get strappin") {
                    style { }
                }
            }


            tabpane = tabpane {
                tab("Original Bootstrap") {

                    tableview(controller.artifacts) {

                        column("Name", Bootstrap.Artifact::name).weightedWidth(25)
                        column("Version", Bootstrap.Artifact::version).weightedWidth((25))
                        column("Size", Bootstrap.Artifact::formattedSize).weightedWidth(.1)
                        column("Path", Bootstrap.Artifact::path)
                        column("Hash", Bootstrap.Artifact::hash).weightedWidth(.1)
                        autosize()

                        bindSelected(model)
                    }
                }
                subscribe<NewBootstrapEvent> { event ->
                    val bootstrap = event.bootstrap
                    val artifacts = bootstrap.artifacts
                    tab("New Bootstrap") {
                        enableValidate.value = true
                        tableview(artifacts) {
                            column("Name", Bootstrap.Artifact::name).weightedWidth(25)
                            column("Version", Bootstrap.Artifact::version).weightedWidth((25))
                            column("Size", Bootstrap.Artifact::formattedSize).weightedWidth(.1)
                            column("Path", Bootstrap.Artifact::path)
                            column("Hash", Bootstrap.Artifact::hash).weightedWidth(.1)
                            bindSelected(model)
                            autosize()
                        }
                    }
                }
            }



            autosize()
        }
    }


}

