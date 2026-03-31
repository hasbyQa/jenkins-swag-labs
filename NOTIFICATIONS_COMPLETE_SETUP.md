# 📧 Email & Slack Notifications - Complete Setup Guide

## ✅ What's Already Configured in the Pipeline

Your Jenkinsfile now includes:
- ✅ Email notifications for build success/failure
- ✅ Slack notifications for build success/failure  
- ✅ Error handling (notifications won't break the build)
- ✅ Recipient providers (developers, requestor, broken build suspects)

## 📧 Email Notifications Setup

### Option 1: Gmail (Recommended)

#### Step 1: Create Gmail App Password

1. Go to https://myaccount.google.com/security
2. Enable 2-Step Verification if not already enabled
3. Go to https://myaccount.google.com/apppasswords
4. Select "Mail" and "Windows Computer"
5. Copy the 16-character password

#### Step 2: Configure Jenkins Email

1. Open Jenkins: `http://localhost:8080`
2. Click **Manage Jenkins** → **System**
3. Scroll to **Email Notification** section
4. Fill in:
   - **SMTP server**: `smtp.gmail.com`
   - **SMTP port**: `587`
   - **Username**: `your-gmail@gmail.com`
   - **Password**: (paste the 16-character app password)
   - **Use SMTP Authentication**: ✅ Check
   - **Use TLS**: ✅ Check
   - **SMTP TLS port**: `587`
   - **Default user e-mail suffix**: `@gmail.com`

5. Click **Test configuration by sending test email**
6. Check your inbox for test email
7. Click **Save**

### Option 2: Outlook/Microsoft 365

1. Go to Jenkins: **Manage Jenkins** → **System** → **Email Notification**
2. Fill in:
   - **SMTP server**: `smtp.outlook.com`
   - **SMTP port**: `587`
   - **Username**: `your-email@outlook.com`
   - **Password**: Your Outlook password
   - **Use SMTP Authentication**: ✅ Check
   - **Use TLS**: ✅ Check

3. Test and Save

### Option 3: Custom SMTP Server

Contact your email provider for:
- SMTP server address
- SMTP port (usually 587 or 465)
- Username and password
- TLS requirement (usually yes)

## 💬 Slack Notifications Setup

### Step 1: Create Slack Webhook

1. Go to https://api.slack.com/apps
2. Click **Create New App** → **From scratch**
3. **App name**: `Jenkins`
4. **Workspace**: Select your workspace
5. Click **Create App**

6. In left sidebar: **Incoming Webhooks**
7. Click **Activate Incoming Webhooks** toggle (ON)
8. Click **Add New Webhook to Workspace**
9. Select channel: `#builds` (or create new)
10. Click **Allow**
11. Copy the **Webhook URL** (looks like: `https://hooks.slack.com/services/...`)

### Step 2: Configure Jenkins Slack Plugin

#### Install Plugin
1. Jenkins → **Manage Jenkins** → **Manage Plugins**
2. Search: `Slack Notification`
3. Click **Install** (may require restart)
4. Restart Jenkins if prompted

#### Configure Slack
1. Jenkins → **Manage Jenkins** → **System**
2. Scroll to **Slack** section
3. Fill in:
   - **Workspace**: Your Slack workspace name
   - **Channel**: `#builds` (default, can override per job)
   - **Credential**: 
     - Click **Add** → **Jenkins**
     - Kind: **Secret text**
     - Secret: Paste the webhook URL
     - ID: `slack-webhook`
     - Click **Add**
   - Select the credential you just created
   - **Bot User**: ✅ Check
   - **Username**: `Jenkins` (optional)
   - **Icon URL**: (optional - Jenkins logo)

4. Click **Test Connection** to verify
5. Click **Save**

## 🔧 How Notifications Work in the Pipeline

### Success Build
```
Jenkinsfile triggers:
  ✅ Green Slack message to #builds
  ✅ Email to developers, requestor
  
Both are wrapped in try-catch to prevent build failure
```

### Failed Build
```
Jenkinsfile triggers:
  ❌ Red Slack message to #builds
  ❌ Email to developers, requestor, broken build suspects
  
Both are wrapped in try-catch to prevent build failure
```

## 🧪 Test the Notifications

### Test Email
1. Go to Jenkins job page
2. Click **Build Now**
3. Wait for build to complete
4. Check your email inbox (may take 10-30 seconds)

### Test Slack
1. Go to Jenkins job page
2. Click **Build Now**
3. Wait for build to complete
4. Check your Slack channel `#builds`

## ❓ Troubleshooting

### Email Not Sending

**Check:**
1. Did you use app password (not Gmail password)?
   - Gmail: Must use 16-char app password from https://myaccount.google.com/apppasswords
   - Outlook: Use your password
   
2. Check SMTP settings:
   - Gmail: smtp.gmail.com:587 with TLS
   - Outlook: smtp.outlook.com:587 with TLS
   
3. Check Jenkins logs:
   - Jenkins → **Manage Jenkins** → **System Log**
   - Look for SMTP errors

4. Test manually:
   - Jenkins → **System** → **Email Notification**
   - Click **Test configuration by sending test e-mail**

**Fix:**
```
If using Gmail:
1. Get new app password from https://myaccount.google.com/apppasswords
2. Update Jenkins System settings
3. Test again
```

### Slack Not Sending

**Check:**
1. Is Slack Notification Plugin installed?
   - Jenkins → **Manage Jenkins** → **Manage Plugins**
   - Search `Slack Notification` - should be installed

2. Is webhook URL correct?
   - Jenkins → **System** → **Slack**
   - Verify credential has the webhook URL

3. Is the channel correct?
   - Make sure `#builds` channel exists
   - Make sure Jenkins bot is invited to channel

4. Check Jenkins logs:
   - Jenkins → **Manage Jenkins** → **System Log**
   - Look for Slack errors

**Fix:**
```
1. Re-create the webhook in Slack API
2. Update Jenkins Slack credential
3. Test connection
4. Re-run a build
```

### Both Not Sending?

**Possible causes:**
1. Build hasn't completed yet (wait 1-2 minutes)
2. Email/Slack not configured (follow setup steps above)
3. Jenkins plugins not installed
4. Network issues

**Debug steps:**
1. Check Jenkins build logs for errors
2. Look at Jenkins System Log
3. Run a test build with verbose logging
4. Check firewall allows outgoing SMTP/Slack

## 📋 Configuration Checklist

### Email Setup ✓
- [ ] SMTP server configured (gmail/outlook/custom)
- [ ] Username/password entered
- [ ] TLS enabled
- [ ] Test email sent successfully
- [ ] Settings saved

### Slack Setup ✓
- [ ] Slack webhook created
- [ ] Slack Notification Plugin installed
- [ ] Jenkins System configured with webhook URL
- [ ] Test connection successful
- [ ] `#builds` channel exists
- [ ] Jenkins bot invited to channel
- [ ] Settings saved

### Test Notifications ✓
- [ ] Clicked "Build Now"
- [ ] Build completed
- [ ] Email received in inbox
- [ ] Slack message in #builds
- [ ] Both success and failure tested

## 🎯 Next Steps

1. **Configure Email** (5 minutes)
   - Follow Option 1 (Gmail), 2 (Outlook), or 3 (Custom)
   - Test with "Test configuration"

2. **Configure Slack** (5 minutes)
   - Create webhook
   - Install plugin
   - Configure in Jenkins System
   - Test with build

3. **Verify Both Work** (2 minutes)
   - Push code to trigger build
   - Check email inbox
   - Check Slack #builds channel

## 📞 Support

**Gmail app password issues?**
- Link: https://support.google.com/accounts/answer/185833

**Slack webhook issues?**
- Link: https://api.slack.com/messaging/webhooks
- Slack Notification Plugin: https://plugins.jenkins.io/slack/

**Jenkins email plugin issues?**
- Link: https://plugins.jenkins.io/email-ext/

---

**Status**: Pipeline configured with email and Slack support. Just need to set credentials in Jenkins System settings.

**Time to complete**: ~10 minutes total
