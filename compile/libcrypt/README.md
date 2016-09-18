# Compile libcrypt

* Get source code from [here](https://sourceforge.net/projects/libcrypt/) and extract to `src`

* Edit `makefile` and set `gcc`, `ld` etc. to use

* `make`

* Make the output directory
  ```
  cd . # compile/libcrypt
  mkdir output/ output/lib output/include
  cp src/libcrypt.a output/lib
  cp src/crypt.h output/include
  cp src/mpi.h output/include
  cp src/mpi-config.h output/include
  ```
