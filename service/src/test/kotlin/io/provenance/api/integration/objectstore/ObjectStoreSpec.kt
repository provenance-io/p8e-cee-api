package io.provenance.api.integration.objectstore

import io.kotest.matchers.shouldBe
import io.provenance.api.domain.usecase.objectStore.get.GetProto
import io.provenance.api.domain.usecase.objectStore.get.models.GetProtoRequestWrapper
import io.provenance.api.domain.usecase.objectStore.store.StoreProto
import io.provenance.api.domain.usecase.objectStore.store.models.StoreProtoRequestWrapper
import io.provenance.api.integration.base.IntegrationTestBase
import io.provenance.api.models.account.AccountInfo
import io.provenance.api.models.account.KeyManagementConfig
import io.provenance.api.models.eos.get.GetProtoRequest
import io.provenance.api.models.eos.store.StoreProtoRequest
import io.provenance.api.models.entity.Entity
import io.provenance.api.models.entity.MemberUUID
import io.provenance.api.util.toPrettyJson
import io.provenance.plugins.vault.VaultConfig
import java.util.UUID
import tech.figure.asset.v1beta1.Asset
import tech.figure.proto.util.toProtoUUID

class ObjectStoreSpec(
    private val storeProto: StoreProto,
    private val getProto: GetProto,
) : IntegrationTestBase({

    val entities = listOf<Entity>(
        MemberUUID(UUID.fromString("deadbeef-face-479b-860c-facefaceface")),
        MemberUUID(UUID.fromString("deadbeef-face-2222-860c-facefaceface")),
    )

    "Object Store" should {
        val entity = entities.first()

        "Store Object and Return Hash" {
            val assetToStore = Asset.newBuilder()
                .setDescription("arvo")
                .setType("tea")
                .setId(UUID.randomUUID().toProtoUUID())
                .build()

            val response = storeProto.execute(
                StoreProtoRequestWrapper(
                    entity,
                    StoreProtoRequest(
                        objectStoreAddress = objectStore1Address,
                        message = assetToStore,
                        type = "tech.figure.asset.v1beta1.Asset",
                        account = AccountInfo(
                            keyManagementConfig = KeyManagementConfig(
                                pluginConfig = VaultConfig(
                                    "$vaultAddress/${entity.id}",
                                    "src/test/resources/vault/token.output"
                                )
                            )
                        )
                    )
                )
            )

            val retrievedAsset = getProto.execute(
                GetProtoRequestWrapper(
                    entity,
                    GetProtoRequest(
                        response.hash,
                        objectStore1Address,
                        type = "tech.figure.asset.v1beta1.Asset",
                        account = AccountInfo(
                            keyManagementConfig = KeyManagementConfig(
                                pluginConfig = VaultConfig(
                                    "$vaultAddress/${entity.id}",
                                    "src/test/resources/vault/token.output"
                                )
                            )
                        )
                    )
                )
            )

            retrievedAsset shouldBe assetToStore.toPrettyJson()
        }
    }
})
