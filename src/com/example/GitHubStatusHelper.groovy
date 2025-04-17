package com.example

class GitHubStatusHelper {
  def script
  String context

  GitHubStatusHelper(script, String context = ${env.BRANCH_NAME}) {
    this.script = script
    this.context = context
  }

  private String getRepoURL() {
    script.sh "git config --get remote.origin.url > .git/remote-url"
    return script.readFile(".git/remote-url").trim()
  }

  private String getCommitSha() {
    script.sh "git rev-parse HEAD > .git/current-commit"
    return script.readFile(".git/current-commit").trim()
  }

  private void update(String message, String state) {
    String repoUrl = getRepoURL()
    String commitSha = getCommitSha()

    script.step([
      $class: 'GitHubCommitStatusSetter',
      reposSource: [ $class: "ManuallyEnteredRepositorySource", url: repoUrl ],
      commitShaSource: [ $class: "ManuallyEnteredShaSource", sha: commitSha ],
      contextSource: [ $class: "ManuallyEnteredCommitContextSource", context: context ],
      errorHandlers: [ [ $class: 'ShallowAnyErrorHandler' ] ],
      statusResultSource: [
        $class: 'ConditionalStatusResultSource',
        results: [
          [ $class: 'AnyBuildResult', state: state, message: message ]
        ]
      ]
    ])
  }

  void pending() { update('Build in progress', 'PENDING') }
  void success() { update('Built successfully', 'SUCCESS') }
  void error()   { update('Build failed', 'ERROR') }
}
