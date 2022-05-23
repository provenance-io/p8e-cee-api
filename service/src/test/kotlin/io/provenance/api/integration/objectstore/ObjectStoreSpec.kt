package io.provenance.api.integration.objectstore

import io.provenance.api.domain.usecase.objectStore.store.StoreAsset
import io.provenance.api.domain.usecase.objectStore.store.models.StoreAssetRequestWrapper
import io.provenance.api.integration.base.IntegrationTestBase
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.eos.StoreAssetRequest
import java.util.UUID

class ObjectStoreSpec(
    private val storeAsset: StoreAsset
) : IntegrationTestBase({

    "Object Store" should {
        "Store Object and Return Hash" {
            storeAsset.execute(StoreAssetRequestWrapper(UUID.randomUUID(),
                StoreAssetRequest(objectStoreAddress = "grpc://localhost:5001", assetId = UUID.randomUUID(), asset = "hi", permissions = null, account = AccountInfo())))
        }
    }
})




//    val p8eNetwork = Network.newNetwork()
//
//    val postgresTestContainer = install(TestContainerExtension("postgres:13-alpine")) {
//        startupAttempts = 1
//        withExposedPorts(5432, 5432)
//        withEnv(
//            mapOf(
//                "POSTGRES_USER" to "postgres",
//                "POSTGRES_PASSWORD" to "password1"
//            )
//        )
//        withNetwork(p8eNetwork)
//        withCommand("postgres")
//    }
//
//    val objectStoreTestContainer = install(TestContainerExtension("ghcr.io/provenance-io/object-store:0.7.0")) {
//        startupAttempts = 1
//        withExposedPorts(7001, 8080)
//        withEnv(
//            mapOf(
//                "OS_PORT" to "8080",
//                "URI_HOST" to "localhost:8080",
//                "REPLICATION_ENABLED" to "true",
//                "DB_HOST" to "postgres-test",
//                "DB_PORT" to "5432",
//                "DB_USER" to "postgres",
//                "DB_PASS"  to "password1",
//                "DB_PASSWORD" to "password1",
//                "DB_NAME" to "test-object-store-1",
//                "DB_SCHEMA" to "public",
//                "DB_CONNECTION_POOL_SIZE" to "10",
//                "STORAGE_TYPE" to "file_system",
//                "STORAGE_BASE_PATH" to "/mnt/data"
//            )
//        )
//        dependsOn(postgresTestContainer)
//        withNetwork(p8eNetwork)
//    }



/*
    object-store-1:
        image: ghcr.io/provenance-io/object-store:0.7.0
        container_name: object-store-1


        networks:
            - p8e-network
        ports:
            - "5001:8080"
        volumes:
            - ./object-store-1:/mnt/data

            OS_URL=0.0.0.0



 */
