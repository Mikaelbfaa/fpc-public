#/bin/bash

# generate $nfiles random content files in a dataset directory
nfiles=$1

mkdir -p dataset

for i in `seq 1 $nfiles`
do
	# Generate files between 1MB and 10MB
	# 1MB = 1048576 bytes, adding random up to 9MB
	file_size=$((1 + RANDOM * 20))
	head -c $file_size < /dev/urandom > dataset/file.$i
done
