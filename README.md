# Fake Store API Test Automation with Jenkins CI/CD

A hands-on project for QA engineers to practice setting up a Jenkins pipeline that automatically runs REST API tests on every code push. Tests target the [Fake Store API](https://fakestoreapi.com/) using REST Assured and JUnit 5, with Allure reports and Slack/email notifications.

---

## What this project covers

- REST API testing with REST Assured
- JUnit 5 test framework
- Allure test reports integrated with Jenkins
- Jenkins pipeline (declarative Jenkinsfile)
- GitHub webhook to trigger builds on push
- Slack and email notifications

---

## Project structure

```
src/test/java/com/fakestoreapi/
    tests/
        BaseTest.java       # Shared REST Assured setup for all tests
        ProductsTest.java   # Tests for /products endpoints
        CartsTest.java      # Tests for /carts endpoints
        UsersTest.java      # Tests for /users endpoints
        AuthTest.java       # Tests for /auth/login endpoint
    utils/
        ApiConfig.java      # API base URL and test credentials
Jenkinsfile                 # CI/CD pipeline definition
Dockerfile                  # Runs tests in a Docker container
```

---

## Test coverage

| Class | Endpoints tested |
|---|---|
| ProductsTest | GET all, GET by id, GET with limit/sort, GET categories, GET by category, POST, PUT, DELETE |
| CartsTest | GET all, GET by id, GET with limit, POST, PUT, DELETE |
| UsersTest | GET all, GET by id, GET with limit, POST, PUT, DELETE |
| AuthTest | POST login (valid credentials), POST login (invalid credentials) |

---

## Running tests locally

**Prerequisites:** Java 17 and Maven installed.

```bash
# Clone the repo
git clone https://github.com/hasbyQa/jenkins-swag-labs.git
cd jenkins-swag-labs

# Run all tests
mvn test

# View the Allure report
mvn allure:serve
```

---

## Running with Docker

```bash
# Build the image
docker build -t fakestore-api-tests .

# Run the tests
docker run --rm fakestore-api-tests
```

---

## Jenkins setup

1. Install Jenkins (locally or via Docker: `jenkins/jenkins:lts`)
2. Install plugins: Git, Pipeline, JUnit, Allure, HTML Publisher, Email Extension
3. Configure **Manage Jenkins → Tools → Allure Commandline** (name: `allure`, auto-install)
4. Configure **Extended E-mail Notification** SMTP settings
5. Add a **Secret Text** credential with id `slack-webhook-url` containing your Slack webhook URL
6. Create a Pipeline job pointing to this repository
7. Add a GitHub webhook: `http://<jenkins-url>/github-webhook/` → triggers on push

---

## Jenkins pipeline stages

| Stage | What it does |
|---|---|
| Checkout | Pulls latest code from GitHub |
| Build | Compiles the project (`mvn compile`) |
| Test | Runs the full API test suite (`mvn test`) |
| Reports | Publishes JUnit + Allure reports, captures test counts |

---

## Notifications

- **Slack** — sent to `#builds` channel on success, failure, and unstable
- **Email** — sent to the configured address on success, failure, and unstable
- Both include test counts (total, passed, failed, pass rate) and a direct link to the Allure report
