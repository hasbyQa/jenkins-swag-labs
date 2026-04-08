#!/usr/bin/env groovy

// ─────────────────────────────────────────────────────────────────────────────
//  Helper methods (outside pipeline{} so they can be called from script{})
// ─────────────────────────────────────────────────────────────────────────────

String buildEmailBody(Map c) {
    def failPart = c.failSection ? "<div style='padding:0 28px 24px;'>${c.failSection}</div>" : ''
    """<!DOCTYPE html><html><head><meta charset="UTF-8"><style>
body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px;color:#000}
.card{background:#fff;border-radius:8px;max-width:680px;margin:auto;box-shadow:0 2px 8px rgba(0,0,0,.12);overflow:hidden}
.banner{background:${c.accent};padding:24px 28px}
.banner h1{margin:0 0 4px;font-size:22px;color:#fff}
.banner p{margin:0;font-size:13px;opacity:.9;color:#fff}
.badge{display:inline-block;background:${c.badgeBg};color:${c.badgeFg};border-radius:4px;padding:2px 10px;font-weight:bold;font-size:13px}
.body{padding:24px 28px}
table.info{width:100%;border-collapse:collapse;margin-bottom:20px}
table.info th{text-align:left;font-size:11px;text-transform:uppercase;letter-spacing:.5px;padding:6px 0;border-bottom:2px solid #ddd;color:#000}
table.info td{padding:8px 0;font-size:14px;border-bottom:1px solid #f0f0f0;color:#000}
table.info td.lbl{color:#555;width:36%;font-weight:600}
.stats{background:#f5f5f5;border-radius:6px;padding:16px 20px;margin-bottom:20px;text-align:center}
.sg{display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:12px}
.sv{font-size:28px;font-weight:bold;color:#000}
.sl{font-size:11px;color:#555;text-transform:uppercase;margin-top:2px}
.buttons{text-align:center;margin:4px 0 8px}
.btn{display:inline-block;padding:10px 20px;border-radius:5px;text-decoration:none;font-size:13px;font-weight:bold;margin:4px;color:#000}
.bp{background:${c.accent};color:#fff}
.bo{background:#fff;border:2px solid ${c.accent};color:#000}
.footer{text-align:center;font-size:11px;color:#888;padding:14px;border-top:1px solid #eee}
a{color:#000}
</style></head><body><div class="card">
<div class="banner"><h1>${c.icon} ${c.title}</h1>
<p>${c.job} &nbsp;|&nbsp; #${c.num} &nbsp;|&nbsp; <span class="badge">${c.status}</span></p></div>
<div class="body">
<table class="info"><tr><th colspan="2">Build Details</th></tr>
<tr><td class="lbl">Branch</td><td>${c.branch}</td></tr>
<tr><td class="lbl">Commit</td><td><code style="background:#f0f0f0;padding:2px 6px;border-radius:3px;color:#000">${c.sha}</code> &nbsp;${c.msg}</td></tr>
<tr><td class="lbl">Build URL</td><td><a href="${c.url}">${c.url}</a></td></tr>
</table>
<div class="stats"><div class="sg">
<div><div class="sv">${c.total}</div><div class="sl">Total</div></div>
<div><div class="sv">${c.passed}</div><div class="sl">Passed</div></div>
<div><div class="sv">${c.failed}</div><div class="sl">Failed</div></div>
<div><div class="sv">${c.rate}%</div><div class="sl">Pass Rate</div></div>
</div></div>
<div class="buttons">
<a class="btn bp" href="${c.url}allure/" style="color:#fff">&#128202; Allure Report</a>
<a class="btn bo" href="${c.url}testReport/">&#9989; JUnit Results</a>
<a class="btn bo" href="${c.url}console/">&#128196; Console</a>
</div></div>
${failPart}
<div class="footer">Jenkins CI &bull; ${c.job}</div>
</div></body></html>"""
}

List buildSlackBlocks(Map c) {
    def blocks = [
        [type:"header", text:[type:"plain_text", text:"${c.slackIcon}  ${c.title} \u2014 Fake Store API Tests", emoji:true]],
        [type:"section", fields:[
            [type:"mrkdwn", text:"*Job:*\n${c.job}"],
            [type:"mrkdwn", text:"*Build:*\n<${c.url}|#${c.num}>"],
            [type:"mrkdwn", text:"*Branch:*\n`${c.branch}`"],
            [type:"mrkdwn", text:"*Status:*\n${c.slackDot}  ${c.status}"]
        ]],
        [type:"section", text:[type:"mrkdwn", text:"*Commit:* `${c.sha}` \u2014 ${c.msg}"]],
        [type:"divider"],
        [type:"section", fields:[
            [type:"mrkdwn", text:"*Total*\n${c.total}"],
            [type:"mrkdwn", text:"*Passed :white_check_mark:*\n${c.passed}"],
            [type:"mrkdwn", text:"*Failed :x:*\n${c.failed}"],
            [type:"mrkdwn", text:"*Pass Rate*\n${c.rate}%"]
        ]]
    ]
    if (c.failSlack) blocks << [type:"section", text:[type:"mrkdwn", text:"*Failed Tests:*\n${c.failSlack}"]]
    blocks << [type:"divider"]
    blocks << [type:"actions", elements:[
        [type:"button", text:[type:"plain_text", text:":bar_chart: Allure Report", emoji:true], url:"${c.url}allure/", style:c.btnStyle],
        [type:"button", text:[type:"plain_text", text:":memo: Test Results",       emoji:true], url:"${c.url}testReport/"],
        [type:"button", text:[type:"plain_text", text:":page_facing_up: Console",  emoji:true], url:"${c.url}console/"]
    ]]
    return blocks
}

void sendNotifications(Map c) {
    try {
        emailext(subject: c.subject, body: buildEmailBody(c),
            to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
            replyTo: '$DEFAULT_REPLYTO', from: '$DEFAULT_FROM', mimeType: 'text/html')
        echo "Email sent"
    } catch (Exception e) { echo "Email failed: ${e.message}" }

    try {
        withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
            writeFile file: 'slack-payload.json', text: groovy.json.JsonOutput.toJson([
                username: "Jenkins CI", icon_emoji: c.slackIcon,
                attachments: [[color: c.accent, blocks: buildSlackBlocks(c)]]
            ])
            sh 'curl -s -X POST "${SLACK_WEBHOOK}" -H \'Content-Type: application/json\' -d @slack-payload.json'
        }
        echo "Slack sent"
    } catch (Exception e) { echo "Slack failed: ${e.message}" }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Pipeline
// ─────────────────────────────────────────────────────────────────────────────

pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 15, unit: 'MINUTES')
    }
    triggers { githubPush() }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code..."
                checkout scm
                script {
                    env.GIT_COMMIT_MSG = sh(script: 'git log -1 --pretty=format:"%s"', returnStdout: true).trim()
                }
            }
        }

        stage('Build') {
            steps {
                echo "Compiling project..."
                sh 'mvn clean compile -B -q'
            }
        }

        stage('Test') {
            steps {
                echo "Running API tests against fakestoreapi.com..."
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh 'mvn test -B'
                }
            }
        }

        stage('Reports') {
            steps {
                script {
                    // ── JUnit counts ─────────────────────────────────────────
                    def r = junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: true
                    env.TOTAL_TESTS   = "${r.totalCount}"
                    env.FAILED_TESTS  = "${r.failCount}"
                    env.SKIPPED_TESTS = "${r.skipCount}"
                    env.PASSED_TESTS  = "${r.totalCount - r.failCount - r.skipCount}"
                    env.PASS_RATE     = r.totalCount > 0
                        ? "${(int)(((r.totalCount - r.failCount - r.skipCount) / r.totalCount) * 100)}" : "0"
                    echo "Tests: total=${env.TOTAL_TESTS} passed=${env.PASSED_TESTS} failed=${env.FAILED_TESTS}"

                    // ── Parse surefire XMLs via Python (XmlSlurper blocked by sandbox) ──
                    // Each failure → one line: FAIL|||type|||fullClass|||cls|||method|||msg|||stack
                    // Stack newlines encoded as __NL__ to stay single-line.
                    def raw = sh(script: '''
python3 << 'PYEOF'
import glob, xml.etree.ElementTree as ET, sys
SEP = '|||'
for path in sorted(glob.glob('target/surefire-reports/TEST-*.xml')):
    try:
        root = ET.parse(path).getroot()
        for tc in root.findall('testcase'):
            for tag in ['failure', 'error']:
                f = tc.find(tag)
                if f is None: continue
                full_cls = tc.get('classname', '')
                cls      = full_cls.split('.')[-1]
                tname    = tc.get('name', '')
                msg      = (f.get('message') or '').replace(SEP,'|').replace('\\n',' ').replace('\\r','')
                stack    = (f.text or '').strip().replace(SEP,'|').replace('\\n','__NL__').replace('\\r','')
                btype    = 'Error' if tag == 'error' else 'Assertion Failure'
                print('FAIL'+SEP+btype+SEP+full_cls+SEP+cls+SEP+tname+SEP+msg+SEP+stack)
    except Exception as ex:
        sys.stderr.write('parse error: '+str(ex)+'\\n')
PYEOF
''', returnStdout: true).trim()

                    def failRowsHtml   = new StringBuilder()
                    def failCardsHtml  = new StringBuilder()
                    def failNamesSlack = new StringBuilder()
                    def bu = env.BUILD_URL ?: ''

                    raw.split('\n').each { line ->
                        if (!line.trim().startsWith('FAIL|||')) return
                        def p = line.trim().substring(7).split('\\|\\|\\|', 6)
                        if (p.length < 6) return
                        def btype   = p[0]; def fullCls = p[1]; def cls = p[2]
                        def tname   = p[3]; def msg = p[4]
                        def stack   = p[5].replace('__NL__', '\n')
                        def me = msg.take(300).replace('&','&amp;').replace('<','&lt;').replace('>','&gt;')
                        def se = stack.take(1500).replace('&','&amp;').replace('<','&lt;').replace('>','&gt;')

                        // Summary row
                        failRowsHtml.append(
                            "<tr>" +
                            "<td style='padding:8px 12px;border-bottom:1px solid #eee;font-weight:600;color:#000'>${cls}</td>" +
                            "<td style='padding:8px 12px;border-bottom:1px solid #eee;font-family:monospace;font-size:12px;color:#000'>${tname}</td>" +
                            "<td style='padding:8px 12px;border-bottom:1px solid #eee;color:#c62828'>${me}</td>" +
                            "</tr>"
                        )

                        // Bug report card
                        failCardsHtml.append(
                            "<div style='background:#f9f9f9;border-left:4px solid #e53935;border-radius:0 6px 6px 0;" +
                            "padding:18px 22px;margin-bottom:20px;font-size:13px;line-height:1.7;color:#000'>" +
                            "<div style='font-weight:bold;color:#b71c1c;font-size:14px;margin-bottom:14px'>" +
                            "&#128030; Bug Report &mdash; ${cls} :: ${tname}</div>" +
                            "<table style='width:100%;border-collapse:collapse'>" +
                            row('Type',             btype) +
                            row('Test Class',       "<code style='font-size:12px;color:#000'>${fullCls}</code>") +
                            row('Test Method',      "<code style='font-size:12px;color:#000'>${tname}</code>") +
                            row('API Under Test',   "<a href='https://fakestoreapi.com' style='color:#000'>https://fakestoreapi.com</a>") +
                            row('Failure Message',  "<span style='color:#c62828;font-weight:500'>${me}</span>") +
                            row('Steps to Reproduce',
                                "1. Clone the repository<br>" +
                                "2. Install Java 17+ and Maven 3.8+<br>" +
                                "3. Run: <code style='background:#f0f0f0;padding:2px 5px;border-radius:3px;color:#000'>" +
                                "mvn test -Dtest=${cls}#${tname} -B</code><br>" +
                                "4. Observe the failure in Maven Surefire output") +
                            row('Expected Behaviour', 'Test should pass — the API assertion must be satisfied') +
                            row('Actual Behaviour',   "<span style='color:#c62828'>${me}</span>") +
                            row('Stack Trace',
                                "<pre style='background:#1a1a2e;color:#e8e8f0;padding:12px 16px;border-radius:5px;" +
                                "font-size:11px;overflow:auto;margin:4px 0;white-space:pre-wrap;line-height:1.5'>${se}</pre>") +
                            row('Allure Report',  "<a href='${bu}allure/' style='color:#000'>${bu}allure/</a>") +
                            row('JUnit Results',  "<a href='${bu}testReport/' style='color:#000'>${bu}testReport/</a>") +
                            "</table></div>"
                        )

                        failNamesSlack.append("• *${cls}* \u203a `${tname}`\n   _${msg.take(100)}_\n")
                    }

                    env.FAIL_ROWS_HTML   = failRowsHtml.toString()
                    env.FAIL_CARDS_HTML  = failCardsHtml.toString()
                    env.FAIL_NAMES_SLACK = failNamesSlack.toString().trim() ?: 'No failed tests'
                }

                allure([commandline: 'allure', results: [[path: 'target/allure-results']],
                        reportBuildPolicy: 'ALWAYS', includeProperties: false])
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, target/allure-results/**',
                                  allowEmptyArchive: true
                echo "Reports published!"
            }
        }
    }

    post {
        cleanup { cleanWs() }

        success {
            script {
                sendNotifications(baseConfig('success'))
            }
        }
        failure {
            script {
                sendNotifications(baseConfig('failure') + [failSection: failSection('#ffebee'), failSlack: env.FAIL_NAMES_SLACK ?: 'No failed tests'])
            }
        }
        unstable {
            script {
                sendNotifications(baseConfig('unstable') + [failSection: failSection('#fff3e0'), failSlack: env.FAIL_NAMES_SLACK ?: 'No failed tests'])
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Small helpers used inside the pipeline
// ─────────────────────────────────────────────────────────────────────────────

/** One label/value table row for the bug report card. */
String row(String label, String value) {
    "<tr><td style='width:155px;color:#555;padding:5px 0;vertical-align:top;font-weight:600'>${label}</td>" +
    "<td style='padding:5px 0;color:#000'>${value}</td></tr>"
}

/** Build the failure section HTML (summary table + bug report cards). */
String failSection(String headerBg) {
    def rows  = env.FAIL_ROWS_HTML  ?: ''
    def cards = env.FAIL_CARDS_HTML ?: ''
    if (!rows) return ''
    """<h3 style='color:#000;margin:24px 0 12px;font-size:15px'>Failed Tests Summary</h3>
<table style='width:100%;border-collapse:collapse;margin-bottom:20px'>
<thead><tr>
<th style='text-align:left;padding:8px 12px;background:${headerBg};color:#000;font-size:12px'>Class</th>
<th style='text-align:left;padding:8px 12px;background:${headerBg};color:#000;font-size:12px'>Test</th>
<th style='text-align:left;padding:8px 12px;background:${headerBg};color:#000;font-size:12px'>Failure</th>
</tr></thead><tbody>${rows}</tbody></table>
<h3 style='color:#000;margin:24px 0 12px;font-size:15px'>Bug Reports</h3>${cards}"""
}

/** Common notification config shared by all three post conditions. */
Map baseConfig(String result) {
    def presets = [
        success : [subject:"\u2705 BUILD PASSED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                   icon:'&#9989;',  title:'Build Passed',   status:'SUCCESS',
                   accent:'#2e7d32', badgeBg:'#c8e6c9', badgeFg:'#1b5e20',
                   slackIcon:':white_check_mark:', slackDot:':large_green_circle:', btnStyle:'primary',
                   failSection:'', failSlack:''],
        failure : [subject:"\u274C BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                   icon:'&#10060;', title:'Build Failed',   status:'FAILURE',
                   accent:'#b71c1c', badgeBg:'#ffcdd2', badgeFg:'#7f0000',
                   slackIcon:':x:', slackDot:':red_circle:', btnStyle:'danger'],
        unstable: [subject:"\u26A0\uFE0F BUILD UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                   icon:'&#9888;',  title:'Build Unstable', status:'UNSTABLE',
                   accent:'#e65100', badgeBg:'#ffe0b2', badgeFg:'#bf360c',
                   slackIcon:':warning:', slackDot:':large_yellow_circle:', btnStyle:'danger']
    ]
    return presets[result] + [
        job: env.JOB_NAME, num: env.BUILD_NUMBER,
        branch: env.GIT_BRANCH  ?: 'unknown',
        sha:    (env.GIT_COMMIT ?: 'unknown').take(10),
        msg:    env.GIT_COMMIT_MSG ?: '',
        url:    env.BUILD_URL   ?: '',
        total:  env.TOTAL_TESTS  ?: '0', passed: env.PASSED_TESTS ?: '0',
        failed: env.FAILED_TESTS ?: '0', rate:   env.PASS_RATE    ?: '0'
    ]
}
