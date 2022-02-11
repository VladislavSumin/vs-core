package ru.vs.build_script.github

object GithubActionLogger {
    fun w(message: String) {
        println("::warning::$message")
    }
}
