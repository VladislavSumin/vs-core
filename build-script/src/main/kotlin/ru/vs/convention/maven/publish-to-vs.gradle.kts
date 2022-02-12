package ru.vs.convention.maven

plugins {
    id("maven-publish")
}

publishing {
    this.repositories {
        maven("https://sumin.jfrog.io/artifactory/vs/") {
//            credentials {
//                username = "deployer"
//                password = ""
//            }
        }
    }
}
