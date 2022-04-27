package io.provenance.onboarding.frameworks.provenance.exceptions

open class ContractTransactionException(message: String? = "", val shouldRetry: Boolean = false, cause: Throwable? = null) : RuntimeException(message, cause)

open class ResourceNotFoundException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class WontRunLoanValidationException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class ExistingScopeNotChangedException(message: String) : RuntimeException(message)
