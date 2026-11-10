# iContacts - iPhone-Style Contact & Dialer for Android

An iOS-inspired Phone and Contacts management app for Android with device contact synchronization, batch contact purging, multiple VCF file importing, and VCF exporting.

---

## Features

- **iPhone Dialer & Keypad**:
  - Circular iOS dialpad buttons (`1`, `2 ABC`, `3 DEF` ... `0 +`, `*`, `#`) with haptic feedback.
  - Formatted phone number display with **"Add Number"** shortcut.
  - In-app active call simulator with timer, speaker, mute, and red end-call button.
  - Zero-permission system dialer integration (`Intent.ACTION_DIAL`).
- **Device Contact Management & "Purge Phone Contact"**:
  - Reads, writes, and syncs directly with Android's `ContactsContract`.
  - **Purge All Phone Contacts**: Safe device-wide wipe with dual confirmation (requires typing `"PURGE"`).
  - **Selective Purge**: Multi-select mode with checkboxes to batch-purge selected contacts.
  - **Clean Duplicates**: Scans and eliminates duplicate names and phone numbers in one tap.
- **Multiple VCF File Import**:
  - Select and parse multiple `.vcf` / vCard files simultaneously via Android's Storage Access Framework (`OpenMultipleDocuments`).
  - Option to save imported contacts directly into the device address book.
- **VCF Export**:
  - Export all contacts or selected contacts to a standard `.vcf` file (`CreateDocument`).
  - Share contact cards via Gmail, Drive, WhatsApp, and messaging apps (`FileProvider`).
- **iOS Contacts Experience**:
  - Grouped alphabetical list with sticky headers.
  - Fast vertical **Alphabet Scrubber** (A–Z, #).
  - Search bar with animated Cancel button.
  - Favorites & Recents with All / Missed filters.

---

## GitHub Actions Automated Release Workflow

A GitHub Actions workflow is configured in [`.github/workflows/release.yml`](.github/workflows/release.yml) to automatically compile, package, and publish release APKs to GitHub.

### How to Release an APK on GitHub

#### Option 1: Automatic Release on Git Tag Push (Recommended)
Push a git tag starting with `v`:
```bash
git tag v1.0.0
git push origin v1.0.0
```
The workflow will automatically:
1. Compile and sign both **Release APK** (`iContacts-release.apk`) and **Debug APK** (`iContacts-debug.apk`).
2. Generate SHA256 checksums (`checksums.txt`).
3. Create a new GitHub Release with release notes and attach the APK files.

#### Option 2: Manual Trigger via GitHub Actions UI
1. Navigate to your repository on GitHub.
2. Click on the **Actions** tab.
3. Select **Release APK** from the left sidebar.
4. Click **Run workflow**:
   - Provide a tag name (e.g. `v1.0.0`).
   - Provide a release title (e.g. `iContacts v1.0.0 - Release`).
   - (Optional) Toggle pre-release checkbox.
5. Click the green **Run workflow** button.

#### Option 3: Automatic Build Artifacts on Push
Whenever changes are pushed to `main` or `master`, the workflow compiles the APKs and uploads them as a downloadable ZIP artifact named `iContacts-APKs` directly in the workflow summary.

---

## (Optional) Custom Signing Key Configuration

By default, the workflow automatically generates a self-signed release keystore so builds succeed immediately out-of-the-box. To sign with your own production keystore:

1. Base64-encode your `.jks` keystore:
   ```bash
   base64 -w 0 my-upload-key.jks
   ```
2. In your GitHub repository, go to **Settings** > **Secrets and variables** > **Actions**.
3. Add the following repository secrets:
   - `KEYSTORE_BASE64`: The base64-encoded string of your keystore file.
   - `STORE_PASSWORD`: Keystore store password.
   - `KEY_PASSWORD`: Keystore key password.
