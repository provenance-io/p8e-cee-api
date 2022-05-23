#!/bin/bash
set -e

export VAULT_ADDR=$1
export SECRET_PATH=$2

# init vault
printf "\nInitializing Vault..\n"
vault operator init | tee vault/init.output >/dev/null
cat vault/init.output | grep '^Unseal' | rev | cut -d ' ' -f 1 | rev > vault/keys.output
while IFS=\= read -r key; do
  UNSEAL_KEYS+=($key)
done < vault/keys.output

# export root token
export ROOT_TOKEN=$(cat vault/init.output | grep '^Initial' | rev | cut -d ' ' -f 1 | rev)
export VAULT_TOKEN=$ROOT_TOKEN
export ENVIRONMENT=test

# unseal vault
#  0 - unsealed
#  1 - error
#  2 - sealed
printf "\nUnsealing Vault..\n"
KEY_INDEX=1
while [[ $(vault status > /dev/null)$? != 0 ]]; do
  sleep 1
  vault operator unseal $(echo "${UNSEAL_KEYS[$KEY_INDEX]}") > /dev/null
  KEY_INDEX=$(( $KEY_INDEX + 1 ))
done
#vault status

printf "\nVault has been unsealed!\n"
#env | grep VAULT

printf "\nLogging in to Vault..\n"
vault login -no-print=true $ROOT_TOKEN
printf "\nLogged in!\n"

printf "\nEnabling KV store..\n\n"
vault secrets enable -version=2 -path=$SECRET_PATH kv
printf "\nView/edit secret at %s/ui/vault/secrets/%s/list\n" "${VAULT_ADDR}" "${SECRET_PATH}"
printf "Log in using token: %s\n" "$(cat ~/.vault-token)"

exit 0;
