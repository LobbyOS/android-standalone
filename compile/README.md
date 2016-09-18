## Compiling Dependencies

The following steps are common for all compilation.

* Get [Android NDK](https://developer.android.com/ndk/downloads/index.html)

* Edit the `setenv.sh` file, change `NDK_ROOT` and other variables.

* Set environment for cross compiling :

  ```bash
  . ./setenv.sh
  ```

  ^ Mind the `.` at the beginning. It's not a typo.

* Some programs may not understand `arm-linux-androideabi` or `i686-linux-android`. If so update `config.guess` & `config.sub` :

  ```bash
  wget -O config.guess 'http://git.savannah.gnu.org/gitweb/?p=config.git;a=blob_plain;f=config.guess;hb=HEAD' && wget -O config.sub 'http://git.savannah.gnu.org/gitweb/?p=config.git;a=blob_plain;f=config.sub;hb=HEAD'
  ```

* For x86 compilation, use `--host=i686-linux-android` in configure script

Compile order :

* php
  * libtool
  * libxml2
  * libmcrypt
  * libcrypt
  * openssl
  * curl
    * openssl

Instructions for compiling each can be found in their respective folder inside the `compile` folder in this repo.

For each folder in `compile`, `compile/[package]/src` will contain the source code and `compile/[package]/output` will be the installation (prefix) directory of package.

## Auto Compile

A script to compile everything automatically is available. It is recommended to run this only when you successfully compile everything manually.

Android ELF Cleaner won't be auto compiled. You have to do it manually.

Just calling the `auto-compile.sh` script will compile dependencies according to their order and finally PHP.

Errors are possible while auto compiling. Read the respective folder's README.md for fixing it.

## Android ELF Cleaner

An extra package is needed to make binaries perfect. For this, [Android ELF Cleaner]() is ran through all the binary files.
