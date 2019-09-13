package dev.openosrs.strapper.exceptions

class VersionException(throwable: Throwable) : Exception() {
    override fun initCause(cause: Throwable?): Throwable {
        return super.initCause(cause)
    }

    override val message: String?
        get() = super.message
}