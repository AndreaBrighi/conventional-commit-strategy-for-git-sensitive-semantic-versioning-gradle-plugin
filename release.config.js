var publishCmd = `
git tag -a -f \${nextRelease.version} \${nextRelease.version} -F CHANGELOG.md || exit 1
./gradlew publishPluginMavenPublicationToGithubRepository -PsigningKey="$SIGNING_KEY" -PsigningPassword="$SIGNING_PASSWORD" || exit 2
git push --force origin \${nextRelease.version} || exit 3
`
var config = require('semantic-release-preconfigured-conventional-commits');
config.plugins.push(
    ["@semantic-release/exec", {
        "publishCmd": publishCmd,
    }],
    "@semantic-release/github",
    "@semantic-release/git",
)
config.branches = [ "main" ]
module.exports = config