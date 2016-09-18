# Compile mcrypt

* Get source code from [here](https://sourceforge.net/projects/mcrypt/)

* Extract source code to `src/`

* Apply [this](http://stackoverflow.com/a/8603829/1372424)

* Configure build

  ```bash
  ./configure --host=$TARGET --prefix="$(`echo realpath '../output'`)"
  ```
* `make` & `make install`
