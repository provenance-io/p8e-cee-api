#!/bin/bash

printf "\nCopying Vault config to Docker volume..\n\n"

rm -rf service/docker/volumes
mkdir -p service/docker/volumes/{config,file,logs}

cat > service/docker/volumes/config/vault.json << EOF
{
  "backend": {
    "file": {
      "path": "/vault/file"
    }
  },
  "listener": {
    "tcp":{
      "address": "0.0.0.0:8200",
      "tls_disable": 1
    }
  },
  "ui": true
}
EOF
