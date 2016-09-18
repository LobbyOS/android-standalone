# Compile OpenSSL

We are following [this manual](https://wiki.openssl.org/index.php/Android#OpenSSL_Library)

* Get the latest source code from [here](https://www.openssl.org/source/) and extract the contents to `src` folder.

* Get [setenv-android.sh](https://wiki.openssl.org/images/7/70/Setenv-android.sh) and place it in `src`. Change the variables inside that file.

* Set up environment
  ```bash
  cd src
  . ./setenv-android.sh
  ```

* Configure for android :
  ```bash
  cd src
  export openssldir="$(`echo realpath '../output'`)"
  export CFLAGS="-Wl,--hash-style=both"
  ./config shared no-ssl2 no-ssl3 no-comp no-hw no-engine --openssldir="$openssldir" --prefix="$openssldir"
  ```

* Build
  ```bash
  make depend
  make
  make install_sw install_ssldirs
  ```

Now the binaries will be in `output` folder.
