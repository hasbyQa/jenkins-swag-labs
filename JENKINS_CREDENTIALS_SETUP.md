# 🔐 Jenkins Credentials Setup for Slack Webhook

## Why This Change?

GitHub's push protection detected the hardcoded Slack webhook URL as a secret and blocked the push. We've moved the webhook to Jenkins credentials (secure storage) instead of the source code.

## ✅ What You Need to Do

### Add Slack Webhook Credential to Jenkins

1. **Go to Jenkins Dashboard**
   - Open: `http://localhost:8080`

2. **Navigate to Credentials**
   - Click **Manage Jenkins** (left sidebar)
   - Click **Manage Credentials**
   - Click **System** (under "Stores scoped to Jenkins")
   - Click **Global credentials (unrestricted)**

3. **Add New Credential**
   - Click **+ Add Credentials** (left sidebar)
   - Fill in:
     - **Kind**: `Secret text`
     - **Scope**: `Global`
     - **Secret**: Paste your Slack webhook URL:
       ```
       ```
     - **ID**: `slack-webhook-url` (IMPORTANT - must match Jenkinsfile)
     - **Description**: `Slack Webhook for Build Notifications`

4. **Click Create**

Done! ✅

## 🔄 Now You Can Push

The Jenkinsfile no longer contains the secret, so you can safely push:

```bash
git add Jenkinsfile
git commit -m "Move Slack webhook to Jenkins credentials"
git push origin main
```

## 🔒 Security Benefits

- ✅ Secret not stored in source code
- ✅ Secret stored securely in Jenkins
- ✅ Can rotate webhook without changing code
- ✅ GitHub push protection passes
- ✅ No accidental leaks via git history

## 📝 Verification

After adding the credential:
1. Push the changes to GitHub
2. Trigger a build
3. Check Slack #builds channel for notification
4. Check Jenkins console for "✅ Slack notification sent successfully"

## ⚠️ If It Still Doesn't Work

**Check the credential ID matches exactly:**

Jenkinsfile expects: `slack-webhook-url`
```groovy
withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')])
```

**If you used a different ID**, either:
- Update the credential ID to `slack-webhook-url`, OR
- Update the Jenkinsfile to use your credential ID

---

**Status**: Ready to push to GitHub safely! 🚀
