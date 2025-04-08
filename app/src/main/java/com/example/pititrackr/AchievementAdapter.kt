package com.example.pititrackr

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class AchievementAdapter(context: Context, private val achievements: List<Achievement>) :
ArrayAdapter<Achievement>(context, R.layout.achievement_item, achievements) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.achievement_item, parent, false)

        val achievement = getItem(position) ?: return view

        view.findViewById<TextView>(R.id.achievementTitle).text = achievement.title
        view.findViewById<TextView>(R.id.achievementDescription).text = achievement.description

        val icon = view.findViewById<ImageView>(R.id.achievementIcon)
        if (achievement.unlocked) {
            icon.setImageResource(R.drawable.medal2) // Tu icono de medalla
            view.alpha = 1f
            view.setBackgroundColor(R.drawable.achievement_background) // Verde para desbloqueado
        } else {
            icon.setImageResource(R.drawable.lock) // Tu icono de candado
            view.alpha = 0.4f
            view.setBackgroundColor(R.drawable.achievement_background) // Naranja para bloqueado
        }

        return view
    }
}