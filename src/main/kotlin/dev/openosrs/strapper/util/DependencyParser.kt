package dev.openosrs.strapper.util

import dev.openosrs.strapper.models.Bootstrap
import mu.KotlinLogging
import java.io.File

class DependencyParser(dir: File) {
    private val logger = KotlinLogging.logger("DepParser")
    private var stringDependencies = ArrayList<String>()
    var artifacts = ArrayList<Bootstrap.Artifact>()

    init {
        val dependencyDeclaration = File(dir, "runelite-client/dependencies.txt")
        var atRuntime = false
        dependencyDeclaration.forEachLine { line: String ->

            if (line.contains("+---") && !line.contains("project")) {
                stringDependencies.add(line.split("+--- ")[1].split(" ")[0])
            }

        }
        stringDependencies.forEach {
            var name = it.split(":")[1]
            var group = it.split(":")[0]
            var version = it.split(":")[2]
            if (!group.contains("runelite")) {
                var path = "https://repo.maven.apache.org/maven2/" + group.replace(".", "/") +
                        "/${name}/$version/${name}-$version.jar"
                logger.info { path }
                var a = Bootstrap.Artifact()
                a.name = name
                a.version = version
                a.path = path
                artifacts.add(a)
            }
        }
    }
}
