package ru.vs.convention.maven

plugins {
    id("maven-publish")
}

val password = System.getenv("VS_MAVEN_PASSWORD") ?: ""

publishing {
    this.repositories {
        maven("https://sumin.jfrog.io/artifactory/vs/") {
            credentials {
                username = "deployer"
                password = password
            }
        }
    }
}
