#!/bin/bash

set -e
# Any subsequent(*) commands which fail will cause the shell script to exit immediately

# Path to folder where this script is
compileDir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function updateBuildConfigFiles(){
	wget -O config.guess 'http://git.savannah.gnu.org/gitweb/?p=config.git;a=blob_plain;f=config.guess;hb=HEAD'
	wget -O config.sub 'http://git.savannah.gnu.org/gitweb/?p=config.git;a=blob_plain;f=config.sub;hb=HEAD'
}

function setenv(){
  . $compileDir/setenv.sh
}

# Compile libtool

function libtool(){
  setenv
  cd $compileDir/libtool/src

  make clean

  ./configure --host=$TARGET --prefix="$(`echo realpath '../output'`)"

  make
  make install
}

# Compile libxml2

function libxml2(){
  setenv
  cd $compileDir/libxml2/src

  make clean

  ./configure --host=$TARGET --enable-static --disable-shared --prefix="$(`echo realpath '../output'`)"

  make libxml2.la

  cd ..

  mkdir -p output/bin
  cp src/xml2-config output/bin
  cp src/include output/include -R
  cp src/.libs output/lib -R
}

# Compile libmcrypt

function libmcrypt(){
  setenv
  cd $compileDir/libmcrypt/src

  updateBuildConfigFiles

  make clean

  ./configure --host=$TARGET --prefix="$(`echo realpath '../output'`)"

  make
  make install
}

# Compile libcrypt

function libcrypt(){
  setenv
  cd $compileDir/libcrypt/src

  export prefix="$(`echo realpath '../output'`)"
  sed -i -e "s~CC=.*~CC=$TARGET-gcc --prefix=$prefix~g" makefile
  sed -i -e "s~LD=.*~LD=$TARGET-ld --prefix=$prefix~g" makefile
  sed -i -e "s~AR=.*~AR=$TARGET-ar --prefix=$prefix~g" makefile

  make clean
  make

  cd ..

  mkdir -p output/ output/lib output/include
  cp src/libcrypt.a output/lib
  cp src/crypt.h output/include
  cp src/mpi.h output/include
  cp src/mpi-config.h output/include
}

# Compile OpenSSL

function openssl(){
  setenv
  cd $compileDir/openssl/src

  sed -i -e "s~ANDROID_NDK_ROOT=.*~ANDROID_NDK_ROOT=$NDK_ROOT~g" setenv-android.sh
  sed -i -e "s~_ANDROID_EABI=.*~_ANDROID_EABI=$TOOLCHAIN~g" setenv-android.sh
  sed -i -e "s~_ANDROID_ARCH=.*~_ANDROID_ARCH=$ANDROID_ARCH~g" setenv-android.sh
  sed -i -e "s~_ANDROID_API=.*~_ANDROID_API=$PLATFORM~g" setenv-android.sh

  . ./setenv-android.sh

  make clean

  export openssldir="$(`echo realpath '../output'`)"
  export CFLAGS="$CFLAGS -Wl,--hash-style=both"
  ./config shared no-ssl2 no-ssl3 no-comp no-hw no-engine --openssldir="$openssldir" --prefix="$openssldir"

  make depend
  make
  make install_sw install_ssldirs
}

# Compile cURL

function curl(){
  setenv
  cd $compileDir/curl/src

  ./configure --host=$TARGET --with-ssl='../../openssl/output' --prefix="$(`echo realpath ../output`)"

  make clean
  make
  make install
}

# Compile PHP

function php(){
  cd $compileDir/php/src

  . $compileDir/setenv.sh

  export CFLAGS="$CFLAGS -pie -fPIE -fpic -fPIC -ldl -lcrypt -lc -lsupc++ -lstdc++ -lstlport_static"
  export CPPFLAGS="$CPPFLAGS -I$(`echo realpath '../../openssl/output/include'`) -I$(`echo realpath '../../libxml2/output/include'`) -I$(`echo realpath '../../libtool/output/include'`) -I$(`echo realpath '../../libiconv/output/include'`) -I$(`echo realpath '../../libcrypt/output/include'`)"
  export LDFLAGS="$LDFLAGS -L$(`echo realpath '../../openssl/output/lib'`) -L$(`echo realpath '../../libxml2/output/lib'`) -L$(`echo realpath '../../libtool/output/lib'`) -L$(`echo realpath '../../libiconv/output/lib'`) -L$(`echo realpath '../../libcrypt/output/lib'`)"

  ./configure --host=$TARGET \
    --with-pic \
    --with-sqlite3 --with-pdo-mysql --with-pdo-sqlite \
    --with-curl="$(`echo realpath '../../curl/output'`)" \
    --with-openssl="$(`echo realpath '../../openssl/output'`)" \
    --with-mcrypt="$(`echo realpath '../../libmcrypt/output'`)" \
    --with-libxml-dir="$(`echo realpath '../../libxml2/output'`)" \
    --with-config-file-path=php.ini --with-config-file-scan-dir=. \
    --enable-cli --enable-pdo --enable-mbstring --enable-zip --enable-intl \
    --without-iconv --disable-cgi --disable-rpath --enable-opcache \
    --disable-posix \
    --prefix="$(`echo realpath '../output'`)"

  sed -i -e 's~@$(PHP_PHARCMD_EXECUTABLE)~php -d extension=phar.so~g' Makefile
  sed -i -e 's~$(top_builddir)/sapi/cli/php~php -d extension=phar.so~g' Makefile

  sed -i -e 's~-I/usr/include~-I${SYSROOT}/usr/include~g' Makefile
  sed -i -e 's~-L/usr/lib/i386-linux-gnu~-L${SYSROOT}/usr/lib~g' Makefile

  make clean
  make
  make install

  cd ..

  mkdir -p php php/extensions
  cp ./output/bin/php ./php/php-cli
  cp ./php-wrapper ./php/php
  cp ../curl/output/lib/libcurl.so ./php/extensions
  cp ../libtool/output/lib/libltdl.so ./php/extensions
  cp ../openssl/output/lib/libcrypto.so.1.1 ./php/extensions
  cp ../openssl/output/lib/libssl.so.1.1 ./php/extensions

  chrpath --delete php/php-cli php/extensions/*

  mkdir -p php-22
  cp ./php/* ./php-22 -r
  ../android-elf-cleaner/output/aec ./php-22/php-cli ./php-22/extensions/*
}

if [ -z $1 ]; then
  libtool
  libxml2
  libmcrypt
  libcrypt
  openssl
  curl
  php
else
  $1
fi