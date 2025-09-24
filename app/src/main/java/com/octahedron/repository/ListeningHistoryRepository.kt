package com.octahedron.repository

import com.octahedron.data.dao.ListeningHistoryDao
import com.octahedron.data.relation.ListeningWithTrackAndArtistsAndAlbum
import com.octahedron.model.Artist
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

class ListeningHistoryRepository @Inject constructor(
    private val listeningHistoryDao: ListeningHistoryDao
) {
    data class TopItem<T>(
        val item: T,
        val playCount: Int
    )

    data class PeriodStats(
        val fromEpochMs: Long,
        val toEpochMs: Long,
        val totalPlayTimeMs: Long,
        val totalPlays: Int,
        val uniqueTracks: Int,
        val totalArtists: Int,
        val topItems: List<TopItem<ListeningWithTrackAndArtistsAndAlbum>>,
        val topArtists: List<TopItem<Artist>>
    )

    private fun now(zone: ZoneId): ZonedDateTime = ZonedDateTime.now(zone)

    fun statsToday(zone: ZoneId): Flow<PeriodStats> {
        val todayStart = now(zone).toLocalDate().atStartOfDay(zone)
        val tomorrowStart = todayStart.plusDays(1)
        return statsBetween(todayStart.toInstant().toEpochMilli(), tomorrowStart.toInstant().toEpochMilli())
    }

    fun statsThisWeek(zone: ZoneId): Flow<PeriodStats> {
        val zNow = now(zone)
        val weekStart = zNow.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toLocalDate()
            .atStartOfDay(zone)
        val nextWeekStart = weekStart.plusWeeks(1)
        return statsBetween(weekStart.toInstant().toEpochMilli(), nextWeekStart.toInstant().toEpochMilli())
    }

    fun statsThisMonth(zone: ZoneId): Flow<PeriodStats> {
        val zNow = now(zone)
        val monthStart = zNow.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zone)
        val nextMonthStart = monthStart.plusMonths(1)
        return statsBetween(monthStart.toInstant().toEpochMilli(), nextMonthStart.toInstant().toEpochMilli())
    }

    fun statsThisYear(zone: ZoneId): Flow<PeriodStats> {
        val zNow = now(zone)
        val yearStart = zNow.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay(zone)
        val nextYearStart = yearStart.plusYears(1)
        return statsBetween(yearStart.toInstant().toEpochMilli(), nextYearStart.toInstant().toEpochMilli())
    }

    fun statsAllTime(zone: ZoneId): Flow<PeriodStats> {
        val to = now(zone).toInstant().toEpochMilli()
        return statsBetween(0L, to)
    }

    fun statsBetween(fromEpochMs: Long, toEpochMs: Long): Flow<PeriodStats> {
        val sqlFrom = fromEpochMs
        val sqlToInclusive = toEpochMs - 1L

        return listeningHistoryDao
            .getBetween(sqlFrom, sqlToInclusive)
            .map { rows -> aggregate(rows, fromEpochMs, toEpochMs) }
    }

    suspend fun purge(olderThan: Long) = listeningHistoryDao.purgeOlderThan(olderThan)

    private fun aggregate(
        rows: List<ListeningWithTrackAndArtistsAndAlbum>,
        fromMs: Long,
        toMs: Long
    ): PeriodStats {
        val totalPlays = rows.size

        val byTrackId: Map<Long, List<ListeningWithTrackAndArtistsAndAlbum>> =
            rows.groupBy { it.track.uid }

        val uniqueTracks = byTrackId.size

        val totalPlayTimeMs = rows.sumOf { it.track.duration }

        val topItems: List<TopItem<ListeningWithTrackAndArtistsAndAlbum>> =
            byTrackId
                .map { (trackId, list) ->
                    val representative = list.first()
                    TopItem(item = representative, playCount = list.size)
                }
                .sortedByDescending { it.playCount }
                .take(10)

        val artistCountsById = mutableMapOf<Long, Int>()
        val anyArtistRef = mutableMapOf<Long, Artist>()

        rows.forEach { l ->
            l.artists.forEach { a ->
                val id = a.uid
                artistCountsById[id] = (artistCountsById[id] ?: 0) + 1
                anyArtistRef.putIfAbsent(id, a)
            }
        }

        val topArtists: List<TopItem<Artist>> =
            artistCountsById
                .entries
                .sortedByDescending { it.value }
                .take(10)
                .map { (artistId, count) ->
                    TopItem(item = anyArtistRef[artistId]!!, playCount = count)
                }

        val totalArtists = artistCountsById.size

        return PeriodStats(
            fromEpochMs = fromMs,
            toEpochMs = toMs,
            totalPlayTimeMs = totalPlayTimeMs,
            totalPlays = totalPlays,
            uniqueTracks = uniqueTracks,
            totalArtists = totalArtists,
            topItems = topItems,
            topArtists = topArtists
        )
    }
}
