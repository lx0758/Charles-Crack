import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("application")
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
}

group = rootProject.group
version = rootProject.version
java.sourceCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation(kotlin("stdlib"))
    implementation("commons-io:commons-io:2.10.0")
    implementation("org.javassist:javassist:3.28.0-GA")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val fatJar = task("fatJar", type = Jar::class) {
    manifest.attributes.apply {
        put("Main-Class", "com.liux.java.charles.crack.Main")
    }
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}
tasks {
    "build" {
        dependsOn(fatJar)
    }
}
