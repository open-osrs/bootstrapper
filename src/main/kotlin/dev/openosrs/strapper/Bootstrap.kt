package dev.openosrs.strapper


import com.google.common.collect.Queues
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.util.regex.Pattern
import javax.json.JsonObject

class Bootstrap : JsonModel {

    var validationQueue = Queues.newConcurrentLinkedQueue<Bootstrap.Artifact>()

    val projectVersionProperty = SimpleStringProperty()
    var projectVersion by projectVersionProperty
    val minimumLauncherVersionProperty = SimpleStringProperty()
    var minimumLauncherVersion by minimumLauncherVersionProperty
    var launcherJvm11Arguments = FXCollections.observableArrayList<String>()
    var launcherArguments = FXCollections.observableArrayList<String>()
    var clientJvmArguments = FXCollections.observableArrayList<String>()
    var clientJvm9Arguments = FXCollections.observableArrayList<String>()
    val clientProperty = SimpleObjectProperty<Client>()
    var client by clientProperty
    val buildCommitProperty = SimpleStringProperty()
    var buildCommit by buildCommitProperty
    val artifacts: ObservableList<Artifact> = FXCollections.observableArrayList<Artifact>()
            .onChange {
                if (it.next()) {
                    validationQueue.addAll(it.addedSubList)
                }
            }

    override fun updateModel(json: JsonObject) {
        with(json) {
            projectVersion = string("projectVersion")
            minimumLauncherVersion = string("minimumLauncherVersion")
            launcherJvm11Arguments.setAll(getJsonArray("launcherJvm11Arguments").map { toString() })
            launcherArguments.setAll(getJsonArray("launcherArguments").map { toString() })
            clientJvmArguments.setAll(getJsonArray("clientJvmArguments").map { toString() })
            clientJvm9Arguments.setAll(getJsonArray("clientJvm9Arguments").map { toString() })
            client = jsonObject("client")?.toModel()
            buildCommit = string("buildCommit")
            artifacts.setAll(getJsonArray("artifacts").toModel())
        }
    }


    class Artifact : JsonModel {
        val pattern = Pattern.compile("^(.+?)-(\\d.*?)\\.jar$")
        val hashProperty = SimpleStringProperty()
        var hash: String by hashProperty

        val nameProperty = SimpleStringProperty()
        var name: String by nameProperty

        val versionProperty = SimpleStringProperty()
        var version: String by versionProperty

        val pathProperty = SimpleStringProperty()
        var path: String by pathProperty

        val sizeProperty = SimpleStringProperty()
        var size: String by sizeProperty

        override fun updateModel(json: JsonObject) {
            with(json) {
                name = pattern.matcher(string("name")!!).results().findFirst().get().group(1)
                version = pattern.matcher(string("name")!!).results().findFirst().get().group(2)
                hash = string("hash")!!
                path = string("path")!!
                this@Artifact.size = string("size")!!
            }
        }

        fun rename(s: String) {
            nameProperty.value = (pattern.matcher(s).results().findFirst().get().group(1))
            versionProperty.value = pattern.matcher(s).results().findFirst().get().group(2)
        }

        override fun toString(): String {
            return "Artifact(hashProperty=${hashProperty.value}, nameProperty=${nameProperty.value}," +
                    " versionProperty=${versionProperty.value}," +
                    " pathProperty=${pathProperty.value}, sizeProperty=${sizeProperty.value})"
        }
    }

    fun listChanged() {

    }

    class ArtifactModel : ItemViewModel<Artifact>() {
        val hash = bind(Artifact::hashProperty)
        val name = bind(Artifact::nameProperty)
        val version = bind(Artifact::versionProperty)
        val path = bind(Artifact::pathProperty)
        val size = bind(Artifact::sizeProperty)
        // val newSize = bind(Artifact::newSize)
        // val newHash = bind(Artifact::newHash)

    }


    class Client : JsonModel// client// jar// net.runelite
    {
        val versionProperty = SimpleStringProperty()
        var version by versionProperty
        val propertiesProperty = SimpleStringProperty()
        var properties by propertiesProperty
        val groupIdProperty = SimpleStringProperty()
        var groupId by groupIdProperty
        val extensionProperty = SimpleStringProperty()
        var extension by extensionProperty
        val classifierProperty = SimpleStringProperty()
        var classifier by classifierProperty
        val artifactIdProperty = SimpleStringProperty()
        var artifactId by artifactIdProperty
        override fun updateModel(json: JsonObject) {
            with(json) {
                version = string("version")
                properties = string("properties")
                groupId = string("groupId")
                extension = string("extension")
                classifier = string("classifier")
                artifactId = string("artifactId")
            }
        }
    }


}









