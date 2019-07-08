#!/usr/bin/env bash
SRC_DIR=/home/jason/work/code/ja-netty-study/proto
DST_DIR=/home/jason/work/code/ja-netty-study/file-transfer-proto/src/main/java/

/usr/local/proto/bin/protoc -I=$SRC_DIR --java_out=$DST_DIR filetransfer.proto