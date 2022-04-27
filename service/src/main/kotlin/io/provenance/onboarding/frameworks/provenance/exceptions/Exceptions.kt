package io.provenance.onboarding.frameworks.provenance.exceptions

open class ContractExceptionException(message: String? = "", val shouldRetry: Boolean = false, cause: Throwable? = null) : RuntimeException(message, cause)
