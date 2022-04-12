package io.provenance.onboarding.domain.usecase.common.errors

class NotFoundError(message: String, cause: Throwable? = null) : Exception(message, cause)
