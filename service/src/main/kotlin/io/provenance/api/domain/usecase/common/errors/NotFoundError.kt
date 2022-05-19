package io.provenance.api.domain.usecase.common.errors

class NotFoundError(message: String, cause: Throwable? = null) : Exception(message, cause)
