#!/bin/bash
set -e

export VAULT_ADDR=$1
export SECRET_PATH=$2

printf "\nEnabling KV store..\n\n"
vault secrets enable -version=2 -path=$SECRET_PATH kv

exit 0;
