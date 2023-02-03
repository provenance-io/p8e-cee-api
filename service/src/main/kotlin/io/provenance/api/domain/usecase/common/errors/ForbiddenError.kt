package io.provenance.api.domain.usecase.common.errors

class ForbiddenError(message: String, cause: Throwable? = null) : Exception(message, cause)
