package dev.openosrs.strapper.models


import com.google.common.collect.Queues
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.apache.commons.io.FileUtils
import tornadofx.*
import java.util.regex.Pattern
import javax.json.JsonObject
import tornadofx.getValue
import tornadofx.setValue
import java.util.concurrent.ConcurrentLinkedQueue

class Bootstrap : JsonModel {


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
    private var buildCommit by buildCommitProperty
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

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("projectVersion", projectVersion)
            add("minimumLauncherVersion", minimumLauncherVersion)
            add("launcherJvm11Arguments", launcherJvm11Arguments)
            add("launcherArguments", launcherArguments)
            add("clientJvmArguments", clientJvmArguments)
            add("clientJvm9Arguments", clientJvm9Arguments)
            add("client", client.toJSON())
            add("buildCommit", buildCommit)
            add("artifacts", artifacts.toJSON())
        }
    }


    class Artifact : JsonModel {
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

        val formattedSizeProperty =  SimpleStringProperty()
        var formattedSize by formattedSizeProperty

        override fun updateModel(json: JsonObject) {
            with(json) {
                name = pattern.matcher(string("name")!!).results().findFirst().get().group(1)
                version = pattern.matcher(string("name")!!).results().findFirst().get().group(2)
                hash = string("hash")!!
                path = string("path")!!
                this@Artifact.size = string("size")!!
                formattedSize = FileUtils.byteCountToDisplaySize(string("size")!!.toLong())
            }
        }



        override fun toJSON(json: JsonBuilder) {
            with (json) {
                add("name", name + version)
                add("path", path)
                add("size", size)
                add("hash", hash)
            }
        }

        fun rename(s: String) {
            nameProperty.value = (Companion.pattern.matcher(s).results().findFirst().get().group(1))
            versionProperty.value = Companion.pattern.matcher(s).results().findFirst().get().group(2)
        }

        override fun toString(): String {
            return "Artifact(hashProperty=${hashProperty.value}, nameProperty=${nameProperty.value}," +
                    " versionProperty=${versionProperty.value}," +
                    " pathProperty=${pathProperty.value}, sizeProperty=${sizeProperty.value})"
        }

        companion object {
            val pattern = Pattern.compile("^(.+?)-(\\d.*?)\\.jar$")
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

        override fun toJSON(json: JsonBuilder) {
            with (json) {
                add("version", version)
                add("properties", properties)
                add("groupId", groupId)
                add("extension", extension)
                add("classifier", classifier)
                add("artifactId", artifactId)
            }
        }
    }

    companion object {
        var validationQueue: ConcurrentLinkedQueue<Artifact> = Queues.newConcurrentLinkedQueue<Artifact>()
    }


}

fun Bootstrap.Artifact.sizePropertyChanged() {
    sizeProperty.onChange {
        formattedSize = FileUtils.byteCountToDisplaySize(size.toLong())
    }
}












