#!/bin/bash

while getopts 'p:v:c' OPTION; do
  case "$OPTION" in
    p)
      PATH_TO_CONTRACTS="$OPTARG"
      echo "Path to contracts you wish to publish: $OPTARG"
      ;;
    v)
      CONTRACTS_VERSION="$OPTARG"
      echo "Version you wish to publish the contracts as: $OPTARG"
      ;;
    c)
      SETUP_SMART_CONTRACT=true
      echo "Running setup with smart contracts."
      ;;
    ?)
      echo "script usage: $(basename \$0) [-p <path to p8e contracts directory> -v <version to publish contracts as>]" >&2
      exit 1
      ;;
  esac
done
shift "$((OPTIND -1))"

function up {
  docker-compose -p p8e-contract-execution-environment -f service/local-docker/dependencies.yaml up --build -d

  sleep 2
  export VAULT_ADDR="http://127.0.0.1:8200"
  SECRET_PATH=kv2_originations
  sh service/local-docker/vault/init-and-unseal.sh $VAULT_ADDR $SECRET_PATH

  until vault status; do echo "Awaiting vault to be unsealed..."; sleep 5; done

  # local-originator
  < service/local-docker/vault/secrets/local-originator.json vault kv put $SECRET_PATH/originators/00000000-0000-0000-0000-000000000001 - > /dev/null
  < service/local-docker/vault/secrets/local-originator.json vault kv put $SECRET_PATH/originators/tp1qy2mqx5x22a400pgd5p6u7mq9shxzvh767jar0 - > /dev/null
  # local-servicer
  < service/local-docker/vault/secrets/local-servicer.json vault kv put $SECRET_PATH/originators/00000000-0000-0000-0000-000000000002 - > /dev/null
  < service/local-docker/vault/secrets/local-servicer.json vault kv put $SECRET_PATH/originators/tp1xr3wfqzlcz469wkex5c3ylaq8pq97crhsg57gd - > /dev/null
  # local-dart
  < service/local-docker/vault/secrets/local-dart.json vault kv put $SECRET_PATH/originators/00000000-0000-0000-0000-000000000003 - > /dev/null
  < service/local-docker/vault/secrets/local-dart.json vault kv put $SECRET_PATH/originators/tp1s2c62ke0mmwhqxguf7e2pt6e98yq38m4atwhwl - > /dev/null
  # local-portfolio-manager
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/00000000-0000-0000-0000-000000000004 - > /dev/null
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/tp1mryqzguyelef5dae7k6l22tnls93cvrc60tjdc - > /dev/null
  # local-controller-a
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/00000000-0000-0000-0000-000000000005 - > /dev/null
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/tp138jtpz5zxa6yc33s0fk2jy9vahqcuvvtwnrzge - > /dev/null
  # local-controller-b
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/00000000-0000-0000-0000-000000000006 - > /dev/null
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/tp1pz4tt4j802j2y3avs5mwy9uyyxtx7k5r8qlehw - > /dev/null
  # local-validator
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/00000000-0000-0000-0000-000000000007 - > /dev/null
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/tp1he6rfnxyx2ssqyknq4ayzf5j46dp29pqt6z708 - > /dev/null
  # local-investor
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/00000000-0000-0000-0000-000000000008 - > /dev/null
  < service/local-docker/vault/secrets/local-portfolio-manager.json vault kv put $SECRET_PATH/originators/tp1f99dgtxxjmgczjsuc48utq4lkk0uhx6eqfqju0 - > /dev/null

  docker ps -a

    if [ "$SETUP_SMART_CONTRACT" = true ]; then
        echo "Setting up smart contracts!"
        upload_classification_contract
    fi
}

function publish() {
    set -e
    if [ -z ${PATH_TO_CONTRACTS+x} ]; then
        echo "Provide a valid path to the contracts directory you wish to publish using the -p argument."
        exit 1
    fi

    if [ -z "${CONTRACTS_VERSION}" ]; then
        echo "Provide a valid version string you wish to publish the contracts as using the -v argument."
        exit 1
    fi

   source ./service/local-docker/bootstrap.env
   FULL_PATH=$(realpath "$PATH_TO_CONTRACTS")
   export FULL_PATH

    if [[ -d "$FULL_PATH" ]]; then
        pushd "$PATH_TO_CONTRACTS" > /dev/null
        ./gradlew p8eClean p8eCheck p8eBootstrap --info && ./gradlew publishToMavenLocal -Pversion="$CONTRACTS_VERSION" -xsignMavenPublication --info
        popd > /dev/null
    else
        echo "Invalid path. Provide a valid path to the contracts directory you wish to publish."
        exit 1
    fi
}

function down {
  docker-compose -p p8e-contract-execution-environment -f service/local-docker/dependencies.yaml down
  docker volume prune -f
}

function bounce {
   down
   up
}

function build_classification() {
    set -e

    if [ -z ${PATH_TO_CONTRACTS+x} ]; then
        echo "Provide a valid path to the smart contracts directory you wish to store on provenance using the -p argument."
        exit 1
    fi

    FULL_PATH=$(realpath "$PATH_TO_CONTRACTS")
    export FULL_PATH

    if [[ -d "$PATH_TO_CONTRACTS" ]]; then
        pushd "$PATH_TO_CONTRACTS" > /dev/null
        cargo build
        make optimize
        popd > /dev/null
        cp "$PATH_TO_CONTRACTS"/artifacts/asset_classification_smart_contract.wasm service/local-docker/prov-init/contracts/asset_classification_smart_contract.wasm
    else
        echo "Invalid path. Provide a valid path to the contracts directory you wish to publish."
        exit 1
    fi
}

function setup() {
      if ! command -v brew &> /dev/null
      then
          echo "This script can only install the prerequisite packages via Homebrew, which was not found in the path. Please manually install the packages instead."
          exit 1
      else
          brew install coreutils
      fi
      if ! command -v docker &> /dev/null
      then
          brew install docker
      fi
      if ! command -v vault &> /dev/null
      then
          brew tap hashicorp/tap
          brew install hashicorp/tap/vault
      fi
      if ! command -v rust &> /dev/null
      then
          brew install rust
      fi
      if ! command -v jq &> /dev/null
      then
          brew install jq
      fi
}

function upload_classification_contract() {
    echo "Uploading contract to provenance!"
    upload=$(docker exec provenance provenanced tx wasm store contracts/asset_classification_smart_contract.wasm \
                     --instantiate-only-address tp1v5d9uek3qwqh25yrchj20mkgrksdfyyxhnsdag \
                     --from tp1v5d9uek3qwqh25yrchj20mkgrksdfyyxhnsdag \
                     --chain-id chain-local \
                     --gas auto \
                     --gas-prices="1905nhash" \
                     --gas-adjustment=1.1 \
                     --broadcast-mode block \
                     --testnet \
                     --output json \
                     --yes)

    code_id=$(echo "$upload" | jq -r '.logs[] | select(.msg_index == 0) | .events[] | select(.type == "store_code") | .attributes[0].value')
    echo "Upload complete. Code id: $code_id"
    instantiate=$(docker exec provenance provenanced tx wasm instantiate "$code_id" \
                       '{
                         "base_contract_name": "assetclassificationalias.pb",
                         "bind_base_name": true,
                         "asset_definitions": '"$(cat service/local-docker/prov-init/contracts/asset_definitions.json)"',
                         "is_test": true
                       }' \
                       --admin "tp1v5d9uek3qwqh25yrchj20mkgrksdfyyxhnsdag" \
                       --from tp1v5d9uek3qwqh25yrchj20mkgrksdfyyxhnsdag \
                       --label examples \
                       --chain-id chain-local \
                       --gas auto \
                       --gas-prices="1905nhash" \
                       --gas-adjustment=1.1 \
                       --broadcast-mode block \
                       --testnet \
                       --output json \
                       --yes | jq)

    contract_address=$(echo "$instantiate" | jq '.logs[] | select(.msg_index == 0) | .events[] | select(.type == "instantiate") | .attributes[] | select(.key == "_contract_address") | .value')
    echo "Asset classification contract fully setup! contract address: $contract_address"
}

${1}
