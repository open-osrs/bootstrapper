package dev.openosrs.strapper.util

import dev.openosrs.strapper.models.Bootstrap
import javafx.collections.ObservableList
import tornadofx.*
import java.net.URL
import java.nio.charset.Charset


class BootstrapLoader {
    private val bootstrapUrl = "https://raw.githubusercontent.com/open-osrs/hosting/master/bootstrap-stable.json"
    private val jsonText = URL(bootstrapUrl).readText(Charset.defaultCharset())


    fun loadBootStrap(): Bootstrap {
        return loadJsonModel(jsonText)
    }

    fun getArtifacts(): ObservableList<Bootstrap.Artifact> {
        return loadBootStrap().artifacts
    }

}