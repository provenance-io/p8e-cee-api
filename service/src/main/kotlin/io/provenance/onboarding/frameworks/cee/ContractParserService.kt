package io.provenance.onboarding.frameworks.cee

import com.google.protobuf.Message
import io.provenance.onboarding.domain.cee.ContractParser
import io.provenance.onboarding.domain.cee.InputParser
import kotlin.reflect.full.isSubclassOf
import org.springframework.stereotype.Component

@Component
class ContractParserService(
    private val parsers: List<InputParser>
) : ContractParser {
    override fun parseInput(input: Any, type: Class<*>): Message =
        parsers.firstOrNull { type.kotlin.isSubclassOf(it.type.kotlin) }?.parse(input, type)
            ?: throw IllegalStateException("Failed to find parser for contract input.")
}
