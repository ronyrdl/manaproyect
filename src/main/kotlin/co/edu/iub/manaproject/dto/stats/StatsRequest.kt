package co.edu.iub.manaproject.dto.stats

import jakarta.validation.constraints.NotBlank

data class StatsRequest(
    val startDate: String?,
    val endDate: String?
)
