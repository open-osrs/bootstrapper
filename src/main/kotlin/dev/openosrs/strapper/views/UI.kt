package dev.openosrs.strapper.views

import dev.openosrs.strapper.controllers.StrapController
import dev.openosrs.strapper.events.NewBootstrapEvent
import dev.openosrs.strapper.events.ProgressLabelUpdateEvent
import dev.openosrs.strapper.models.Bootstrap
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TabPane
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.VBox
import javafx.scene.paint.Paint
import javafx.scene.text.FontWeight
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.awt.Color
import javax.swing.Painter
import kotlin.streams.toList

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
    private var progressLabel = SimpleStringProperty("Click Update to start")


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
                    drawer {
                        style {
                            accentColor = javafx.scene.paint.Color.RED
                        }
                        val artifacts = controller.artifacts
                        val jvmArguments = controller.bootstrap.launcherArguments.asObservable()
                        val jvm11Arguments = controller.bootstrap.launcherJvm11Arguments.asObservable()
                        val clientJvmArguments = controller.bootstrap.clientJvmArguments.asObservable()
                        val clientJvm9Arguments = controller.bootstrap.clientJvm9Arguments.asObservable()
                        item("Artifacts", expanded = true) {
                            tableview(artifacts) {
                                column("Name", Bootstrap.Artifact::name)
                                column("Version", Bootstrap.Artifact::version)
                                readonlyColumn("Size", Bootstrap.Artifact::formattedSize)
                                column("Path", Bootstrap.Artifact::path).maxWidth(150)
                                column("Hash", Bootstrap.Artifact::hash).maxWidth(150)
                                bindSelected(model)
                            }
                        }
                        item("Java Launcher args") {
                            listview(jvmArguments) {
                            }
                        }
                        item("Java 11 Launcher args") {
                            listview(jvm11Arguments) {
                            }
                        }
                        item("Client Java 11 arguments") {
                            listview(clientJvmArguments)
                        }
                        item("Client Java 9 arguments") {
                            listview(clientJvm9Arguments) {
                            }
                        }
                    }
                }

                subscribe<NewBootstrapEvent> { event ->
                    val bootstrap = event.bootstrap
                    val artifacts = bootstrap.artifacts
                    val client = bootstrap.client
                    val jvm11Arguments = bootstrap.launcherJvm11Arguments.asObservable()
                    val jvmArguments = bootstrap.launcherArguments.asObservable()
                    val clientJvmArguments = bootstrap.clientJvmArguments.asObservable()
                    val clientJvm9Arguments = bootstrap.clientJvm9Arguments.asObservable()
                    tab("New Bootstrap") {
                        this.isClosable = false
                        requestFocus()
                        enableValidate.value = true

                        drawer {
                            item("Artifacts", expanded = true) {
                                tableview(artifacts) {
                                    style {
                                        if (controller.bootstrap.artifacts.stream()
                                                .map(Bootstrap.Artifact::name).toList()
                                                .contains(selectedItem?.name)) {
                                            baseColor = javafx.scene.paint.Color.SPRINGGREEN
                                        }
                                    }
                                    column("Name", Bootstrap.Artifact::name)
                                    column("Version", Bootstrap.Artifact::version)
                                    readonlyColumn("Size", Bootstrap.Artifact::formattedSize)
                                    column("Path", Bootstrap.Artifact::path).maxWidth(150)
                                    column("Hash", Bootstrap.Artifact::hash).maxWidth(150)
                                    bindSelected(model)

                                    contextmenu {
                                        item("Copy").action {
                                            clipboard.putString(selectedItem.toString())
                                        }
                                        item("Delete").action {
                                            selectedItem?.apply {
                                                controller.newBootstrap.artifacts.remove(this)
                                            }
                                        }
                                    }
                                }
                            }
                            item("Java Launcher args") {
                                listview(jvmArguments) {
                                }
                            }
                            item("Java 11 Launcher args") {
                                listview(jvm11Arguments) {
                                }
                            }
                            item("Client Java 11 arguments") {
                                listview(clientJvmArguments)
                            }
                            item("Client Java 9 arguments") {
                                listview(clientJvm9Arguments) {
                                }
                            }
                        }
                    }
                }
            }



        }
    }


}


