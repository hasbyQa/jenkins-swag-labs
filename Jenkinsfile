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
                sh 'SELENIUM_TIMEOUT=30 mvn test -B'
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
            slackSend(
                color: 'good',
                message: "✅ Build #${BUILD_NUMBER} PASSED\nBranch: ${GIT_BRANCH}\nURL: ${BUILD_URL}"
            )
            emailext(
                subject: "✅ Build #${BUILD_NUMBER} PASSED - ${JOB_NAME}",
                body: """
                    Build #${BUILD_NUMBER} PASSED
                    
                    Job: ${JOB_NAME}
                    Status: SUCCESS
                    Branch: ${GIT_BRANCH}
                    Commit: ${GIT_COMMIT}
                    
                    Details: ${BUILD_URL}
                    Console Output: ${BUILD_URL}console
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/plain'
            )
        }
        
        failure {
            echo '❌ Build or tests failed!'
            slackSend(
                color: 'danger',
                message: "❌ Build #${BUILD_NUMBER} FAILED\nBranch: ${GIT_BRANCH}\nCommit: ${GIT_COMMIT}\nURL: ${BUILD_URL}"
            )
            emailext(
                subject: "❌ Build #${BUILD_NUMBER} FAILED - ${JOB_NAME}",
                body: """
                    Build #${BUILD_NUMBER} FAILED
                    
                    Job: ${JOB_NAME}
                    Status: FAILURE
                    Branch: ${GIT_BRANCH}
                    Commit: ${GIT_COMMIT}
                    
                    Details: ${BUILD_URL}
                    Console Output: ${BUILD_URL}console
                    
                    Please check the logs and fix the issues.
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/plain'
            )
        }
    }
}
