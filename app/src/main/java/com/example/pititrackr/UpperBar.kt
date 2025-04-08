package com.example.pititrackr

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class UpperBar : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.upper_bar, container, false)

        val medalButton = view.findViewById<ImageButton>(R.id.medalButton)
        medalButton.setOnClickListener {
            val intent = Intent(requireContext(), AchievementsActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
