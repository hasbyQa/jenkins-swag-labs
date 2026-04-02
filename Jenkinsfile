#!/usr/bin/env groovy

// ── Parse test summary from surefire reports ─────────────────────────────────
def getTestSummary() {
    def summary = [total: 0, passed: 0, failed: 0, skipped: 0, failedTests: []]
    try {
        def fileList = sh(
            script: 'find target/surefire-reports -name "TEST-*.xml" 2>/dev/null || true',
            returnStdout: true
        ).trim()

        if (fileList) {
            fileList.split('\n').each { filePath ->
                filePath = filePath.trim()
                if (filePath) {
                    def content = readFile(filePath)
                    def xml = new XmlSlurper().parseText(content)
                    def total    = xml.@tests.toInteger()
                    def failures = xml.@failures.toInteger()
                    def errors   = xml.@errors.toInteger()
                    def skipped  = xml.@skipped.toInteger()

                    summary.total   += total
                    summary.passed  += (total - failures - errors - skipped)
                    summary.failed  += (failures + errors)
                    summary.skipped += skipped

                    xml.testcase.each { tc ->
                        if (tc.failure || tc.error) {
                            def error = tc.failure ?: tc.error
                            summary.failedTests << [
                                name     : tc.@name,
                                className: tc.@classname?.tokenize('.')?.last() ?: 'Unknown',
                                message  : error.text()?.take(300) ?: 'No message'
                            ]
                        }
                    }
                }
            }
        }
        echo "Summary: total=${summary.total} passed=${summary.passed} failed=${summary.failed} skipped=${summary.skipped}"
    } catch (Exception e) {
        echo "Parse error: ${e.message}"
    }
    return summary
}

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 45, unit: 'MINUTES')
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code from GitHub..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Building project with clean compilation..."
                sh 'mvn clean compile -B -q'
                echo "Build completed successfully"
            }
        }

        stage('Test') {
            steps {
                echo "Running test suite..."
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh 'SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200'
                }
            }
        }

        stage('Allure Report') {
            steps {
                echo "Generating Allure Report..."
                sh 'mvn allure:report -B -DskipTests=true || true'

                junit testResults: 'target/surefire-reports/**/*.xml',
                      allowEmptyResults: true

                publishHTML([
                    reportDir: 'target/site/allure-report',
                    reportFiles: 'index.html',
                    reportName: 'Allure Test Report',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: true
                ])

                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, target/allure-results/**',
                                  allowEmptyArchive: true

                echo "Allure Report generated and published!"
            }
        }
    }

    post {
        always {
            echo "Cleaning workspace..."
            cleanWs()
        }

        success {
            script {
                def ts = getTestSummary()
                def passRate = ts.total > 0 ? (int)((ts.passed / ts.total) * 100) : 0

                // Export to shell environment
                env.TOTAL_TESTS  = "${ts.total}"
                env.PASSED_TESTS = "${ts.passed}"
                env.FAILED_TESTS = "${ts.failed}"
                env.PASS_RATE    = "${passRate}"

                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "BUILD PASSED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """BUILD SUCCESSFULLY COMPLETED

Job Name   : ${JOB_NAME}
Build      : #${BUILD_NUMBER}
Status     : SUCCESS
Branch     : ${env.GIT_BRANCH ?: 'unknown'}
Commit     : ${env.GIT_COMMIT ?: 'unknown'}

TEST RESULTS:
  Total   : ${ts.total}
  Passed  : ${ts.passed}
  Failed  : ${ts.failed}
  Skipped : ${ts.skipped}
  Pass Rate: ${passRate}%

REPORTS:
  Allure Report : ${BUILD_URL}Allure_Test_Report/
  JUnit Results : ${BUILD_URL}testReport/
  Console Output: ${BUILD_URL}console/

Full details: ${BUILD_URL}""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ────────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        sh '''
                        curl -s -X POST "${SLACK_WEBHOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins Bot",
                            "icon_emoji": ":jenkins:",
                            "blocks": [
                              {
                                "type": "header",
                                "text": {
                                  "type": "plain_text",
                                  "text": "BUILD PASSED - Swag Labs Tests",
                                  "emoji": true
                                }
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                  {"type": "mrkdwn", "text": "*Build:*\n#'"${BUILD_NUMBER}"'"},
                                  {"type": "mrkdwn", "text": "*Branch:*\n'"${GIT_BRANCH}"'"},
                                  {"type": "mrkdwn", "text": "*Status:*\nSUCCESS"}
                                ]
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Total Tests:*\n'"${TOTAL_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Passed:*\n'"${PASSED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Failed:*\n'"${FAILED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Pass Rate:*\n'"${PASS_RATE}"'%"}
                                ]
                              },
                              {"type": "divider"},
                              {
                                "type": "actions",
                                "elements": [
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Allure Report", "emoji": true},
                                    "url": "'"${BUILD_URL}"'Allure_Test_Report/",
                                    "style": "primary"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Jenkins Build", "emoji": true},
                                    "url": "'"${BUILD_URL}"'"
                                  }
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }

        failure {
            script {
                def ts = getTestSummary()
                def passRate = ts.total > 0 ? (int)((ts.passed / ts.total) * 100) : 0

                // Export to shell environment
                env.TOTAL_TESTS  = "${ts.total}"
                env.PASSED_TESTS = "${ts.passed}"
                env.FAILED_TESTS = "${ts.failed}"
                env.PASS_RATE    = "${passRate}"

                // Build failed-test detail string for email
                def failedTestDetails = ''
                if (ts.failedTests.size() > 0) {
                    ts.failedTests.take(10).eachWithIndex { test, idx ->
                        failedTestDetails += "\n  ${idx + 1}. ${test.name} (${test.className})\n     ${test.message}\n"
                    }
                } else {
                    failedTestDetails = '\n  No test-level detail available — check console output.\n'
                }

                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "BUILD FAILED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """BUILD FAILED

Job Name   : ${JOB_NAME}
Build      : #${BUILD_NUMBER}
Status     : FAILURE
Branch     : ${env.GIT_BRANCH ?: 'unknown'}
Commit     : ${env.GIT_COMMIT ?: 'unknown'}

TEST RESULTS:
  Total   : ${ts.total}
  Passed  : ${ts.passed}
  Failed  : ${ts.failed}
  Skipped : ${ts.skipped}
  Pass Rate: ${passRate}%

FAILED TESTS:${failedTestDetails}
REPORTS:
  Allure Report : ${BUILD_URL}Allure_Test_Report/
  JUnit Results : ${BUILD_URL}testReport/
  Console Output: ${BUILD_URL}console/

Full details: ${BUILD_URL}""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ────────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        sh '''
                        curl -s -X POST "${SLACK_WEBHOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins Bot",
                            "icon_emoji": ":jenkins:",
                            "blocks": [
                              {
                                "type": "header",
                                "text": {
                                  "type": "plain_text",
                                  "text": "BUILD FAILED - Swag Labs Tests",
                                  "emoji": true
                                }
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                  {"type": "mrkdwn", "text": "*Build:*\n#'"${BUILD_NUMBER}"'"},
                                  {"type": "mrkdwn", "text": "*Branch:*\n'"${GIT_BRANCH}"'"},
                                  {"type": "mrkdwn", "text": "*Status:*\nFAILURE"}
                                ]
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Total Tests:*\n'"${TOTAL_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Passed:*\n'"${PASSED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Failed:*\n'"${FAILED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Pass Rate:*\n'"${PASS_RATE}"'%"}
                                ]
                              },
                              {"type": "divider"},
                              {
                                "type": "actions",
                                "elements": [
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Allure Report", "emoji": true},
                                    "url": "'"${BUILD_URL}"'Allure_Test_Report/",
                                    "style": "danger"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Build Logs", "emoji": true},
                                    "url": "'"${BUILD_URL}"'console"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "Test Results", "emoji": true},
                                    "url": "'"${BUILD_URL}"'testReport"
                                  }
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }

        unstable {
            script {
                def ts = getTestSummary()
                def passRate = ts.total > 0 ? (int)((ts.passed / ts.total) * 100) : 0

                env.TOTAL_TESTS  = "${ts.total}"
                env.PASSED_TESTS = "${ts.passed}"
                env.FAILED_TESTS = "${ts.failed}"
                env.PASS_RATE    = "${passRate}"

                def failedTestDetails = ''
                if (ts.failedTests.size() > 0) {
                    ts.failedTests.take(10).eachWithIndex { test, idx ->
                        failedTestDetails += "\n  ${idx + 1}. ${test.name} (${test.className})\n     ${test.message}\n"
                    }
                } else {
                    failedTestDetails = '\n  No test-level detail available.\n'
                }

                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "BUILD UNSTABLE: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """BUILD UNSTABLE - Some Tests Failed

Job Name   : ${JOB_NAME}
Build      : #${BUILD_NUMBER}
Status     : UNSTABLE
Branch     : ${env.GIT_BRANCH ?: 'unknown'}
Commit     : ${env.GIT_COMMIT ?: 'unknown'}

TEST RESULTS:
  Total   : ${ts.total}
  Passed  : ${ts.passed}
  Failed  : ${ts.failed}
  Skipped : ${ts.skipped}
  Pass Rate: ${passRate}%

FAILED TESTS:${failedTestDetails}
REPORTS:
  Allure Report : ${BUILD_URL}Allure_Test_Report/
  JUnit Results : ${BUILD_URL}testReport/
  Console Output: ${BUILD_URL}console/

Full details: ${BUILD_URL}""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ────────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        sh '''
                        curl -s -X POST "${SLACK_WEBHOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins Bot",
                            "icon_emoji": ":jenkins:",
                            "blocks": [
                              {
                                "type": "header",
                                "text": {
                                  "type": "plain_text",
                                  "text": "BUILD UNSTABLE - Swag Labs Tests",
                                  "emoji": true
                                }
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                  {"type": "mrkdwn", "text": "*Build:*\n#'"${BUILD_NUMBER}"'"},
                                  {"type": "mrkdwn", "text": "*Branch:*\n'"${GIT_BRANCH}"'"},
                                  {"type": "mrkdwn", "text": "*Status:*\nUNSTABLE"}
                                ]
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Total Tests:*\n'"${TOTAL_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Passed:*\n'"${PASSED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Failed:*\n'"${FAILED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Pass Rate:*\n'"${PASS_RATE}"'%"}
                                ]
                              },
                              {"type": "divider"},
                              {
                                "type": "actions",
                                "elements": [
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Allure Report", "emoji": true},
                                    "url": "'"${BUILD_URL}"'Allure_Test_Report/",
                                    "style": "danger"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Build Logs", "emoji": true},
                                    "url": "'"${BUILD_URL}"'console"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "Test Results", "emoji": true},
                                    "url": "'"${BUILD_URL}"'testReport"
                                  }
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }
    }
}
