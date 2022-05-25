package io.provenance.api.frameworks.provenance.extensions

import com.google.protobuf.Any
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import cosmos.tx.v1beta1.TxOuterClass.TxBody
import io.provenance.metadata.v1.MsgWriteContractSpecificationRequest
import io.provenance.metadata.v1.MsgWriteRecordRequest
import io.provenance.metadata.v1.MsgWriteRecordSpecificationRequest
import io.provenance.metadata.v1.MsgWriteScopeRequest
import io.provenance.metadata.v1.MsgWriteScopeSpecificationRequest
import io.provenance.metadata.v1.MsgWriteSessionRequest

fun TxBody.toJson(): String {
    val printer: JsonFormat.Printer = JsonFormat.printer().usingTypeRegistry(
        JsonFormat.TypeRegistry.newBuilder()
            .add(MsgWriteContractSpecificationRequest.getDescriptor())
            .add(MsgWriteScopeSpecificationRequest.getDescriptor())
            .add(MsgWriteScopeRequest.getDescriptor())
            .add(MsgWriteSessionRequest.getDescriptor())
            .add(MsgWriteRecordSpecificationRequest.getDescriptor())
            .add(MsgWriteRecordRequest.getDescriptor())
            .build()
    )
    return printer.print(this)
}

fun Message.toAny(typeUrlPrefix: String = ""): Any = Any.pack(this, typeUrlPrefix)

fun Iterable<Any>.toTxBody(memo: String? = null, timeoutHeight: Long? = null): TxBody =
    TxBody.newBuilder()
        .addAllMessages(this)
        .also { builder ->
            memo?.run { builder.memo = this }
            timeoutHeight?.run { builder.timeoutHeight = this }
        }
        .build()

fun Any.toTxBody(memo: String? = null, timeoutHeight: Long? = null): TxBody =
    listOf(this).toTxBody(memo, timeoutHeight)
