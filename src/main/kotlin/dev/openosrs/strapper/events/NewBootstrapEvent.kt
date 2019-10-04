package dev.openosrs.strapper.events

import dev.openosrs.strapper.models.Bootstrap
import tornadofx.*

class NewBootstrapEvent(val bootstrap: Bootstrap) :FXEvent() {
}