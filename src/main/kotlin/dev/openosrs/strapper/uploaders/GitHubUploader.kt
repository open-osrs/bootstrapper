package dev.openosrs.strapper.uploaders

import dev.openosrs.strapper.controllers.StrapController
import dev.openosrs.strapper.views.UI
import org.kohsuke.github.GitHubBuilder

class GitHubUploader() {
    private val gh = GitHubBuilder()
    private val uiView: UI by inject()



}