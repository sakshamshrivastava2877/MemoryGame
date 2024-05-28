package com.example.assignment3.ui.gallery

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.assignment3.R
import com.example.assignment3.databinding.FragmentGalleryBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GalleryFragment : Fragment(), View.OnClickListener {
    private var score = 0
    private var result: String = ""
    private var userAnswer: String = ""
    private var rowCount = 6
    private var columnCount = 6
    private lateinit var sharedPreferences: SharedPreferences
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared preferences
        sharedPreferences = requireContext().getSharedPreferences("HighScore", Context.MODE_PRIVATE)

        binding.apply {
            addButtons() // Add buttons programmatically
            btnStart.setOnClickListener {
                startGame()
            }
            btnPlayAgain.setOnClickListener {
                score = 0
                tvScore.text = "0"
                startGame()
            }
        }

        val buttonCount = countButtons()
        println("Number of buttons: $buttonCount")
    }

    private fun startGame() {
        result = ""
        userAnswer = ""
        disableButtons()
        lifecycleScope.launch {
            repeat(3) {
                delay(1000)
                val randomPanel = (1..(rowCount * columnCount)).random()
                result += randomPanel.toString()
                val buttonId = resources.getIdentifier("panel$randomPanel", "id", requireContext().packageName)
                val button = requireView().findViewById<Button>(buttonId)
                val drawableDefault = ActivityCompat.getDrawable(requireContext(), R.drawable.btn_lose)

                button.setBackgroundColor(Color.WHITE)
                delay(1000)
                button.background = drawableDefault
            }
            requireActivity().runOnUiThread {
                enableButtons()
            }
        }
    }

    private fun loseAnimation() {
        binding.apply {
            score = 0
            tvScore.text = "0"
            disableButtons()
            val drawableLose = ActivityCompat.getDrawable(requireContext(), R.drawable.btn_lose)
            val drawableDefault = ActivityCompat.getDrawable(requireContext(), R.drawable.btn_state)
            lifecycleScope.launch {

                gridLayout.forEach { view ->
                    if (view is Button) {
                        view.background = drawableLose
                        delay(1000)
                        view.background = drawableDefault
                    }
                }
                delay(1000)
                startGame()
            }
        }
    }

    private fun enableButtons() {
        binding.gridLayout.forEach { view ->
            if (view is Button) {
                view.isEnabled = true
            }
        }
    }

    private fun disableButtons() {
        binding.gridLayout.forEach { view ->
            if (view is Button) {
                view.isEnabled = false
            }
        }
    }

    private fun countButtons(): Int {
        var buttonCount = 0
        binding.gridLayout.forEach { view ->
            if (view is Button) {
                buttonCount++
            }
        }
        return buttonCount
    }

    private fun addButtons() {
        val drawableYellow = ActivityCompat.getDrawable(requireContext(), R.drawable.orange_btn)

        for (i in 1..(rowCount * columnCount)) {
            val button = requireView().findViewById<Button>(resources.getIdentifier("panel$i", "id", requireContext().packageName))
            button.setOnClickListener(this)
            button.tag = i.toString() // Assign button tag as its position in the sequence
            button.background = drawableYellow // Set the background color to Orange_btn
        }
    }

    override fun onClick(view: View) {
        // Append the tag of the clicked button to the user's answer
        userAnswer += view.tag.toString()

        // Check if the user's input matches the flashed sequence
        if (userAnswer == result) {
            Toast.makeText(requireContext(), "W I N :)", Toast.LENGTH_SHORT).show()
            score++
            binding.tvScore.text = score.toString()
            // Save high score
            saveHighScore(score)
            startGame()
        } else if (!result.startsWith(userAnswer)) {
            // If the user's input doesn't match the flashed sequence, reset their input
            userAnswer = ""
        }
    }

    private fun saveHighScore(highScore: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("HighScore", highScore)
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
