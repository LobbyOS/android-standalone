# Compile PHP

Overwrite `src/.gitignore` with `output/.gitignore`

PHP version currently used : 7.0.10

* Get latest PHP Source Code from [here](http://www.php.net/downloads.php) and extract the source code into `src/`

* Install `php` on your host system:

  ```
  sudo apt install php pear
  sudo pear install pear/PHP_Archive
  ```

* Compile dependencies. [List of dependencies](https://github.com/LobbyOS/android-standalone#compiling-dependencies)

* Edit `configure` script and find `cannot run test program while cross compiling`

  In all of those finds, just above them replace `if test "$cross_compiling" = yes; then :` with `if test "$cross_compiling" = no; then :`

  [Reference](https://bugs.php.net/patch-display.php?bug_id=61839&patch=php-configure-cross.patch&revision=latest)

* Edit `ext/standard/dns.c` and add `#undef HAVE_RES_NSEARCH` at the top after all includes.

  Also add prefix `__` to strings `res_ninit`, `dn_skipname` in `dns.c` which would make it into this :

  ```
  __res_ninit
  __dn_skipname
  ```

* Set environment

  ```bash
  export CFLAGS="$CFLAGS -pie -fPIE -fpic -fPIC -ldl -lcrypt -lc"
  export CPPFLAGS="$CPPFLAGS -I$(`echo realpath '../../openssl/output/include'`) -I$(`echo realpath '../../libxml2/output/include'`) -I$(`echo realpath '../../libtool/output/include'`) -I$(`echo realpath '../../libiconv/output/include'`) -I$(`echo realpath '../../libcrypt/output/include'`) -I$SYSROOT/usr/include"
  export LDFLAGS="$LDFLAGS -L$(`echo realpath '../../openssl/output/lib'`) -L$(`echo realpath '../../libxml2/output/lib'`) -L$(`echo realpath '../../libtool/output/lib'`) -L$(`echo realpath '../../libiconv/output/lib'`) -L$(`echo realpath '../../libcrypt/output/lib'`) -I$SYSROOT/usr/lib"
  ```

* Use this to configure the build :

  ```bash
  cd src
  ./configure --host=$TARGET \
    --with-pic \
    --with-sqlite3 --with-pdo-mysql --with-pdo-sqlite \
    --with-curl="$(`echo realpath '../../curl/output'`)" \
    --with-openssl="$(`echo realpath '../../openssl/output'`)" \
    --with-mcrypt="$(`echo realpath '../../libmcrypt/output'`)" \
    --with-libxml-dir="$(`echo realpath '../../libxml2/output'`)" \
    --with-config-file-path=php.ini --with-config-file-scan-dir=. \
    --enable-cli --enable-pdo --enable-mbstring --enable-zip --enable-intl \
    --without-iconv --disable-cgi --disable-rpath --enable-opcache=no \
    --disable-posix \
    --prefix="$(`echo realpath '../output'`)"
  ```

* Edit `Makefile` and replace strings `@$(PHP_PHARCMD_EXECUTABLE)` & `$(top_builddir)/sapi/cli/php` with `php -d extension=phar.so`

* Edit `Makefile` and replace accordingly :

| String | Replace With |
| ------ | ------------ |
| -I/usr/include | -I${SYSROOT}/usr/include |
| -L/usr/lib/i386-linux-gnu | -L${SYSROOT}/usr/lib |

* While building, `Zend/zend_operators.h` file may cause an error. In this case, edit the file at the line where error occured and make the `if` condition false.

* Run `make` & `make install`

* Include all the compiled binaries in one folder to run `php` :

  ```bash
  cd . # Root directory [compile/php]
  mkdir php php/extensions
  cp ./output/bin/php ./php/php-cli
  cp ./php-wrapper ./php/php
  cp ../curl/output/lib/libcurl.so ./php/extensions
  cp ../libtool/output/lib/libltdl.so ./php/extensions
  cp ../openssl/output/lib/libcrypto.so.1.1 ./php/extensions
  cp ../openssl/output/lib/libssl.so.1.1 ./php/extensions
  ```

* Running binary on Android API 22 will throw an error because of `VERNEED` & `VERNUM` dynamic entries. This bug doesn't exist on `>=22`

  So we create another package for API 22. `php-22` folder will have the binaries for API 22 :

  ```bash
  mkdir php-22
  cp ./php/* ./php-22 -r
  ../android-elf-cleaner/output/aec ./php-22/php-cli ./php-22/extensions/*
  ```

* Zend Opcache is not enabled, but the binary is available. Get it from [here](https://lobby.subinsb.com/services/android/php-arm.zip).

Now we have the `php` binary and the dependencies of it in `php` (For API Level > 22) and `php-22` (For API Level <= 22) directory.

You can test the made binaries out by :

```
cd . # Root directory [compile/php]
adb push ./php /sdcard/php
adb shell
$ run-as com.appname
$ cp /sdcard/php ./
$ chmod 0744 ./php/php
$ sh ./php
```

# Packaging

Zip `php` to `php-[arch].zip` and `php-22` to `php-[arch]-22.zip`. Don't forget to rename `php-22` folder to `php` in the archive.