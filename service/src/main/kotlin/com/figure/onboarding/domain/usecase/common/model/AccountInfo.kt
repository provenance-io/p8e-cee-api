package com.figure.onboarding.domain.usecase.common.model

import java.util.UUID

data class AccountInfo(
    val originatorUuid: UUID,
    val keyRingIndex: Int = 0,
    val keyIndex: Int = 0,
    val isTestNet: Boolean = true,
)
