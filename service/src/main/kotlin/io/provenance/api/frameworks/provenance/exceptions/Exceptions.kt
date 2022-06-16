package io.provenance.api.frameworks.provenance.exceptions

class ContractExecutionException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)
class ContractExecutionBatchException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)
class ContractTxException(message: String) : RuntimeException(message)
