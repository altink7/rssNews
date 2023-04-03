package at.altin.rssnews.data.converters

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class StringDateConverter {
    @TypeConverter
    fun convertDateToString(input: Date): String {
        return input.toString()
    }

    @TypeConverter
    fun convertStringToDate(input: String): Date? {
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        return try {
        return format.parse(input)?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}