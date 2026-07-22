package co.edu.iub.manaproject.controller

import co.edu.iub.manaproject.dto.stats.DailyStatsResponse
import co.edu.iub.manaproject.service.StatsService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stats")
@PreAuthorize("hasRole('ADMIN')")
class StatsController(
    private val statsService: StatsService
) {

    @GetMapping("/daily")
    fun getDailyStats(
        @RequestParam date: String
    ): DailyStatsResponse {
        return statsService.getDailyStats(date)
    }

    @GetMapping("/range")
    fun getStatsBetween(
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): List<DailyStatsResponse> {
        return statsService.getStatsBetween(startDate, endDate)
    }
}
