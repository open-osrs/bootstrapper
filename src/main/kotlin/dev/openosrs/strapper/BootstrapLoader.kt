package dev.openosrs.strapper

import com.google.gson.Gson
import java.net.URL
import java.nio.charset.Charset


class BootstrapLoader {
    private val bootstrapUrl = "https://raw.githubusercontent.com/runelite-extended/maven-repo/master/bootstrap.json"
    private val jsonText = URL(bootstrapUrl).readText(Charset.defaultCharset())

    fun loadBootStrap() : Bootstrap {
        with(Gson()) { return fromJson<Bootstrap>(jsonText, Bootstrap::class.java) }
    }
}