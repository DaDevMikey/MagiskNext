![](docs/images/logo.png)

[![Downloads](https://img.shields.io/badge/dynamic/json?color=green&label=Downloads&query=totalString&url=https%3A%2F%2Fraw.githubusercontent.com%2Ftopjohnwu%2Fmagisk-files%2Fcount%2Fcount.json&cacheSeconds=1800)](https://raw.githubusercontent.com/topjohnwu/magisk-files/count/count.json)

# Magisk Next

## Introduction

**Magisk Next** is a Work-In-Progress (WIP) community-driven fork of Magisk, the suite of open-source software for customizing Android (supporting Android 6.0+). 

Magisk Next aims to modernize the root management and module ecosystem, baking advanced stealth, stability watchdog safeguards, and visual refinement directly into the core system.

### New & Highlight Features in Magisk Next:

- **Native Stealth & DenyList**: Dynamic process isolation and partition unmounting built directly into the core `magiskd` daemon. Stealth is achieved natively without needing Zygisk/Shamiko module stacking.
- **Advanced Bootloop Protector**: A built-in startup watchdog that monitors the system booting process. If a system crash or module bootloop is detected within 90 seconds, Magisk Next automatically disables active modules and reboots into Safe Mode.
- **Ultimate Setup Wizard**: A premium, highly polished onboarding experience featuring dynamic Xiaomi-style morphing icons, an expanding circular reveal on completion, and a custom setup profile selector (Standard, Developer, or Module Creator) to configure app defaults.
- **Bouncy Overscroll Animations**: Beautiful, native iOS-like rubber-banding stretch transitions on lists and scrollable screens using non-clipping `graphicsLayer` overlay rendering, keeping AppBars and bottom menus static.
- **Developer Settings (AdminMainActivity)**: Deep diagnostics controls protected by a secure password (`MagiskNext2026`). Attempts to bypass or launch it via third-party Activity Launchers while developer settings are locked will cause an instant `SecurityException` crash.
- **Action Scripts**: Quick Settings tile support to run custom root shell scripts directly from your quick settings panel.
- **Module Hub**: A built-in discovery page to search, sort (by Name or Author), and install community-created modules online.
- **Toolbox Utilities**: Integrated device utilities including App Systemizer, Native App Downgrader, Private App Data Backup/Restore, and Module Backup/Restore packaging.
- **MagiskSU**: Robust root access control for applications.
- **MagiskBoot**: The most complete tool for unpacking and repacking Android boot images.

## Downloads & Code

Get updates and releases on the [Magisk Next Releases](https://github.com/DaDevMikey/MagiskNext/releases).

## Developer & User Documentation

For full guides on how to implement the Module Hub, WebUI Bridge, Action Scripts, and Banners into your modules, or to learn about all the user-facing tools, please see our dedicated [Documentation Website](https://dadevmikey.github.io/MagiskNext/docs).

Alternatively, you can run the documentation locally:
```bash
cd docs
npm install
npm run docs:dev
```
- [Official Stock Magisk Documentation](https://topjohnwu.github.io/Magisk/)
- [Zygisk module sample](https://github.com/topjohnwu/zygisk-module-sample)

## Bug Reports

**Only bug reports from Debug builds will be accepted.**

For installation issues, upload both boot image and install logs.<br>
For Magisk issues, upload boot logcat or dmesg.<br>
For Magisk app crashes, record and upload the logcat when the crash occurs.

## Translation Contributions

Default string resources for the Magisk app and its stub APK are located here:

- `app/core/src/main/res/values/strings.xml`
- `app/stub-res/src/main/res/values/strings.xml`

Translate each and place them in the respective locations (`[module]/src/main/res/values-[lang]/strings.xml`).

## License

    Magisk, including all git submodules are free software:
    you can redistribute it and/or modify it under the terms of the
    GNU General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
