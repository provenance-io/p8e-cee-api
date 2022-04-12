package io.provenance.onboarding.domain.usecase.common.errors

class ApiError(message: String, cause: Throwable? = null) : Exception(message, cause)
