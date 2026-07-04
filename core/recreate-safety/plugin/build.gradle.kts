plugins {
    kotlin("jvm")
    // Группа и версия через общие конвеншны vs-core (как у test-модулей)
    id("ru.vladislavsumin.convention.publication.group")
    id("ru.vladislavsumin.convention.publication.version")
}

dependencies {
    // API компилятора Kotlin для написания IR-плагинов.
    // compileOnly — классы предоставляются компилятором во время сборки.
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.3.21")
}

kotlin {
    jvmToolchain(21)
}
