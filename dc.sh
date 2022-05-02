#!/bin/bash

while getopts 'p:' OPTION; do
  case "$OPTION" in
    p)
      PATH_TO_CONTRACTS="$OPTARG"
      echo "Path to contracts you wish to publish: $OPTARG"
      ;;
    ?)
      echo "script usage: $(basename \$0) [-p <path to p8e contracts directory>]" >&2
      exit 1
      ;;
  esac
done
shift "$(($OPTIND -1))"

function up {
  #config vault
  sh service/docker/vault/config.sh

  docker-compose -p p8e-contract-execution-environment -f service/docker/dependencies.yaml up --build -d

  sleep 2
  sh service/docker/vault/init-and-unseal.sh 'http://127.0.0.1:8200' 'kv2_originations'
  sh service/docker/vault/add-secret.sh 'http://127.0.0.1:8200' 'kv2_originations' deadbeef-face-479b-860c-facefaceface \
   0A4104C51E49E4F0ABA2FD5B8CF99445D6D6C385164DBC8F35E7374CAC241D4155ADC48EF9B199F799DC865EC24AF54376CF5DD29A1287F1FD3410709A62F5DDE49349 \
   0A4104C51E49E4F0ABA2FD5B8CF99445D6D6C385164DBC8F35E7374CAC241D4155ADC48EF9B199F799DC865EC24AF54376CF5DD29A1287F1FD3410709A62F5DDE49349 \
   0A4104C51E49E4F0ABA2FD5B8CF99445D6D6C385164DBC8F35E7374CAC241D4155ADC48EF9B199F799DC865EC24AF54376CF5DD29A1287F1FD3410709A62F5DDE49349 \
   0A201AD27E627DD0A6A5816D293C41DF801D3E7BA868EEC0F433FDA581D71E38016E \
   0A201AD27E627DD0A6A5816D293C41DF801D3E7BA868EEC0F433FDA581D71E38016E \
   0A201AD27E627DD0A6A5816D293C41DF801D3E7BA868EEC0F433FDA581D71E38016E \
   "stable payment cliff fault abuse clinic bus belt film then forward world goose bring picnic rich special brush basic lamp window coral worry change"

  sh service/docker/vault/add-secret.sh 'http://127.0.0.1:8200' 'kv2_originations' deadbeef-face-2222-860c-facefaceface \
   0A41042C52EB79307D248B6CFB2A4AF562E403D4826BB0F540F024BBC3937528F6EB0B7FFA7A6585B751DBA25C173E658F3FEAAB0F05980C76E985CE0D55294F3600D7 \
   0A41042C52EB79307D248B6CFB2A4AF562E403D4826BB0F540F024BBC3937528F6EB0B7FFA7A6585B751DBA25C173E658F3FEAAB0F05980C76E985CE0D55294F3600D7 \
   0A41042C52EB79307D248B6CFB2A4AF562E403D4826BB0F540F024BBC3937528F6EB0B7FFA7A6585B751DBA25C173E658F3FEAAB0F05980C76E985CE0D55294F3600D7 \
   0A2100AF41AAD44E6D0A1DF587491D01C11DB4E0F1BBDDE33F19CB2C4ADDDBE7FC82C4 \
   0A2100AF41AAD44E6D0A1DF587491D01C11DB4E0F1BBDDE33F19CB2C4ADDDBE7FC82C4 \
   0A2100AF41AAD44E6D0A1DF587491D01C11DB4E0F1BBDDE33F19CB2C4ADDDBE7FC82C4 \
   "jealous bright oyster fluid guide talent crystal minor modify broken stove spoon pen thank action smart enemy chunk ladder soon focus recall elite pulp"

  docker ps -a
}

function publish() {
    if [ -z ${PATH_TO_CONTRACTS+x} ]; then
        echo "Provide a valid path to the contracts directory you wish to publish using the -p argument."
        exit
    fi

   source ./service/docker/bootstrap.env
    export FULL_PATH=$(realpath $PATH_TO_CONTRACTS)

    if [[ -d "$FULL_PATH" ]]; then
        pushd $PATH_TO_CONTRACTS > /dev/null
        ./gradlew p8eClean p8eCheck p8eBootstrap --info && ./gradlew publishToMavenLocal -xsignMavenPublication --info
        popd > /dev/null
    else
        echo "Invalid path. Provide a valid path to the contracts directory you wish to publish."
    fi
}

function down {
  docker-compose -p p8e-contract-execution-environment -f service/docker/dependencies.yaml down
  docker volume prune -f
}

function bounce {
   down
   up
}

${1}
