#!/usr/bin/env bash

mvn clean install
rm -rf *.pb
proto_dependencies=$(ls -1 target/protoc-dependencies/*|grep target|cut -d":" -f1|sed s/^/-I/g|tr '\n' ' ')
protoc -I.  ${proto_dependencies} -Isrc/main/proto --include_imports  --include_source_info  --descriptor_set_out=sample_proto_descriptor_set.pb  src/main/proto/hudsonservice.proto

docker run -it --rm --name envoy -p 51051:51051 -p 9901:9901 -v "$(pwd)/sample_proto_descriptor_set.pb:/tmp/sample_proto_descriptor_set.pb:ro" -v "$(pwd)/envoy.yml:/etc/envoy/envoy.yaml:ro" envoyproxy/envoy
