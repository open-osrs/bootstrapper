package dev.openosrs.strapper

import javafx.collections.ObservableList
import tornadofx.*
import java.net.URL
import java.nio.charset.Charset


class BootstrapLoader {
    private val bootstrapUrl = "https://raw.githubusercontent.com/runelite-extended/maven-repo/master/bootstrap.json"
    private val jsonText = URL(bootstrapUrl).readText(Charset.defaultCharset())


    fun loadBootStrap(): Bootstrap {
        return loadJsonModel(jsonText)
    }

    fun getArtifacts(): ObservableList<Bootstrap.Artifact> {
        return loadBootStrap().artifacts
    }

}