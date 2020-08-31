## Generate flamegraph for a Gradle Kotlin DSL build 

1. Install [Gradle profiler](https://github.com/gradle/gradle-profiler)
2. Run `./gradlew createBuild`
3. Follow the instructions

### How it works 

`./gradlew createBuild` generates a medium size Gradle build (120 modules, 121 gradle.kts scripts) under `build/profile`.

Gradle profiler runs the build a few times, changing the ABI of a Kotlin class under `buildSrc` before each run to ensures all Kotlin scripts need to be recompiled. At the end it should generate a flamegraph under [./build/flamegraph](./build/flamegraph).

#### Improving the way Gradle interfaces with the Kotlin script compiler 

The interface between Gradle and the Kotlin script compiler is implemented in [KotlinCompiler.kt](https://github.com/gradle/gradle/blob/176e1c4818c68a01eb7aa7a248eee19a71414716/subprojects/kotlin-dsl/src/main/kotlin/org/gradle/kotlin/dsl/support/KotlinCompiler.kt#L88-L87).

It is worth noting how `KOTLIN_COMPILER_ENVIRONMENT_KEEPALIVE_PROPERTY` system property is being used to workaround [`KT-35394`](https://youtrack.jetbrains.com/issue/KT-35394) [here](https://github.com/gradle/gradle/blob/176e1c4818c68a01eb7aa7a248eee19a71414716/subprojects/kotlin-dsl/src/main/kotlin/org/gradle/kotlin/dsl/support/KotlinCompiler.kt#L375-L390). This has a very negative impact on performance, it would be much better to keep the Kotlin compiler environment alive for the duration of the whole build and dispose of it during ~buildFinished~. Is there some public or internal Kotlin compiler API we can use to accomplish that?
