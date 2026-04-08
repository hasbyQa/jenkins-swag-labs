#!/usr/bin/env groovy

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 15, unit: 'MINUTES')
    }

    triggers {
        githubPush()
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checking out code from GitHub..."
                checkout scm
                script {
                    // Capture the commit subject line for use in notifications
                    env.GIT_COMMIT_MSG = sh(
                        script: 'git log -1 --pretty=format:"%s"',
                        returnStdout: true
                    ).trim()
                }
            }
        }

        stage('Build') {
            steps {
                echo "Compiling project..."
                sh 'mvn clean compile -B -q'
                echo "Build completed successfully"
            }
        }

        stage('Test') {
            steps {
                echo "Running API test suite against fakestoreapi.com..."
                // catchError keeps the pipeline running so reports are always published
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh 'mvn test -B'
                }
            }
        }

        stage('Reports') {
            steps {
                echo "Publishing test reports..."

                script {
                    // ── JUnit counts ──────────────────────────────────────────
                    def results = junit testResults: 'target/surefire-reports/**/*.xml',
                                        allowEmptyResults: true
                    env.TOTAL_TESTS   = "${results.totalCount}"
                    env.FAILED_TESTS  = "${results.failCount}"
                    env.SKIPPED_TESTS = "${results.skipCount}"
                    env.PASSED_TESTS  = "${results.totalCount - results.failCount - results.skipCount}"
                    env.PASS_RATE     = results.totalCount > 0
                        ? "${(int)(((results.totalCount - results.failCount - results.skipCount) / results.totalCount) * 100)}"
                        : "0"
                    echo "Tests: total=${env.TOTAL_TESTS} passed=${env.PASSED_TESTS} failed=${env.FAILED_TESTS}"

                    // ── Parse surefire XMLs → failure details for notifications ──
                    def failRowsHtml   = new StringBuilder()
                    def failCardsHtml  = new StringBuilder()
                    def failNamesSlack = new StringBuilder()

                    findFiles(glob: 'target/surefire-reports/TEST-*.xml').each { fw ->
                        def suite = new XmlSlurper().parseText(readFile(fw.path))
                        suite.testcase.each { tc ->
                            def isFailure = tc.failure.size() > 0
                            def isError   = tc.error.size() > 0
                            if (!isFailure && !isError) return

                            def fn       = isFailure ? tc.failure[0] : tc.error[0]
                            def fullCls  = tc.'@classname'.toString()
                            def cls      = fullCls.tokenize('.').last()
                            def tname    = tc.'@name'.toString()
                            def msg      = fn.'@message'.toString()
                            def stack    = fn.text().toString().trim()
                            def bugType  = isError ? 'Error' : 'Assertion Failure'

                            def e = { String s -> s.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;') }
                            def msgEsc   = e(msg.take(300))
                            def stackEsc = e(stack.take(1500))

                            // -- Summary table row ----------------------------
                            failRowsHtml.append(
                                "<tr>" +
                                "<td style='padding:8px 12px;border-bottom:1px solid #fce4e4;font-weight:600;'>${cls}</td>" +
                                "<td style='padding:8px 12px;border-bottom:1px solid #fce4e4;font-family:monospace;font-size:12px;'>${tname}</td>" +
                                "<td style='padding:8px 12px;border-bottom:1px solid #fce4e4;color:#c62828;'>${msgEsc}</td>" +
                                "</tr>"
                            )

                            // -- Detailed bug report card ---------------------
                            failCardsHtml.append(
                                "<div style='background:#fff8f8;border-left:4px solid #e53935;border-radius:0 6px 6px 0;" +
                                "padding:18px 22px;margin-bottom:20px;font-size:13px;line-height:1.6;'>" +

                                "<div style='font-weight:bold;color:#b71c1c;font-size:14px;margin-bottom:14px;'>" +
                                "&#128030; Bug Report &mdash; ${cls} :: ${tname}</div>" +

                                "<table style='width:100%;border-collapse:collapse;'>" +

                                "<tr><td style='width:160px;color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Type</td>" +
                                "<td style='padding:5px 0;'>${bugType}</td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Test Class</td>" +
                                "<td style='padding:5px 0;font-family:monospace;font-size:12px;'>${fullCls}</td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Test Method</td>" +
                                "<td style='padding:5px 0;font-family:monospace;font-size:12px;'>${tname}</td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>API Under Test</td>" +
                                "<td style='padding:5px 0;'><a href='https://fakestoreapi.com'>https://fakestoreapi.com</a></td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Failure Message</td>" +
                                "<td style='padding:5px 0;color:#c62828;font-weight:500;'>${msgEsc}</td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Steps to Reproduce</td>" +
                                "<td style='padding:5px 0;'>" +
                                "1. Clone the repository<br>" +
                                "2. Ensure Java 17+ and Maven 3.8+ are installed<br>" +
                                "3. Run: <code style='background:#f0f0f0;padding:2px 6px;border-radius:3px;font-size:12px;'>" +
                                "mvn test -Dtest=${cls}#${tname} -B</code><br>" +
                                "4. Observe the assertion failure in the Maven Surefire output" +
                                "</td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Expected Behaviour</td>" +
                                "<td style='padding:5px 0;'>Test should pass — assertion should be satisfied by the API response</td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Actual Behaviour</td>" +
                                "<td style='padding:5px 0;color:#c62828;'>${msgEsc}</td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Stack Trace</td>" +
                                "<td style='padding:5px 0;'>" +
                                "<pre style='background:#1a1a2e;color:#e8e8f0;padding:12px 16px;border-radius:5px;" +
                                "font-size:11px;overflow:auto;margin:4px 0;white-space:pre-wrap;word-break:break-all;line-height:1.5;'>" +
                                "${stackEsc}</pre></td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>Allure Report</td>" +
                                "<td style='padding:5px 0;'><a href='${env.BUILD_URL}allure/'>${env.BUILD_URL}allure/</a></td></tr>" +

                                "<tr><td style='color:#888;padding:5px 0;vertical-align:top;font-weight:600;'>JUnit Results</td>" +
                                "<td style='padding:5px 0;'><a href='${env.BUILD_URL}testReport/'>${env.BUILD_URL}testReport/</a></td></tr>" +

                                "</table></div>"
                            )

                            // -- Slack failure list ---------------------------
                            failNamesSlack.append("• *${cls}* › `${tname}`\n   _${msg.take(100)}_\n")
                        }
                    }

                    env.FAIL_ROWS_HTML   = failRowsHtml.toString()
                    env.FAIL_CARDS_HTML  = failCardsHtml.toString()
                    env.FAIL_NAMES_SLACK = failNamesSlack.toString().trim()
                }

                allure([
                    commandline      : 'allure',
                    results          : [[path: 'target/allure-results']],
                    reportBuildPolicy: 'ALWAYS',
                    includeProperties: false
                ])

                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, target/allure-results/**',
                                  allowEmptyArchive: true

                echo "Reports published!"
            }
        }
    }

    post {
        // cleanup always runs last — workspace is wiped after all other post steps
        cleanup {
            echo "Cleaning workspace..."
            cleanWs()
        }

        // ══════════════════════════════════════════════════════════════════
        //  SUCCESS
        // ══════════════════════════════════════════════════════════════════
        success {
            script {
                def commitShort = (env.GIT_COMMIT ?: 'unknown').take(10)
                def commitMsg   = env.GIT_COMMIT_MSG ?: ''
                def branch      = env.GIT_BRANCH ?: 'unknown'
                def total       = env.TOTAL_TESTS  ?: '0'
                def passed      = env.PASSED_TESTS ?: '0'
                def failed      = env.FAILED_TESTS ?: '0'
                def passRate    = env.PASS_RATE    ?: '0'

                // ── Email ─────────────────────────────────────────────────
                try {
                    def emailBody = """<!DOCTYPE html><html><head><meta charset="UTF-8"><style>
body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
.card{background:#fff;border-radius:8px;max-width:640px;margin:auto;box-shadow:0 2px 8px rgba(0,0,0,.12);overflow:hidden}
.banner{background:#2e7d32;padding:24px 28px;color:#fff}
.banner h1{margin:0 0 4px;font-size:22px}
.banner p{margin:0;font-size:13px;opacity:.85}
.badge{display:inline-block;background:#a5d6a7;color:#1b5e20;border-radius:4px;padding:2px 10px;font-weight:bold;font-size:13px}
.body{padding:24px 28px}
table.info{width:100%;border-collapse:collapse;margin-bottom:20px}
table.info th{text-align:left;color:#555;font-size:11px;text-transform:uppercase;letter-spacing:.5px;padding:6px 0;border-bottom:1px solid #e0e0e0}
table.info td{padding:8px 0;font-size:14px;color:#333;border-bottom:1px solid #f0f0f0}
table.info td.lbl{color:#777;width:36%}
.stats{background:#f9fbe7;border-radius:6px;padding:16px 20px;margin-bottom:20px;text-align:center}
.stats-grid{display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:12px}
.sv{font-size:28px;font-weight:bold;color:#2e7d32}
.sl{font-size:11px;color:#777;text-transform:uppercase;margin-top:2px}
.buttons{text-align:center;margin-top:4px}
.btn{display:inline-block;padding:10px 20px;border-radius:5px;text-decoration:none;font-size:13px;font-weight:bold;margin:4px}
.bp{background:#2e7d32;color:#fff}
.bo{background:#fff;color:#2e7d32;border:1px solid #2e7d32}
.footer{text-align:center;font-size:11px;color:#aaa;padding:14px;border-top:1px solid #eee}
</style></head><body><div class="card">
<div class="banner"><h1>&#9989; Build Passed</h1>
<p>${env.JOB_NAME} &nbsp;|&nbsp; #${env.BUILD_NUMBER} &nbsp;|&nbsp; <span class="badge">SUCCESS</span></p></div>
<div class="body">
<table class="info"><tr><th colspan="2">Build Details</th></tr>
<tr><td class="lbl">Branch</td><td>${branch}</td></tr>
<tr><td class="lbl">Commit</td><td><code style="background:#f0f0f0;padding:2px 6px;border-radius:3px;">${commitShort}</code> &nbsp;${commitMsg}</td></tr>
<tr><td class="lbl">Build URL</td><td><a href="${env.BUILD_URL}">${env.BUILD_URL}</a></td></tr>
</table>
<div class="stats"><div class="stats-grid">
<div><div class="sv">${total}</div><div class="sl">Total</div></div>
<div><div class="sv">${passed}</div><div class="sl">Passed</div></div>
<div><div class="sv">${failed}</div><div class="sl">Failed</div></div>
<div><div class="sv">${passRate}%</div><div class="sl">Pass Rate</div></div>
</div></div>
<div class="buttons">
<a class="btn bp" href="${env.BUILD_URL}allure/">&#128202; Allure Report</a>
<a class="btn bo" href="${env.BUILD_URL}testReport/">&#9989; JUnit Results</a>
<a class="btn bo" href="${env.BUILD_URL}console/">&#128196; Console</a>
</div></div>
<div class="footer">Jenkins CI &bull; ${env.JOB_NAME}</div>
</div></body></html>"""

                    emailext(
                        subject: "\u2705 BUILD PASSED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: emailBody,
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        replyTo: '$DEFAULT_REPLYTO',
                        from: '$DEFAULT_FROM',
                        mimeType: 'text/html'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ─────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        def payload = groovy.json.JsonOutput.toJson([
                            username   : "Jenkins CI",
                            icon_emoji : ":white_check_mark:",
                            attachments: [[
                                color : "#2e7d32",
                                blocks: [
                                    [type: "header", text: [type: "plain_text", text: ":white_check_mark:  Build Passed \u2014 Fake Store API Tests", emoji: true]],
                                    [type: "section", fields: [
                                        [type: "mrkdwn", text: "*Job:*\n${env.JOB_NAME}"],
                                        [type: "mrkdwn", text: "*Build:*\n<${env.BUILD_URL}|#${env.BUILD_NUMBER}>"],
                                        [type: "mrkdwn", text: "*Branch:*\n`${branch}`"],
                                        [type: "mrkdwn", text: "*Status:*\n:large_green_circle:  SUCCESS"]
                                    ]],
                                    [type: "section", text: [type: "mrkdwn", text: "*Commit:* `${commitShort}` \u2014 ${commitMsg}"]],
                                    [type: "divider"],
                                    [type: "section", fields: [
                                        [type: "mrkdwn", text: "*Total Tests*\n${total}"],
                                        [type: "mrkdwn", text: "*Passed :white_check_mark:*\n${passed}"],
                                        [type: "mrkdwn", text: "*Failed :x:*\n${failed}"],
                                        [type: "mrkdwn", text: "*Pass Rate*\n${passRate}%"]
                                    ]],
                                    [type: "divider"],
                                    [type: "actions", elements: [
                                        [type: "button", text: [type: "plain_text", text: ":bar_chart: Allure Report", emoji: true], url: "${env.BUILD_URL}allure/", style: "primary"],
                                        [type: "button", text: [type: "plain_text", text: ":memo: Test Results",  emoji: true], url: "${env.BUILD_URL}testReport/"],
                                        [type: "button", text: [type: "plain_text", text: ":page_facing_up: Console", emoji: true], url: "${env.BUILD_URL}console/"]
                                    ]]
                                ]
                            ]]
                        ])
                        writeFile file: 'slack-payload.json', text: payload
                        sh 'curl -s -X POST "${SLACK_WEBHOOK}" -H \'Content-Type: application/json\' -d @slack-payload.json'
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }

        // ══════════════════════════════════════════════════════════════════
        //  FAILURE
        // ══════════════════════════════════════════════════════════════════
        failure {
            script {
                def commitShort  = (env.GIT_COMMIT ?: 'unknown').take(10)
                def commitMsg    = env.GIT_COMMIT_MSG ?: ''
                def branch       = env.GIT_BRANCH ?: 'unknown'
                def total        = env.TOTAL_TESTS  ?: '0'
                def passed       = env.PASSED_TESTS ?: '0'
                def failed       = env.FAILED_TESTS ?: '0'
                def passRate     = env.PASS_RATE    ?: '0'
                def failRows     = env.FAIL_ROWS_HTML  ?: ''
                def failCards    = env.FAIL_CARDS_HTML ?: ''
                def failSlack    = env.FAIL_NAMES_SLACK ?: '_No failure details available_'

                def failSection = failRows ? """
<h3 style="color:#b71c1c;margin:24px 0 12px;">Failed Tests Summary</h3>
<table style="width:100%;border-collapse:collapse;margin-bottom:20px;">
<thead><tr>
<th style="text-align:left;padding:8px 12px;background:#ffebee;color:#b71c1c;font-size:12px;">Class</th>
<th style="text-align:left;padding:8px 12px;background:#ffebee;color:#b71c1c;font-size:12px;">Test</th>
<th style="text-align:left;padding:8px 12px;background:#ffebee;color:#b71c1c;font-size:12px;">Failure</th>
</tr></thead><tbody>${failRows}</tbody></table>
<h3 style="color:#b71c1c;margin:24px 0 12px;">Detailed Bug Reports</h3>
${failCards}""" : ''

                // ── Email ─────────────────────────────────────────────────
                try {
                    def emailBody = """<!DOCTYPE html><html><head><meta charset="UTF-8"><style>
body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
.card{background:#fff;border-radius:8px;max-width:680px;margin:auto;box-shadow:0 2px 8px rgba(0,0,0,.12);overflow:hidden}
.banner{background:#b71c1c;padding:24px 28px;color:#fff}
.banner h1{margin:0 0 4px;font-size:22px}
.banner p{margin:0;font-size:13px;opacity:.85}
.badge{display:inline-block;background:#ef9a9a;color:#7f0000;border-radius:4px;padding:2px 10px;font-weight:bold;font-size:13px}
.body{padding:24px 28px}
table.info{width:100%;border-collapse:collapse;margin-bottom:20px}
table.info th{text-align:left;color:#555;font-size:11px;text-transform:uppercase;letter-spacing:.5px;padding:6px 0;border-bottom:1px solid #e0e0e0}
table.info td{padding:8px 0;font-size:14px;color:#333;border-bottom:1px solid #f0f0f0}
table.info td.lbl{color:#777;width:36%}
.stats{background:#ffebee;border-radius:6px;padding:16px 20px;margin-bottom:20px;text-align:center}
.stats-grid{display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:12px}
.sv{font-size:28px;font-weight:bold;color:#b71c1c}
.sp{color:#2e7d32}
.sl{font-size:11px;color:#777;text-transform:uppercase;margin-top:2px}
.buttons{text-align:center;margin-top:4px}
.btn{display:inline-block;padding:10px 20px;border-radius:5px;text-decoration:none;font-size:13px;font-weight:bold;margin:4px}
.bp{background:#b71c1c;color:#fff}
.bo{background:#fff;color:#b71c1c;border:1px solid #b71c1c}
.footer{text-align:center;font-size:11px;color:#aaa;padding:14px;border-top:1px solid #eee}
</style></head><body><div class="card">
<div class="banner"><h1>&#10060; Build Failed</h1>
<p>${env.JOB_NAME} &nbsp;|&nbsp; #${env.BUILD_NUMBER} &nbsp;|&nbsp; <span class="badge">FAILURE</span></p></div>
<div class="body">
<table class="info"><tr><th colspan="2">Build Details</th></tr>
<tr><td class="lbl">Branch</td><td>${branch}</td></tr>
<tr><td class="lbl">Commit</td><td><code style="background:#f0f0f0;padding:2px 6px;border-radius:3px;">${commitShort}</code> &nbsp;${commitMsg}</td></tr>
<tr><td class="lbl">Build URL</td><td><a href="${env.BUILD_URL}">${env.BUILD_URL}</a></td></tr>
</table>
<div class="stats"><div class="stats-grid">
<div><div class="sv">${total}</div><div class="sl">Total</div></div>
<div><div class="sv sp">${passed}</div><div class="sl">Passed</div></div>
<div><div class="sv">${failed}</div><div class="sl">Failed</div></div>
<div><div class="sv">${passRate}%</div><div class="sl">Pass Rate</div></div>
</div></div>
<div class="buttons">
<a class="btn bp" href="${env.BUILD_URL}allure/">&#128202; Allure Report</a>
<a class="btn bo" href="${env.BUILD_URL}testReport/">&#10060; JUnit Results</a>
<a class="btn bo" href="${env.BUILD_URL}console/">&#128196; Console</a>
</div>
${failSection}
</div>
<div class="footer">Jenkins CI &bull; ${env.JOB_NAME}</div>
</div></body></html>"""

                    emailext(
                        subject: "\u274C BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: emailBody,
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        replyTo: '$DEFAULT_REPLYTO',
                        from: '$DEFAULT_FROM',
                        mimeType: 'text/html'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ─────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        def blocks = [
                            [type: "header", text: [type: "plain_text", text: ":x:  Build Failed \u2014 Fake Store API Tests", emoji: true]],
                            [type: "section", fields: [
                                [type: "mrkdwn", text: "*Job:*\n${env.JOB_NAME}"],
                                [type: "mrkdwn", text: "*Build:*\n<${env.BUILD_URL}|#${env.BUILD_NUMBER}>"],
                                [type: "mrkdwn", text: "*Branch:*\n`${branch}`"],
                                [type: "mrkdwn", text: "*Status:*\n:red_circle:  FAILURE"]
                            ]],
                            [type: "section", text: [type: "mrkdwn", text: "*Commit:* `${commitShort}` \u2014 ${commitMsg}"]],
                            [type: "divider"],
                            [type: "section", fields: [
                                [type: "mrkdwn", text: "*Total Tests*\n${total}"],
                                [type: "mrkdwn", text: "*Passed :white_check_mark:*\n${passed}"],
                                [type: "mrkdwn", text: "*Failed :x:*\n${failed}"],
                                [type: "mrkdwn", text: "*Pass Rate*\n${passRate}%"]
                            ]]
                        ]
                        if (failSlack) {
                            blocks << [type: "section", text: [type: "mrkdwn", text: "*Failed Tests:*\n${failSlack}"]]
                        }
                        blocks << [type: "divider"]
                        blocks << [type: "actions", elements: [
                            [type: "button", text: [type: "plain_text", text: ":bar_chart: Allure Report", emoji: true], url: "${env.BUILD_URL}allure/", style: "danger"],
                            [type: "button", text: [type: "plain_text", text: ":memo: Test Results",  emoji: true], url: "${env.BUILD_URL}testReport/"],
                            [type: "button", text: [type: "plain_text", text: ":page_facing_up: Console", emoji: true], url: "${env.BUILD_URL}console/"]
                        ]]

                        def payload = groovy.json.JsonOutput.toJson([
                            username   : "Jenkins CI",
                            icon_emoji : ":x:",
                            attachments: [[color: "#b71c1c", blocks: blocks]]
                        ])
                        writeFile file: 'slack-payload.json', text: payload
                        sh 'curl -s -X POST "${SLACK_WEBHOOK}" -H \'Content-Type: application/json\' -d @slack-payload.json'
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }

        // ══════════════════════════════════════════════════════════════════
        //  UNSTABLE
        // ══════════════════════════════════════════════════════════════════
        unstable {
            script {
                def commitShort  = (env.GIT_COMMIT ?: 'unknown').take(10)
                def commitMsg    = env.GIT_COMMIT_MSG ?: ''
                def branch       = env.GIT_BRANCH ?: 'unknown'
                def total        = env.TOTAL_TESTS  ?: '0'
                def passed       = env.PASSED_TESTS ?: '0'
                def failed       = env.FAILED_TESTS ?: '0'
                def passRate     = env.PASS_RATE    ?: '0'
                def failRows     = env.FAIL_ROWS_HTML  ?: ''
                def failCards    = env.FAIL_CARDS_HTML ?: ''
                def failSlack    = env.FAIL_NAMES_SLACK ?: '_No failure details available_'

                def failSection = failRows ? """
<h3 style="color:#e65100;margin:24px 0 12px;">Failed Tests Summary</h3>
<table style="width:100%;border-collapse:collapse;margin-bottom:20px;">
<thead><tr>
<th style="text-align:left;padding:8px 12px;background:#fff3e0;color:#e65100;font-size:12px;">Class</th>
<th style="text-align:left;padding:8px 12px;background:#fff3e0;color:#e65100;font-size:12px;">Test</th>
<th style="text-align:left;padding:8px 12px;background:#fff3e0;color:#e65100;font-size:12px;">Failure</th>
</tr></thead><tbody>${failRows}</tbody></table>
<h3 style="color:#e65100;margin:24px 0 12px;">Detailed Bug Reports</h3>
${failCards}""" : ''

                // ── Email ─────────────────────────────────────────────────
                try {
                    def emailBody = """<!DOCTYPE html><html><head><meta charset="UTF-8"><style>
body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
.card{background:#fff;border-radius:8px;max-width:680px;margin:auto;box-shadow:0 2px 8px rgba(0,0,0,.12);overflow:hidden}
.banner{background:#e65100;padding:24px 28px;color:#fff}
.banner h1{margin:0 0 4px;font-size:22px}
.banner p{margin:0;font-size:13px;opacity:.85}
.badge{display:inline-block;background:#ffcc80;color:#bf360c;border-radius:4px;padding:2px 10px;font-weight:bold;font-size:13px}
.body{padding:24px 28px}
table.info{width:100%;border-collapse:collapse;margin-bottom:20px}
table.info th{text-align:left;color:#555;font-size:11px;text-transform:uppercase;letter-spacing:.5px;padding:6px 0;border-bottom:1px solid #e0e0e0}
table.info td{padding:8px 0;font-size:14px;color:#333;border-bottom:1px solid #f0f0f0}
table.info td.lbl{color:#777;width:36%}
.stats{background:#fff8e1;border-radius:6px;padding:16px 20px;margin-bottom:20px;text-align:center}
.stats-grid{display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:12px}
.sv{font-size:28px;font-weight:bold;color:#e65100}
.sp{color:#2e7d32}
.sf{color:#c62828}
.sl{font-size:11px;color:#777;text-transform:uppercase;margin-top:2px}
.buttons{text-align:center;margin-top:4px}
.btn{display:inline-block;padding:10px 20px;border-radius:5px;text-decoration:none;font-size:13px;font-weight:bold;margin:4px}
.bp{background:#e65100;color:#fff}
.bo{background:#fff;color:#e65100;border:1px solid #e65100}
.footer{text-align:center;font-size:11px;color:#aaa;padding:14px;border-top:1px solid #eee}
</style></head><body><div class="card">
<div class="banner"><h1>&#9888; Build Unstable</h1>
<p>${env.JOB_NAME} &nbsp;|&nbsp; #${env.BUILD_NUMBER} &nbsp;|&nbsp; <span class="badge">UNSTABLE</span></p></div>
<div class="body">
<table class="info"><tr><th colspan="2">Build Details</th></tr>
<tr><td class="lbl">Branch</td><td>${branch}</td></tr>
<tr><td class="lbl">Commit</td><td><code style="background:#f0f0f0;padding:2px 6px;border-radius:3px;">${commitShort}</code> &nbsp;${commitMsg}</td></tr>
<tr><td class="lbl">Build URL</td><td><a href="${env.BUILD_URL}">${env.BUILD_URL}</a></td></tr>
</table>
<div class="stats"><div class="stats-grid">
<div><div class="sv">${total}</div><div class="sl">Total</div></div>
<div><div class="sv sp">${passed}</div><div class="sl">Passed</div></div>
<div><div class="sv sf">${failed}</div><div class="sl">Failed</div></div>
<div><div class="sv">${passRate}%</div><div class="sl">Pass Rate</div></div>
</div></div>
<div class="buttons">
<a class="btn bp" href="${env.BUILD_URL}allure/">&#128202; Allure Report</a>
<a class="btn bo" href="${env.BUILD_URL}testReport/">&#9888; JUnit Results</a>
<a class="btn bo" href="${env.BUILD_URL}console/">&#128196; Console</a>
</div>
${failSection}
</div>
<div class="footer">Jenkins CI &bull; ${env.JOB_NAME}</div>
</div></body></html>"""

                    emailext(
                        subject: "\u26A0\uFE0F BUILD UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: emailBody,
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        replyTo: '$DEFAULT_REPLYTO',
                        from: '$DEFAULT_FROM',
                        mimeType: 'text/html'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ─────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        def blocks = [
                            [type: "header", text: [type: "plain_text", text: ":warning:  Build Unstable \u2014 Fake Store API Tests", emoji: true]],
                            [type: "section", fields: [
                                [type: "mrkdwn", text: "*Job:*\n${env.JOB_NAME}"],
                                [type: "mrkdwn", text: "*Build:*\n<${env.BUILD_URL}|#${env.BUILD_NUMBER}>"],
                                [type: "mrkdwn", text: "*Branch:*\n`${branch}`"],
                                [type: "mrkdwn", text: "*Status:*\n:large_yellow_circle:  UNSTABLE"]
                            ]],
                            [type: "section", text: [type: "mrkdwn", text: "*Commit:* `${commitShort}` \u2014 ${commitMsg}"]],
                            [type: "divider"],
                            [type: "section", fields: [
                                [type: "mrkdwn", text: "*Total Tests*\n${total}"],
                                [type: "mrkdwn", text: "*Passed :white_check_mark:*\n${passed}"],
                                [type: "mrkdwn", text: "*Failed :x:*\n${failed}"],
                                [type: "mrkdwn", text: "*Pass Rate*\n${passRate}%"]
                            ]]
                        ]
                        if (failSlack) {
                            blocks << [type: "section", text: [type: "mrkdwn", text: "*Failed Tests:*\n${failSlack}"]]
                        }
                        blocks << [type: "divider"]
                        blocks << [type: "actions", elements: [
                            [type: "button", text: [type: "plain_text", text: ":bar_chart: Allure Report", emoji: true], url: "${env.BUILD_URL}allure/", style: "danger"],
                            [type: "button", text: [type: "plain_text", text: ":memo: Test Results",  emoji: true], url: "${env.BUILD_URL}testReport/"],
                            [type: "button", text: [type: "plain_text", text: ":page_facing_up: Console", emoji: true], url: "${env.BUILD_URL}console/"]
                        ]]

                        def payload = groovy.json.JsonOutput.toJson([
                            username   : "Jenkins CI",
                            icon_emoji : ":warning:",
                            attachments: [[color: "#e65100", blocks: blocks]]
                        ])
                        writeFile file: 'slack-payload.json', text: payload
                        sh 'curl -s -X POST "${SLACK_WEBHOOK}" -H \'Content-Type: application/json\' -d @slack-payload.json'
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }
    }
}
