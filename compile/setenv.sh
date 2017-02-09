#!/bin/bash

# BEGIN CONFIG

NDK_ROOT="/media/simsu/hello/Android/crystax-ndk-10.3.2/"

# arm-linux-androideabi or i686-linux-android
TARGET="arm-linux-androideabi"

# arm-linux-androideabi-4.9 or x86-4.9
TOOLCHAIN="arm-linux-androideabi-4.9"

# arch-x86 or arch-arm
ANDROID_ARCH=arch-arm

# platform
PLATFORM="android-16"

SYSROOT="$PLATFORM/$ANDROID_ARCH/"

# END CONFIG

for host in "linux-x86_64" "linux-x86" "darwin-x86_64" "darwin-x86"
do
  if [ -d "$NDK_ROOT/toolchains/$TOOLCHAIN/prebuilt/$host/bin" ]; then
    ANDROID_TOOLCHAIN="$NDK_ROOT/toolchains/$TOOLCHAIN/prebuilt/$host/bin/"
    break
  fi
done

# Error checking
if [ -z "$ANDROID_TOOLCHAIN" ] || [ ! -d "$ANDROID_TOOLCHAIN" ]; then
  echo "Error: ANDROID_TOOLCHAIN is not valid. Please edit this script."
  echo "$ANDROID_TOOLCHAIN"
  exit 1
fi

export PATH="$ANDROID_TOOLCHAIN:$NDK_ROOT:$PATH"
export SYSROOT="$NDK_ROOT/platforms/$SYSROOT"
export CFLAGS="--sysroot=${SYSROOT} -I${SYSROOT}/usr/include -I${NDK_ROOT}/sources/icu/56.1/include -I${NDK_ROOT}/sources/cxx-stl/stlport/stlport -I."
export CPPFLAGS=$CFLAGS
export LDFLAGS="--sysroot=${SYSROOT} -L${SYSROOT}/usr/lib -L${NDK_ROOT}/sources/crystax/libs/armeabi -L${NDK_ROOT}/sources/icu/56.1/libs/armeabi -L${NDK_ROOT}/sources/cxx-stl/stlport/libs/armeabi -L${NDK_ROOT}/sources/cxx-stl/gnu-libstdc++/4.9/libs/armeabi"