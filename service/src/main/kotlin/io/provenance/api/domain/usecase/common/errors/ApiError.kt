package io.provenance.api.domain.usecase.common.errors

class ApiError(message: String, cause: Throwable? = null) : Exception(message, cause)
