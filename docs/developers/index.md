# Magisk Next Developers Guide

Welcome to the Magisk Next developer documentation! Here you will find all the new features and tools available for developers.

- [Module Development Guide](modules.md): Learn EVERYTHING you can do in modules, including WebUI dashboards, Action scripts, Banners, and Custom systemizer configs.
- [App Development Guide](app.md): Learn how to modify and build the Magisk Next application codebase.

## The Module Hub

The Module Hub is a centralized repository for community modules, built right into the Magisk Next app. 

To get your module listed in the Discovery Hub, you simply need to host your module on a public Git repository and submit a pull request to the `MagiskNext-Modules` repository.

### Updating Your `module.prop`

To take full advantage of Magisk Next, there are a few new properties you can add to your `module.prop` file. Please refer to the [Module Development Guide](modules.md) for full details on implementing these features:

```properties
# Add a custom banner image url
banner=https://example.com/banner.png

# Set to true if your module has a MagiskJS WebUI
hasWebUI=true
```
