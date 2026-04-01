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
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh 'SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200'
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Report') {
            steps {
                script {
                    // Archive artifacts
                    archiveArtifacts artifacts: 'target/**', allowEmptyArchive: true
                    
                    // Generate Allure reports
                    sh 'mvn allure:report -B -DskipTests || true'
                    
                    // Check if Allure report was generated
                    if (fileExists('target/site/allure-report/index.html')) {
                        echo "✅ Allure report generated successfully"
                        publishHTML([
                            reportDir: 'target/site/allure-report',
                            reportFiles: 'index.html',
                            reportName: '📊 Allure Test Report',
                            keepAll: true,
                            alwaysLinkToLastBuild: true,
                            allowMissing: false
                        ])
                    } else {
                        echo "⚠️ Allure report not found at target/site/allure-report"
                    }
                }
            }
        }

        stage('Deploy to GitHub Pages') {
            when {
                expression { 
                    fileExists('target/site/allure-report/index.html')
                }
            }
            steps {
                script {
                    try {
                        withCredentials([string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN')]) {
                            sh '''
                            set -e
                            
                            # Configuration
                            GH_REPO="https://${GITHUB_TOKEN}@github.com/hasbyQa/jenkins-swag-labs.git"
                            DEPLOY_DIR="/tmp/gh-pages-${BUILD_NUMBER}"
                            BUILD_DIR="build-${BUILD_NUMBER}"
                            
                            # Clean up previous deployment directory
                            rm -rf "${DEPLOY_DIR}" 2>/dev/null || true
                            mkdir -p "${DEPLOY_DIR}"
                            
                            echo "📦 Cloning gh-pages branch..."
                            
                            # Clone or initialize gh-pages branch
                            if git clone -b gh-pages --single-branch "${GH_REPO}" "${DEPLOY_DIR}" 2>/dev/null; then
                                echo "✅ Cloned existing gh-pages branch"
                                cd "${DEPLOY_DIR}"
                            else
                                echo "ℹ️ Creating new gh-pages branch"
                                cd "${DEPLOY_DIR}"
                                git init
                                git config user.email "hasbiyallah.umutoniwabo@amalitechtraining.org"
                                git config user.name "Jenkins CI/CD"
                                git remote add origin "${GH_REPO}"
                                git checkout -b gh-pages 2>/dev/null || git checkout --orphan gh-pages
                            fi
                            
                            # Configure git
                            git config user.email "hasbiyallah.umutoniwabo@amalitechtraining.org"
                            git config user.name "Jenkins CI/CD"
                            
                            # Create build directory
                            mkdir -p "${BUILD_DIR}"
                            
                            # Copy Allure report
                            echo "📊 Copying Allure report..."
                            if [ -d "${WORKSPACE}/target/site/allure-report" ]; then
                                cp -r "${WORKSPACE}/target/site/allure-report"/* "${BUILD_DIR}/" 2>/dev/null || true
                                echo "✅ Report copied to build-${BUILD_NUMBER}"
                                ls -la "${BUILD_DIR}" | head -10
                            else
                                echo "⚠️ Allure report not found at ${WORKSPACE}/target/site/allure-report"
                                mkdir -p "${BUILD_DIR}"
                                echo "<html><body><h1>Build #${BUILD_NUMBER} Report</h1><p>Report generation in progress...</p></body></html>" > "${BUILD_DIR}/index.html"
                            fi
                            
                            # Commit and push
                            echo "🔄 Committing changes..."
                            git add -A
                            git commit -m "✅ Allure Report - Build #${BUILD_NUMBER}" || echo "ℹ️ No changes to commit"
                            
                            echo "📤 Pushing to GitHub Pages..."
                            git push -u origin gh-pages 2>&1 | tail -5
                            
                            # Cleanup
                            cd /tmp
                            rm -rf "${DEPLOY_DIR}"
                            
                            echo ""
                            echo "════════════════════════════════════════════════════════════════"
                            echo "✅ GitHub Pages Deployment Complete!"
                            echo "════════════════════════════════════════════════════════════════"
                            echo "📊 Report:    https://hasbyQa.github.io/jenkins-swag-labs/build-${BUILD_NUMBER}/"
                            echo "🏠 Dashboard: https://hasbyQa.github.io/jenkins-swag-labs/"
                            echo "════════════════════════════════════════════════════════════════"
                            '''
                        }
                    } catch (Exception e) {
                        echo "⚠️ Deployment error: ${e.message}"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                cleanWs(deleteDirs: true)
            }
        }
        
        success {
            script {
                echo "✅ Build Successful!"
                
                // Parse test results
                def passRate = 100
                def testInfo = "All tests passed successfully!"
                
                try {
                    def testResult = sh(
                        script: 'find target/surefire-reports -name "TEST-*.xml" -type f 2>/dev/null | wc -l',
                        returnStdout: true
                    ).trim()
                    
                    if (testResult.toInteger() > 0) {
                        testInfo = "✅ Tests completed successfully"
                    }
                } catch (Exception e) {
                    echo "Info: Could not parse test count"
                }
                
                // Slack notification
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
                        sh '''
                        curl -X POST "${SLACK_URL}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins",
                            "icon_emoji": ":jenkins:",
                            "attachments": [
                              {
                                "color": "#36a64f",
                                "title": "✅ BUILD PASSED",
                                "title_link": "https://hasbyQa.github.io/jenkins-swag-labs/build-'"${BUILD_NUMBER}"'/",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "✅ SUCCESS", "short": true}
                                ],
                                "actions": [
                                  {"type": "button", "text": "View Allure Report", "url": "https://hasbyQa.github.io/jenkins-swag-labs/build-'"${BUILD_NUMBER}"'/", "style": "primary"},
                                  {"type": "button", "text": "Jenkins Build", "url": "'"${BUILD_URL}"'"}
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "✅ Slack notification sent"
                } catch (Exception e) {
                    echo "⚠️ Slack notification failed: ${e.message}"
                }
                
                // Email notification
                try {
                    emailext(
                        subject: "✅ BUILD PASSED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """BUILD SUCCESSFULLY COMPLETED ✅

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${env.JOB_NAME}
  • Build Number: #${env.BUILD_NUMBER}
  • Status: ✅ SUCCESS
  • Branch: ${env.GIT_BRANCH}
  • Commit: ${env.GIT_COMMIT}

═══════════════════════════════════════════════════════════

TEST RESULTS:
  ${testInfo}
  
DETAILED REPORTS (Hosted on GitHub Pages):
  📊 Allure Test Report: https://hasbyQa.github.io/jenkins-swag-labs/build-${env.BUILD_NUMBER}/
  📈 JUnit Results: ${env.BUILD_URL}testReport/
  🔍 Console Output: ${env.BUILD_URL}console/

═══════════════════════════════════════════════════════════

BUILD ARTIFACTS:
  • Test Results: Available in Jenkins UI
  • Detailed Test Logs: Check Allure Report on GitHub Pages
  • Build Duration: ${currentBuild.durationString}

═══════════════════════════════════════════════════════════

For more details, visit: ${env.BUILD_URL}

Thank you!""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo "✅ Email notification sent"
                } catch (Exception e) {
                    echo "⚠️ Email failed: ${e.message}"
                }
            }
        }
        
        failure {
            script {
                echo "❌ Build Failed!"
                
                // Slack notification
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
                        sh '''
                        curl -X POST "${SLACK_URL}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins",
                            "icon_emoji": ":jenkins:",
                            "attachments": [
                              {
                                "color": "#ff0000",
                                "title": "❌ BUILD FAILED",
                                "title_link": "'"${BUILD_URL}"'console/",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "❌ FAILURE", "short": true}
                                ],
                                "actions": [
                                  {"type": "button", "text": "View Console", "url": "'"${BUILD_URL}"'console/", "style": "danger"},
                                  {"type": "button", "text": "Jenkins Build", "url": "'"${BUILD_URL}"'"}
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "✅ Slack notification sent"
                } catch (Exception e) {
                    echo "⚠️ Slack notification failed: ${e.message}"
                }
                
                // Email notification
                try {
                    emailext(
                        subject: "❌ BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """BUILD FAILED ❌

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${env.JOB_NAME}
  • Build Number: #${env.BUILD_NUMBER}
  • Status: ❌ FAILURE
  • Branch: ${env.GIT_BRANCH}
  • Commit: ${env.GIT_COMMIT}

═══════════════════════════════════════════════════════════

FAILURE ANALYSIS:
  1. Review the Console Output for error messages
  2. Check test logs for debugging information
  3. Verify dependencies and build configuration

═══════════════════════════════════════════════════════════

ACTIONS:
  • View Console: ${env.BUILD_URL}console/
  • Check Tests: ${env.BUILD_URL}testReport/
  • Build Details: ${env.BUILD_URL}

═══════════════════════════════════════════════════════════

For more details, visit: ${env.BUILD_URL}

Need help? Review the console output for error details!""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo "✅ Email notification sent"
                } catch (Exception e) {
                    echo "⚠️ Email failed: ${e.message}"
                }
            }
        }
    }
}
