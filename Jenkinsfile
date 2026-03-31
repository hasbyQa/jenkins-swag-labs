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
                        body: """
                        BUILD SUCCESSFULLY COMPLETED

                        Job: ${JOB_NAME}
                        Build Number: ${BUILD_NUMBER}
                        Status: ✅ SUCCESS

                        Branch: ${GIT_BRANCH}
                        Commit: ${GIT_COMMIT}
                        
                        Test Results: All tests passed!
                        
                        Build Details: ${BUILD_URL}
                        Console Output: ${BUILD_URL}console
                        """.stripIndent(),
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        recipientProviders: [
                            developers(),
                            requestor()
                        ],
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
                    slackSend(
                        color: '#36a64f',
                        channel: '#builds',
                        message: """
                        ✅ *BUILD PASSED*
                        Job: ${JOB_NAME}
                        Build: #${BUILD_NUMBER}
                        Branch: ${GIT_BRANCH}
                        Details: <${BUILD_URL}|View Build>
                        """.stripIndent(),
                        tokenCredentialId: 'slack-webhook-url'
                    )
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
                        body: """
                        BUILD FAILED

                        Job: ${JOB_NAME}
                        Build Number: ${BUILD_NUMBER}
                        Status: ❌ FAILURE

                        Branch: ${GIT_BRANCH}
                        Commit: ${GIT_COMMIT}
                        
                        Build Details: ${BUILD_URL}
                        Console Output: ${BUILD_URL}console
                        
                        Please review the logs and fix the issues.
                        """.stripIndent(),
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        recipientProviders: [
                            developers(),
                            requestor(),
                            brokenBuildSuspects()
                        ],
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
                    slackSend(
                        color: '#ff0000',
                        channel: '#builds',
                        message: """
                        ❌ *BUILD FAILED*
                        Job: ${JOB_NAME}
                        Build: #${BUILD_NUMBER}
                        Branch: ${GIT_BRANCH}
                        Details: <${BUILD_URL}|View Build>
                        """.stripIndent(),
                        tokenCredentialId: 'slack-webhook-url'
                    )
                    echo '✅ Slack notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Slack notification failed: ${e.message}"
                }
            }
        }
    }
}
