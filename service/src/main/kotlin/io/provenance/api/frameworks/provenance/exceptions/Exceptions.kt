package io.provenance.api.frameworks.provenance.exceptions

class ContractExecutionException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)
class ResourceNotFoundException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)
class WontRunLoanValidationException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)
class ExistingScopeNotChangedException(message: String) : RuntimeException(message)
class ContractTxException(message: String) : RuntimeException(message)
