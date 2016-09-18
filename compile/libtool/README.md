# Compile libtool

* Download source code from [here](https://www.gnu.org/software/libtool/)

* Configure with :

  ```bash
  ./configure --host=$TARGET --prefix="$(`echo realpath '../output'`)"
  ```

* `make` & `make install`
