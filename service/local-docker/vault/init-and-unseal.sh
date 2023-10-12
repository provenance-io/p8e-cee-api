#!/bin/bash
set -e

export VAULT_ADDR=$1
export SECRET_PATH=$2
export VAULT_TOKEN=root

echo "root" | tee ~/.vault-token

printf "\nEnabling KV store..\n\n"
vault secrets enable -version=2 -path=$SECRET_PATH kv
printf "\nView/edit secret at %s/ui/vault/secrets/%s/list\n" "${VAULT_ADDR}" "${SECRET_PATH}"
printf "Log in using token: root"

exit 0;
