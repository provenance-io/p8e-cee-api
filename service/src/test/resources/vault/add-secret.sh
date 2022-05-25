#!/bin/bash
set -e

export VAULT_ADDR=$1
export SECRET_PATH=$2
export ORIGINATOR_UUID=$3
export PUBLIC_ENCRYPTION_KEY=$4
export PUBLIC_SIGNING_KEY=$5
export PUBLIC_AUTHORIZATION_KEY=$6
export PRIVATE_ENCRYPTION_KEY=$7
export PRIVATE_SIGNING_KEY=$8
export PRIVATE_AUTHORIZATION_KEY=$9
export MNEMONIC="${10}"

printf "\nInserting dev key pair as LOCAL secret..\n"

vault kv put $SECRET_PATH/originators/$ORIGINATOR_UUID \
      private_encryption_key=$PRIVATE_ENCRYPTION_KEY \
      public_encryption_key=$PUBLIC_ENCRYPTION_KEY \
      private_signing_key=$PRIVATE_SIGNING_KEY \
      public_signing_key=$PUBLIC_SIGNING_KEY \
      public_auth_key=$PUBLIC_AUTHORIZATION_KEY \
      private_auth_key=$PRIVATE_AUTHORIZATION_KEY \
      private_mnemonic="$MNEMONIC"
      > /dev/null

printf "\n"
