#!/bin/bash
set -e

source ./service/local-docker/vault/.env

VAULT_BINARY="docker exec -i --env-file ./service/local-docker/vault/.env $VAULT_DOCKER_CONTAINER_NAME vault"
if ! docker exec -i "$VAULT_DOCKER_CONTAINER_NAME" vault --help 2>/dev/null 1>&2
then
    echo "No Docker container named '$VAULT_DOCKER_CONTAINER_NAME' was found running..."
    if ! command -v vault &> /dev/null
    then
        echo "Vault must be running under an appropriately named Docker container or local machine installation"
        exit 1
    else
        echo "Found a local installation of Vault to use instead."
        VAULT_BINARY="vault"
    fi
fi

echo "$VAULT_TOKEN" | tee ~/.vault-token

printf "\nEnabling KV store..\n\n"
$VAULT_BINARY secrets enable -version=2 -path="$SECRET_PATH" kv
printf "\nView/edit secret at %s/ui/vault/secrets/%s/list\n" "${VAULT_ADDR}" "${SECRET_PATH}"
printf "Log in using token: %s\n" "$VAULT_TOKEN"

exit 0;
