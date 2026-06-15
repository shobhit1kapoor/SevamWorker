package com.sevam.customer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PartnerOnboardingStateTest {

    @Test
    fun workerCannotGoOnlineBeforeAdminApproval() {
        val state = SevamPartnerUiState(
            approvalStatus = ApprovalStatus.UNDER_REVIEW,
            isOnline = false,
        )

        assertThat(state.approvalStatus).isNotEqualTo(ApprovalStatus.APPROVED)
        assertThat(state.isOnline).isFalse()
    }

    @Test
    fun submittedKycCanUnlockServiceSelection() {
        val state = SevamPartnerUiState(
            kyc = KycDetails(
                governmentIdUploaded = true,
                selfieUploaded = true,
                addressProofUploaded = true,
                status = KycStatus.SUBMITTED,
            ),
            onboardingStep = OnboardingStep.SERVICE_CATEGORY,
        )

        assertThat(state.kyc.status).isEqualTo(KycStatus.SUBMITTED)
        assertThat(state.onboardingStep).isEqualTo(OnboardingStep.SERVICE_CATEGORY)
    }
}
