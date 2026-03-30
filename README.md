# 🚀 Swag Labs Test Automation with Jenkins CI/CD

A complete hands-on learning project for QA engineers to understand and implement CI/CD pipelines using Jenkins, Docker, and GitHub webhooks. This project automates testing of the Swag Labs (SauceDemo) web application using Selenium WebDriver and JUnit 5.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Objectives](#objectives)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Component Details](#component-details)
- [Jenkins Setup](#jenkins-setup)
- [GitHub Integration](#github-integration)
- [Pipeline Details](#pipeline-details)
- [Test Reports](#test-reports)
- [Troubleshooting](#troubleshooting)
- [Learning Resources](#learning-resources)

---

## 📖 Project Overview

This project demonstrates a complete **CI/CD pipeline** that:

1. **Monitors** a GitHub repository for code changes
2. **Automatically triggers** Jenkins when code is pushed
3. **Compiles** the project using Maven
4. **Executes** the test suite (UI tests with Selenium + API tests)
5. **Publishes** test reports and metrics
6. **Notifies** the team of results via Slack/Email (optional)

Perfect for learning:
- ✅ How Jenkins pipelines work
- ✅ Declarative pipeline syntax
- ✅ Docker containerization
- ✅ GitHub webhook integration
- ✅ Test automation best practices
- ✅ CI/CD workflow automation

---

## 🎯 Objectives

1. **Understand CI/CD** - Learn how continuous integration supports test automation
2. **Create Jenkins Jobs** - Build and configure Jenkins pipelines
3. **Automate Workflows** - Code pull → Build → Test → Report
4. **Integrate Notifications** - Slack/Email alerts on build status
5. **Hands-On Practice** - Real-world DevOps experience

---

## ⚙️ Prerequisites

- **Docker** and **Docker Compose** (latest version)
- **Git** installed locally
- **GitHub** account with a repository
- **Java 17+** (optional, if running tests locally)
- **Maven 3.6+** (optional, if building locally)
- **ngrok account** (free) - for webhook tunneling to local Jenkins

### Install Docker

**macOS/Windows:**
Download [Docker Desktop](https://www.docker.com/products/docker-desktop)

**Linux (Ubuntu/Debian):**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

### Install Git
```bash
# macOS
brew install git

# Ubuntu/Debian
sudo apt-get install git

# Windows
# Download from https://git-scm.com/download/win
```

---

## 🚀 Quick Start

### 1. Clone or Set Up Your Repository

```bash
# If you don't have a repo yet, create one on GitHub first
git clone <your-github-repo-url>
cd swag-labs-docker-tests-main
```

### 2. Make the Quickstart Script Executable

```bash
chmod +x quickstart.sh
```

### 3. Run the Quickstart Script

**Option A: Jenkins only (local access)**
```bash
./quickstart.sh
```

**Option B: Jenkins + ngrok (public access for webhooks)**
```bash
./quickstart.sh --with-ngrok --ngrok-token <your-ngrok-token>
```

> Get your free ngrok token at [https://ngrok.com](https://ngrok.com)

The script will:
- ✅ Check Docker installation
- ✅ Start Jenkins container
- ✅ Wait for Jenkins to be ready
- ✅ Display the initial admin password
- ✅ Show setup instructions

### 4. Complete Jenkins Setup in Browser

After running the quickstart script:

1. Open `http://localhost:8080`
2. Paste the initial admin password (from script output)
3. Click "Install suggested plugins" (or custom select):
   - Pipeline
   - Git
   - GitHub
   - HTML Publisher
   - JUnit
   - Email Extension
   - Slack Notification (optional)
4. Create your admin user account
5. Instance configuration → Save and Continue

---

## 📁 Project Structure

```
swag-labs-docker-tests-main/
├── docker-compose.yml          # Docker setup for Jenkins + ngrok
├── Dockerfile                  # Container image for running tests
├── Jenkinsfile                 # Pipeline-as-Code (declarative)
├── pom.xml                     # Maven project configuration
├── quickstart.sh              # Automated setup script
├── README.md                  # This file
│
└── src/test/java/com/swaglabs/
    ├── pages/                 # Page Object Model
    │   ├── BasePage.java
    │   ├── LoginPage.java
    │   ├── CartPage.java
    │   ├── CheckoutPage.java
    │   └── InventoryPage.java
    │
    ├── tests/                 # Test Classes
    │   ├── BaseTest.java
    │   ├── LoginTest.java
    │   ├── CartTest.java
    │   ├── CheckoutTest.java
    │   └── ApiTest.java
    │
    └── utils/                 # Utilities
        └── TestConfig.java
```

---

## 🔧 Component Details

### Jenkinsfile (Pipeline-as-Code)

The `Jenkinsfile` is the heart of your CI/CD automation. It's a declarative pipeline that defines:

**Stages:**
1. **Checkout Code** - Pull latest from Git
2. **Build** - Compile project with Maven
3. **Download Dependencies** - Pre-download Maven artifacts
4. **Run Tests** - Execute test suite with Maven Surefire
5. **Generate HTML Reports** - Create readable test reports
6. **Archive Reports** - Store reports in Jenkins

**Triggers:**
- **Poll SCM** - Check for changes every 15 minutes
- **GitHub Push** - Trigger on every push (requires webhook)

**Post Actions:**
- **Success** - Log successful completion (Slack notification ready)
- **Failure** - Log failure details (Slack notification ready)
- **Always** - Clean up workspace

### Docker Compose Configuration

The `docker-compose.yml` sets up:

**Jenkins Service:**
- Image: `jenkins/jenkins:lts` (Long-Term Support)
- Port: `8080` (web UI) + `50000` (agent communication)
- Volumes:
  - `jenkins_home` - Persist jobs and configurations
  - `/var/run/docker.sock` - Allow Docker-in-Docker
  - `maven_cache` - Speed up builds by caching dependencies

**Ngrok Service (Optional):**
- Exposes local Jenkins to the internet
- Enables GitHub webhooks from cloud
- Access dashboard at `http://localhost:4040`

### Dockerfile

Containerizes your test suite with:
- Maven 3.9.6 + Java 17
- Google Chrome + ChromeDriver
- Headless mode for CI environments
- 30-second Selenium timeout for CI delays

Run tests in Docker:
```bash
docker build -t swag-labs-tests .
docker run swag-labs-tests
```

---

## 🔐 Jenkins Setup

### Step 1: Access Jenkins Web UI

```
http://localhost:8080
```

### Step 2: Install Required Plugins

After initial setup, install these plugins:
- **Pipeline** - Declarative pipeline support
- **Git** - Git repository integration
- **GitHub** - GitHub-specific features
- **HTML Publisher** - Publish HTML test reports
- **JUnit** - JUnit test result parsing
- **Email Extension** - Email notifications
- **Slack** (optional) - Slack notifications

**To install plugins:**
1. Go to Manage Jenkins → Plugin Manager
2. Search for plugin name
3. Click "Install without restart"
4. Restart Jenkins when done

### Step 3: Create Pipeline Job

1. Click **New Item**
2. Enter name: `Swag Labs Tests`
3. Select **Pipeline**
4. Click **OK**

### Step 4: Configure Pipeline

1. Under **Definition**, select **Pipeline script from SCM**
2. SCM: Select **Git**
3. Repository URL: `https://github.com/<your-username>/<your-repo>.git`
4. Script Path: `Jenkinsfile`
5. Build Triggers: ✅ Check **GitHub hook trigger for GITScm polling**
6. Click **Save**

### Step 5: Set GitHub Credentials (if private repo)

1. Go to **Manage Jenkins** → **Manage Credentials**
2. Select **System** → **Global credentials**
3. Click **Add Credentials**
4. Kind: **GitHub App** or **Username with password**
5. Enter your GitHub credentials
6. Click **OK**

---

## 🔗 GitHub Integration

### Enable Webhooks for Automatic Triggering

**Without ngrok (for public servers):**

1. Go to your GitHub repository
2. Settings → Webhooks → Add webhook
3. **Payload URL:** `http://<your-server-ip>:8080/github-webhook/`
4. **Content type:** `application/json`
5. **Events:** Select **Push events** (or "Just the push event")
6. Click **Add webhook**

**With ngrok (for local development):**

The quickstart script will display your ngrok public URL. Use:
```
https://<ngrok-url>.ngrok.io/github-webhook/
```

The ngrok URL changes every time you restart, so you'll need to update the webhook or use ngrok's paid plan for a static URL.

### Manual Trigger

You can manually trigger builds in Jenkins:
1. Go to job page
2. Click **Build Now**
3. Check build logs in **Console Output**

---

## 📊 Pipeline Details

### Declarative Pipeline Structure

```groovy
pipeline {
    agent any                    # Run on any available Jenkins agent
    
    options { ... }              # Build behavior options
    
    parameters { ... }           # Parameters users can set
    
    environment { ... }          # Environment variables
    
    triggers { ... }             # What triggers the pipeline
    
    stages {
        stage('...') { ... }     # Each stage is a logical step
    }
    
    post {
        always { ... }           # Run regardless of result
        success { ... }          # Run if all stages succeed
        failure { ... }          # Run if any stage fails
    }
}
```

### Environment Variables

Set in `Jenkinsfile` for use in any stage:

```groovy
environment {
    JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
    MAVEN_HOME = '/usr/share/maven'
    TEST_REPORT_DIR = 'target/surefire-reports'
}
```

Available for reference in stages:
- `${BUILD_NUMBER}` - Build number
- `${GIT_BRANCH}` - Branch name
- `${GIT_COMMIT}` - Commit SHA
- `${BUILD_URL}` - Jenkins build URL

### Shell Script in Jenkinsfile

Use `sh '...'` to run shell commands:

```groovy
stage('Run Tests') {
    steps {
        sh '''
            mvn test -B \
                -Dmaven.surefire.timeout=600
        '''
    }
}
```

---

## 📈 Test Reports

### JUnit Results

Jenkins automatically parses `target/surefire-reports/**/*.xml` files. View at:
- Job page → **Test Result** (shows pass/fail summary)
- Job page → **Trend** (graph of pass rate over time)

### HTML Reports

Custom HTML reports are published as artifacts. After a build:
1. Go to build page
2. Click **Artifacts** → `target/surefire-reports-html/index.html`
3. View formatted test report

### Artifact Archiving

Reports are saved for viewing later:
```groovy
archiveArtifacts artifacts: "${HTML_REPORT_DIR}/**",
                  allowEmptyArchive: true
```

---

## 🔔 Notifications (Optional)

### Slack Integration

Uncomment Slack notifications in `Jenkinsfile`:

1. Install Slack plugin in Jenkins
2. In Jenkins, go to **Manage Jenkins** → **System** → **Slack**
3. Enter Slack workspace and token
4. Uncomment in `post` section:

```groovy
success {
    slackSend(
        color: 'good',
        message: "✅ Tests PASSED\n" +
                "Build: ${BUILD_NUMBER}\n" +
                "Branch: ${GIT_BRANCH}\n" +
                "URL: ${BUILD_URL}"
    )
}
```

### Email Notifications

Uncomment email notification:

```groovy
failure {
    emailext(
        subject: "❌ Build Failed: ${BUILD_NUMBER}",
        body: "Build ${BUILD_NUMBER} failed on ${GIT_BRANCH}\n" +
              "Check: ${BUILD_URL}",
        to: "team@example.com"
    )
}
```

---

## 🛠️ Docker Commands

### View Jenkins Logs

```bash
docker-compose logs -f jenkins
```

### Stop Jenkins

```bash
docker-compose down
```

### Remove All Data (Clean Start)

```bash
docker-compose down -v
```

### Check Container Status

```bash
docker-compose ps
```

### Restart Jenkins

```bash
docker-compose restart jenkins
```

### Access Jenkins Container

```bash
docker exec -it jenkins-ci bash
```

---

## 🚨 Troubleshooting

### Jenkins Won't Start

**Symptom:** `docker-compose up` fails or Jenkins port 8080 is already in use

**Solution:**
```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use different port in docker-compose.yml:
# ports:
#   - "8081:8080"  # Use 8081 instead
```

### Git Cloning Fails in Pipeline

**Symptom:** Build fails at "Checkout Code" stage

**Solution:**
- Add SSH key to Jenkins credentials, OR
- Use HTTPS URL with GitHub token, OR
- Make sure repository is public

### Tests Timeout in Pipeline

**Symptom:** `maven.surefire.timeout exceeded`

**Solution:**
Increase timeout in `Jenkinsfile`:
```groovy
-Dmaven.surefire.timeout=1200  # 20 minutes instead of 10
```

### Port Already in Use

**Symptom:** `Error bind address already in use`

**Solution:**
```bash
# Linux/Mac
lsof -i :8080

# Windows
netstat -ano | findstr :8080

# Free the port or use different port in docker-compose.yml
```

### Ngrok Connection Issues

**Symptom:** Ngrok tunnel isn't working

**Solution:**
```bash
# Check ngrok container logs
docker-compose logs ngrok

# Verify ngrok token is correct
# Restart ngrok
docker-compose restart ngrok

# Check ngrok dashboard at http://localhost:4040
```

### Permission Denied for quickstart.sh

**Solution:**
```bash
chmod +x quickstart.sh
```

---

## 📚 Learning Resources

### Jenkins Documentation
- [Jenkins Official Guide](https://www.jenkins.io/doc/)
- [Declarative Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Blue Ocean UI](https://www.jenkins.io/doc/book/blueocean/)

### Docker & Containerization
- [Docker Official Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/)
- [Best Practices for Writing Dockerfiles](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)

### CI/CD Concepts
- [CI/CD Pipeline Basics](https://www.atlassian.com/continuous-delivery/pipeline/)
- [GitHub Actions vs Jenkins](https://github.blog/2021-05-13-jenkins-vs-github-actions/)

### Test Automation
- [Selenium WebDriver Documentation](https://www.selenium.dev/documentation/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Page Object Model Pattern](https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/)

### Webhooks
- [GitHub Webhooks Guide](https://docs.github.com/en/developers/webhooks-and-events/webhooks/)
- [Ngrok Documentation](https://ngrok.com/docs/)

---

## 📝 Example Workflow

### 1. Make Code Changes Locally

```bash
git clone <your-repo>
cd swag-labs-docker-tests-main

# Make changes to test files
nano src/test/java/com/swaglabs/tests/LoginTest.java

# Commit and push
git add .
git commit -m "Add new login validation test"
git push origin main
```

### 2. GitHub Webhook Triggers Jenkins

Jenkins automatically receives the webhook and:
- Pulls your latest code
- Compiles the project
- Runs all tests
- Publishes reports

### 3. View Results in Jenkins

1. Go to `http://localhost:8080`
2. Click your job name
3. View the build in progress or completed
4. Check **Console Output** for logs
5. Check **Test Results** for pass/fail details

### 4. Share Results

- Share Jenkins URL with team
- Slack/Email notifications sent automatically
- Review artifacts and reports

---

## 🎓 Practice Exercises

### Exercise 1: Modify the Pipeline
Add a new stage to your `Jenkinsfile`:
```groovy
stage('Code Quality') {
    steps {
        sh 'mvn verify -DskipTests'
    }
}
```

### Exercise 2: Add Slack Notifications
1. Create a Slack workspace and channel
2. Generate a webhook URL
3. Uncomment Slack notifications in `Jenkinsfile`
4. Push changes and trigger a build

### Exercise 3: Parameterize the Pipeline
Add parameters to run tests with different browsers:
```groovy
parameters {
    choice(name: 'BROWSER', choices: ['chrome', 'firefox'])
}
```

### Exercise 4: Scheduled Builds
Modify triggers to run tests on a schedule:
```groovy
triggers {
    cron('0 2 * * *')  # Run at 2 AM daily
}
```

---

## 📞 Support

For issues or questions:
1. Check **Troubleshooting** section above
2. Review Jenkins logs: `docker-compose logs jenkins`
3. Check Jenkinsfile syntax: Use Jenkins **Declarative Directive Generator**
4. Review GitHub webhook delivery logs

---

## 📄 License

This project is provided for educational purposes.

---

## 🙏 Acknowledgments

Built with:
- **Jenkins** - Leading open-source CI/CD platform
- **Docker** - Container orchestration
- **Maven** - Build automation
- **Selenium** - Browser automation
- **JUnit 5** - Testing framework
- **GitHub** - Version control

---

**Happy CI/CD learning! 🚀**
