###############################################################################
# This script creates a loan and then executes validation oracle request.
# Script Requirements
# 1. Included the latest loan package.
#    ./dc.sh -v 0.7.0 -p "<loan package contracts path>" publish
# 2. Deployed the latest smart contract on chain
#    ./dc.sh -w "validation_oracle_smart_contract.wasm" -p "<validation oracle smart contract path>" build_contract
#    ./dc.sh upload_validation_oracle_contract
#
# Usage:
#   Each parameter is defaulted, so this script should work when run once
#   without any parameters.  However, to prevent having to reset your local
#   Provenance instance after each run, you should change the defaults so
#   new assets are created.  Here is a sample command specifying overriding
#   each parameter:
#    create_mortgage_and_request_validation.sh
#        --validation_type validation11106
#        --vo_request_id 1234506
#        --scope_uuid deadbeef-face-479b-860c-facefaceee16
#        --cee_port 8085
#        --provenanced "docker exec -it provenance provenanced"
#
# Most results are output the screen and results.txt.  Additional results
#     that are vast like the JSON returned from the calls is included
#     only in the results.txt file.
###############################################################################
scope_uuid="deadbeef-face-479b-860c-facefaceeee3"
cee_port="8080"
validation_type="validationType123"
vo_request_id="12345"
provenanced="provenanced"

while [ $# -gt 0 ]; do
   if [[ $1 == *"--"* ]]; then
        param="${1/--/}"
        declare $param="$2"
        # echo $1 $2 // Optional to see the parameter:value result
   fi
  shift
done

echo "" > results.txt

echo "-------------------------------------" | tee -a results.txt
echo "Running Validation Oracle Happy Path" | tee -a results.txt
echo "scope_uuid: $scope_uuid" | tee -a results.txt
echo "cee_port: $cee_port" | tee -a results.txt
echo "validation_type: $validation_type" | tee -a results.txt
echo "vo_request_id: $vo_request_id" | tee -a results.txt
echo "-------------------------------------" | tee -a results.txt
echo "" | tee -a results.txt

echo "Executing Record Loan Contract using 00000000-0000-0000-0000-000000000002" | tee -a results.txt
echo "********************************************************************" >> results.txt

execute_results_for_record_loan=`curl --location --request POST 'http://localhost:'$cee_port'/p8e-cee-api/internal/api/v1/cee/execute' \
                                 --header 'x-uuid: 00000000-0000-0000-0000-000000000002' \
                                 --header 'Content-Type: application/json' \
                                 --data-raw '{
                                     "scope": {
                                         "scopeUuid": "'$scope_uuid'"
                                     },
                                     "config": {
                                         "client": {
                                             "objectStoreUrl": "grpc://localhost:5001"
                                         },
                                         "contract": {
                                             "contractName": "io.provenance.scope.loan.contracts.RecordLoanContract",
                                             "scopeSpecificationName": "io.provenance.scope.loan.LoanScopeSpecification",
                                             "parserConfig": {
                                                 "name": "io.provenance.api.frameworks.cee.parsers.JsonMessageParser",
                                                 "descriptors": [
                                                     "tech.figure.loan.v1beta1.MISMOLoanMetadata"
                                                 ]
                                             }
                                         },
                                         "provenanceConfig": {
                                             "chainId": "chain-local",
                                             "nodeEndpoint": "grpc://localhost:9090"
                                         }
                                     },
                                     "permissions": {
                                         "permissionDart": true
                                     },
                                     "records": {
                                         "asset": {
                                             "id": {
                                                 "value": "91888240-669f-460c-917d-448302e93f2b"
                                             },
                                             "type": "LOAN",
                                             "description": "30 year fixed rate mortgage",
                                             "kv": {
                                                 "mismoLoan": {
                                                     "@type": "type.googleapis.com/tech.figure.loan.v1beta1.MISMOLoanMetadata",
                                                     "uli": "5493001WHVQBGRSWEU7500000114",
                                                     "document": {
                                                         "id": {
                                                             "value": "1774edb8-c7a7-4fd7-9e8e-33d4e6bd5185"
                                                         },
                                                         "uri": "object://localhost:8081/tBicH-1gPPBmJ4u5uNbuaUEnxJk2CfHPWfKcdNh_Gyo=",
                                                         "fileName": "MISMO v3.4 - 91888240-669f-460c-917d-448302e93f2b",
                                                         "contentType": "application/xml",
                                                         "documentType": "MISMO v3.4",
                                                         "checksum": {
                                                             "checksum": "BqCRBJBbuS3-VA-iqiDQ-zumVXq1KyPc7lhl4q6W92Q=",
                                                             "algorithm": "sha512"
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 }' 2>>results.txt`

echo "$execute_results_for_record_loan" >> results.txt
record_loan_executed_hash=`echo $execute_results_for_record_loan | sed  's/^.*hash\":\"\([0-9a-zA-Z]*\).*$/\1/g'`
echo "record_loan_executed_hash: $record_loan_executed_hash" | tee -a results.txt
echo "********************************************************************" >> results.txt
echo | tee -a results.txt

# Needed this sleep or query using CLI wasn't finding tx
sleep 5

echo "Retrieving loan scope and height for Simple using CLI" | tee -a results.txt
echo "********************************************************************" >> results.txt
record_loan_prov_hash_tx_cli_results=`$provenanced -t q tx $record_loan_executed_hash`
echo "$record_loan_prov_hash_tx_cli_results" >> results.txt
echo | tee -a results.txt

loan_scope_id_from_cli_hash_lookup=`echo $record_loan_prov_hash_tx_cli_results | sed 's/^.*scope_addr\",\"value\":\"\\\"\([0-9a-zA-Z]*\).*$/\1/g'`
echo "loan_scope_id_from_cli_hash_lookup: $loan_scope_id_from_cli_hash_lookup" | tee -a results.txt
record_loan_record_height_from_cli_hash_lookup=`echo $record_loan_prov_hash_tx_cli_results | sed 's/^.*height: \"\([0-9]*\)\".*timeout_height.*$/\1/g'`
echo "record_loan_record_height_from_cli_hash_lookup: $record_loan_record_height_from_cli_hash_lookup" | tee -a results.txt
echo "********************************************************************" >> results.txt
echo | tee -a results.txt

echo "Getting VO Smart Contract Address" | tee -a results.txt
vo_contract_address_results=`$provenanced -t q name resolve validationoraclealias.pb`
echo "vo_contract_address_results: $vo_contract_address_results" >> results.txt

vo_contract_address=`echo $vo_contract_address_results | sed  's/^.*address: \([0-9a-zA-Z]*\).*$/\1/g'`

echo "vo_contract_address: $vo_contract_address" | tee -a results.txt
echo "********************************************************************" >> results.txt
echo | tee -a results.txt

echo "Setting Up Validation Definition for $validation_type"
echo "Setting Up Validation Definition for $validation_type" >> results.txt
echo "********************************************************************" >> results.txt

execute_results_for_vo_setup=`curl --location --request POST 'http://localhost:'$cee_port'/p8e-cee-api/external/api/v1/p8e/smartcontract/execute' \
                                    --header 'x-uuid: 00000000-0000-0000-0000-000000000002' \
                                    --header 'Content-Type: application/json' \
                                    --data-raw '{
                                                    "provenanceConfig": {
                                                        "chainId": "chain-local",
                                                        "nodeEndpoint": "grpc://localhost:9090"
                                                    },
                                                    "contractConfig": {
                                                        "contractAddress": "'$vo_contract_address'",
                                                        "verifierAddress": "tp1mryqzguyelef5dae7k6l22tnls93cvrc60tjdc",
                                                        "accessRoutes": [
                                                            {
                                                                "route": "grpc://object-store-v2.p8e:80",
                                                                "name": "object-store"
                                                            }
                                                        ]
                                                    },
                                                    "libraryInvocation": {
                                                        "methodName": "addValidationDefinition",
                                                        "parameterClassName": "tech.figure.validationoracle.client.domain.execute.AddValidationDefinitionExecute",
                                                        "parameterClassJson": "{\"validationType\":\"'$validation_type'\",\"displayName\":\"displayName123\",\"validators\":[{\"validationCosts\":[{\"amount\":\"345\",\"denom\":\"nhash\",\"destination\":{\"address\":\"tp1mryqzguyelef5dae7k6l22tnls93cvrc60tjdc\",\"name\":\"Entity1\",\"description\":\"descr2\"}}],\"validation_type\":\"abc123\",\"validator\":{\"address\":\"tp1mryqzguyelef5dae7k6l22tnls93cvrc60tjdc\",\"name\":\"Entity1\",\"description\":\"descr2\"}}],\"enabled\":true}"
                                                    }
                                                }' 2>>results.txt`

echo "$execute_results_for_vo_setup" >> results.txt
echo "********************************************************************" >> results.txt
echo | tee -a results.txt

echo "Query for Validation Definition for $validation_type"  | tee -a results.txt
echo "********************************************************************" >> results.txt

execute_query_results_for_vo_setup=`curl --location --request GET 'http://localhost:'$cee_port'/p8e-cee-api/external/api/v1/p8e/smartcontract/query' \
                                    --header 'x-uuid: 00000000-0000-0000-0000-000000000002' \
                                    --header 'Content-Type: application/json' \
                                    --data-raw '{
                                                    "provenanceConfig": {
                                                        "chainId": "chain-local",
                                                        "nodeEndpoint": "grpc://localhost:9090"
                                                    },
                                                    "contractConfig": {
                                                        "contractAddress": "'$vo_contract_address'",
                                                        "verifierAddress": "tp1mryqzguyelef5dae7k6l22tnls93cvrc60tjdc",
                                                        "accessRoutes": [
                                                            {
                                                                "route": "grpc://object-store-v2.p8e:80",
                                                                "name": "object-store"
                                                            }
                                                        ]
                                                    },
                                                    "libraryInvocation": {
                                                        "methodName": "queryValidationDefinitionByType",
                                                        "parameterClassName": "tech.figure.validationoracle.client.domain.query.QueryValidationDefinitionByType",
                                                        "parameterClassJson": "{\"query_validation_definition_by_type\":{\"type\":\"'$validation_type'\"}}"
                                                    }
                                                }' 2>>results.txt`

echo "$execute_query_results_for_vo_setup" >> results.txt
echo "********************************************************************" >> results.txt
echo | tee -a results.txt

echo "Submit VO Request for $vo_request_id"
echo "Submit VO Request for $vo_request_id" >> results.txt
echo "********************************************************************" >> results.txt

execute_vo_request=`curl --location --request POST 'http://localhost:'$cee_port'/p8e-cee-api/external/api/v1/p8e/smartcontract/execute' \
                                    --header 'x-uuid: 00000000-0000-0000-0000-000000000002' \
                                    --header 'Content-Type: application/json' \
                                    --data-raw '{
                                                    "provenanceConfig": {
                                                        "chainId": "chain-local",
                                                        "nodeEndpoint": "grpc://localhost:9090"
                                                    },
                                                    "contractConfig": {
                                                        "contractAddress": "'$vo_contract_address'",
                                                        "verifierAddress": "tp1mryqzguyelef5dae7k6l22tnls93cvrc60tjdc",
                                                        "accessRoutes": [
                                                            {
                                                                "route": "grpc://object-store-v2.p8e:80",
                                                                "name": "object-store"
                                                            }
                                                        ]
                                                    },
                                                    "libraryInvocation": {
                                                        "methodName": "requestValidationExecute",
                                                        "parameterClassName": "tech.figure.validationoracle.client.domain.execute.RequestValidationExecute",
                                                        "parameterClassJson": "{ \"id\": \"'$vo_request_id'\", \"scopes\": [\"'$loan_scope_id_from_cli_hash_lookup'\"], \"quote\": [] }"
                                                    }
                                                }' 2>>results.txt`

echo "$execute_vo_request" >> results.txt
echo "********************************************************************" >> results.txt
echo | tee -a results.txt

echo "Query for Validation Request with id $vo_request_id" | tee -a results.txt
echo "********************************************************************" >> results.txt

execute_query_vo_request=`curl --location --request GET 'http://localhost:'$cee_port'/p8e-cee-api/external/api/v1/p8e/smartcontract/query' \
                                    --header 'x-uuid: 00000000-0000-0000-0000-000000000002' \
                                    --header 'Content-Type: application/json' \
                                    --data-raw '{
                                                    "provenanceConfig": {
                                                        "chainId": "chain-local",
                                                        "nodeEndpoint": "grpc://localhost:9090"
                                                    },
                                                    "contractConfig": {
                                                        "contractAddress": "'$vo_contract_address'",
                                                        "verifierAddress": "tp1mryqzguyelef5dae7k6l22tnls93cvrc60tjdc",
                                                        "accessRoutes": [
                                                            {
                                                                "route": "grpc://object-store-v2.p8e:80",
                                                                "name": "object-store"
                                                            }
                                                        ]
                                                    },
                                                    "libraryInvocation" :{
                                                        "methodName": "queryValidationRequestById",
                                                        "parameterClassName": "tech.figure.validationoracle.client.domain.query.QueryValidationRequestById",
                                                        "parameterClassJson": "{\"query_validation_request_by_id\":{ \"id\": \"'$vo_request_id'\"}}"
                                                    }
                                                }' 2>>results.txt`

echo "$execute_query_vo_request" | tee -a results.txt
echo "********************************************************************" >> results.txt
echo | tee -a results.txt

echo "You can see more detailed results in results.txt"
