package com.example.expensetracker.presentation.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.expensetracker.R
import com.example.expensetracker.databinding.ActivityMainBinding
import com.example.expensetracker.presentation.add.AddExpenseFragment
import com.example.expensetracker.presentation.list.ExpenseListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("ExpenseTracker", "Starting MainActivity")
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setSupportActionBar(binding.toolbar)

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ExpenseListFragment())
                    .commit()
            }

            binding.fabAdd.setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AddExpenseFragment())
                    .addToBackStack(null)
                    .commit()
            }

            Log.d("ExpenseTracker", "MainActivity created successfully")
        } catch (e: Exception) {
            Log.e("ExpenseTracker", "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
