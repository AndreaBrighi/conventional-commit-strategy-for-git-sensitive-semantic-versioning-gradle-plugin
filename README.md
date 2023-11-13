# Conventional commit strategy for Git sensitive Semantic Versioning (SemVer) Gradle Plugin

This library provides a [conventional commit](https://www.conventionalcommits.org/en/v1.0.0/) strategy for the [Git sensitive Semantic Versioning (SemVer) Gradle Plugin](https://github.com/DanySK/git-sensitive-semantic-versioning-gradle-plugin)

## Usage

Add the following to your `build.gradle.kts`:

```kotlin
import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit
...

buildscript {
    dependencies {
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.0")
    }
}
...

gitSemVer {
    ...
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
}
```

## Supported commit types

The following commit types are supported:
- `fix`: will trigger a patch version update
- `feat`: will trigger a minor version update
- `perf`: will trigger a minor version update
- `BREAKING CHANGE`: will trigger a major version update
- `*!`: will trigger a major version update
- `*`: will trigger a none version update
