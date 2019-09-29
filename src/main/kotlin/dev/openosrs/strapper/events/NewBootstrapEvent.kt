package dev.openosrs.strapper.events

import dev.openosrs.strapper.Bootstrap
import tornadofx.*

class NewBootstrapEvent(val bootstrap: Bootstrap) :FXEvent() {
}