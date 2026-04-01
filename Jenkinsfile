pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -B'
            }
        }

        stage('Test') {
            steps {
                sh 'SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200'
            }
        }

        stage('Report') {
            steps {
                junit 'target/surefire-reports/**/*.xml'
                archiveArtifacts artifacts: 'target/**', allowEmptyArchive: true
                
                // Generate Allure reports
                script {
                    sh 'mvn allure:report || true'
                }
                
                // Publish Allure Report
                publishHTML([
                    reportDir: 'target/site/allure-report',
                    reportFiles: 'index.html',
                    reportName: '📊 Allure Test Report',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: false
                ])
                
                echo '✅ Test reports generated successfully!'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        
        success {
            echo '✅ Build and tests passed!'
            
            // Email notification for success
            script {
                try {
                    emailext(
                        subject: "✅ BUILD PASSED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """BUILD SUCCESSFULLY COMPLETED ✅

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${JOB_NAME}
  • Build Number: #${BUILD_NUMBER}
  • Status: ✅ SUCCESS
  • Branch: ${GIT_BRANCH}
  • Commit: ${GIT_COMMIT}

═══════════════════════════════════════════════════════════

TEST RESULTS:
  ✅ All tests passed successfully!
  
DETAILED REPORTS:
  📊 Allure Test Report: ${BUILD_URL}📊_Allure_Test_Report/
  📈 JUnit Results: ${BUILD_URL}testReport/
  🔍 Console Output: ${BUILD_URL}console/

═══════════════════════════════════════════════════════════

BUILD ARTIFACTS:
  • Test Results: Available in Jenkins UI
  • Detailed Test Logs: Check Allure Report
  • Build Duration: See Console Output

═══════════════════════════════════════════════════════════

For more details, visit: ${BUILD_URL}

Thank you!""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo '✅ Email notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Email notification failed: ${e.message}"
                }
            }
            
            // Slack notification for success
            script {
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_HOOK')]) {
                        sh '''
                        curl -X POST "${SLACK_HOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins",
                            "icon_emoji": ":jenkins:",
                            "attachments": [
                              {
                                "color": "#36a64f",
                                "title": "✅ BUILD PASSED",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Details", "value": "<'"${BUILD_URL}"'|View Build>", "short": true}
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo '✅ Slack notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Slack notification failed: ${e.message}"
                }
            }
        }
        
        failure {
            echo '❌ Build or tests failed!'
            
            // Email notification for failure
            script {
                try {
                    emailext(
                        subject: "❌ BUILD FAILED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """BUILD FAILED ❌

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${JOB_NAME}
  • Build Number: #${BUILD_NUMBER}
  • Status: ❌ FAILURE
  • Branch: ${GIT_BRANCH}
  • Commit: ${GIT_COMMIT}

═══════════════════════════════════════════════════════════

TEST RESULTS:
  ❌ Some tests failed or build encountered errors!
  
DETAILED REPORTS & LOGS:
  📊 Allure Test Report: ${BUILD_URL}📊_Allure_Test_Report/
  📈 JUnit Results: ${BUILD_URL}testReport/
  🔍 Console Output: ${BUILD_URL}console/

═══════════════════════════════════════════════════════════

FAILURE ANALYSIS:
  1. Review the Console Output for error messages
  2. Check Allure Report for failed test details
  3. Examine test logs for debugging information
  
ACTION REQUIRED:
  Please review the logs and fix the issues!

═══════════════════════════════════════════════════════════

For more details, visit: ${BUILD_URL}

Need help? Check the detailed test report in Allure!""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo '✅ Email notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Email notification failed: ${e.message}"
                }
            }
            
            // Slack notification for failure
            script {
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_HOOK')]) {
                        sh '''
                        curl -X POST "${SLACK_HOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins",
                            "icon_emoji": ":jenkins:",
                            "attachments": [
                              {
                                "color": "#ff0000",
                                "title": "❌ BUILD FAILED",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Details", "value": "<'"${BUILD_URL}"'|View Build>", "short": true}
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo '✅ Slack notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Slack notification failed: ${e.message}"
                }
            }
        }
    }
}
