package io.provenance.api.models.p8e.contracts

/**
 * This data class has the details that are needed to use reflection to invoke
 * the method desired on the smart contract client Kotlin library.
 * In order to make the call, the method name, the name of the class to that
 * the method takes as a parameter and the data to put into that parameter class
 * (in JSON) are all required.
 */
data class SmartContractClientLibraryInvocation(
    val methodName: String,
    val parameterClassName: String,
    val parameterClassJson: String
)
