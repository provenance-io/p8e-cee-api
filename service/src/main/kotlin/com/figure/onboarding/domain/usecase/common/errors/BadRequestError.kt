package com.figure.onboarding.domain.usecase.common.errors

class BadRequestError(message: String, cause: Throwable? = null) : Exception(message, cause)
