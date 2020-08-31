tasks {

    register("createBuild") {
        group = "profile"
        description = "Creates a Gradle build to profile Kotlin DSL compilation time"
        doLast {
            val plugins = listOf(
                "my-java-conventions", "my-groovy-conventions",
                "my-kotlin-conventions", "my-node-conventions",
                "my-publishing-conventions"
            )
            val pluginPermutations = groovy.util.PermutationGenerator(plugins)

            val projectOutputDir = buildDir.resolve("profile")
            val projectDir = projectOutputDir.apply {
                mkdirs()
            }
            file("template").copyRecursively(projectDir, true)

            val settingsFile = projectDir.resolve("settings.gradle.kts")
            pluginPermutations.withIndex().forEach { (index, plugins) ->
                val subName = "sub$index"
                projectDir.resolve("subprojects/$subName/build.gradle.kts").apply {
                    parentFile.mkdirs()
                    writeText(
                        plugins.joinToString(
                            prefix = "plugins {\n",
                            postfix = "\n}",
                            separator = "\n    "
                        ) {
                           "`$it`"
                        }
                    )
                }
                settingsFile.appendText(
                    "include(\"subprojects:$subName\")\n"
                )
            }
            val relativeProjectDir = relativePath(projectDir)
            println("""
Gradle build with ${pluginPermutations.total} modules generated in $relativeProjectDir

Profile the generated build by running:

    cd $relativeProjectDir
    gradle-profiler --profile async-profiler --output-dir ../flamegraph --scenario-file benchmark.scenarios --gradle-version ${org.gradle.util.GradleVersion.current().version}
""")
        }
    }
}