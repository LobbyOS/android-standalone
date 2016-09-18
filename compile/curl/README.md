# Compile cURL

* Get source code from [here](https://curl.haxx.se/download.html) and extract to `src` folder

* Configure build :

  ```bash
  cd src
  ./configure --host=$TARGET --with-ssl='../../openssl/output' --prefix="$(`echo realpath ../output`)"
  ```

* Build

  ```bash
  make
  make install
  ```
