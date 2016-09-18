# Compile libxml

* Get latest source code from [here](ftp://xmlsoft.org/libxml2/) and extract the contents to `src` folder.

* Configure :

  ```bash
  cd src
  ./configure --host=$TARGET --enable-static --disable-shared --prefix="$(`echo realpath '../output'`)"
  ```

* Build :
  ```bash
  cd src
  make libxml2.la
  ```

* Copy built binaries to `output` folder

  ```bash
  mkdir output/bin
  cp src/xml2-config output/bin
  cp src/include output/include -R
  cp src/.libs output/lib -R
  ```