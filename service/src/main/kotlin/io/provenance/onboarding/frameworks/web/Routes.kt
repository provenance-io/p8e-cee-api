package io.provenance.onboarding.frameworks.web

object Routes {
    private const val CONTEXT = "/p8e-cee-api"
    const val MANAGE_BASE = "$CONTEXT/manage"
    const val SECURE_BASE = "$CONTEXT/secure/api"
    const val EXTERNAL_BASE = "$CONTEXT/external/api"
    const val INTERNAL_BASE = "$CONTEXT/internal/api"
    const val SECURE_BASE_V1 = "$SECURE_BASE/v1"
    const val EXTERNAL_BASE_V1 = "$EXTERNAL_BASE/v1"
    const val INTERNAL_BASE_V1 = "$INTERNAL_BASE/v1"
    const val DOCS_BASE = "$CONTEXT/secure/docs"
}
