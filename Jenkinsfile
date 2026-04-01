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
                    alwaysLinkToLastBuild: true
                ])
                
                // Deploy Allure reports to GitHub Pages
                script {
                    try {
                        sh '''
                        # Set git config for deployment
                        git config --global user.email "hasbiyallah.umutoniwabo@amalitechtraining.org"
                        git config --global user.name "Jenkins CI/CD"
                        
                        # Create gh-pages directory
                        mkdir -p /tmp/gh-pages
                        cd /tmp/gh-pages
                        
                        # Clone or initialize gh-pages branch
                        if [ -d ".git" ]; then
                            git pull origin gh-pages 2>/dev/null || true
                        else
                            git clone -b gh-pages --single-branch https://github.com/hasbyQa/jenkins-swag-labs.git . 2>/dev/null || {
                                git init
                                git remote add origin https://github.com/hasbyQa/jenkins-swag-labs.git
                            }
                        fi
                        
                        # Create build directory with build number
                        BUILD_DIR="build-${BUILD_NUMBER}"
                        mkdir -p "$BUILD_DIR"
                        
                        # Copy Allure report to gh-pages
                        cp -r $WORKSPACE/target/site/allure-report/* "$BUILD_DIR/"
                        
                        # Create index.html pointing to latest build
                        cat > index.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Jenkins Allure Reports</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        h1 { color: #333; }
        .report-link { display: block; padding: 10px; margin: 10px 0; background: #007bff; color: white; text-decoration: none; border-radius: 4px; text-align: center; }
        .report-link:hover { background: #0056b3; }
        .latest { background: #28a745; }
        .latest:hover { background: #20c997; }
        .builds-list { margin-top: 30px; }
        .build-item { padding: 10px; margin: 5px 0; background: #f9f9f9; border-left: 4px solid #007bff; }
        .build-item a { color: #007bff; text-decoration: none; }
        .build-item a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🧪 Jenkins CI/CD - Allure Test Reports</h1>
        <p>Automated test reports for Swag Labs project</p>
        
        <h2>Latest Build Report</h2>
        <a href="build-${BUILD_NUMBER}/index.html" class="report-link latest">📊 View Latest Report (Build #${BUILD_NUMBER})</a>
        
        <div class="builds-list">
            <h2>All Builds</h2>
            <p>Access reports from previous builds:</p>
        </div>
    </div>
    
    <script>
        // Auto-update with latest builds (optional)
        console.log('Reports hosted on GitHub Pages');
    </script>
</body>
</html>
EOF
                        
                        # Commit and push to gh-pages
                        git add -A
                        git commit -m "Update Allure report - Build #${BUILD_NUMBER}" || echo "No changes to commit"
                        git push -u origin gh-pages 2>&1 || {
                            echo "Push failed - trying with token auth"
                            # If push fails, report it but don't fail the build
                            echo "Note: GitHub Pages deployment requires valid credentials"
                        }
                        
                        echo "✅ Reports available at: https://hasbyQa.github.io/jenkins-swag-labs/build-${BUILD_NUMBER}/"
                        '''
                    } catch (Exception e) {
                        echo "⚠️ GitHub Pages deployment info: ${e.message}"
                    }
                }
                
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
  
DETAILED REPORTS (Hosted on GitHub Pages):
  📊 Allure Test Report: https://hasbyQa.github.io/jenkins-swag-labs/build-${BUILD_NUMBER}/
  📈 JUnit Results: ${BUILD_URL}testReport/
  🔍 Console Output: ${BUILD_URL}console/

═══════════════════════════════════════════════════════════

BUILD ARTIFACTS:
  • Test Results: Available in Jenkins UI
  • Detailed Test Logs: Check Allure Report on GitHub Pages
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
                                "title_link": "https://hasbyQa.github.io/jenkins-swag-labs/build-'"${BUILD_NUMBER}"'/",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Reports", "value": "<https://hasbyQa.github.io/jenkins-swag-labs/build-'"${BUILD_NUMBER}"'/|📊 Allure>", "short": true}
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
  
DETAILED REPORTS & LOGS (Hosted on GitHub Pages):
  📊 Allure Test Report: https://hasbyQa.github.io/jenkins-swag-labs/build-${BUILD_NUMBER}/
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

Need help? Check the detailed test report on GitHub Pages!""",
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
                                "title_link": "https://hasbyQa.github.io/jenkins-swag-labs/build-'"${BUILD_NUMBER}"'/",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Reports", "value": "<https://hasbyQa.github.io/jenkins-swag-labs/build-'"${BUILD_NUMBER}"'/|📊 Allure>", "short": true}
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
