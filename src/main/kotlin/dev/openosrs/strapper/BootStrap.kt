package dev.openosrs.strapper


import com.google.gson.annotations.SerializedName

data class BootStrap(
    @SerializedName("artifacts")
    var artifacts: List<Artifact>,
    @SerializedName("buildCommit")
    var buildCommit: String, // 320adb0a07e89e593a2117b7d2429fb23ed487f8
    @SerializedName("client")
    var client: Client,
    @SerializedName("clientJvm9Arguments")
    var clientJvm9Arguments: List<String>,
    @SerializedName("clientJvmArguments")
    var clientJvmArguments: List<String>,
    @SerializedName("launcherArguments")
    var launcherArguments: List<String>,
    @SerializedName("launcherJvm11Arguments")
    var launcherJvm11Arguments: List<String>,
    @SerializedName("minimumLauncherVersion")
    var minimumLauncherVersion: String, // 2.0.0
    @SerializedName("projectVersion")
    var projectVersion: String // 1.5.33-SNAPSHOT
) {
    data class Artifact(
        @SerializedName("hash")
        var hash: String, // 4a0f0028aacf49fa66a941464274d3c420a46d8f46578ff4250df807266d1cf4
        @SerializedName("name")
        var name: String, // runescape-api-1.5.33-SNAPSHOT.jar
        @SerializedName("path")
        var path: String, // https://nexus.thatgamerblue.com/repository/runelite/us/runelitepl/rs/runescape-api/1.5.33-SNAPSHOT/runescape-api-1.5.33-20190912.142921-3.jar
        @SerializedName("size")
        var size: String // 56638
    )

    data class Client(
        @SerializedName("artifactId")
        var artifactId: String, // client
        @SerializedName("classifier")
        var classifier: String,
        @SerializedName("extension")
        var extension: String, // jar
        @SerializedName("groupId")
        var groupId: String, // net.runelite
        @SerializedName("properties")
        var properties: String,
        @SerializedName("version")
        var version: String // 1.5.33
    )
}