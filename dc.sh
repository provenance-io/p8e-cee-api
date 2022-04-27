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
   0A4104F43FD6CD0DA184A666D444A4359EB9D38E06DB1EDC97B221DF90ED8189CFB8476EE2BF633DE0B4560F0D2BA284696C02ED31ADB32F09DEBE8D8906AA3E2BE71A \
   0A410469BE2EC4699803FB7B49077F1CE0C88E2D2D41D23661EB1F420A72AA2B8DDF4325B349208B7F348C108B7C145FAEF41BB68FF338A75810522A89323E54BE91A6 \
   0A4104DB326CF64C7466C2D63A2CD2C0401D1D6F8583C5F837C7510205DFB5CFA89D2115D0A3E0930D5FA01ACC664F1A182E1C0B43099D689B579849DEDDA1C63D0CF7 \
   0A2009ABB890B396449706BB5998EEC6F6B0C795297B391A4BC2CF227B263555FF15 \
   0A200B221F389D9B8E4964A8EB66F4EC07F83FCDD2E9DCE79B356FDDBE97767CF11C \
   0A2100F5FE00731E3BC71F22CF054712A7C1F9A610848FC04BA81E6822D82D82C3562E \
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

 echo $SIGNING_PRIVATE_KEY
 echo $ENCRYPTION_PRIVATE_KEY
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
