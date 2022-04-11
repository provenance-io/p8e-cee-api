package com.figure.onboarding.domain.usecase.common.errors

class ApiError(message: String, cause: Throwable? = null) : Exception(message, cause)
