package io.provenance.onboarding.frameworks.provenance.exceptions

import java.util.UUID

open class CannotOnboardUnfundedLoanException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class ContractExceptionException(message: String? = "", val shouldRetry: Boolean = false, cause: Throwable? = null) : RuntimeException(message, cause)

open class ContractInProgressException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class ContractRetryException(message: String? = "", val willRetry: Boolean = true, cause: Throwable? = null) : RuntimeException(message, cause)

open class InsufficientFundsException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class InvalidFactException(message: String? = "", val factName: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

open class InvalidLoanFactException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class InvalidLoanTypeException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class InvalidOriginatorException(message: String? = "", val originatorUuid: UUID? = null, cause: Throwable? = null) : RuntimeException(message, cause)

open class NotificationException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class ResourceNotFoundException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class LoanAlreadyExistsException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class LoanFundingPaused(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class WontRunLoanValidationException(message: String? = "", cause: Throwable? = null) : RuntimeException(message, cause)

open class ExistingScopeNotChangedException(message: String) : java.lang.RuntimeException(message)
