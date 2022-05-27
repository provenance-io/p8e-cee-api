package io.provenance.api.domain.usecase.provenance.query

import com.google.protobuf.Message
import io.dartinc.registry.v1beta1.ENote
import io.provenance.api.domain.usecase.AbstractUseCase
import io.provenance.api.domain.usecase.cee.common.client.CreateClient
import io.provenance.api.domain.usecase.cee.common.client.model.CreateClientRequest
import io.provenance.api.domain.usecase.provenance.query.models.QueryScopeRequestWrapper
import io.provenance.api.frameworks.provenance.ProvenanceService
import io.provenance.api.util.toPrettyJson
import io.provenance.scope.contract.annotations.Record
import io.provenance.scope.loan.LoanPackage
import io.provenance.scope.loan.LoanScopeFacts
import io.provenance.scope.loan.LoanScopeSpecification
import io.provenance.scope.sdk.ContractSpecMapper.orThrowContractDefinition
import io.provenance.scope.sdk.extensions.resultType
import io.provenance.scope.sdk.extensions.uuid
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import tech.figure.loan.v1beta1.LoanDocuments
import tech.figure.servicing.v1beta1.LoanStateOuterClass
import tech.figure.servicing.v1beta1.ServicingRightsOuterClass
import tech.figure.validation.v1beta1.LoanValidation

@Component
class QueryScope(
    private val provenanceService: ProvenanceService,
    private val createClient: CreateClient
) : AbstractUseCase<QueryScopeRequestWrapper, Unit>() {
    override suspend fun execute(args: QueryScopeRequestWrapper) {
        val client = createClient.execute(CreateClientRequest(args.uuid, args.request.account, args.request.client))
        val scope = provenanceService.getScope(args.request.provenanceConfig, args.request.scopeUuid, args.request.height)
        println(scope.toPrettyJson())

        val clazz = Class.forName("io.provenance.scope.loan.LoanPackage")
        val constructor = clazz.declaredConstructors
            .filter {
                it.parameters.isNotEmpty() &&
                    it.parameters.all { param ->
                        Message::class.java.isAssignableFrom(param.type) &&
                            param.getAnnotation(Record::class.java) != null
                    }
            }

            val constructor2 = constructor.takeIf { it.isNotEmpty() }
            // TODO different error type?
            .orThrowContractDefinition("Unable to build POJO of type ${clazz.name} because not all constructor params implement ${Message::class.java.name} and have a \"Record\" annotation")
            val constructor3 = constructor2.firstOrNull {
                it.parameters.any { param ->
                    scope.recordsList.any { wrapper ->
                        (wrapper.record.name == param.getAnnotation(Record::class.java)?.name &&
                            wrapper.record.resultType() == param.type.name)
                    }
                }
            }
            .orThrowContractDefinition("No constructor params have a matching record in scope ${scope.uuid()}")

        println(constructor3)
        val obj = client.hydrate(LoanPackageLocal::class.java, scope)
        println(obj.toPrettyJson())
    }
}

data class ExampleEnoteHydrate(
    @Record("eNote") val enote: ENote
)

/**
 * Denotes the [Record]s that are part of a [LoanScopeSpecification] for the loan package.
 */
data class LoanPackageLocal(
    /** The loan asset. */
    @Record(LoanScopeFacts.asset) var asset: Asset?,
    /** The servicing rights to the loan. Defaults to the lender. */
    @Record(LoanScopeFacts.servicingRights) var servicingRights: ServicingRightsOuterClass.ServicingRights?,
    /** A list of metadata for documents, including their URIs in an encrypted object store. */
    @Record(LoanScopeFacts.documents) var documents: LoanDocuments?,
    /** Servicing data for the loan, including a list of metadata on loan states. */
    @Record(LoanScopeFacts.servicingData) var servicingData: LoanStateOuterClass.ServicingData?,
    /** A list of third-party validation iterations. */
    @Record(LoanScopeFacts.loanValidations) var loanValidations: LoanValidation?,
    /** The eNote for the loan. */
    @Record(LoanScopeFacts.eNote) var eNote: ENote?,
)
