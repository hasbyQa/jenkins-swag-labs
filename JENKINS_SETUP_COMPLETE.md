# 🔧 Jenkins Complete Configuration Guide

## 📋 What You Need to Configure

After the code is pushed to GitHub, you need to configure Jenkins with credentials and SMTP settings.

---

## 1️⃣ Slack Webhook Credential Setup

### Create Secret Text Credential for Slack Webhook

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
     - **Secret**: Paste your Slack webhook URL (get from https://api.slack.com/apps → Incoming Webhooks)
     - **ID**: `slack-webhook-url` ⚠️ **EXACT MATCH REQUIRED**
     - **Description**: `Slack Webhook for Build Notifications`

4. **Click Create**

---

## 2️⃣ Jenkins System Email Configuration

### Configure Email Settings

1. **Go to System Settings**
   - Click **Manage Jenkins** (left sidebar)
   - Click **System**

2. **Find "Email Notification" Section**

3. **Configure SMTP Server**

   **Option A: Gmail (Recommended)**
   - **SMTP server**: `smtp.gmail.com`
   - **SMTP port**: `587`
   - **Username**: Your Gmail address (e.g., `your-email@gmail.com`)
   - **Password**: 16-character app password (from https://myaccount.google.com/apppasswords)
   - **Use SMTP Authentication**: ✅ Check
   - **Use TLS**: ✅ Check
   - **SMTP TLS port**: `587`
   - **Default user e-mail suffix**: `@gmail.com`

   **Option B: Outlook/Microsoft 365**
   - **SMTP server**: `smtp.outlook.com`
   - **SMTP port**: `587`
   - **Username**: Your Outlook email
   - **Password**: Your Outlook password
   - **Use SMTP Authentication**: ✅ Check
   - **Use TLS**: ✅ Check
   - **SMTP TLS port**: `587`

   **Option C: Custom SMTP**
   - Contact your email provider for SMTP details

4. **Test Email Configuration**
   - Click **Test configuration by sending test e-mail**
   - Enter your email address
   - Click **Test**
   - Check your inbox (may take 10-30 seconds)

5. **Save Settings**

---

## 3️⃣ Extended Email Plugin Configuration

### Configure Email Notifications

1. **Manage Jenkins** → **System** (scroll down)

2. **Find "Extended E-mail Notification" Section**

3. **Fill in Email Configuration**
   - **SMTP server**: (same as above - gmail/outlook/custom)
   - **SMTP port**: `587`
   - **Default user e-mail suffix**: `@amalitechtraining.org`
   - **Use SMTP Authentication**: ✅ Check
   - **Use TLS**: ✅ Check
   - **SMTP TLS port**: `587`
   - **From Address**: `jenkins@amalitechtraining.org`
   - **Default Recipients**: Leave empty (handled by recipientProviders in Jenkinsfile)

4. **Click Save**

---

## 4️⃣ Verify Slack Notification Plugin

### Check Slack Plugin is Installed

1. **Go to Plugin Manager**
   - Click **Manage Jenkins**
   - Click **Manage Plugins**
   - Search: `Slack Notification`

2. **Verify Status**
   - Should show as **installed**
   - If not, click **Install**
   - Restart Jenkins if prompted

---

## 🧪 Test the Configuration

### Trigger a Build

1. **Go to Your Job**
   - Click the job name in Jenkins

2. **Trigger Build**
   - Click **Build Now** button

3. **Monitor Notifications**
   - **Email**: Check inbox (build success notification)
   - **Slack**: Check `#builds` channel
   - **Jenkins**: Click build → **Console Output** → look for:
     - `✅ Slack notification sent successfully`
     - `✅ Email notification sent successfully`

### If Email Doesn't Arrive

**Check:**
1. Did you use an app password (Gmail) or regular password (Outlook)?
2. Is SMTP server and port correct?
3. Is TLS enabled?
4. Check Jenkins logs:
   - **Manage Jenkins** → **System Log**
   - Look for SMTP errors

**Fix:**
1. Go back to **System** settings
2. Update SMTP configuration
3. Test again

### If Slack Doesn't Send

**Check:**
1. Is credential ID exactly `slack-webhook-url`?
2. Is the webhook URL correct?
3. Does `#builds` channel exist?
4. Is Jenkins bot invited to channel?

**Fix:**
1. Go to **Manage Credentials**
2. Click credential → **Update**
3. Verify webhook URL is correct
4. Go back to Jenkins and rebuild

---

## ✅ Checklist

### Slack Setup ✓
- [ ] Slack Notification Plugin installed
- [ ] Credential created with ID `slack-webhook-url`
- [ ] Webhook URL is correct
- [ ] `#builds` channel exists
- [ ] Jenkins bot invited to channel

### Email Setup ✓
- [ ] SMTP server configured (gmail/outlook/custom)
- [ ] Username and password entered
- [ ] TLS enabled
- [ ] Test email sent successfully
- [ ] Extended Email Plugin configured

### Build Test ✓
- [ ] Clicked "Build Now"
- [ ] Build completed
- [ ] Email received in inbox
- [ ] Slack message in #builds channel
- [ ] Console shows success messages

---

## 📞 Troubleshooting

### Gmail Issue: "Invalid Credentials"
**Solution**: Use 16-character **app password**, not Gmail password
- Go to https://myaccount.google.com/apppasswords
- Select "Mail" and "Windows Computer"
- Copy the 16-char password
- Update Jenkins SMTP password

### Slack Issue: "the credential with the provided ID () could not be found"
**Solution**: Credential ID doesn't match
- Go to **Manage Credentials**
- Verify credential ID is exactly: `slack-webhook-url`
- If different, update the Jenkinsfile to match OR change credential ID

### Email Issue: "Not sent to the following valid addresses"
**Solution**: SMTP server not configured or TLS issue
- Go to **System** → **Email Notification**
- Verify SMTP server and port
- Enable TLS
- Test with test email
- Check Jenkins logs for errors

### Both Not Working
**Steps**:
1. Restart Jenkins: `docker restart jenkins`
2. Re-test email: **System** → **Email Notification** → **Test**
3. Check Jenkins logs: **System Log**
4. Verify credentials are created and correct
5. Try a new build

---

## 🎯 Success Indicators

✅ **Email Success**
```
Sending email to: hasbiyallah.umutoniwabo@amalitechtraining.org
✅ Email notification sent successfully
```

✅ **Slack Success**
```
✅ Slack notification sent successfully
```

✅ **Both Combined**
- Email arrives in inbox
- Slack message in #builds channel
- Jenkins console shows success messages above

---

## 📝 Configuration Summary

| Component | Status | Details |
|-----------|--------|---------|
| **Jenkinsfile** | ✅ Ready | Uses `tokenCredentialId: 'slack-webhook-url'` |
| **Slack Credential** | 🔄 Pending | Create with your webhook URL |
| **SMTP Server** | 🔄 Pending | Configure Gmail/Outlook/Custom |
| **Email Plugin** | 🔄 Pending | Configure extended email settings |
| **Test Build** | 🔄 Pending | Run after configuration complete |

---

**Status**: Configuration guide created. Follow steps 1-4 to complete setup, then test with a build.

**Time to complete**: ~15 minutes total
