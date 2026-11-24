package com.refillfinder.app.data.local.converter

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Room TypeConverters — converts non-primitive types ↔ primitive for SQLite storage.
 *
 * Syllabus: Room Type Converters
 */
class Converters {

    // ── Timestamp ↔ Long ──────────────────────────────────────────────────
    @TypeConverter
    fun timestampToLong(ts: Timestamp?): Long? = ts?.toDate()?.time

    @TypeConverter
    fun longToTimestamp(value: Long?): Timestamp? =
        value?.let { Timestamp(Date(it)) }

    // ── Date ↔ Long ───────────────────────────────────────────────────────
    @TypeConverter
    fun dateToLong(date: Date?): Long? = date?.time

    @TypeConverter
    fun longToDate(value: Long?): Date? = value?.let { Date(it) }

    // ── List<String> ↔ String (CSV) ───────────────────────────────────────
    @TypeConverter
    fun listToString(list: List<String>?): String? = list?.joinToString(",")

    @TypeConverter
    fun stringToList(value: String?): List<String>? =
        value?.split(",")?.filter { it.isNotBlank() }
}
