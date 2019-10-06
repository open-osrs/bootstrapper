package dev.openosrs.strapper.events

import tornadofx.*

class ProgressLabelUpdateEvent(val processedDependencies: String) : FXEvent() {
}