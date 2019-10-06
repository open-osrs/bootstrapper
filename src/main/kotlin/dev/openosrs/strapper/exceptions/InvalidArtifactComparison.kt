package dev.openosrs.strapper.exceptions

class InvalidArtifactComparison(throwable: Throwable) : Exception() {
    override val message: String?
        get() = "The two artifacts are not comparable."
}