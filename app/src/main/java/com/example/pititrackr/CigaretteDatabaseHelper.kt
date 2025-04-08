import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CigaretteDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "cigarettes.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "cigarette_entries"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_EMOTION = "emotion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_EMOTION TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertCigarette(date: String, emotion: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_EMOTION, emotion)
        }
        db.insert(TABLE_NAME, null, values)
    }

    fun getCigaretteCount(date: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_DATE = ?",
            arrayOf(date)
        )
        return if (cursor.moveToFirst()) cursor.getInt(0) else 0.also { cursor.close() }
    }

    fun getAllEntries(): List<Pair<String, Int>> {
        val entries = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_DATE, COUNT(*) FROM $TABLE_NAME GROUP BY $COLUMN_DATE ORDER BY $COLUMN_DATE ASC",
            null
        )

        while (cursor.moveToNext()) {
            val date = cursor.getString(0)
            val count = cursor.getInt(1)
            entries.add(date to count)
        }

        cursor.close()
        return entries
    }

    fun getEmotionStats(): Map<String, Int> {
        val emotionCounts = mutableMapOf<String, Int>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_EMOTION, COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_EMOTION IS NOT NULL GROUP BY $COLUMN_EMOTION",
            null
        )

        while (cursor.moveToNext()) {
            val emotion = cursor.getString(0) ?: "Otro"
            val count = cursor.getInt(1)
            emotionCounts[emotion] = count
        }

        cursor.close()
        return emotionCounts
    }

    fun getEmotionStatsByDate(date: String): Map<String, Int> {
        val emotionCounts = mutableMapOf<String, Int>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_EMOTION, COUNT(*) FROM $TABLE_NAME WHERE $COLUMN_DATE = ? AND $COLUMN_EMOTION IS NOT NULL GROUP BY $COLUMN_EMOTION",
            arrayOf(date)
        )

        while (cursor.moveToNext()) {
            val emotion = cursor.getString(0) ?: "Otro"
            val count = cursor.getInt(1)
            emotionCounts[emotion] = count
        }

        cursor.close()
        return emotionCounts
    }

    fun getAllCigarettesByDateRange(days: Int): List<Pair<String, String?>> {
        val result = mutableListOf<Pair<String, String?>>()
        val db = readableDatabase

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until days) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)

            val cursor = db.rawQuery(
                "SELECT $COLUMN_DATE, $COLUMN_EMOTION FROM $TABLE_NAME WHERE $COLUMN_DATE = ?",
                arrayOf(date)
            )

            while (cursor.moveToNext()) {
                val dateVal = cursor.getString(0)
                val emotion = cursor.getString(1)
                result.add(dateVal to emotion)
            }
            cursor.close()
        }

        return result
    }

}
