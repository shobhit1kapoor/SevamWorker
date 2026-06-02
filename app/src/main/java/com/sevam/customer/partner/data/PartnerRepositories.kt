package com.sevam.customer.partner.data

import com.sevam.customer.JobStatus
import com.sevam.customer.PartnerJob
import com.sevam.customer.SevamPartnerUiState

interface PartnerRepository {
    fun initialState(): SevamPartnerUiState
}

class MockPartnerRepository : PartnerRepository {
    override fun initialState(): SevamPartnerUiState = SevamPartnerUiState()

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
