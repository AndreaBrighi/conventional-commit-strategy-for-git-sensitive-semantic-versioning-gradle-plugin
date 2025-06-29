package io.github.andreabrighi.gradle.gitsemver.conventionalcommit.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import java.util.concurrent.TimeUnit

internal class Tests :
    StringSpec(
        {
            "git tagged + development with basic chore commit (conventional commits)" {
                conventionalCommitTest(
                    "chore: Test commit",
                    expectedVersion = "1.2.3-foodev01+",
                    pluginConfiguration = "developmentIdentifier.set(\"foodev\")",
                )
            }
            "git tagged + development with fix commit (conventional commits)" {
                conventionalCommitTest(
                    "fix: Test commit",
                    expectedVersion = "1.2.4-foodev01+",
                    pluginConfiguration = "developmentIdentifier.set(\"foodev\")",
                )
            }
            "git tagged + development with feat commit (conventional commits)" {
                conventionalCommitTest(
                    "feat: Test commit",
                    expectedVersion = "1.3.0-foodev01+",
                    pluginConfiguration = "developmentIdentifier.set(\"foodev\")",
                )
            }
            "git tagged + development with breaking change commit (conventional commits) using !: " {
                conventionalCommitTest(
                    "feat!: Test commit",
                    expectedVersion = "2.0.0-foodev01+",
                    pluginConfiguration = "developmentIdentifier.set(\"foodev\")",
                )
            }
            "git tagged + development with breaking change commit (conventional commits) using BREAKING CHANGE footer" {
                conventionalCommitTest(
                    "feat: Test commit\nBREAKING CHANGE: test",
                    expectedVersion = "2.0.0-foodev01+",
                    pluginConfiguration = "developmentIdentifier.set(\"foodev\")",
                )
            }
            """git tagged + development with breaking change commit (conventional commits)
                "using !: and BREAKING CHANGE footer""" {
                conventionalCommitTest(
                    "feat!: Test commit\nBREAKING CHANGE: test",
                    expectedVersion = "2.0.0-foodev01+",
                    pluginConfiguration = "developmentIdentifier.set(\"foodev\")",
                )
            }
            "git tagged + development with breaking change commit and fix commit (conventional commits)" {
                conventionalCommitTest(
                    "fix: Test commit",
                    "feat!: Test commit",
                    expectedVersion = "2.0.0-foodev02+",
                    pluginConfiguration = "developmentIdentifier.set(\"foodev\")",
                )
            }
        },
    ) {
    companion object {
        private fun folder(closure: TemporaryFolder.() -> Unit) =
            TemporaryFolder().apply {
                create()
                closure()
            }

        private fun TemporaryFolder.file(
            name: String,
            content: () -> String,
        ) = newFile(name).writeText(content().trimIndent())

        private fun TemporaryFolder.runCommand(
            vararg command: String,
            wait: Long = 10,
        ) {
            val process =
                ProcessBuilder(*command)
                    .directory(root)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start()
            process.waitFor(wait, TimeUnit.SECONDS)
            require(process.exitValue() == 0) {
                "command '${command.joinToString(" ")}' failed with exit value ${process.exitValue()}"
            }
        }

        private fun TemporaryFolder.runCommand(
            command: String,
            wait: Long = 10,
        ) = runCommand(
            *command.split(" ").toTypedArray(),
            wait = wait,
        )

        private fun TemporaryFolder.initGit() {
            runCommand("git init")
            runCommand("git add .")
            runCommand("git config user.name gitsemver")
            runCommand("git config user.email none@test.com")
            runCommand("git config --global init.defaultBranch master")
            runCommand("git", "commit", "-m", "\"Test commit\"")
        }

        private fun TemporaryFolder.initGitWithTag() {
            initGit()
            runCommand("git", "tag", "-a", "1.2.3", "-m", "\"test\"")
        }

        private fun TemporaryFolder.runGradle(
            vararg arguments: String = arrayOf("printGitSemVer", "--stacktrace"),
        ): String =
            GradleRunner
                .create()
                .withProjectDir(root)
                .withPluginClasspath()
                .withArguments(*arguments)
                .build()
                .output

        private fun configuredPlugin(
            pluginConfiguration: String = "",
            otherChecks: TemporaryFolder.() -> Unit = {},
        ): TemporaryFolder =
            folder {
                file("settings.gradle") { "rootProject.name = 'testproject'" }
                file("build.gradle.kts") {
                    """
                    import org.danilopianini.gradle.gitsemver.*
                    import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.*
                        
                    plugins {
                        id("org.danilopianini.git-semver")
                    }
                    gitSemVer {
                        $pluginConfiguration
                    }
                    """.trimIndent()
                }
                otherChecks()
            }

        fun conventionalCommitTest(
            vararg commits: String,
            expectedVersion: String,
            pluginConfiguration: String = "",
        ) {
            val workingDirectory =
                configuredPlugin(
                    """$pluginConfiguration
                    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)""",
                ) {
                    initGitWithTag()
                    commits.forEachIndexed { index, it ->
                        file("something$index") { "something$index" }
                        runCommand("git add something$index")
                        runCommand("git", "commit", "-m", it)
                    }
                }
            val result = workingDirectory.runGradle()
            println(result)
            result shouldContain expectedVersion
        }
    }
}
