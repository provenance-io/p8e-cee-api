package io.provenance.api.frameworks.provenance.utility

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import cosmos.crypto.secp256k1.Keys
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.client.grpc.Signer
import io.provenance.hdwallet.common.hashing.sha256
import io.provenance.hdwallet.ec.extensions.toECPrivateKey
import io.provenance.hdwallet.signer.BCECSigner
import io.provenance.metadata.v1.MsgWriteRecordRequest
import io.provenance.metadata.v1.MsgWriteScopeRequest
import io.provenance.metadata.v1.MsgWriteSessionRequest
import io.provenance.metadata.v1.Party
import io.provenance.metadata.v1.PartyType
import io.provenance.metadata.v1.RecordInput
import io.provenance.metadata.v1.RecordInputStatus
import io.provenance.metadata.v1.RecordOutput
import io.provenance.metadata.v1.ResultStatus
import io.provenance.api.domain.usecase.common.model.ScopeConfig
import io.provenance.api.models.p8e.TxBody
import io.provenance.api.frameworks.provenance.extensions.toAny
import io.provenance.api.frameworks.provenance.extensions.toBase64String
import io.provenance.api.frameworks.provenance.extensions.toJson
import io.provenance.api.frameworks.provenance.extensions.toTxBody
import io.provenance.scope.encryption.util.getAddress
import io.provenance.scope.util.MetadataAddress
import io.provenance.scope.util.toByteString
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID

object ProvenanceUtils {

    data class RecordInputSpec(
        val name: String,
        val typeName: String,
        val hash: String,
    )

    // Record specification
    const val RecordSpecName = "Asset"
    val RecordSpecInputs = listOf(
        RecordInputSpec(
            name = "AssetHash",
            typeName = "String",
            hash = "4B6A6C36E8B2622334C244B46799A47DBEAAF94E9D5B7637BC12A3A4988A62C0", // sha356(RecordSpecInputs.name)
        )
    )

    // Record process
    const val RecordProcessName = "OnboardAssetProcess"
    const val RecordProcessMethod = "OnboardAsset"
    const val RecordProcessHash = "32D60974A2B2E9A9D9E93D9956E3A7D2BD226E1511D64D1EA39F86CBED62CE78" // sha356(RecordProcessMethod)

    // Create a metadata TX message for a new scope onboard
    private fun buildNewScopeMetadataTransaction(
        config: ScopeConfig,
        scopeHash: String,
        owner: String,
        additionalAudiences: Set<String>
    ): TxOuterClass.TxBody {
        // Generate a session identifier
        val sessionId: UUID = UUID.randomUUID()

        // Create the set of all audiences (including the owner)
        val allAudiences = setOf(
            owner,
        ).plus(additionalAudiences)

        // Create the list of all parties (including the owner)
        val allParties = setOf(
            Party.newBuilder().apply {
                address = owner
                role = PartyType.PARTY_TYPE_OWNER
            }.build()
        )

        // Build TX message body
        return listOf(

            // write-scope
            MsgWriteScopeRequest.newBuilder().apply {
                scopeUuid = config.scopeId.toString()
                specUuid = config.scopeSpecId.toString()
                scopeBuilder
                    .setScopeId(MetadataAddress.forScope(config.scopeId).bytes.toByteString())
                    .setSpecificationId(MetadataAddress.forScopeSpecification(config.scopeSpecId).bytes.toByteString())
                    .setValueOwnerAddress(owner)
                    .addAllOwners(
                        listOf(
                            Party.newBuilder().apply {
                                address = owner
                                role = PartyType.PARTY_TYPE_OWNER
                            }.build()
                        )
                    )
                    .addAllDataAccess(allAudiences)
            }.addAllSigners(listOf(owner)).build().toAny(),

            // write-session
            MsgWriteSessionRequest.newBuilder().apply {
                sessionIdComponentsBuilder
                    .setScopeUuid(config.scopeId.toString())
                    .setSessionUuid(sessionId.toString())
                sessionBuilder
                    .setSessionId(MetadataAddress.forSession(config.scopeId, sessionId).bytes.toByteString())
                    .setSpecificationId(MetadataAddress.forContractSpecification(config.contractSpecId).bytes.toByteString())
                    .addAllParties(allParties)
                    .auditBuilder
                    .setCreatedBy(owner)
                    .setUpdatedBy(owner)
            }.addAllSigners(listOf(owner)).build().toAny(),

            // write-record
            MsgWriteRecordRequest.newBuilder().apply {
                contractSpecUuid = config.contractSpecId.toString()
                recordBuilder
                    .setSessionId(MetadataAddress.forSession(config.scopeId, sessionId).bytes.toByteString())
                    .setSpecificationId(MetadataAddress.forRecordSpecification(config.contractSpecId, RecordSpecName).bytes.toByteString())
                    .setName(RecordSpecName)
                    .addAllInputs(
                        RecordSpecInputs.map {
                            RecordInput.newBuilder().apply {
                                name = it.name
                                typeName = it.typeName
                                hash = if (it.name == "AssetHash") {
                                    scopeHash
                                } else {
                                    ""
                                }
                                status = RecordInputStatus.RECORD_INPUT_STATUS_PROPOSED
                            }.build()
                        }
                    )
                    .addAllOutputs(
                        RecordSpecInputs.map {
                            RecordOutput.newBuilder().apply {
                                hash = if (it.name == "AssetHash") {
                                    scopeHash
                                } else {
                                    ""
                                }
                                status = ResultStatus.RESULT_STATUS_PASS
                            }.build()
                        }
                    )
                    .processBuilder
                    .setName(RecordProcessName)
                    .setMethod(RecordProcessMethod)
                    .setHash(RecordProcessHash)
            }.addAllSigners(listOf(owner)).build().toAny(),
        ).toTxBody()
    }

    fun createScopeTx(
        config: ScopeConfig,
        factHash: String,
        address: String,
        additionalAudiences: Set<String> = emptySet(),
    ): TxBody {
        // create the metadata TX message
        val txBody = buildNewScopeMetadataTransaction(
            config,
            factHash,
            address,
            additionalAudiences,
        )

        return TxBody(
            json = ObjectMapper().readValue(txBody.toJson(), ObjectNode::class.java),
            base64 = txBody.messagesList.map { it.toByteArray().toBase64String() }
        )
    }

    fun getSigner(publicKey: PublicKey, privateKey: PrivateKey, isMainnet: Boolean): Signer {
        return object : Signer {
            override fun address(): String = publicKey.getAddress(isMainnet)

            override fun pubKey(): Keys.PubKey =
                Keys.PubKey.newBuilder()
                    .setKey((publicKey as BCECPublicKey).q.getEncoded(true).toByteString())
                    .build()

            override fun sign(data: ByteArray): ByteArray =
                BCECSigner().sign(privateKey.toECPrivateKey(), data.sha256())
                    .encodeAsBTC()
                    .toByteArray()
        }
    }
}
