plugins {
    id("com.gradle.develocity") version "4.1"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.28"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        publishing.onlyIf { it.buildResult.failures.isNotEmpty() }
    }
}

gitHooks {
    preCommit {
        tasks("ktlintCheck")
    }
    commitMsg { conventionalCommits() }
    createHooks(true)
}

rootProject.name = "conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin"
