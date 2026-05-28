# User Guide and Features

Magisk Next brings several powerful features accessible directly from the application. This guide covers every way a user can make use of the app.

## Native Stealth and DenyList
Magisk Next integrates unmounting and process isolation natively within the Magisk daemon. This eliminates the need for stacking multiple modules (like Zygisk, Shamiko, and Play Integrity Fix) to hide root from apps.

- **Enforce DenyList:** You can strictly hide Magisk from selected apps by enabling "Enforce DenyList" in settings.
- **Per-App Unmounting:** When an app is on the DenyList, Magisk Next dynamically unmounts all module modifications and isolates the process, providing a clean, unrooted environment for that specific app.
- *(Note on SuList vs DenyList: Magisk Next relies on a highly optimized DenyList approach to maintain system stability while accurately targeting apps that require stealth.)*

## Advanced Bootloop Protector
Built-in protection against `system_server` crashes and bootloops gives you peace of mind when installing untested modules. 
- If a module causes your device to bootloop, Magisk Next will automatically detect the failure to boot within 90 seconds.
- It will safely disable modules and automatically reboot your device into safe mode, allowing you to remove the problematic module without needing custom recovery or timing volume button presses.

## Action Scripts
Action scripts allow you to run complex workflows with a single tap. 
- If a module includes an `action.sh` script, you will see an "Action" button on the module's card in the Magisk app.
- You can also bind these action scripts to **Android Quick Settings tiles** for instant access from anywhere, without even opening the Magisk app.

## Toolbox Power Features
The Magisk Next Toolbox provides powerful device management utilities built right into the app:

### App Systemizer
Convert user-installed apps into system apps dynamically. The Toolbox generates a custom Magisk module that mounts the selected app into `/system/priv-app/`, granting it system privileges across reboots.

### Native App Downgrader
Force install an older version of an app over a newer one without losing data. Select an older APK from your storage, and the Toolbox will bypass Android's downgrade restrictions.

### App Data Backup
Extract and compress the private `/data/data/` directory of any installed app. The backup is safely stored as a `tar.gz` archive in your Downloads folder.

### Backup & Restore Modules
Package all of your active modules into a single `tar.gz` archive. This is perfect for migrating devices, testing, or simply backing up your setup. You can restore this archive at any time through the Toolbox.

## Batch Uninstall
When cleaning up your setup, you can select multiple modules and queue them for batch uninstallation. A single reboot will safely remove all queued modules at once.
