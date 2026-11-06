# SystemTricks ⚡
> Essential Hardware Key, Gesture &amp; System Tweaks for Android (LSPosed / Xposed)

[![Android](https://img.shields.io/badge/Android-10%20to%2015-3DDC84?style=flat&logo=android&logoColor=white)](https://github.com/BabaModz-Official/SystemTricks/releases)
[![Framework](https://img.shields.io/badge/Framework-LSPosed-8A2BE2?style=flat)](https://github.com/BabaModz-Official/SystemTricks/releases)
[![Release](https://img.shields.io/badge/Release-v1.0-blue?style=flat)](https://github.com/BabaModz-Official/SystemTricks/releases)

**SystemTricks** is a lightweight, all-in-one system customization module for modern Android devices (Android 10 - 15, including HyperOS, MIUI, AOSP, LineageOS, Pixel, OneUI). 

It brings back essential power-user controls such as volume cursor control, lockscreen track skipping, pocket-safe power button flashlight, double-tap to sleep, and clean status bar icon toggles without any battery drain.

---

## 🚀 Features

### 🔘 Hardware Keys &amp; Gestures
* **Volume Keys Control Cursor:** Move the text cursor Left / Right effortlessly using Volume Up &amp; Down keys when typing on keyboard (supports Normal &amp; Inverted modes).
* **Volume Keys Skip Track:** Long-press Volume Up or Down while screen is OFF and music is playing to skip to Next or Previous track (with haptic feedback).
* **Power Key Toggle Torch (Pocket Safe):** Long-press the Power button when screen is OFF to toggle the flashlight.
  * **Proximity Sensor Check:** Automatically blocks the flashlight if the phone is inside a pocket or bag to prevent accidental pocket burns and battery drain.
* **Torch Auto-Off Timer:** Automatically turns off the flashlight after 2, 5, or 10 minutes to protect LED hardware from overheating.
* **Prevent Accidental Wake-up (Pocket Mode):** Prevents the screen from turning on if the power button or screen is tapped while the proximity sensor is covered (in pocket).
* **Double-Tap Status Bar to Sleep:** Double-tap anywhere on the status bar or empty lock screen area to immediately lock/sleep the device.
* **Quick Settings Pulldown:** Swipe down from the screen edge (Right or Left) to directly expand Quick Settings instead of regular notifications.
* **Back Gesture Trigger Height:** Restrict gesture navigation height (from 10% up to full height) to eliminate conflicts with app side-drawer hamburger menus.

### 🔔 Notification Tweaks
* **Throttle Notification Sounds &amp; Vibrations:** Avoid annoying notification spam from active group chats (Throttle intervals: 5s, 10s, 30s, 1m, 2m).
* **Screen-Off Notifications Only:** Mutes notification sound and vibration while you are actively using your phone (screen ON), only alerting you when screen is OFF.
* **Hide USB Debugging Notification:** Keeps your notification shade clean by hiding the persistent \"USB debugging connected\" notification.

### 📶 Status Bar &amp; UI Tweaks
* **Hide VPN Icon:** Hides the persistent VPN key icon from the status bar and security footer.
* **Hide CA Certificate Warning:** Removes the \"Network may be monitored\" security warning in the quick settings footer.
* **Hide Next Alarm Icon:** Hides the alarm clock icon from status bar.
* **Show 4G for LTE / Hide LTE+:** Customize carrier network icons.
* **Hide QS Footer Build Number:** Cleans up the bottom footer of Quick Settings by hiding the build version watermark.
* **Custom Carrier Text:** Replace or hide the network carrier name on lock screen and status bar.

### 📱 App Specific Tweaks
* **Mute Google Dialer Call Recording Announcement:** Silences the \"This call is now being recorded\" voice warning.
* **Bypass Outlook Intune / Device Security Policies:** Bypasses mandatory device PIN and encryption requirements for enterprise Outlook accounts.

---

## 📲 Installation

1. Ensure your device is rooted with **Magisk**, **KernelSU**, or **APatch**, and has **LSPosed** installed and working.
2. Download the latest **`SystemTricks_v1.0.apk`** from the [Releases](https://github.com/BabaModz-Official/SystemTricks/releases) section.
3. Install the APK on your device.
4. Open **LSPosed Manager**:
   * Navigate to the **Modules** tab.
   * Enable **SystemTricks**.
   * Ensure **System Framework (`android`)** is selected in scopes.
5. **Reboot your phone** (Required for system_server hooks to load).
6. Open the **SystemTricks** app to configure your preferred tweaks.
   * Changes apply **instantly in real time** without needing further reboots!

---

## 🛠 Compatibility
* **Android Versions:** Android 10, 11, 12, 13, 14, 15
* **Supported ROMs:** HyperOS / MIUI, AOSP, LineageOS, Pixel Experience, OxygenOS, OneUI, etc.
* **Root Frameworks:** LSPosed, LSPosed (Ipatch / Mod), EdXposed

---

## 💖 Credits &amp; Attribution
* Original concept and base tricks by **Dark_Eyes_** ([dark_tricks](https://github.com/darkeyes84/dark_tricks)).
* Maintained, modernized, and enhanced for modern Android versions.
