# Module Development Guide

Magisk Next introduces several powerful new features for module developers to create interactive, engaging, and dynamic modules. This guide covers EVERYTHING you can do to enhance your Magisk Next modules.

## WebUI Dashboard
Modules can now provide a rich, interactive web-based dashboard directly within the Magisk Next app, seamlessly bridging web technologies with root-level system access via MagiskJS.

### Setup
1. Set `hasWebUI=true` in your `module.prop` file.
2. Create a `webroot/` directory in your module's root folder.
3. Add an `index.html` file inside `webroot/`. This will serve as the entry point.

### MagiskJS API
The Magisk Next app injects a Javascript interface called `MagiskJS` into the WebView. You can execute shell commands with root privileges directly from your Javascript code:
- `MagiskJS.exec(cmd)`: Executes a shell command and returns the standard output (stdout) as a string.
- `MagiskJS.execRoot(cmd)`: Explicitly executes a shell command with root privileges and returns stdout.
- `MagiskJS.toast(msg)`: Displays a native Android toast message on the screen.

*Note: The `webroot/` is copied to a secure cache directory at runtime. You can build advanced UIs using standard HTML/CSS/JS or modern web frameworks.*

## Action Scripts
Action scripts allow you to bind custom root shell scripts to a button within the Magisk Next app UI and to Android Quick Settings tiles for quick execution.

### Setup
Simply place an `action.sh` script in your module's root directory. Magisk Next will dynamically detect its presence (`hasAction=true`) and automatically render an Action button on your module's card.
When the user clicks the button, Magisk Next executes:
```sh
cd /data/adb/modules/$MODID && sh ./action.sh
```

## Module Banners
Enhance your module's visual appeal by adding a custom banner image that displays at the top of your module card in the Magisk Next app.

### Setup
There are two ways to add a banner:
1. **Remote URL:** Add `banner=https://example.com/banner.png` to your `module.prop`.
2. **Local File:** Place an image named `banner.png`, `banner.jpg`, or `banner.webp` in your module's root directory. Magisk Next will automatically load it.

## Custom Systemizer Configs
While the Magisk Next Toolbox offers an automated App Systemizer for users, module developers can also create custom systemizer configurations manually.
To systemize an app through your module, place the target APK in:
`$MODDIR/system/priv-app/<PackageName>/<PackageName>.apk`

This overlays the app into the system partition without physically modifying the device's system image.
