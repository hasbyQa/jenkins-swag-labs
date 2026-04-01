#!/usr/bin/env groovy

// ── Function to parse test results from XML files ──────────────────────────
def getTestSummary() {
    def summary = [total: 0, passed: 0, failed: 0, skipped: 0, failedTests: []]
    try {
        def surefiresDir = new File("${WORKSPACE}/target/surefire-reports")
        if (!surefiresDir.exists()) {
            echo "⚠️ Surefire reports directory not found"
            return summary
        }
        
        surefiresDir.listFiles().each { file ->
            if (file.name.startsWith('TEST-') && file.name.endsWith('.xml')) {
                def testSuite = new XmlSlurper().parse(file)
                summary.total += testSuite.@tests.toInteger() ?: 0
                summary.failed += testSuite.@failures.toInteger() ?: 0
                summary.skipped += testSuite.@skipped.toInteger() ?: 0
                
                // Extract failed test details
                testSuite.testcase.each { testCase ->
                    if (testCase.failure || testCase.error) {
                        def failMsg = testCase.failure?.text() ?: testCase.error?.text() ?: "Unknown error"
                        summary.failedTests << [
                            name: testCase.@name,
                            className: testCase.@classname,
                            message: failMsg.take(500)
                        ]
                    }
                }
            }
        }
        
        summary.passed = summary.total - summary.failed - summary.skipped
        echo "✅ Test Summary: Total=${summary.total} Passed=${summary.passed} Failed=${summary.failed} Skipped=${summary.skipped}"
    } catch (Exception e) {
        echo "⚠️ Could not parse test results: ${e.message}"
    }
    return summary
}

// ── Function to send Slack notifications ────────────────────────────────────
def sendSlackNotification(String color, String message, Map testSummary = [:]) {
    try {
        withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
            def payload = [
                channel: '#builds',
                username: 'Jenkins CI',
                icon_emoji: ':jenkins:',
                attachments: [[
                    color: color,
                    title: message,
                    fields: [
                        [title: 'Job', value: env.JOB_NAME, short: true],
                        [title: 'Build', value: "#${env.BUILD_NUMBER}", short: true],
                        [title: 'Branch', value: env.GIT_BRANCH ?: 'N/A', short: true],
                        [title: 'Status', value: message, short: true]
                    ] + (testSummary ? [
                        [title: 'Total Tests', value: testSummary.total?.toString() ?: 'N/A', short: true],
                        [title: 'Passed', value: testSummary.passed?.toString() ?: 'N/A', short: true],
                        [title: 'Failed', value: testSummary.failed?.toString() ?: 'N/A', short: true],
                        [title: 'Skipped', value: testSummary.skipped?.toString() ?: 'N/A', short: true]
                    ] : []),
                    actions: [
                        [type: 'button', text: 'View Report', url: "https://hasbyQa.github.io/jenkins-swag-labs/build-${env.BUILD_NUMBER}/", style: 'primary'],
                        [type: 'button', text: 'Build Log', url: env.BUILD_URL]
                    ]
                ]]
            ]
            
            def json = groovy.json.JsonOutput.toJson(payload)
            sh """
            curl -X POST '${SLACK_URL}' \
              -H 'Content-Type: application/json' \
              -d '${json}'
            """
            echo '✅ Slack notification sent'
        }
    } catch (Exception e) {
        echo "⚠️ Slack notification failed: ${e.message}"
    }
}

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
                    archiveArtifacts artifacts: 'target/**', allowEmptyArchive: true
                    
                    // Generate Allure reports
                    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                        sh 'mvn allure:report -B || true'
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
                }
            }
        }

        stage('Deploy to GitHub Pages') {
            when {
                expression { fileExists('target/site/allure-report/index.html') }
            }
            steps {
                script {
                    try {
                        withCredentials([string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN')]) {
                            sh '''
                            set -e
                            GH_REPO="https://${GITHUB_TOKEN}@github.com/hasbyQa/jenkins-swag-labs.git"
                            DEPLOY_DIR="/tmp/gh-pages-${BUILD_NUMBER}"
                            BUILD_DIR="build-${BUILD_NUMBER}"
                            
                            rm -rf "${DEPLOY_DIR}" 2>/dev/null || true
                            mkdir -p "${DEPLOY_DIR}"
                            
                            if git clone -b gh-pages --single-branch "${GH_REPO}" "${DEPLOY_DIR}" 2>/dev/null; then
                                echo "✅ Cloned existing gh-pages branch"
                            else
                                echo "ℹ️ Creating new gh-pages branch"
                                cd "${DEPLOY_DIR}"
                                git init
                                git config user.email "hasbiyallah.umutoniwabo@amalitechtraining.org"
                                git config user.name "Jenkins CI/CD"
                                git remote add origin "${GH_REPO}"
                                git checkout -b gh-pages 2>/dev/null || git checkout --orphan gh-pages
                            fi
                            
                            cd "${DEPLOY_DIR}"
                            mkdir -p "${BUILD_DIR}"
                            
                            if [ -d "${WORKSPACE}/target/site/allure-report" ]; then
                                cp -r "${WORKSPACE}/target/site/allure-report"/* "${BUILD_DIR}/" 2>/dev/null || true
                                echo "✅ Allure report copied"
                            fi
                            
                            # Create professional index.html for GitHub Pages
                            cat > index.html << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🧪 Test Reports - Jenkins CI/CD</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; padding: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        header { text-align: center; color: white; margin-bottom: 50px; }
        h1 { font-size: 3em; margin-bottom: 10px; text-shadow: 2px 2px 4px rgba(0,0,0,0.3); }
        .status { display: inline-block; background: #4CAF50; color: white; padding: 12px 24px; border-radius: 20px; margin-top: 15px; font-weight: bold; }
        .reports-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 30px; margin-bottom: 50px; }
        .card { background: white; border-radius: 12px; padding: 30px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
        .card h3 { color: #667eea; margin-bottom: 15px; font-size: 1.5em; }
        .card p { color: #666; line-height: 1.6; margin-bottom: 15px; }
        .btn { display: inline-block; background: #667eea; color: white; padding: 12px 30px; border-radius: 6px; text-decoration: none; font-weight: bold; }
        .btn:hover { background: #764ba2; }
        .builds { background: white; border-radius: 12px; padding: 40px; margin-bottom: 30px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); }
        .builds h2 { color: #333; margin-bottom: 30px; border-bottom: 3px solid #667eea; padding-bottom: 15px; }
        .build-item { padding: 20px; background: #f8f9fa; margin: 15px 0; border-left: 5px solid #667eea; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }
        .build-item h4 { color: #333; margin: 0; }
        .build-item p { color: #666; font-size: 0.9em; margin: 5px 0 0; }
        .build-item a { background: #667eea; color: white; padding: 10px 20px; border-radius: 4px; text-decoration: none; margin-left: 20px; }
        .badge { background: #4CAF50; color: white; padding: 4px 12px; border-radius: 20px; font-size: 11px; margin-left: 10px; font-weight: bold; }
        footer { text-align: center; color: white; padding: 20px; opacity: 0.8; }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>🧪 Swag Labs Test Reports</h1>
            <p>Jenkins CI/CD - Automated Test Results & Allure Reports</p>
            <div class="status">✅ Reports System Ready</div>
        </header>

        <div class="reports-grid">
            <div class="card">
                <h3>📊 Allure Reports</h3>
                <p>Detailed test execution reports with screenshots, logs, and metrics.</p>
                <a href="#builds" class="btn">View Reports</a>
            </div>
            <div class="card">
                <h3>🔄 CI/CD Pipeline</h3>
                <p>Automated build and test pipeline on every GitHub push.</p>
                <a href="https://github.com/hasbyQa/jenkins-swag-labs" class="btn">View Repo</a>
            </div>
            <div class="card">
                <h3>🎯 Test Coverage</h3>
                <p>Complete coverage for login, shopping cart, and checkout flows.</p>
                <a href="#builds" class="btn">See Details</a>
            </div>
        </div>

        <div class="builds" id="builds">
            <h2>📋 Build History</h2>
            <div id="build-list">
                <div style="text-align: center; padding: 40px; color: #999;">⏳ Loading builds...</div>
            </div>
        </div>

        <footer>
            <p>🔐 Secure • 🚀 Automated • 📊 Detailed</p>
            <p>Powered by Jenkins CI/CD & Allure Reports</p>
        </footer>
    </div>

    <script>
        async function loadBuilds() {
            const buildList = document.getElementById('build-list');
            try {
                const builds = [];
                for (let i = 1; i <= 200; i++) {
                    const response = await fetch('build-' + i + '/index.html', { method: 'HEAD', cache: 'no-store' });
                    if (response.ok) builds.push(i);
                }
                
                if (builds.length === 0) {
                    buildList.innerHTML = '<div style="text-align: center; padding: 40px; color: #999;">🚀 No builds yet. Trigger a Jenkins build to see reports.</div>';
                    return;
                }
                
                builds.sort((a, b) => b - a);
                
                let html = '<div style="margin-bottom: 20px; padding: 15px; background: #e3f2fd; border-radius: 6px;">';
                html += '<strong>📈 Build History: ' + builds.length + ' build' + (builds.length !== 1 ? 's' : '') + '</strong></div>';
                
                for (let i = 0; i < builds.length; i++) {
                    const buildNum = builds[i];
                    const isLatest = (buildNum === builds[0]);
                    html += '<div class="build-item">';
                    html += '<div><h4>Build #' + buildNum + (isLatest ? '<span class="badge">LATEST</span>' : '') + '</h4>';
                    html += '<p>📊 Allure Test Report</p></div>';
                    html += '<a href="build-' + buildNum + '/">View Report</a></div>';
                }
                buildList.innerHTML = html;
            } catch (e) {
                buildList.innerHTML = '<div style="text-align: center; padding: 40px; color: #999;">📊 Loading... refresh in a moment.</div>';
            }
        }
        
        document.addEventListener('DOMContentLoaded', loadBuilds);
        setInterval(loadBuilds, 30000);
    </script>
</body>
</html>
EOF

                            git config user.email "hasbiyallah.umutoniwabo@amalitechtraining.org"
                            git config user.name "Jenkins CI/CD"
                            git add -A
                            git commit -m "✅ Allure Report - Build #${BUILD_NUMBER}" || true
                            git push -u origin gh-pages 2>&1 | tail -5
                            
                            echo ""
                            echo "✅ GitHub Pages Deployment Successful!"
                            echo "📊 Build Report: https://hasbyQa.github.io/jenkins-swag-labs/build-${BUILD_NUMBER}/"
                            echo "🏠 Dashboard:    https://hasbyQa.github.io/jenkins-swag-labs/"
                            
                            rm -rf "${DEPLOY_DIR}"
                            '''
                        }
                    } catch (Exception e) {
                        echo "⚠️ Deployment warning: ${e.message}"
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs(deleteDirs: true)
        }
        
        success {
            script {
                echo "✅ Build Successful!"
                def testSummary = getTestSummary()
                sendSlackNotification('#36a64f', '✅ BUILD PASSED', testSummary)
                
                try {
                    def passRate = testSummary.total > 0 ? (int)((testSummary.passed / testSummary.total) * 100) : 0
                    def htmlBody = """
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
        .content { padding: 30px; }
        .success { background: #e8f5e9; border-left: 4px solid #4CAF50; padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .success h2 { color: #2e7d32; margin: 0; }
        .metrics { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; margin: 20px 0; }
        .metric { background: #f5f5f5; padding: 15px; border-radius: 4px; text-align: center; border-left: 4px solid #667eea; }
        .value { font-size: 28px; font-weight: bold; color: #333; }
        .label { font-size: 12px; color: #666; margin-top: 5px; }
        .button { display: inline-block; background: #667eea; color: white; padding: 12px 25px; text-decoration: none; border-radius: 4px; margin: 5px; font-weight: bold; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        td { padding: 10px; border-bottom: 1px solid #ddd; }
        tr:nth-child(even) { background: #f5f5f5; }
        footer { background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #666; border-top: 1px solid #ddd; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>✅ Build Passed</h1>
            <p>${env.JOB_NAME} #${env.BUILD_NUMBER}</p>
        </div>
        <div class="content">
            <div class="success">
                <h2>Build Completed Successfully!</h2>
                <p>All tests passed without errors.</p>
            </div>
            
            <h3>Test Results</h3>
            <div class="metrics">
                <div class="metric">
                    <div class="value">${testSummary.total}</div>
                    <div class="label">Total Tests</div>
                </div>
                <div class="metric">
                    <div class="value" style="color: #4CAF50;">${testSummary.passed}</div>
                    <div class="label">Passed</div>
                </div>
                <div class="metric">
                    <div class="value" style="color: #2196F3;">${testSummary.skipped}</div>
                    <div class="label">Skipped</div>
                </div>
                <div class="metric">
                    <div class="value" style="color: #FF9800;">${passRate}%</div>
                    <div class="label">Pass Rate</div>
                </div>
            </div>
            
            <h3>Build Details</h3>
            <table>
                <tr><td><strong>Job</strong></td><td>${env.JOB_NAME}</td></tr>
                <tr><td><strong>Build</strong></td><td>#${env.BUILD_NUMBER}</td></tr>
                <tr><td><strong>Branch</strong></td><td>${env.GIT_BRANCH ?: 'N/A'}</td></tr>
                <tr><td><strong>Duration</strong></td><td>${currentBuild.durationString}</td></tr>
            </table>
            
            <center>
                <a href="https://hasbyQa.github.io/jenkins-swag-labs/build-${env.BUILD_NUMBER}/" class="button">View Allure Report</a>
                <a href="${env.BUILD_URL}" class="button">View Build</a>
            </center>
        </div>
        <footer>
            <p>Automated notification from Jenkins CI/CD</p>
        </footer>
    </div>
</body>
</html>
"""
                    emailext(
                        subject: "✅ BUILD PASSED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: htmlBody,
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/html'
                    )
                    echo '✅ Email sent'
                } catch (Exception e) {
                    echo "⚠️ Email failed: ${e.message}"
                }
            }
        }
        
        failure {
            script {
                echo "❌ Build Failed!"
                def testSummary = getTestSummary()
                sendSlackNotification('#ff0000', '❌ BUILD FAILED', testSummary)
                
                try {
                    def passRate = testSummary.total > 0 ? (int)((testSummary.passed / testSummary.total) * 100) : 0
                    def failedTests = testSummary.failedTests.collect { test ->
                        """<div style="margin: 10px 0; padding: 10px; background: #ffebee; border-left: 4px solid #f44336; border-radius: 4px;">
    <strong style="color: #c62828;">${test.name}</strong>
    <p style="margin: 5px 0 0; color: #666; font-size: 12px;"><strong>Class:</strong> ${test.className}</p>
    <p style="margin: 5px 0 0; font-family: monospace; font-size: 11px; color: #666;">${test.message.take(200)}</p>
</div>"""
                    }.join()
                    
                    def htmlBody = """
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; overflow: hidden; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
        .content { padding: 30px; }
        .failure { background: #ffebee; border-left: 4px solid #f44336; padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .failure h2 { color: #c62828; margin: 0; }
        .metrics { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; margin: 20px 0; }
        .metric { background: #f5f5f5; padding: 15px; border-radius: 4px; text-align: center; border-left: 4px solid #667eea; }
        .value { font-size: 28px; font-weight: bold; color: #333; }
        .label { font-size: 12px; color: #666; margin-top: 5px; }
        .button { display: inline-block; background: #667eea; color: white; padding: 12px 25px; text-decoration: none; border-radius: 4px; margin: 5px; }
        footer { background: #f5f5f5; padding: 15px; text-align: center; font-size: 12px; color: #666; border-top: 1px solid #ddd; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>❌ Build Failed</h1>
            <p>${env.JOB_NAME} #${env.BUILD_NUMBER}</p>
        </div>
        <div class="content">
            <div class="failure">
                <h2>Build Encountered Errors</h2>
                <p>One or more tests failed. Review the details below.</p>
            </div>
            
            <h3>Test Results</h3>
            <div class="metrics">
                <div class="metric">
                    <div class="value">${testSummary.total}</div>
                    <div class="label">Total</div>
                </div>
                <div class="metric">
                    <div class="value" style="color: #4CAF50;">${testSummary.passed}</div>
                    <div class="label">Passed</div>
                </div>
                <div class="metric">
                    <div class="value" style="color: #f44336;">${testSummary.failed}</div>
                    <div class="label">Failed</div>
                </div>
                <div class="metric">
                    <div class="value" style="color: #FF9800;">${passRate}%</div>
                    <div class="label">Pass Rate</div>
                </div>
            </div>
            
            ${testSummary.failedTests ? '<h3>Failed Tests</h3>' + failedTests : ''}
            
            <center>
                <a href="https://hasbyQa.github.io/jenkins-swag-labs/build-${env.BUILD_NUMBER}/" class="button">View Allure Report</a>
                <a href="${env.BUILD_URL}console" class="button">View Console</a>
            </center>
        </div>
        <footer>
            <p>Automated notification from Jenkins CI/CD</p>
        </footer>
    </div>
</body>
</html>
"""
                    emailext(
                        subject: "❌ BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: htmlBody,
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/html'
                    )
                    echo '✅ Email sent'
                } catch (Exception e) {
                    echo "⚠️ Email failed: ${e.message}"
                }
            }
        }
    }
}
