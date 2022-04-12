#!/bin/bash

function up {
  #config vault
  sh service/docker/vault/config.sh

  docker volume prune -f
  docker-compose -p p8e-cee-api -f service/docker/dependencies.yaml up --build -d

  sleep 2
  sh service/docker/vault/init-and-unseal.sh 'http://127.0.0.1:8200' 'kv2_originations'
  sh service/docker/vault/add-secret.sh 'http://127.0.0.1:8200' 'kv2_originations' deadbeef-face-479b-860c-facefaceface \
   0A4104F43FD6CD0DA184A666D444A4359EB9D38E06DB1EDC97B221DF90ED8189CFB8476EE2BF633DE0B4560F0D2BA284696C02ED31ADB32F09DEBE8D8906AA3E2BE71A \
   0A410469BE2EC4699803FB7B49077F1CE0C88E2D2D41D23661EB1F420A72AA2B8DDF4325B349208B7F348C108B7C145FAEF41BB68FF338A75810522A89323E54BE91A6 \
   0A4104DB326CF64C7466C2D63A2CD2C0401D1D6F8583C5F837C7510205DFB5CFA89D2115D0A3E0930D5FA01ACC664F1A182E1C0B43099D689B579849DEDDA1C63D0CF7 \
   0A2009ABB890B396449706BB5998EEC6F6B0C795297B391A4BC2CF227B263555FF15 \
   0A200B221F389D9B8E4964A8EB66F4EC07F83FCDD2E9DCE79B356FDDBE97767CF11C \
   0A2100F5FE00731E3BC71F22CF054712A7C1F9A610848FC04BA81E6822D82D82C3562E \
   "jealous bright oyster fluid guide talent crystal minor modify broken stove spoon pen thank action smart enemy chunk ladder soon focus recall elite pulp"

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

function down {
  docker-compose -p p8e-cee-api -f service/docker/dependencies.yaml down
}

function bounce {
   down
   up
}

function local_specs {

  ./cli/bin/cli write-specs-asset \
      --contract-spec-id "18573cf8-ddb9-491e-a4cb-bf2176160a63" \
      --scope-spec-id "997e8228-c37f-4668-9a66-6cfb3b2a23cd" \
      --key-mnemonic "jealous bright oyster fluid guide talent crystal minor modify broken stove spoon pen thank action smart enemy chunk ladder soon focus recall elite pulp" \
      --chain-id local-chain \
      --node https://127.0.0.1:9090 \
      --raw-log

  ./cli/bin/cli write-specs-loan-state \
      --contract-spec-id "63a8bb4c-c6e0-4cb5-993b-b134c4b5cbbb" \
      --scope-spec-id "2eeada14-07cb-45fe-af6d-fdc48b627817" \
      --key-mnemonic "jealous bright oyster fluid guide talent crystal minor modify broken stove spoon pen thank action smart enemy chunk ladder soon focus recall elite pulp" \
      --chain-id local-chain \
      --node https://127.0.0.1:9090 \
      --raw-log
}

${1}
