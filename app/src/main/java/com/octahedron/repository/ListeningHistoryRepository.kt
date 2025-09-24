package com.octahedron.repository

import com.octahedron.data.AppDatabase
import com.octahedron.data.dao.AlbumDao
import com.octahedron.data.dao.ArtistDao
import com.octahedron.data.dao.ListeningHistoryDao
import com.octahedron.data.dao.TrackAlbumDao
import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.TrackDao
import com.octahedron.data.relation.ListeningWithTrackAndArtistsAndAlbum
import com.octahedron.model.Artist
import jakarta.inject.Inject
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ListeningHistoryRepository @Inject constructor(
    private val db: AppDatabase,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val trackAlbumDao: TrackAlbumDao,
    private val trackArtistDao: TrackArtistDao,
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
        val topItems: List<TopItem<ListeningWithTrackAndArtistsAndAlbum>>,
        val topArtists: List<TopItem<Artist>>
    )

    private fun now(zone: ZoneId): ZonedDateTime = ZonedDateTime.now(zone)

    // ---- Périodes prédéfinies ----

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

        val uniqueTracks = rows.asSequence()
            .map { it.track.uid }
            .toSet()
            .size

        val totalPlayTimeMs = rows.asSequence()
            .map { it.track.duration }
            .sum()

        val topItems = rows.asSequence()
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(10)
            .map { TopItem(item = it.key, playCount = it.value) }
            .toList()

        val artistCounts = HashMap<Artist, Int>()
        rows.forEach { l ->
            l.artists.forEach { a ->
                artistCounts[a] = (artistCounts[a] ?: 0) + 1
            }
        }
        val topArtists = artistCounts.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { TopItem(item = it.key, playCount = it.value) }

        return PeriodStats(
            fromEpochMs = fromMs,
            toEpochMs = toMs,
            totalPlayTimeMs = totalPlayTimeMs,
            totalPlays = totalPlays,
            uniqueTracks = uniqueTracks,
            topItems = topItems,
            topArtists = topArtists
        )
    }
}
