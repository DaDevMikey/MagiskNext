# Introduction

Magisk Next is a fork of Magisk that focuses on native stealth, improved module development experience, and safety.

## Getting Started

- [User Guide and Features](features.md): Discover EVERYTHING you can do with Magisk Next, including Toolbox, Batch Uninstall, App Downgrading, Bootloop Protection, App Profiles, SuList vs DenyList, Unmounting modules per app, and more.

## Highlights

### Native Stealth
By integrating unmounting and process isolation natively within the Magisk daemon, Magisk Next eliminates the need for stacking multiple modules like Zygisk, Shamiko, and Play Integrity Fix to hide root from apps.

### Advanced Bootloop Protector
Built-in protection against system_server crashes and bootloops. If a module causes your device to bootloop, Magisk Next will automatically detect the failure to boot within 90 seconds, disable modules, and safely reboot.

### Action Scripts
Bind your custom root shell scripts to Android Quick Settings tiles. Quick and easy access to complex workflows.
