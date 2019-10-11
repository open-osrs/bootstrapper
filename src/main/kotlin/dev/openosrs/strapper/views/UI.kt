package dev.openosrs.strapper.views

import dev.openosrs.strapper.controllers.StrapController
import dev.openosrs.strapper.events.ProgressLabelUpdateEvent
import dev.openosrs.strapper.events.NewBootstrapEvent
import dev.openosrs.strapper.models.Bootstrap
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TabPane
import javafx.scene.effect.BlurType
import javafx.scene.effect.Effect
import javafx.scene.effect.Glow
import javafx.scene.layout.VBox
import javafx.scene.text.FontWeight
import javafx.stage.DirectoryChooser
import tornadofx.*

class UI : View("OpenOSRS Bootstrapper") {
    private val controller by inject<StrapController>()
    override val root = VBox()

    enum class StrapMode(val text: String) {
        TEST("test"),
        STAGING("staging"),
        NIGHTLY("nightly"),
        STABLE("stable")
    }

    private val modeOptions = FXCollections.observableArrayList(StrapMode.values().toList())
    private val mode = SimpleObjectProperty<StrapMode>()
            .onChange {
                if (it != null) {
                    StrapController.mode = it.text
                }
            }

    var completion = SimpleDoubleProperty(0.0)
    var progressLabel = SimpleStringProperty("Click Update to start")


    private val model: Bootstrap.ArtifactModel by inject()
    private val enableValidate = SimpleBooleanProperty()
    private var enableBootstrap = SimpleBooleanProperty()
    private var enableSaveButton = SimpleBooleanProperty()

    private lateinit var tabPane: TabPane


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
                    tooltip("Start a new bootstrap")
                    enableWhen {
                        enableBootstrap
                    }
                    text = "New Bootstrap"
                    setOnAction {
                        val file = with(DirectoryChooser()) {
                            title = "Choose project dir"
                            showDialog(null)
                        }
                        runAsyncWithProgress {
                            controller.strapArtifacts(file)
                            controller.addStaticDependencies()
                            controller.buildBootstrap(file)
                            controller.addBuildArtifacts(file)
                            controller.removeDuplicateDependencies()
                            controller.useNewestVersions()
                            controller.completeStrapping()
                        }
                    }
                }

                button {
                    style {
                        spacing = Dimension(40.0, Dimension.LinearUnits.px)
                    }
                    tooltip("Validate that the artifacts were uploaded and hash is the same")
                    text = "Validate"
                    enableWhen { enableValidate }
                    setOnAction {
                        runAsyncWithProgress {
                            controller.validate()
                        }.finally {
                            enableSaveButton.value = true
                        }
                    }
                }

                combobox(values = modeOptions) {
                    tooltip("Select a bootstrap mode")
                    paddingHorizontalProperty.value = 100.0
                    setOnAction {
                        enableBootstrap.value = true
                    }
                    bind(mode)
                }

                spacer { }

                button {
                    tooltip("Save the bootstrap")
                    text = "Export"
                    style {
                        fontSize = Dimension(28.0, Dimension.LinearUnits.px)
                    }
                    enableWhen {
                        enableSaveButton
                    }
                }
            }
            label("Select a mode to get strappin") {
                style {
                    fontWeight = FontWeight.BOLD
                    fontSize = Dimension(28.0, Dimension.LinearUnits.px)
                }
                subscribe<ProgressLabelUpdateEvent> { event ->
                    text = event.processedDependencies
                }
                bind(progressLabel)
            }




                tabPane = tabpane {
                    tab("Original Bootstrap") {

                        tableview(controller.artifacts) {

                            column("Name", Bootstrap.Artifact::name).weightedWidth(25)
                            column("Version", Bootstrap.Artifact::version).weightedWidth((25))
                            readonlyColumn("Size", Bootstrap.Artifact::formattedSize).weightedWidth(.1)
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
                            this.isClosable = false
                            requestFocus()
                            enableValidate.value = true
                            tableview(artifacts) {
                                column("Name", Bootstrap.Artifact::name).weightedWidth(25)
                                column("Version", Bootstrap.Artifact::version).weightedWidth((25))
                                readonlyColumn("Size", Bootstrap.Artifact::formattedSize).weightedWidth(.1)
                                column("Path", Bootstrap.Artifact::path)
                                column("Hash", Bootstrap.Artifact::hash).weightedWidth(.1)
                                bindSelected(model)
                                autosize()

                                contextmenu {
                                    item("Delete").action {
                                        selectedItem?.apply {
                                            controller.newBootstrap.artifacts.remove(this)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }



                autosize()
            }
        }


    }


