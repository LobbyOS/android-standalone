# Android Standalone Package

A Standalone Lobby Installation for Android.

This repository has the Android app's [project files](https://github.com/LobbyOS/android-standalone/tree/master/App) and the tools required to [cross compile PHP for android](https://github.com/LobbyOS/android-standalone/tree/master/compile).

## HowTo

`App` directory is the android studio project.

`compile` directory is where the PHP is compiled.

`Lobby` represents the Lobby installation folder made by app. This is similar to [Lobby Linux Standalone](https://github.com/LobbyOS/linux-standalone/tree/master/Lobby)

## Contribution

When you contribute, you should ignore the `compile/[package]/src`, `compile/[package]/output`, `compile/php/php` & `compile/php/php-22` directories by adding this `.gitignore` file in those folders :

```
# Ignore everything in this directory
*
# Except this file
!.gitignore
```
