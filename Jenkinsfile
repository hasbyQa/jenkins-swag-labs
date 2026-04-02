#!/usr/bin/env groovy

// ── Parse test summary from surefire reports ─────────────────────────────────
def getTestSummary() {
    def summary = [total: 0, passed: 0, failed: 0, skipped: 0, failedTests: []]
    try {
        def files = findFiles(glob: 'target/surefire-reports/TEST-*.xml')
        for (def file : files) {
            def xml = new XmlSlurper().parse(file.path)
            def total = xml.@tests.toInteger()
            def failures = xml.@failures.toInteger()
            def errors = xml.@errors.toInteger()
            def skipped = xml.@skipped.toInteger()
            
            summary.total += total
            summary.passed += (total - failures - errors - skipped)
            summary.failed += (failures + errors)
            summary.skipped += skipped
            
            xml.testcase.each { tc ->
                if (tc.failure || tc.error) {
                    def error = tc.failure ?: tc.error
                    summary.failedTests << [
                        name: tc.@name,
                        className: tc.@classname?.tokenize('.')?.last() ?: 'Unknown',
                        message: error.text()?.take(300) ?: 'No message'
                    ]
                }
            }
        }
        echo "✅ Summary: total=${summary.total} passed=${summary.passed} failed=${summary.failed} skipped=${summary.skipped}"
    } catch (Exception e) {
        echo "⚠️  Parse error: ${e.message}"
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
                echo "📥 Checking out code from GitHub..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "🔨 Building project with clean compilation..."
                sh 'mvn clean compile -B -q'
                echo "✅ Build completed successfully"
            }
        }

        stage('Test') {
            steps {
                echo "🧪 Running test suite..."
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh 'SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200'
                }
            }
        }

        stage('Allure Report') {
            steps {
                echo "📊 Generating Allure Report..."
                sh 'mvn allure:report -B -DskipTests=true || true'
                
                // Publish JUnit test results
                junit testResults: 'target/surefire-reports/**/*.xml', 
                      allowEmptyResults: true
                
                // Publish Allure Report to Jenkins
                publishHTML([
                    reportDir: 'target/site/allure-report',
                    reportFiles: 'index.html',
                    reportName: '📊 Allure Test Report',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: true
                ])
                
                // Archive test artifacts
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, target/allure-results/**', 
                                  allowEmptyArchive: true
                
                echo "✅ Allure Report generated and published!"
            }
        }
    }

    post {
        always {
            echo "🧹 Cleaning workspace..."
            cleanWs()
        }
        
        success {
            script {
                echo "========== SUCCESS NOTIFICATIONS =========="
                def ts = getTestSummary()
                def passRate = ts.total > 0 ? (int)((ts.passed / ts.total) * 100) : 0
                def reportUrl = "${BUILD_URL}Allure_Test_Report/"
                def buildInfo = "🏗️ Build #${env.BUILD_NUMBER} | 📂 Branch: ${env.GIT_BRANCH ?: 'unknown'}"
                
                // Slack notification
                echo "[1/2] Sending Slack notification..."
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        sh '''
                        curl -X POST "${SLACK_WEBHOOK}" \
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
                                  "text": "✅ BUILD PASSED - Swag Labs Tests",
                                  "emoji": true
                                }
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Job:*\\n'"${JOB_NAME}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Build:*\\n#'"${BUILD_NUMBER}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Branch:*\\n'"${GIT_BRANCH}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Status:*\\n✅ SUCCESS"
                                  }
                                ]
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Total Tests:*\\n'"${TOTAL_TESTS}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Passed:*\\n'"${PASSED_TESTS}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Failed:*\\n'"${FAILED_TESTS}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Pass Rate:*\\n'"${PASS_RATE}"'%"
                                  }
                                ]
                              },
                              {
                                "type": "divider"
                              },
                              {
                                "type": "actions",
                                "elements": [
                                  {
                                    "type": "button",
                                    "text": {
                                      "type": "plain_text",
                                      "text": "📊 View Allure Report",
                                      "emoji": true
                                    },
                                    "url": "'"${BUILD_URL}"'Allure_Test_Report/",
                                    "style": "primary"
                                  },
                                  {
                                    "type": "button",
                                    "text": {
                                      "type": "plain_text",
                                      "text": "🔨 View Jenkins Build",
                                      "emoji": true
                                    },
                                    "url": "'"${BUILD_URL}"'"
                                  }
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "✅ Slack notification sent successfully!"
                } catch (Exception e) {
                    echo "⚠️  Slack notification failed: ${e.message}"
                }
                
                echo "[2/2] Archiving artifacts..."
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, target/allure-results/**', 
                                  allowEmptyArchive: true
                
                echo "========== SUCCESS NOTIFICATIONS COMPLETE =========="
            }
        }
        
        failure {
            script {
                echo "========== FAILURE NOTIFICATIONS =========="
                def ts = getTestSummary()
                def passRate = ts.total > 0 ? (int)((ts.passed / ts.total) * 100) : 0
                def reportUrl = "${BUILD_URL}Allure_Test_Report/"
                
                // Slack notification
                echo "[1/2] Sending Slack notification..."
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        def failureBlocks = ''
                        if (ts.failedTests.size() > 0) {
                            ts.failedTests.take(5).eachWithIndex { test, idx ->
                                failureBlocks += "*${idx+1}. ${test.name}*\n"
                                failureBlocks += "_Class: ${test.className}_\n"
                                failureBlocks += "```${test.message}```\n"
                            }
                        }
                        
                        sh '''
                        curl -X POST "${SLACK_WEBHOOK}" \
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
                                  "text": "❌ BUILD FAILED - Swag Labs Tests",
                                  "emoji": true
                                }
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Job:*\\n'"${JOB_NAME}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Build:*\\n#'"${BUILD_NUMBER}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Branch:*\\n'"${GIT_BRANCH}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Status:*\\n❌ FAILURE"
                                  }
                                ]
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Total Tests:*\\n'"${TOTAL_TESTS}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Passed:*\\n'"${PASSED_TESTS}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Failed:*\\n'"${FAILED_TESTS}"'"
                                  },
                                  {
                                    "type": "mrkdwn",
                                    "text": "*Pass Rate:*\\n'"${PASS_RATE}"'%"
                                  }
                                ]
                              },
                              {
                                "type": "divider"
                              },
                              {
                                "type": "actions",
                                "elements": [
                                  {
                                    "type": "button",
                                    "text": {
                                      "type": "plain_text",
                                      "text": "📊 View Allure Report",
                                      "emoji": true
                                    },
                                    "url": "'"${BUILD_URL}"'Allure_Test_Report/",
                                    "style": "danger"
                                  },
                                  {
                                    "type": "button",
                                    "text": {
                                      "type": "plain_text",
                                      "text": "🔍 View Build Logs",
                                      "emoji": true
                                    },
                                    "url": "'"${BUILD_URL}"'console"
                                  },
                                  {
                                    "type": "button",
                                    "text": {
                                      "type": "plain_text",
                                      "text": "📈 Test Results",
                                      "emoji": true
                                    },
                                    "url": "'"${BUILD_URL}"'testReport"
                                  }
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "✅ Slack failure notification sent!"
                } catch (Exception e) {
                    echo "⚠️  Slack notification failed: ${e.message}"
                }
                
                echo "[2/2] Archiving artifacts..."
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, target/allure-results/**', 
                                  allowEmptyArchive: true
                
                echo "========== FAILURE NOTIFICATIONS COMPLETE =========="
            }
        }
    }
}
