package io.provenance.api.frameworks.cee

import com.google.protobuf.Message
import io.provenance.api.domain.cee.ContractParser
import io.provenance.api.domain.cee.InputParser
import org.springframework.stereotype.Component
import kotlin.reflect.full.isSubclassOf

@Component
class ContractParserService(
    private val parsers: List<InputParser>
) : ContractParser {
    override fun parseInput(input: Any, type: Class<*>): Message =
        parsers.firstOrNull { type.kotlin.isSubclassOf(it.type.kotlin) && it.default }?.parse(input, type)
            ?: throw IllegalStateException("Failed to find parser for contract input.")

    override fun getParser(name: String): InputParser? =
        parsers.firstOrNull { it.javaClass.name == name }
}
