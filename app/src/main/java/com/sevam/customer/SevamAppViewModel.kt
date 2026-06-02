package com.sevam.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevam.customer.partner.data.MockPartnerRepository
import com.sevam.customer.partner.data.PartnerAuthClient
import com.sevam.customer.partner.data.PartnerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep {
    BASIC_PROFILE,
    KYC,
    SERVICE_CATEGORY,
    WORK_SETUP,
    PAYOUT,
    ADMIN_REVIEW,
    APPROVED,
}

enum class KycStatus {
    NOT_STARTED,
    SUBMITTED,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED,
}

enum class ApprovalStatus {
    NOT_SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
}

enum class JobStatus {
    REQUESTED,
    ACCEPTED,
    ON_THE_WAY,
    ARRIVED,
    STARTED,
    COMPLETED,
    REJECTED,
}

data class PartnerProfile(
    val name: String = "",
    val phone: String = "",
    val city: String = "Bangalore",
    val area: String = "Koramangala 5th Block",
    val photoLabel: String = "S",
    val category: String? = null,
    val rating: Double = 4.8,
    val completedJobs: Int = 128,
)

data class KycDetails(
    val idType: String = "Aadhaar",
    val governmentIdUploaded: Boolean = false,
    val selfieUploaded: Boolean = false,
    val addressProofUploaded: Boolean = false,
    val status: KycStatus = KycStatus.NOT_STARTED,
)

data class WorkSetup(
    val skills: Set<String> = emptySet(),
    val experienceYears: String = "",
    val toolsAvailable: Boolean = false,
    val serviceArea: String = "Koramangala, HSR Layout, Indiranagar",
    val availability: String = "Mon-Sat, 9 AM - 7 PM",
    val preferredWorkTypes: Set<String> = emptySet(),
)

data class PayoutDetails(
    val upiId: String = "",
    val bankAccountLast4: String = "",
    val added: Boolean = false,
)

data class PartnerJob(
    val id: String,
    val serviceName: String,
    val customerName: String,
    val customerArea: String,
    val fullAddress: String,
    val distanceKm: Double,
    val scheduledTime: String,
    val estimatedEarning: Int,
    val customerPaid: Int,
    val sevamFee: Int,
    val status: JobStatus,
)

data class EarningsSummary(
    val today: Int = 1450,
    val weekly: Int = 8200,
    val monthly: Int = 32400,
    val pendingPayout: Int = 5400,
)

data class SevamPartnerUiState(
    val isLoggedIn: Boolean = false,
    val isDebugSession: Boolean = false,
    val phoneNumber: String = "",
    val otp: String = "",
    val authErrorMessage: String? = null,
    val onboardingStep: OnboardingStep = OnboardingStep.BASIC_PROFILE,
    val profile: PartnerProfile = PartnerProfile(),
    val kyc: KycDetails = KycDetails(),
    val workSetup: WorkSetup = WorkSetup(),
    val payout: PayoutDetails = PayoutDetails(),
    val approvalStatus: ApprovalStatus = ApprovalStatus.NOT_SUBMITTED,
    val isOnline: Boolean = false,
    val selectedJobTab: String = "Requests",
    val jobRequests: List<PartnerJob> = MockPartnerRepository.initialJobRequests,
    val jobs: List<PartnerJob> = MockPartnerRepository.initialJobs,
    val earnings: EarningsSummary = EarningsSummary(),
    val supportCategory: String? = null,
)

private const val OTP_MAX_LENGTH = 6

@HiltViewModel
class SevamAppViewModel @Inject constructor(
    private val repository: PartnerRepository,
    private val authClient: PartnerAuthClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow(repository.initialState())
    val uiState: StateFlow<SevamPartnerUiState> = _uiState

    fun updatePhoneNumber(value: String) {
        _uiState.update { it.copy(phoneNumber = value, authErrorMessage = null) }
    }

    fun updateOtp(value: String) {
        _uiState.update { it.copy(otp = value.filter(Char::isDigit).take(OTP_MAX_LENGTH), authErrorMessage = null) }
    }

    fun requestOtp(onSuccess: () -> Unit) {
        val phone = normalizePhoneNumber(_uiState.value.phoneNumber.ifBlank { "+91 98765 43210" })
        _uiState.update { it.copy(phoneNumber = phone, authErrorMessage = null) }
        viewModelScope.launch {
            val result = authClient.requestOtp(phone)
            if (result.success) {
                onSuccess()
            } else {
                _uiState.update { it.copy(authErrorMessage = result.errorMessage ?: "Could not send OTP.") }
            }
        }
    }

    fun completeLogin() {
        if (_uiState.value.otp.length < OTP_MAX_LENGTH) {
            _uiState.update { it.copy(authErrorMessage = "Enter the 6-digit OTP to continue.") }
            return
        }
        viewModelScope.launch {
            val current = _uiState.value
            val result = authClient.verifyOtp(current.phoneNumber, current.otp)
            if (result.success) {
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        isDebugSession = false,
                        profile = it.profile.copy(phone = current.phoneNumber),
                        otp = "",
                        authErrorMessage = null,
                    )
                }
                refreshPartnerData()
            } else {
                _uiState.update { it.copy(authErrorMessage = result.errorMessage ?: "OTP verification failed.") }
            }
        }
    }

    fun completeDebugLogin() {
        val phone = normalizePhoneNumber(_uiState.value.phoneNumber.ifBlank { "+91 98765 43210" })
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                isDebugSession = true,
                phoneNumber = phone,
                profile = it.profile.copy(phone = phone),
                otp = "",
                authErrorMessage = null,
            )
        }
        refreshPartnerData()
    }

    fun logout() {
        authClient.clearSession()
        _uiState.value = repository.initialState()
    }

    fun updateProfileName(value: String) {
        _uiState.update { it.copy(profile = it.profile.copy(name = value, photoLabel = value.firstOrNull()?.uppercase() ?: "S")) }
    }

    fun updateProfileCity(value: String) {
        _uiState.update { it.copy(profile = it.profile.copy(city = value)) }
    }

    fun updateProfileArea(value: String) {
        _uiState.update { it.copy(profile = it.profile.copy(area = value)) }
    }

    fun submitBasicProfile() {
        val profile = _uiState.value.profile
        if (profile.name.isBlank() || profile.city.isBlank() || profile.area.isBlank()) {
            _uiState.update { it.copy(authErrorMessage = "Add your name, city, and area to continue.") }
            return
        }
        _uiState.update { it.copy(onboardingStep = OnboardingStep.KYC, authErrorMessage = null) }
        syncPartnerProfile()
    }

    fun selectIdType(value: String) {
        _uiState.update { it.copy(kyc = it.kyc.copy(idType = value)) }
    }

    fun toggleKycUpload(type: String) {
        _uiState.update {
            it.copy(
                kyc = when (type) {
                    "id" -> it.kyc.copy(governmentIdUploaded = !it.kyc.governmentIdUploaded)
                    "selfie" -> it.kyc.copy(selfieUploaded = !it.kyc.selfieUploaded)
                    else -> it.kyc.copy(addressProofUploaded = !it.kyc.addressProofUploaded)
                },
            )
        }
    }

    fun submitKyc() {
        val kyc = _uiState.value.kyc
        if (!kyc.governmentIdUploaded || !kyc.selfieUploaded || !kyc.addressProofUploaded) {
            _uiState.update { it.copy(authErrorMessage = "Upload ID, selfie, and address proof before submitting KYC.") }
            return
        }
        _uiState.update {
            it.copy(
                kyc = it.kyc.copy(status = KycStatus.SUBMITTED),
                onboardingStep = OnboardingStep.SERVICE_CATEGORY,
                authErrorMessage = null,
            )
        }
    }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(category = category),
                workSetup = it.workSetup.copy(skills = emptySet(), preferredWorkTypes = emptySet()),
                onboardingStep = OnboardingStep.WORK_SETUP,
            )
        }
    }

    fun toggleSkill(skill: String) {
        _uiState.update {
            val skills = if (skill in it.workSetup.skills) it.workSetup.skills - skill else it.workSetup.skills + skill
            it.copy(workSetup = it.workSetup.copy(skills = skills))
        }
    }

    fun togglePreferredWorkType(type: String) {
        _uiState.update {
            val workTypes = if (type in it.workSetup.preferredWorkTypes) {
                it.workSetup.preferredWorkTypes - type
            } else {
                it.workSetup.preferredWorkTypes + type
            }
            it.copy(workSetup = it.workSetup.copy(preferredWorkTypes = workTypes))
        }
    }

    fun updateExperience(value: String) {
        _uiState.update { it.copy(workSetup = it.workSetup.copy(experienceYears = value.filter(Char::isDigit).take(2))) }
    }

    fun updateServiceArea(value: String) {
        _uiState.update { it.copy(workSetup = it.workSetup.copy(serviceArea = value)) }
    }

    fun updateAvailability(value: String) {
        _uiState.update { it.copy(workSetup = it.workSetup.copy(availability = value)) }
    }

    fun toggleToolsAvailable() {
        _uiState.update { it.copy(workSetup = it.workSetup.copy(toolsAvailable = !it.workSetup.toolsAvailable)) }
    }

    fun submitWorkSetup() {
        val setup = _uiState.value.workSetup
        if (setup.skills.isEmpty() || setup.experienceYears.isBlank()) {
            _uiState.update { it.copy(authErrorMessage = "Choose at least one skill and add your experience.") }
            return
        }
        _uiState.update { it.copy(onboardingStep = OnboardingStep.PAYOUT, authErrorMessage = null) }
        syncPartnerProfile()
    }

    fun updateUpi(value: String) {
        _uiState.update { it.copy(payout = it.payout.copy(upiId = value)) }
    }

    fun updateBankLast4(value: String) {
        _uiState.update { it.copy(payout = it.payout.copy(bankAccountLast4 = value.filter(Char::isDigit).take(4))) }
    }

    fun submitPayout() {
        val payout = _uiState.value.payout
        if (payout.upiId.isBlank() && payout.bankAccountLast4.length < 4) {
            _uiState.update { it.copy(authErrorMessage = "Add a UPI ID or bank account details for payouts.") }
            return
        }
        _uiState.update {
            it.copy(
                payout = it.payout.copy(added = true),
                approvalStatus = ApprovalStatus.UNDER_REVIEW,
                kyc = it.kyc.copy(status = KycStatus.UNDER_REVIEW),
                onboardingStep = OnboardingStep.ADMIN_REVIEW,
                authErrorMessage = null,
            )
        }
        syncPartnerProfile()
    }

    fun approveForDemo() {
        _uiState.update {
            it.copy(
                approvalStatus = ApprovalStatus.APPROVED,
                kyc = it.kyc.copy(status = KycStatus.VERIFIED),
                onboardingStep = OnboardingStep.APPROVED,
                authErrorMessage = null,
            )
        }
    }

    fun toggleOnline() {
        viewModelScope.launch {
            val current = _uiState.value
            _uiState.value = repository.setOnline(current, !current.isOnline)
        }
    }

    fun selectJobTab(tab: String) {
        _uiState.update { it.copy(selectedJobTab = tab) }
    }

    fun acceptJob(jobId: String) {
        viewModelScope.launch {
            _uiState.value = repository.acceptJob(_uiState.value, jobId)
        }
    }

    fun rejectJob(jobId: String) {
        viewModelScope.launch {
            _uiState.value = repository.rejectJob(_uiState.value, jobId)
        }
    }

    fun advanceJob(jobId: String) {
        viewModelScope.launch {
            _uiState.value = repository.advanceJob(_uiState.value, jobId)
        }
    }

    fun selectSupportCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = repository.createSupportTicket(_uiState.value, category)
        }
    }

    fun skillsForSelectedCategory(): List<String> {
        return MockPartnerRepository.skillsByCategory[_uiState.value.profile.category] ?: emptyList()
    }

    fun activeJobs(): List<PartnerJob> = _uiState.value.jobs.filter { it.status in activeStatuses }

    fun completedJobs(): List<PartnerJob> = _uiState.value.jobs.filter { it.status == JobStatus.COMPLETED }

    private fun normalizePhoneNumber(value: String): String {
        val compact = value.replace(" ", "")
        return if (compact.startsWith("+")) compact else "+91$compact"
    }

    private fun refreshPartnerData() {
        viewModelScope.launch {
            _uiState.value = repository.refreshState(_uiState.value)
        }
    }

    private fun syncPartnerProfile() {
        viewModelScope.launch {
            _uiState.value = repository.updateProfile(_uiState.value)
        }
    }

    private companion object {
        val activeStatuses = setOf(JobStatus.ACCEPTED, JobStatus.ON_THE_WAY, JobStatus.ARRIVED, JobStatus.STARTED)
    }
}
