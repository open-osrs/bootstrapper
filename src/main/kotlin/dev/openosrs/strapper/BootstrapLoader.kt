package dev.openosrs.strapper

import com.google.gson.Gson
import java.net.URL


class BootstrapLoader {
    private val bootstrapUrl = "https://raw.githubusercontent.com/runelite-extended/maven-repo/master/bootstrap.json"
    private val jsonText = URL(bootstrapUrl).readText()

    fun loadBootStrap() : BootStrap {
        with(Gson()) { return fromJson<BootStrap>(jsonText, BootStrap::class.java) }
    }
}