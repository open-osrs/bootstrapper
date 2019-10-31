package dev.openosrs.strapper.exceptions

class VersionException(throwable: Throwable) : Exception() {

    override val message: String?
        get() = super.message
}