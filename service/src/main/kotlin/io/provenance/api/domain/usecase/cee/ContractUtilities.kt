package io.provenance.api.domain.usecase.cee

import com.google.protobuf.Message
import io.provenance.api.domain.cee.ContractParser
import io.provenance.api.models.cee.ParserConfig
import io.provenance.scope.contract.annotations.Input
import io.provenance.scope.contract.spec.P8eContract
import kotlin.reflect.KType
import kotlin.reflect.full.functions
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

class ContractUtilities {
    companion object {
        @Suppress("TooGenericExceptionCaught")
        fun getRecords(contractParser: ContractParser, records: Map<String, Any>, contract: Class<out P8eContract>, parserConfig: ParserConfig?): Map<String, Message> {
            val contractRecords = mutableMapOf<String, Message>()

            try {
                contract.kotlin.functions.forEach { func ->
                    func.parameters.forEach { param ->
                        (param.annotations.firstOrNull { it is Input } as? Input)?.let { input ->
                            val parameterClass = Class.forName(param.type.toClassNameString())
                            records.getOrDefault(input.name, null)?.let {

                                val record = when (val parser = parserConfig?.name?.let { name -> contractParser.getParser(name) }) {
                                    null -> {
                                        contractParser.parseInput(it, parameterClass)
                                    }
                                    else -> {
                                        parser.parse(it, parameterClass, parserConfig.descriptors)
                                    }
                                }

                                contractRecords[input.name] = record
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                log.error("Failed to get inputs for contract ${contract.simpleName}")
                throw ex
            }

            return contractRecords
        }
    }
}

private fun KType?.toClassNameString(): String? = this?.classifier?.toString()?.drop("class ".length)
