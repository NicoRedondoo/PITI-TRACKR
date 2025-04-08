package com.example.pititrackr

import android.content.Context

object AchievementManager {
    private val achievements = mutableListOf<Achievement>()
    private var listeners = mutableListOf<(Achievement) -> Unit>()


    fun getAchievement(id: String): Achievement?{
        return  achievements.find { it.id == id }
    }
    fun registerListener(listener: (Achievement) -> Unit) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: (Achievement) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners(achievement: Achievement) {
        listeners.forEach { it(achievement) }
    }

    fun initAchievements(context: Context) {
        achievements.clear()
        achievements.addAll(
            listOf(
                Achievement("LESS_THAN_YESTERDAY", "Fumaste menos que ayer", "Hoy has fumado menos que el día anterior."),
                Achievement("FIVE_LESS_THAN_YESTERDAY", "Fumaste 5 menos que ayer", "Redujiste tu consumo en 5 cigarros respecto a ayer."),
                Achievement("LESS_3_DAYS_IN_ROW", "3 días de mejora", "Fumaste menos que el día anterior durante 3 días seguidos."),
                Achievement("DAY_WITHOUT_SMOKING", "Día sin fumar", "No fumaste ni un solo cigarro en todo el día."),
                Achievement("THREE_DAYS_NO_SMOKE", "3 días sin fumar", "Llevas 3 días seguidos sin fumar."),
                Achievement("ONE_WEEK_NO_SMOKE", "1 semana sin fumar", "¡Una semana entera sin fumar!"),
                Achievement("WEEK_LESS_THAN_LAST", "Mejor semana", "Fumaste menos esta semana que la anterior"),
                Achievement("HALF_CONSUMPTION", "Reducción importante", "Fumas la mitad que cuando empezaste"),
                Achievement("TWO_WEEKS_NO_SMOKE", "2 semanas limpias", "14 días consecutivos sin fumar"),
                Achievement("MONTH_NO_SMOKE", "Mes sin fumar", "30 días consecutivos sin fumar (¡increíble!)"),
                Achievement("WEEK_NO_NICOTINE", "Sin nicotina", "7 días sin fumar ni usar sustitutos"),
                Achievement("YEAR_NO_SMOKE", "Año nuevo", "365 días sin fumar (¡eres libre!)"),
                Achievement("FIRST_DAY", "Primer paso", "Completaste tu primer día registrando consumo"),
                Achievement("WEEKLY_GOAL", "Objetivo semanal", "Cumpliste tu meta semanal de reducción"),
                Achievement("MONTHLY_GOAL", "Objetivo mensual", "Cumpliste tu meta mensual de reducción"),
                Achievement("QUIT_SMOKING", "Libre de humo", "Has dejado de fumar completamente")
            )
        )


        val prefs = context.getSharedPreferences("Achievements", Context.MODE_PRIVATE)
        achievements.forEach { it.unlocked = prefs.getBoolean(it.id, false) }
    }

    fun getAchievements() = achievements.toList()

    fun unlock(context: Context, id: String) {
        val achievement = achievements.find { it.id == id } ?: return
        if (!achievement.unlocked) {
            achievement.unlocked = true
            context.getSharedPreferences("Achievements", Context.MODE_PRIVATE)
                .edit()
                .putBoolean(id, true)
                .apply()
            notifyListeners(achievement)
        }
    }

    fun lock(context: Context, id: String) {
        val achievement = achievements.find { it.id == id } ?: return
        if (achievement.unlocked) {
            achievement.unlocked = false
            context.getSharedPreferences("Achievements", Context.MODE_PRIVATE)
                .edit()
                .putBoolean(id, false)
                .apply()
        }
    }

    fun isUnlocked(id: String): Boolean {
        return achievements.find { it.id == id }?.unlocked ?: false
    }

    fun getUnlockedCount(): Int = achievements.count { it.unlocked }
}