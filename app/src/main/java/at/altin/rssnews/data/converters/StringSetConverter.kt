package at.altin.rssnews.data.converters

import androidx.room.TypeConverter

private const val DELIMITER = "____________"
class StringSetConverter {

    @TypeConverter
    fun convertListToString(input: Set<String>): String {
        return input.joinToString(DELIMITER)
    }

    @TypeConverter
    fun convertStringToList(input: String): Set<String> {
        return try {
            input.split(DELIMITER).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}