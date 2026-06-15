package com.sevam.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.ToggleOff
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.sevam.core.ui.SevamColors
import com.sevam.customer.partner.data.MockPartnerRepository
import com.sevam.partner.BuildConfig

private const val LOGIN_ROUTE = "login"
private const val OTP_ROUTE = "otp"
private const val ONBOARDING_ROUTE = "onboarding"
private const val HOME_ROUTE = "home"
private const val JOBS_ROUTE = "jobs"
private const val EARNINGS_ROUTE = "earnings"
private const val PROFILE_ROUTE = "profile"

private data class PartnerBottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun SevamNavGraph(
    viewModel: SevamAppViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val shellRoutes = setOf(HOME_ROUTE, JOBS_ROUTE, EARNINGS_ROUTE, PROFILE_ROUTE)
    val shouldShowShell = currentRoute in shellRoutes

    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            viewModel.openDebugApprovedHome()
        }
    }

    LaunchedEffect(state.isLoggedIn, state.onboardingStep) {
        if (!state.isLoggedIn && currentRoute != LOGIN_ROUTE && currentRoute != OTP_ROUTE) {
            navController.navigate(LOGIN_ROUTE) {
                popUpTo(0)
            }
        } else if (state.isLoggedIn && state.onboardingStep != OnboardingStep.APPROVED && currentRoute != ONBOARDING_ROUTE) {
            navController.navigate(ONBOARDING_ROUTE) {
                popUpTo(LOGIN_ROUTE) { inclusive = true }
            }
        } else if (state.isLoggedIn && state.onboardingStep == OnboardingStep.APPROVED && currentRoute !in shellRoutes) {
            navController.navigate(HOME_ROUTE) {
                popUpTo(LOGIN_ROUTE) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFFAFBFF),
        bottomBar = {
            if (shouldShowShell) {
                PartnerBottomBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = false
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LOGIN_ROUTE,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LOGIN_ROUTE) {
                PartnerLoginScreen(
                    phoneNumber = state.phoneNumber,
                    errorMessage = state.authErrorMessage,
                    onPhoneNumberChange = viewModel::updatePhoneNumber,
                    onContinue = {
                        viewModel.requestOtp {
                            navController.navigate(OTP_ROUTE)
                        }
                    },
                    onDebugContinue = viewModel::completeDebugLogin,
                )
            }
            composable(OTP_ROUTE) {
                PartnerOtpScreen(
                    phoneNumber = state.phoneNumber,
                    otp = state.otp,
                    errorMessage = state.authErrorMessage,
                    onOtpChange = viewModel::updateOtp,
                    onBack = { navController.popBackStack() },
                    onVerify = viewModel::completeLogin,
                    onDebugContinue = viewModel::completeDebugLogin,
                )
            }
            composable(ONBOARDING_ROUTE) {
                PartnerOnboardingScreen(state = state, viewModel = viewModel)
            }
            composable(HOME_ROUTE) {
                PartnerHomeScreen(state = state, viewModel = viewModel)
            }
            composable(JOBS_ROUTE) {
                PartnerJobsScreen(state = state, viewModel = viewModel)
            }
            composable(EARNINGS_ROUTE) {
                PartnerEarningsScreen(state = state)
            }
            composable(PROFILE_ROUTE) {
                PartnerProfileScreen(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun PartnerBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        PartnerBottomItem(HOME_ROUTE, "Home", Icons.Outlined.Home),
        PartnerBottomItem(JOBS_ROUTE, "Jobs", Icons.Outlined.WorkOutline),
        PartnerBottomItem(EARNINGS_ROUTE, "Earnings", Icons.Outlined.AttachMoney),
        PartnerBottomItem(PROFILE_ROUTE, "Profile", Icons.Outlined.AccountCircle),
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Colors.border),
    ) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(24.dp)) },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (currentRoute == item.route) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Colors.blue,
                    selectedTextColor = Colors.blue,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Colors.muted,
                    unselectedTextColor = Colors.muted,
                ),
            )
        }
    }
    }
}

@Composable
private fun PartnerLoginScreen(
    phoneNumber: String,
    errorMessage: String?,
    onPhoneNumberChange: (String) -> Unit,
    onContinue: () -> Unit,
    onDebugContinue: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            BrandMark()
            Text("Welcome to Sevam Partner", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Get verified, receive nearby jobs, and manage your earnings from one simple app.",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        item {
            PartnerCard {
                Text("Sign in with phone OTP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone number") },
                    placeholder = { Text("+91 98765 43210") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                )
                errorMessage?.let { ErrorText(it) }
                PrimaryAction("Send OTP", onClick = onContinue)
                TextButton(onClick = onDebugContinue, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Continue in Debug")
                }
            }
        }
        item {
            TrustRow("Verified jobs", Icons.Outlined.CheckCircle)
            TrustRow("Transparent payout breakdown", Icons.Outlined.CreditCard)
            TrustRow("Support for workers and safety issues", Icons.Outlined.SupportAgent)
        }
    }
}

@Composable
private fun PartnerOtpScreen(
    phoneNumber: String,
    otp: String,
    errorMessage: String?,
    onOtpChange: (String) -> Unit,
    onBack: () -> Unit,
    onVerify: () -> Unit,
    onDebugContinue: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text("Verify your OTP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Enter the 6-digit code sent to $phoneNumber.", color = Color(0xFF64748B))
        }
        item {
            OutlinedTextField(
                value = otp,
                onValueChange = onOtpChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            errorMessage?.let { ErrorText(it) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                    Text("Back")
                }
                PrimaryAction("Verify", modifier = Modifier.weight(1f), onClick = onVerify)
            }
            TextButton(onClick = onDebugContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Continue in Debug")
            }
        }
    }
}

@Composable
private fun PartnerOnboardingScreen(
    state: SevamPartnerUiState,
    viewModel: SevamAppViewModel,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(18.dp),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Set up your partner profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(stepSubtitle(state.onboardingStep), color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
            StepProgress(current = state.onboardingStep)
        }
        item {
            when (state.onboardingStep) {
                OnboardingStep.BASIC_PROFILE -> BasicProfileStep(state, viewModel)
                OnboardingStep.KYC -> KycStep(state, viewModel)
                OnboardingStep.SERVICE_CATEGORY -> CategoryStep(viewModel)
                OnboardingStep.WORK_SETUP -> WorkSetupStep(state, viewModel)
                OnboardingStep.PAYOUT -> PayoutStep(state, viewModel)
                OnboardingStep.ADMIN_REVIEW -> AdminReviewStep(state, viewModel)
                OnboardingStep.APPROVED -> Unit
            }
        }
    }
}

@Composable
private fun BasicProfileStep(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    PartnerCard {
        Text("Basic profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(state.profile.name, viewModel::updateProfileName, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.profile.phone, {}, enabled = false, label = { Text("Phone number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.profile.city, viewModel::updateProfileCity, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.profile.area, viewModel::updateProfileArea, label = { Text("Address area") }, modifier = Modifier.fillMaxWidth())
        UploadRow("Profile photo", uploaded = true, onClick = {})
        state.authErrorMessage?.let { ErrorText(it) }
        PrimaryAction("Continue to KYC", onClick = viewModel::submitBasicProfile)
    }
}

@Composable
private fun KycStep(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    PartnerCard {
        Text("KYC verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Upload documents so Sevam can verify that you are real and trustworthy.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Aadhaar", "PAN", "Voter ID", "Driving License")) { idType ->
                ChoiceChip(label = idType, selected = state.kyc.idType == idType) { viewModel.selectIdType(idType) }
            }
        }
        UploadRow("${state.kyc.idType} document", state.kyc.governmentIdUploaded) { viewModel.toggleKycUpload("id") }
        UploadRow("Selfie", state.kyc.selfieUploaded) { viewModel.toggleKycUpload("selfie") }
        UploadRow("Address proof", state.kyc.addressProofUploaded) { viewModel.toggleKycUpload("address") }
        StatusPill("Status: ${state.kyc.status.displayName()}", Colors.infoBg, Colors.info)
        state.authErrorMessage?.let { ErrorText(it) }
        PrimaryAction("Submit KYC", onClick = viewModel::submitKyc)
    }
}

@Composable
private fun CategoryStep(viewModel: SevamAppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MockPartnerRepository.serviceCategories.forEach { category ->
            PartnerCard(modifier = Modifier.clickable { viewModel.selectCategory(category) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(categoryIcon(category), contentDescription = null, tint = Colors.blue)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(category, fontWeight = FontWeight.SemiBold)
                        Text("${MockPartnerRepository.skillsByCategory[category].orEmpty().take(3).joinToString(", ")}...", color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun WorkSetupStep(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    PartnerCard {
        Text("${state.profile.category} setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Choose your skills and work preferences.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
        FlowChips(viewModel.skillsForSelectedCategory(), state.workSetup.skills, viewModel::toggleSkill)
        OutlinedTextField(
            value = state.workSetup.experienceYears,
            onValueChange = viewModel::updateExperience,
            label = { Text("Years of experience") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        ToggleCard("Tools available", state.workSetup.toolsAvailable, viewModel::toggleToolsAvailable)
        OutlinedTextField(state.workSetup.serviceArea, viewModel::updateServiceArea, label = { Text("Service area") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.workSetup.availability, viewModel::updateAvailability, label = { Text("Availability") }, modifier = Modifier.fillMaxWidth())
        Text("Preferred work type", fontWeight = FontWeight.SemiBold)
        FlowChips(MockPartnerRepository.workTypes, state.workSetup.preferredWorkTypes, viewModel::togglePreferredWorkType)
        state.authErrorMessage?.let { ErrorText(it) }
        PrimaryAction("Continue to payouts", onClick = viewModel::submitWorkSetup)
    }
}

@Composable
private fun PayoutStep(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    PartnerCard {
        Text("Payout details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Add UPI or bank details so Sevam can pay you after completed jobs.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(state.payout.upiId, viewModel::updateUpi, label = { Text("UPI ID") }, placeholder = { Text("name@upi") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            state.payout.bankAccountLast4,
            viewModel::updateBankLast4,
            label = { Text("Bank account last 4 digits") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        state.authErrorMessage?.let { ErrorText(it) }
        PrimaryAction("Submit for admin review", onClick = viewModel::submitPayout)
    }
}

@Composable
private fun AdminReviewStep(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    PartnerCard {
        Icon(Icons.Outlined.PendingActions, contentDescription = null, tint = Colors.blue, modifier = Modifier.size(42.dp))
        Text("Profile under review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Your documents, service category, work details, and payout information have been submitted. Sevam admin approval is required before you can go online.",
            color = Color(0xFF64748B),
        )
        StatusPill("Admin status: ${state.approvalStatus.displayName()}", Colors.warningBg, Colors.warning)
        OutlinedButton(onClick = viewModel::approveForDemo, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Debug: preview approved worker home")
        }
    }
}

@Composable
private fun PartnerHomeScreen(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            HomePartnerHeader(state)
        }
        item {
            HeroStatusCard(state, viewModel)
        }
        item {
            MetricGrid(state)
        }
        if (state.approvalStatus != ApprovalStatus.APPROVED) {
            item {
                ReviewBanner()
            }
        }
        item {
            SectionHeader("New Job Request", null)
        }
        if (state.jobRequests.isEmpty()) {
            item { EmptyCard("No new requests right now. Stay online to receive nearby jobs.") }
        } else {
            items(state.jobRequests.take(1)) { job ->
                JobRequestCard(job = job, onAccept = { viewModel.acceptJob(job.id) }, onReject = { viewModel.rejectJob(job.id) })
            }
        }
    }
}

@Composable
private fun PartnerJobsScreen(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    val tabs = listOf("Requests", "Active", "Completed")
    val selectedTab = state.selectedJobTab.takeIf { it in tabs } ?: "Requests"
    WorkerScaffold(title = "Jobs", subtitle = "Manage your service jobs") {
        item {
            SegmentedTabs(tabs = tabs, selected = selectedTab) { tab ->
                viewModel.selectJobTab(tab)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(Icons.Outlined.WorkOutline, "Requests", "${state.jobRequests.size}", Colors.blueSoft, Colors.blue, Modifier.weight(1f))
                StatTile(Icons.Outlined.PlayCircle, "Active", "${viewModel.activeJobs().size}", Colors.successBg, Colors.success, Modifier.weight(1f))
                StatTile(Icons.Outlined.CheckCircle, "Done", "${viewModel.completedJobs().size}", Color(0xFFF3E8FF), Color(0xFF8B5CF6), Modifier.weight(1f))
            }
        }
        when (selectedTab) {
            "Requests" -> {
                if (state.jobRequests.isEmpty()) item { EmptyCard("No pending requests.") }
                items(state.jobRequests) { job -> JobRequestCard(job, { viewModel.acceptJob(job.id) }, { viewModel.rejectJob(job.id) }) }
            }
            "Active" -> {
                val activeJobs = viewModel.activeJobs()
                if (activeJobs.isEmpty()) item { EmptyCard("No active jobs.") }
                items(activeJobs) { job -> ActiveJobCard(job, onAdvance = { viewModel.advanceJob(job.id) }) }
            }
            "Completed" -> {
                val completed = viewModel.completedJobs()
                if (completed.isEmpty()) item { EmptyCard("Completed jobs will appear here.") }
                items(completed) { job -> CompletedJobCard(job) }
            }
        }
    }
}

@Composable
private fun PartnerEarningsScreen(state: SevamPartnerUiState) {
    WorkerScaffold(title = "Earnings", subtitle = "Track income and payouts") {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                EarningSummaryCard("Today", "Rs ${state.earnings.today}", Icons.Outlined.Payments, Colors.successBg, Colors.success, Modifier.weight(1f))
                EarningSummaryCard("Total", "Rs ${state.earnings.monthly}", Icons.Outlined.CalendarMonth, Colors.blueSoft, Colors.blue, Modifier.weight(1f))
            }
        }
        item { SectionHeader("Recent Earnings", null) }
        item { SegmentedTabs(tabs = listOf("All", "Paid", "Pending"), selected = "All", onSelect = {}) }
        items(state.jobs) { job ->
            EarningHistoryCard(job)
        }
    }
}

@Composable
private fun PartnerProfileScreen(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    WorkerScaffold(title = "", subtitle = "") {
        item {
            ProfileHero(state)
        }
        item {
            ProfileStats(state)
        }
        item {
            PartnerCard(contentPadding = 18.dp) {
                ProfileMenuRow(Icons.Outlined.Person, "Personal Information", "View and edit your details") {}
                ProfileDivider()
                ProfileMenuRow(Icons.Outlined.Verified, "KYC Information", "View your KYC details") {}
                ProfileDivider()
                ProfileMenuRow(Icons.Outlined.CreditCard, "Bank Information", "View your bank details") {}
                ProfileDivider()
                ProfileMenuRow(Icons.Outlined.Description, "Documents", "Manage your documents") {}
                ProfileDivider()
                ProfileMenuRow(Icons.Outlined.Notifications, "Notification Settings", "Manage preferences") {}
                ProfileDivider()
                ProfileMenuRow(Icons.Outlined.SupportAgent, "Help & Support", "Get help and contact support") {
                    viewModel.selectSupportCategory("Help & Support")
                }
            }
        }
        item {
            PartnerCard(modifier = Modifier.clickable(onClick = viewModel::logout), contentPadding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconTile(Icons.Outlined.Logout, Color(0xFFFFEFEF), Color(0xFFEF4444))
                    Text("Logout", color = Color(0xFFEF4444), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Colors.muted)
                }
            }
        }
    }
}

@Composable
private fun WorkerScaffold(
    title: String,
    subtitle: String,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        if (title.isNotBlank() || subtitle.isNotBlank()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (title.isNotBlank()) {
                            Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Colors.text)
                        }
                        if (subtitle.isNotBlank()) {
                            Text(subtitle, color = Colors.muted, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal)
                        }
                    }
                    NotificationButton()
                }
            }
        }
        content()
    }
}

@Composable
private fun HeroStatusCard(state: SevamPartnerUiState, viewModel: SevamAppViewModel) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Colors.blue,
        shadowElevation = 10.dp,
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFF0875FF), Color(0xFF0054E9))))
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.95f), modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(shape = CircleShape, color = if (state.isOnline) Color(0xFF18C964) else Colors.muted, modifier = Modifier.size(20.dp)) {}
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (state.isOnline) "You're Online" else "You're Offline", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        if (state.isOnline) "You are ready to receive jobs." else "Go online when you are ready.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    StatusPill("KYC Verified", Color.White, Colors.blue)
                }
                Icon(
                    if (state.isOnline) Icons.Outlined.ToggleOn else Icons.Outlined.ToggleOff,
                    contentDescription = null,
                    tint = if (state.isOnline) Color(0xFF22C55E) else Color.White.copy(alpha = 0.72f),
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { viewModel.toggleOnline() },
                )
            }
        }
    }
}

@Composable
private fun MetricGrid(state: SevamPartnerUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile(Icons.Outlined.Payments, "Today's Earnings", "Rs ${state.earnings.today}", Colors.successBg, Colors.success, Modifier.weight(1f))
        StatTile(Icons.Outlined.WorkOutline, "Today's Jobs", "${state.jobs.count { it.status != JobStatus.COMPLETED }}", Colors.blueSoft, Colors.blue, Modifier.weight(1f))
        StatTile(Icons.Outlined.Star, "Rating", "${state.profile.rating}", Color(0xFFFFF4E5), Colors.orange, Modifier.weight(1f))
    }
}

@Composable
private fun JobRequestCard(job: PartnerJob, onAccept: () -> Unit, onReject: () -> Unit) {
    PartnerCard(contentPadding = 16.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                JobIconBubble(job.serviceName)
                StatusPill(if (job.distanceKm <= 3.0) "New" else "Nearby", Colors.blueSoft, Colors.blue)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(job.serviceName.shortServiceName(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Colors.text, modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Estimated Earning", color = Colors.muted, style = MaterialTheme.typography.bodySmall)
                        Text("Rs ${job.estimatedEarning}", fontWeight = FontWeight.Bold, color = Colors.orange, style = MaterialTheme.typography.headlineSmall)
                    }
                }
                InfoRow(Icons.Outlined.CalendarMonth, job.scheduledTime.asCompactTimeLabel())
                InfoRow(Icons.Outlined.LocationOn, job.customerArea)
                InfoRow(Icons.Outlined.Map, "${job.distanceKm} km away")
            }
        }
        HorizontalDivider(color = Colors.border)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Colors.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Colors.text),
            ) {
                Text("Reject")
            }
            PrimaryAction("Accept Job", modifier = Modifier.weight(1f), onClick = onAccept)
        }
    }
}

@Composable
private fun ActiveJobCard(job: PartnerJob, onAdvance: () -> Unit) {
    PartnerCard(contentPadding = 20.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            JobIconBubble(job.serviceName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(job.status.displayName(), Colors.infoBg, Colors.info)
                Text(job.serviceName.shortServiceName(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Colors.text)
                Text(job.customerName, color = Colors.muted)
                InfoRow(Icons.Outlined.LocationOn, job.fullAddress)
                InfoRow(Icons.Outlined.CalendarMonth, job.scheduledTime.asCompactTimeLabel())
            }
            Text("Rs ${job.estimatedEarning}", fontWeight = FontWeight.Bold, color = Colors.orange, style = MaterialTheme.typography.titleLarge)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Colors.border)) {
                Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Call")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Colors.border)) {
                Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Map")
            }
        }
        PrimaryAction(nextJobAction(job.status), onClick = onAdvance)
        if (job.status == JobStatus.ARRIVED) Text("Customer start OTP required before starting.", color = Colors.warning, style = MaterialTheme.typography.bodySmall)
        if (job.status == JobStatus.STARTED) Text("Customer completion OTP required before completing.", color = Colors.warning, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CompletedJobCard(job: PartnerJob) {
    PartnerCard(contentPadding = 20.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            JobIconBubble(job.serviceName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill("Completed", Colors.successBg, Colors.success)
                Text(job.serviceName.shortServiceName(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Colors.text)
                Text(job.customerArea, color = Colors.muted)
            }
            Text("Rs ${job.estimatedEarning}", fontWeight = FontWeight.Bold, color = Colors.text, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun PayoutHistoryCard(job: PartnerJob) {
    PartnerCard {
        Text(job.serviceName, fontWeight = FontWeight.SemiBold)
        InfoList(
            listOf(
                "Customer paid" to "Rs ${job.customerPaid}",
                "Sevam fee" to "Rs ${job.sevamFee}",
                "You earned" to "Rs ${job.estimatedEarning}",
            ),
        )
    }
}

@Composable
private fun HomePartnerHeader(state: SevamPartnerUiState) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Avatar(label = state.profile.photoLabel, size = 72.dp)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(displayName(state.profile.name).substringBefore(" "), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Colors.text)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.profile.category ?: "Partner", color = Colors.muted, style = MaterialTheme.typography.titleMedium)
                Text("-", color = Colors.muted, style = MaterialTheme.typography.titleMedium)
                Text("Verified Partner", color = Colors.muted, style = MaterialTheme.typography.titleMedium)
                Icon(Icons.Outlined.Verified, contentDescription = null, tint = Colors.blue, modifier = Modifier.size(20.dp))
            }
        }
        NotificationButton()
    }
}

@Composable
private fun NotificationButton() {
    Box {
        Surface(shape = CircleShape, color = Color.White, shadowElevation = 2.dp, border = BorderStroke(1.dp, Colors.border)) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Colors.text, modifier = Modifier.padding(12.dp).size(24.dp))
        }
        Surface(shape = CircleShape, color = Color(0xFFFF3B4F), modifier = Modifier.align(Alignment.TopEnd).size(10.dp)) {}
    }
}

@Composable
private fun SegmentedTabs(tabs: List<String>, selected: String, onSelect: (String) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Colors.border), shadowElevation = 1.dp) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                val isSelected = selected == tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(tab) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Colors.blue else Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(tab, color = if (isSelected) Color.White else Colors.muted, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    label: String,
    value: String,
    iconBackground: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    PartnerCard(modifier = modifier, contentPadding = 12.dp) {
        IconTile(icon, iconBackground, iconColor)
        Text(label, color = Colors.muted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = Colors.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EarningSummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconBackground: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    PartnerCard(modifier = modifier, contentPadding = 18.dp) {
        IconTile(icon, iconBackground, iconColor)
        Text(label, color = Colors.muted, style = MaterialTheme.typography.titleMedium)
        Text(value, color = Colors.text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EarningHistoryCard(job: PartnerJob) {
    val paid = job.status == JobStatus.COMPLETED
    PartnerCard(contentPadding = 18.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            JobIconBubble(job.serviceName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(job.serviceName.shortServiceName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Colors.text)
                InfoRow(Icons.Outlined.CalendarMonth, job.scheduledTime.asCompactTimeLabel())
                InfoRow(Icons.Outlined.LocationOn, job.customerArea)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Rs ${job.estimatedEarning}", color = if (paid) Colors.success else Colors.orange, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                StatusPill(if (paid) "Paid" else "Pending", if (paid) Colors.successBg else Colors.warningBg, if (paid) Colors.success else Colors.warning)
            }
        }
    }
}

@Composable
private fun ProfileHero(state: SevamPartnerUiState) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Avatar(label = state.profile.photoLabel, size = 82.dp)
        Spacer(Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(displayName(state.profile.name), color = Colors.text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.profile.category ?: "Partner", color = Colors.muted, style = MaterialTheme.typography.titleMedium)
                Text("-", color = Colors.muted)
                Text("Verified Partner", color = Colors.muted, style = MaterialTheme.typography.titleMedium)
                Icon(Icons.Outlined.Verified, contentDescription = null, tint = Colors.blue, modifier = Modifier.size(20.dp))
            }
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Colors.muted, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun ProfileStats(state: SevamPartnerUiState) {
    PartnerCard(contentPadding = 18.dp) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStat(Icons.Outlined.WorkOutline, "${state.profile.completedJobs}", "Jobs Completed", Colors.blueSoft, Colors.blue, Modifier.weight(1f))
            VerticalDivider()
            ProfileStat(Icons.Outlined.AttachMoney, "Rs ${state.earnings.monthly}", "Total Earnings", Colors.successBg, Colors.success, Modifier.weight(1f))
            VerticalDivider()
            ProfileStat(Icons.Outlined.Star, "${state.profile.rating}", "Rating", Color(0xFFFFF4E5), Colors.orange, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProfileStat(icon: ImageVector, value: String, label: String, iconBackground: Color, iconColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IconTile(icon, iconBackground, iconColor)
        Text(value, color = Colors.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, color = Colors.muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProfileMenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconTile(icon, Colors.blueSoft, Colors.blue)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Colors.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Colors.muted, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Colors.muted)
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(color = Colors.border, modifier = Modifier.padding(start = 74.dp))
}

@Composable
private fun IconTile(icon: ImageVector, background: Color, foreground: Color) {
    Surface(shape = RoundedCornerShape(15.dp), color = background, modifier = Modifier.size(50.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
private fun JobIconBubble(serviceName: String) {
    val isElectrical = serviceName.contains("switch", ignoreCase = true) || serviceName.contains("electric", ignoreCase = true)
    IconTile(
        icon = if (isElectrical) Icons.Outlined.Badge else Icons.Outlined.SupportAgent,
        background = if (isElectrical) Color(0xFFFFF4E5) else Colors.blueSoft,
        foreground = if (isElectrical) Colors.orange else Colors.blue,
    )
}

@Composable
private fun VerticalDivider() {
    Surface(modifier = Modifier.width(1.dp).height(92.dp), color = Colors.border) {}
}

@Composable
private fun PartnerCard(
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.CardColors = CardDefaults.cardColors(containerColor = Color.White),
    contentColor: Color = Color.Unspecified,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = colors.containerColor,
        contentColor = contentColor,
        shadowElevation = 5.dp,
        border = BorderStroke(1.dp, Colors.border),
    ) {
        Column(modifier = Modifier.padding(contentPadding), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun StepProgress(current: OnboardingStep) {
    val steps = listOf(
        OnboardingStep.BASIC_PROFILE,
        OnboardingStep.KYC,
        OnboardingStep.SERVICE_CATEGORY,
        OnboardingStep.WORK_SETUP,
        OnboardingStep.PAYOUT,
        OnboardingStep.ADMIN_REVIEW,
    )
    val currentIndex = steps.indexOf(current).coerceAtLeast(0)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        steps.forEachIndexed { index, _ ->
            Surface(
                modifier = Modifier.weight(1f).height(5.dp),
                shape = RoundedCornerShape(999.dp),
                color = if (index <= currentIndex) Colors.blue else Color(0xFFE2E8F0),
            ) {}
        }
    }
}

@Composable
private fun PrimaryAction(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Colors.blue),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier = Modifier) {
    PartnerCard(modifier = modifier) {
        Text(label, color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun FlowChips(options: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { option ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggle(option) },
                        shape = RoundedCornerShape(999.dp),
                        color = if (option in selected) Colors.blueSoft else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (option in selected) Colors.blue else Color(0xFFE2E8F0)),
                    ) {
                        Text(option, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), color = if (option in selected) Colors.blue else Color(0xFF334155))
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun UploadRow(label: String, uploaded: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (uploaded) Colors.successBg else Color(0xFFF8FAFC),
        modifier = Modifier.clickable(onClick = onClick),
        border = BorderStroke(1.dp, if (uploaded) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Description, contentDescription = null, tint = if (uploaded) Colors.success else Color(0xFF64748B))
                Text(label, fontWeight = FontWeight.SemiBold)
            }
            Text(if (uploaded) "Added" else "Tap to add", color = if (uploaded) Colors.success else Colors.blue, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ToggleCard(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Icon(if (selected) Icons.Outlined.ToggleOn else Icons.Outlined.ToggleOff, contentDescription = null, tint = if (selected) Colors.blue else Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Colors.text)
        subtitle?.let { Text(it, color = Colors.muted, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun ReviewBanner() {
    PartnerCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.PendingActions, contentDescription = null, tint = Colors.warning)
            Column {
                Text("Profile under review", fontWeight = FontWeight.SemiBold)
                Text("You can explore the app, but real jobs unlock after admin approval.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    PartnerCard {
        Text(message, color = Color(0xFF64748B))
    }
}

@Composable
private fun InfoList(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { (label, value) ->
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(label, color = Color(0xFF64748B))
                Text(value, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Colors.blue)
            Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = Colors.muted, modifier = Modifier.size(18.dp))
        Text(text, color = Colors.muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StatusPill(text: String, background: Color, foreground: Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = background) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = foreground,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun Avatar(label: String, size: androidx.compose.ui.unit.Dp = 48.dp) {
    Surface(shape = CircleShape, color = Colors.blueSoft, shadowElevation = 2.dp, modifier = Modifier.size(size), border = BorderStroke(1.dp, Color(0xFFBFD8FF))) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.ifBlank { "S" }.take(1),
                color = Colors.blue,
                fontWeight = FontWeight.Bold,
                style = if (size > 60.dp) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun BrandMark() {
    Surface(shape = CircleShape, color = Colors.orange, shadowElevation = 4.dp) {
        Text(
            "S",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TrustRow(text: String, icon: ImageVector) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(14.dp), color = Colors.blueSoft) {
            Icon(icon, contentDescription = null, tint = Colors.blue, modifier = Modifier.padding(10.dp).size(20.dp))
        }
        Text(text, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(message, color = Color(0xFFDC2626), style = MaterialTheme.typography.bodySmall)
}

private fun stepSubtitle(step: OnboardingStep): String {
    return when (step) {
        OnboardingStep.BASIC_PROFILE -> "Start with your basic details."
        OnboardingStep.KYC -> "Verify your identity and address."
        OnboardingStep.SERVICE_CATEGORY -> "Choose the type of work you provide."
        OnboardingStep.WORK_SETUP -> "Add skills, experience, tools, and availability."
        OnboardingStep.PAYOUT -> "Add payout details for worker earnings."
        OnboardingStep.ADMIN_REVIEW -> "Wait for Sevam admin approval."
        OnboardingStep.APPROVED -> "You are ready to receive jobs."
    }
}

private fun nextJobAction(status: JobStatus): String {
    return when (status) {
        JobStatus.ACCEPTED -> "Mark On the Way"
        JobStatus.ON_THE_WAY -> "Mark Arrived"
        JobStatus.ARRIVED -> "Start Job with OTP"
        JobStatus.STARTED -> "Complete Job with OTP"
        else -> "Done"
    }
}

private fun categoryIcon(category: String): ImageVector {
    return when (category) {
        "Labour" -> Icons.Outlined.WorkOutline
        "Plumber" -> Icons.Outlined.SupportAgent
        "Electrician" -> Icons.Outlined.Badge
        "House Help" -> Icons.Outlined.Home
        "House Cooking" -> Icons.Outlined.Person
        else -> Icons.Outlined.AccountCircle
    }
}

private fun KycStatus.displayName(): String {
    return when (this) {
        KycStatus.NOT_STARTED -> "Not Started"
        KycStatus.SUBMITTED -> "Submitted"
        KycStatus.UNDER_REVIEW -> "Under Review"
        KycStatus.VERIFIED -> "Verified"
        KycStatus.REJECTED -> "Rejected"
    }
}

private fun ApprovalStatus.displayName(): String {
    return when (this) {
        ApprovalStatus.NOT_SUBMITTED -> "Not Submitted"
        ApprovalStatus.UNDER_REVIEW -> "Under Review"
        ApprovalStatus.APPROVED -> "Approved"
        ApprovalStatus.REJECTED -> "Rejected"
    }
}

private fun JobStatus.displayName(): String {
    return when (this) {
        JobStatus.REQUESTED -> "Requested"
        JobStatus.ACCEPTED -> "Accepted"
        JobStatus.ON_THE_WAY -> "On the Way"
        JobStatus.ARRIVED -> "Arrived"
        JobStatus.STARTED -> "Started"
        JobStatus.COMPLETED -> "Completed"
        JobStatus.REJECTED -> "Rejected"
    }
}

private fun displayName(name: String): String {
    return name.ifBlank { "Partner" }.replace("%20", " ")
}

private fun String.shortServiceName(): String {
    return replace("Leak ", "")
        .replace("Bathroom Tap", "Tap")
        .replace("Assistance", "")
        .trim()
}

private fun String.asCompactTimeLabel(): String {
    return replace(",", "").replace("  ", " ")
}

private object Colors {
    val text = Color(0xFF080E25)
    val muted = Color(0xFF606779)
    val border = Color(0xFFE4E8F0)
    val blue = Color(0xFF075FEF)
    val blueSoft = Color(0xFFEAF2FF)
    val orange = Color(0xFFFF6B1A)
    val success = Color(0xFF059669)
    val successBg = Color(0xFFEAFBF2)
    val warning = Color(0xFFE97700)
    val warningBg = Color(0xFFFFF4E5)
    val info = Color(0xFF2563EB)
    val infoBg = Color(0xFFEFF6FF)
}
