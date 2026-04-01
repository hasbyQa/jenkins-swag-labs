# How to Fix GitHub Push Access

Your changes are committed locally but can't be pushed due to authentication. Here are the solutions:

## Solution 1: Use GitHub Personal Access Token (Recommended) ⭐

### Step 1: Create a Personal Access Token on GitHub

1. Go to: https://github.com/settings/tokens
2. Click **"Generate new token"** → **"Generate new token (classic)"**
3. Give it a name: `jenkins-deployment`
4. Select scopes:
   - ✅ `repo` (full control of private repositories)
   - ✅ `admin:public_key` (for key management)
5. Click **"Generate token"**
6. **Copy the token** (you won't see it again!)

### Step 2: Configure Git with the Token

```bash
# Replace YOUR_TOKEN with the token you just copied
git remote set-url origin https://hasbyQa:YOUR_TOKEN@github.com/hasbyQa/jenkins-swag-labs.git

# Verify the URL was updated
git remote -v
```

### Step 3: Push Your Changes

```bash
git push origin main
```

Expected output:
```
Counting objects: 3, done.
Delta compression using up to 8 threads.
Compressing objects: 100% (3/3), done.
Writing objects: 100% (3/3), 360 bytes | 360.00 KiB/s, done.
Total 3 (delta 1), reused 0 (delta 0), pack-reused 0
To https://hasbyQa:***@github.com/hasbyQa/jenkins-swag-labs.git
   9d57020..512b06f  main -> main
```

---

## Solution 2: Use SSH Keys

### Step 1: Generate SSH Key (if you don't have one)

```bash
ssh-keygen -t ed25519 -C "hasbiyallah.umutoniwabo@amalitechtraining.org"
# Press Enter for default location
# Enter a passphrase (optional)
```

### Step 2: Add Public Key to GitHub

1. Copy your public key:
   ```bash
   cat ~/.ssh/id_ed25519.pub
   ```

2. Go to: https://github.com/settings/keys
3. Click **"New SSH key"**
4. Title: `Jenkins Server`
5. Paste the public key
6. Click **"Add SSH key"**

### Step 3: Update Git Remote URL

```bash
git remote set-url origin git@github.com:hasbyQa/jenkins-swag-labs.git

# Verify
git remote -v
```

### Step 4: Test and Push

```bash
# Test SSH connection
ssh -T git@github.com

# Should see: "Hi hasbyQa! You've successfully authenticated..."

# Now push
git push origin main
```

---

## Solution 3: Check Repository Access

If both above fail, the issue might be permissions:

### Verify Collaborator Access

1. Go to: https://github.com/hasbyQa/jenkins-swag-labs/settings/access
2. Check if `hasby-umutoniwabo` is listed as a collaborator
3. If not, add them with **Write** permission

### Check GitHub Organization

If this repo is under an organization:
1. Go to organization settings
2. Verify team membership
3. Ensure team has `Write` access to the repository

---

## After Pushing Successfully

Once `git push origin main` succeeds:

### Option A: Manual Build Trigger
1. Go to Jenkins job
2. Click **"Build Now"**
3. Wait ~6 minutes for completion

### Option B: Automatic via GitHub Webhook
- If webhook is configured, push automatically triggers build
- Check Jenkins job → **Configure** → **Build Triggers** → **GitHub hook trigger...**

---

## Verify Build Results

After build completes:

✅ **Email Check**
- Should receive: `✅ BUILD PASSED: jenkins_lab #X`
- Contains: Allure report link
- From: `hasbiyallah.umutoniwabo@amalitechtraining.org`

✅ **Slack Check**
- Message in `#builds` channel
- Shows: Build passed, job name, build number

✅ **Jenkins UI Check**
- JUnit test results: 19/19 passing
- Allure Report link available
- Build artifacts archived

---

## Troubleshooting

### "fatal: unable to access 'https://github.com/...': The requested URL returned error: 403"

**Causes:**
- Wrong credentials (username/password, expired PAT)
- User doesn't have access to repository
- HTTPS access not enabled

**Fix:**
- Use Solution 1 (PAT) with correct token
- Or use Solution 2 (SSH) instead

### "Permission denied (publickey)"

**Causes:**
- SSH key not added to GitHub
- SSH key has wrong permissions
- SSH not installed

**Fix:**
```bash
# Check SSH key permissions
ls -la ~/.ssh/id_ed25519
# Should be: -rw------- (600)

chmod 600 ~/.ssh/id_ed25519
chmod 644 ~/.ssh/id_ed25519.pub
```

### "host key verification failed"

**Fix:**
```bash
# First time SSH connection needs verification
ssh -T git@github.com
# Type "yes" when prompted

# Then try push again
git push origin main
```

---

## Recommended Approach

**For this project, use Solution 1 (Personal Access Token)** because:
- ✅ Easier setup for CI/CD systems
- ✅ Can be revoked anytime
- ✅ Better security than storing passwords
- ✅ Works with GitHub organization policies

---

**Questions?** Run this for debug info:
```bash
cd /home/ancientfvck/Downloads/swag-labs-docker-tests-main
git remote -v
git config user.email
git config user.name
git log --oneline -3
```
