import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.3.21"
	id("org.springframework.boot") version "2.1.2.RELEASE"
	id("org.jetbrains.kotlin.jvm") version kotlinVersion
	id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
	id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
	id("io.spring.dependency-management") version "1.0.6.RELEASE"
}

version = "1.0.0-SNAPSHOT"

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "1.8"
		freeCompilerArgs = listOf("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("junit:junit:4.13.1")
	compile("org.springframework.boot:spring-boot-starter-web")
	compile("org.springframework.boot:spring-boot-starter-data-jpa")
	compile("com.h2database:h2")
	compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	compile("org.jetbrains.kotlin:kotlin-reflect")
	compile("com.fasterxml.jackson.module:jackson-module-kotlin")
	compile("com.google.code.gson:gson")
	implementation("com.hazelcast:hazelcast:5.0")
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:6.3.0")
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-hazelcast:6.3.0")
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-jcache:6.3.0")
	implementation("javax.cache:cache-api:1.1.0")
	testCompile("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "junit")
	}
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

