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
                sh 'SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200'
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
                
                echo "✅ Allure Test Report generated and published in Jenkins!"
            }
        }
    }

    post {
        always {
            echo "🧹 Cleaning workspace..."
            cleanWs()
        }
        
        success {
            echo "✅ Build and all tests PASSED!"
            echo "📊 Allure Report available at: ${BUILD_URL}Allure_Test_Report/"
        }
        
        failure {
            echo "❌ Build or tests FAILED!"
            echo "📊 Check Allure Report at: ${BUILD_URL}Allure_Test_Report/ for details"
        }
    }
}
