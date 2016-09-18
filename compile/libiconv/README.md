# Compile libiconv

* Download source code from [here](https://www.gnu.org/software/libiconv/)

* Update `config.guess`, `config.sub` files in `src/build-aux` & `src/libcharset/build-aux`

* Configure :

  ```bash
  ./configure --host=arm-linux-androideabi --prefix="$(`echo realpath '../output'`)"
  ```

* `make` & `make install`
