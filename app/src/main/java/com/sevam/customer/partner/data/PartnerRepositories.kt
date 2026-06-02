package com.sevam.customer.partner.data

import com.sevam.customer.ApprovalStatus
import com.sevam.customer.EarningsSummary
import com.sevam.customer.JobStatus
import com.sevam.customer.KycStatus
import com.sevam.customer.PartnerJob
import com.sevam.customer.PartnerProfile
import com.sevam.customer.SevamPartnerUiState
import com.sevam.partner.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

interface PartnerRepository {
    fun initialState(): SevamPartnerUiState
    suspend fun refreshState(current: SevamPartnerUiState): SevamPartnerUiState
    suspend fun updateProfile(current: SevamPartnerUiState): SevamPartnerUiState
    suspend fun setOnline(current: SevamPartnerUiState, online: Boolean): SevamPartnerUiState
    suspend fun acceptJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState
    suspend fun rejectJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState
    suspend fun advanceJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState
    suspend fun createSupportTicket(current: SevamPartnerUiState, category: String): SevamPartnerUiState
}

interface PartnerAuthTokenProvider {
    fun accessToken(): String?
}

@Singleton
class InMemoryPartnerAuthTokenProvider @Inject constructor() : PartnerAuthTokenProvider {
    @Volatile
    private var token: String? = null

    override fun accessToken(): String? = token

    fun setAccessToken(value: String?) {
        token = value
    }
}

class MockPartnerRepository @Inject constructor() : PartnerRepository {
    override fun initialState(): SevamPartnerUiState = SevamPartnerUiState()

    override suspend fun refreshState(current: SevamPartnerUiState): SevamPartnerUiState = current.copy(
        jobRequests = initialJobRequests,
        jobs = initialJobs,
        earnings = EarningsSummary(),
    )

    override suspend fun updateProfile(current: SevamPartnerUiState): SevamPartnerUiState = current

    override suspend fun setOnline(current: SevamPartnerUiState, online: Boolean): SevamPartnerUiState {
        return if (current.approvalStatus == ApprovalStatus.APPROVED) {
            current.copy(isOnline = online)
        } else {
            current.copy(authErrorMessage = "Admin approval is required before going online.")
        }
    }

    override suspend fun acceptJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState {
        val request = current.jobRequests.firstOrNull { job -> job.id == jobId } ?: return current
        return current.copy(
            jobRequests = current.jobRequests.filterNot { job -> job.id == jobId },
            jobs = listOf(request.copy(status = JobStatus.ACCEPTED)) + current.jobs,
        )
    }

    override suspend fun rejectJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState {
        return current.copy(jobRequests = current.jobRequests.filterNot { job -> job.id == jobId })
    }

    override suspend fun advanceJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState {
        return current.copy(
            jobs = current.jobs.map { job ->
                if (job.id != jobId) {
                    job
                } else {
                    job.copy(
                        status = when (job.status) {
                            JobStatus.ACCEPTED -> JobStatus.ON_THE_WAY
                            JobStatus.ON_THE_WAY -> JobStatus.ARRIVED
                            JobStatus.ARRIVED -> JobStatus.STARTED
                            JobStatus.STARTED -> JobStatus.COMPLETED
                            else -> job.status
                        },
                    )
                }
            },
        )
    }

    override suspend fun createSupportTicket(current: SevamPartnerUiState, category: String): SevamPartnerUiState {
        return current.copy(supportCategory = category)
    }

    companion object {
        val serviceCategories = listOf(
            "Labour",
            "Plumber",
            "Electrician",
            "House Help",
            "House Cooking",
            "Haircut/Grooming",
        )

        val skillsByCategory = mapOf(
            "Labour" to listOf("Loading", "Unloading", "House shifting", "Gardening", "Construction help"),
            "Plumber" to listOf("Pipe repair", "Tap repair", "Leak repair", "Drain cleaning", "Bathroom fitting"),
            "Electrician" to listOf("Wiring", "Fan install", "Light fitting", "Geyser repair", "MCB repair"),
            "House Help" to listOf("Daily cleaning", "Deep cleaning", "Dish washing", "Laundry", "Senior help"),
            "House Cooking" to listOf("Daily cooking", "One-time cooking", "Vegetarian", "Non-vegetarian", "North Indian", "South Indian"),
            "Haircut/Grooming" to listOf("Men's haircut", "Women's haircut", "Facial", "Massage", "Mehendi", "Waxing"),
        )

        val workTypes = listOf("Instant jobs", "Scheduled jobs", "Daily work", "One-time work")

        val initialJobRequests = listOf(
            PartnerJob(
                id = "REQ-1042",
                serviceName = "Pipe Leak Repair",
                customerName = "Ananya Rao",
                customerArea = "Koramangala 4th Block",
                fullAddress = "A-204, Skyline Apartments, Koramangala 4th Block, Bengaluru 560034",
                distanceKm = 2.1,
                scheduledTime = "Today, 2:30 PM",
                estimatedEarning = 420,
                customerPaid = 549,
                sevamFee = 129,
                status = JobStatus.REQUESTED,
            ),
            PartnerJob(
                id = "REQ-1043",
                serviceName = "Bathroom Tap Repair",
                customerName = "Rohan Mehta",
                customerArea = "HSR Layout Sector 2",
                fullAddress = "23, Green Vista Homes, HSR Layout Sector 2, Bengaluru 560102",
                distanceKm = 4.7,
                scheduledTime = "Today, 5:00 PM",
                estimatedEarning = 360,
                customerPaid = 449,
                sevamFee = 89,
                status = JobStatus.REQUESTED,
            ),
        )

        val initialJobs = listOf(
            PartnerJob(
                id = "JOB-8841",
                serviceName = "AC Service Assistance",
                customerName = "Nikhil Sharma",
                customerArea = "Indiranagar",
                fullAddress = "12, Sunrise Residency, 100 Feet Road, Indiranagar, Bengaluru 560038",
                distanceKm = 5.8,
                scheduledTime = "Today, 11:00 AM",
                estimatedEarning = 520,
                customerPaid = 699,
                sevamFee = 179,
                status = JobStatus.ACCEPTED,
            ),
            PartnerJob(
                id = "JOB-7719",
                serviceName = "Kitchen Sink Repair",
                customerName = "Priya Kapoor",
                customerArea = "BTM Layout",
                fullAddress = "56, Lake View Road, BTM Layout Stage 1, Bengaluru 560029",
                distanceKm = 6.2,
                scheduledTime = "Yesterday, 4:00 PM",
                estimatedEarning = 390,
                customerPaid = 499,
                sevamFee = 109,
                status = JobStatus.COMPLETED,
            ),
        )
    }
}

@Singleton
class RemoteFirstPartnerRepository @Inject constructor(
    private val apiClient: PartnerApiClient,
    private val mockRepository: MockPartnerRepository,
) : PartnerRepository {
    override fun initialState(): SevamPartnerUiState = mockRepository.initialState()

    override suspend fun refreshState(current: SevamPartnerUiState): SevamPartnerUiState {
        val afterProfile = apiClient.getProfile()?.mergeInto(current) ?: current
        val afterJobs = apiClient.getJobs()?.mergeInto(afterProfile) ?: afterProfile
        val afterEarnings = apiClient.getEarnings()?.mergeInto(afterJobs) ?: afterJobs
        return if (afterProfile === current && afterJobs === afterProfile && afterEarnings === afterJobs) {
            mockRepository.refreshState(current)
        } else {
            afterEarnings.copy(authErrorMessage = null)
        }
    }

    override suspend fun updateProfile(current: SevamPartnerUiState): SevamPartnerUiState {
        return apiClient.updateProfile(current)?.mergeInto(current) ?: mockRepository.updateProfile(current)
    }

    override suspend fun setOnline(current: SevamPartnerUiState, online: Boolean): SevamPartnerUiState {
        if (current.approvalStatus != ApprovalStatus.APPROVED) {
            return mockRepository.setOnline(current, online)
        }
        return apiClient.updateProfile(current.copy(isOnline = online))?.mergeInto(current)?.copy(isOnline = online)
            ?: mockRepository.setOnline(current, online)
    }

    override suspend fun acceptJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState {
        return if (apiClient.updateJob(jobId, "ACCEPT")) {
            refreshState(mockRepository.acceptJob(current, jobId))
        } else {
            mockRepository.acceptJob(current, jobId)
        }
    }

    override suspend fun rejectJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState {
        return if (apiClient.updateJob(jobId, "REJECT")) {
            refreshState(mockRepository.rejectJob(current, jobId))
        } else {
            mockRepository.rejectJob(current, jobId)
        }
    }

    override suspend fun advanceJob(current: SevamPartnerUiState, jobId: String): SevamPartnerUiState {
        val job = current.jobs.firstOrNull { it.id == jobId }
        val action = when (job?.status) {
            JobStatus.ARRIVED -> "START"
            JobStatus.STARTED -> "COMPLETE"
            else -> null
        }
        val remoteUpdated = action?.let { apiClient.updateJob(jobId, it, otp = "000000") } ?: false
        return if (remoteUpdated) {
            refreshState(mockRepository.advanceJob(current, jobId))
        } else {
            mockRepository.advanceJob(current, jobId)
        }
    }

    override suspend fun createSupportTicket(current: SevamPartnerUiState, category: String): SevamPartnerUiState {
        apiClient.createSupportTicket(category)
        return mockRepository.createSupportTicket(current, category)
    }
}

@Singleton
class PartnerApiClient @Inject constructor(
    private val tokenProvider: PartnerAuthTokenProvider,
    @com.sevam.customer.di.IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val baseUrl = BuildConfig.SEVAM_API_BASE_URL.trimEnd('/')

    suspend fun getProfile(): PartnerProfileResponse? = getJson("partner/profile")?.let(::PartnerProfileResponse)

    suspend fun updateProfile(state: SevamPartnerUiState): PartnerProfileResponse? {
        val body = JSONObject()
            .put("name", state.profile.name)
            .put("bio", "${state.profile.city} • ${state.profile.area}")
            .put("skills", JSONArray(state.workSetup.skills.map(::mapSkillToJobType)))
            .put("isOnline", state.isOnline)
        return requestJson("PUT", "partner/profile", body)?.let(::PartnerProfileResponse)
    }

    suspend fun getJobs(): PartnerJobsResponse? = getJson("partner/jobs")?.let(::PartnerJobsResponse)

    suspend fun updateJob(jobId: String, action: String, otp: String? = null): Boolean {
        val body = JSONObject().put("action", action)
        if (otp != null) body.put("otp", otp)
        return requestJson("POST", "partner/jobs/$jobId", body) != null
    }

    suspend fun getEarnings(): PartnerEarningsResponse? = getJson("partner/earnings")?.let(::PartnerEarningsResponse)

    suspend fun createSupportTicket(category: String): Boolean {
        val body = JSONObject()
            .put("category", category.uppercase().replace(" ", "_"))
            .put("subject", "$category issue")
            .put("description", "Partner requested help for $category from the Android app.")
        return requestJson("POST", "partner/support", body) != null
    }

    private suspend fun getJson(path: String): JSONObject? = requestJson("GET", path, null)

    private suspend fun requestJson(method: String, path: String, body: JSONObject?): JSONObject? = withContext(ioDispatcher) {
        val token = tokenProvider.accessToken()
        if (baseUrl.isBlank() || token.isNullOrBlank()) return@withContext null

        val connection = (URL("$baseUrl/$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }

        try {
            if (body != null) {
                connection.outputStream.use { stream ->
                    stream.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val code = connection.responseCode
            if (code !in 200..299) return@withContext null

            val text = connection.inputStream.bufferedReader().use { it.readText() }
            if (text.isBlank()) null else JSONObject(text)
        } catch (_: IOException) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun mapSkillToJobType(skill: String): String = when {
        skill.contains("pipe", ignoreCase = true) || skill.contains("tap", ignoreCase = true) || skill.contains("leak", ignoreCase = true) -> "PLUMBING"
        skill.contains("wiring", ignoreCase = true) || skill.contains("fan", ignoreCase = true) || skill.contains("light", ignoreCase = true) -> "ELECTRICAL"
        skill.contains("clean", ignoreCase = true) || skill.contains("dish", ignoreCase = true) -> "CLEANING"
        else -> "OTHER"
    }
}

class PartnerProfileResponse(private val json: JSONObject) {
    fun mergeInto(state: SevamPartnerUiState): SevamPartnerUiState {
        val user = json.optJSONObject("user")
        val partner = json.optJSONObject("partner")
        val skills = partner?.optJSONArray("skills").toStringSet()
        val bio = partner?.optString("bio").orEmpty()
        val cityArea = bio.split("•").map { it.trim() }

        return state.copy(
            profile = state.profile.copy(
                name = user?.optString("name").orEmpty().ifBlank { state.profile.name },
                phone = user?.optString("phone").orEmpty().ifBlank { state.profile.phone },
                city = cityArea.getOrNull(0).orEmpty().ifBlank { state.profile.city },
                area = cityArea.getOrNull(1).orEmpty().ifBlank { state.profile.area },
                category = skills.firstOrNull()?.toWorkerCategory() ?: state.profile.category,
                rating = partner?.optDouble("rating", state.profile.rating) ?: state.profile.rating,
                completedJobs = partner?.optInt("totalJobs", state.profile.completedJobs) ?: state.profile.completedJobs,
            ),
            workSetup = state.workSetup.copy(skills = skills.map { it.toWorkerSkill() }.toSet()),
            isOnline = partner?.optBoolean("isOnline", state.isOnline) ?: state.isOnline,
            approvalStatus = if (partner?.optBoolean("isApproved", false) == true) ApprovalStatus.APPROVED else ApprovalStatus.UNDER_REVIEW,
            kyc = state.kyc.copy(status = if (partner?.optString("kycStatus") == "SUBMITTED") KycStatus.SUBMITTED else state.kyc.status),
        )
    }
}

class PartnerJobsResponse(private val json: JSONObject) {
    fun mergeInto(state: SevamPartnerUiState): SevamPartnerUiState {
        val requests = json.optJSONArray("requests").toJobList(JobStatus.REQUESTED)
        val active = json.optJSONArray("active").toJobList(JobStatus.ACCEPTED)
        val upcoming = json.optJSONArray("upcoming").toJobList(JobStatus.ACCEPTED)
        val completed = json.optJSONArray("completed").toJobList(JobStatus.COMPLETED)
        return state.copy(
            jobRequests = requests.ifEmpty { state.jobRequests },
            jobs = (active + upcoming + completed).ifEmpty { state.jobs },
        )
    }
}

class PartnerEarningsResponse(private val json: JSONObject) {
    fun mergeInto(state: SevamPartnerUiState): SevamPartnerUiState {
        val summary = json.optJSONObject("summary") ?: return state
        return state.copy(
            earnings = EarningsSummary(
                today = summary.optInt("todayEarnings", state.earnings.today),
                weekly = summary.optInt("weeklyEarnings", state.earnings.weekly),
                monthly = summary.optInt("monthlyEarnings", state.earnings.monthly),
                pendingPayout = summary.optInt("pendingPayout", state.earnings.pendingPayout),
            ),
        )
    }
}

private fun JSONArray?.toStringSet(): Set<String> {
    if (this == null) return emptySet()
    return buildSet {
        for (index in 0 until length()) add(optString(index))
    }
}

private fun JSONArray?.toJobList(defaultStatus: JobStatus): List<PartnerJob> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                PartnerJob(
                    id = item.optString("id"),
                    serviceName = item.optString("serviceType").toServiceName(),
                    customerName = item.optString("customerName").ifBlank { "Customer" },
                    customerArea = item.optString("customerArea").ifBlank { "Nearby area" },
                    fullAddress = item.optString("fullAddress").ifBlank { "Address visible after accepting" },
                    distanceKm = item.optDouble("distanceKm", 0.0),
                    scheduledTime = item.optString("scheduledAt").ifBlank { "Scheduled" },
                    estimatedEarning = item.optInt("estimatedEarning"),
                    customerPaid = item.optInt("customerPaid"),
                    sevamFee = item.optInt("sevamFee"),
                    status = item.optString("status").toPartnerStatus(defaultStatus),
                ),
            )
        }
    }
}

private fun String.toPartnerStatus(default: JobStatus): JobStatus = when (this) {
    "PENDING" -> JobStatus.REQUESTED
    "ACCEPTED" -> JobStatus.ACCEPTED
    "IN_PROGRESS" -> JobStatus.STARTED
    "COMPLETED" -> JobStatus.COMPLETED
    "CANCELLED" -> JobStatus.REJECTED
    else -> default
}

private fun String.toServiceName(): String = lowercase()
    .split("_")
    .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

private fun String.toWorkerCategory(): String = when (this) {
    "PLUMBING" -> "Plumber"
    "ELECTRICAL" -> "Electrician"
    "CLEANING" -> "House Help"
    else -> "Labour"
}

private fun String.toWorkerSkill(): String = when (this) {
    "PLUMBING" -> "Pipe repair"
    "ELECTRICAL" -> "Wiring"
    "CLEANING" -> "Daily cleaning"
    else -> "One-time work"
}
