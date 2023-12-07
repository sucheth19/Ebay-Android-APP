package com.example.ebayhw4

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class LoadScreen : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var titleTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_load_screen)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val searchParamsJson = intent.getStringExtra("Search_PARAMS")
      //  scheduleSplashScreen(searchParamsJson)
        progressBar = findViewById(R.id.progressBar)
        titleTextView = findViewById(R.id.textView4)

        // Set the initial title
        titleTextView.text = "Searching Products ..."

        // Start the splash screen timer
        scheduleSplashScreen(searchParamsJson)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
//                navigateUpToMain()
                navigateUpToSearchResults()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

private fun navigateUpToSearchResults() {
    // Create an Intent to navigate up to the SearchResults activity
    val searchResultsIntent = Intent(this, MainActivity::class.java)

    // Add any extras you need to pass to the SearchResults activity
//    searchResultsIntent.putExtra("Search_PARAMS", searchParamsJson)

    // Navigate up to the SearchResults activity
    startActivity(searchResultsIntent)

    // Finish the current activity (itemDetailsSplash)
    finish()
}
    private fun scheduleSplashScreen(searchParamsJson: String?) {
        val splashScreenDuration = getSplashScreenDuration()

        Handler().postDelayed(
            {
                // After the splash screen duration, start the SearchResults activity
                startSearchResultsActivity(searchParamsJson)
                finish()
            },
            splashScreenDuration
        )
    }


    private fun startSearchResultsActivity(searchParamsJson: String?) {
        // Create an Intent to start the SearchResults activity
        val searchResultsIntent = Intent(this, SearchResults::class.java)

        // Put the search parameters as extras to the intent
        searchResultsIntent.putExtra("Search_PARAMS", searchParamsJson)

        // Start the SearchResults activity
        startActivity(searchResultsIntent)
    }

    private fun getSplashScreenDuration() = 2000L
}