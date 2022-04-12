package io.provenance.onboarding.domain.usecase.common.errors

class BadRequestError(message: String, cause: Throwable? = null) : Exception(message, cause)
