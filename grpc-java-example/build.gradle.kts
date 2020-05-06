import com.google.protobuf.gradle.*

plugins {
    idea
    java
    id("com.google.protobuf") version("0.8.8")
}

group = "cdv.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val grpcVersion = "1.29.0"
val protobufVersion = "3.11.0"
val lombokVersion = "1.18.12"

dependencies {
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
    implementation("io.grpc:grpc-core:${grpcVersion}")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    runtimeOnly("io.grpc:grpc-netty-shaded:${grpcVersion}")

    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

idea {
    module {
        generatedSourceDirs.addAll(listOf(
                file("${protobuf.protobuf.generatedFilesBaseDir}/main/grpc"),
                file("${protobuf.protobuf.generatedFilesBaseDir}/main/java")
        ))
    }
}