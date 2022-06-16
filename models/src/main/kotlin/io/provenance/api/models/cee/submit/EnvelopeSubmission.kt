package io.provenance.api.models.cee.submit

data class EnvelopeSubmission(
    val envelope: ByteArray,
    val state: ByteArray
)
