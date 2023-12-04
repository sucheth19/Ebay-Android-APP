package com.example.ebayhw4

import PhotosFragment
import ProductFragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject


class itemDetailsSplash : AppCompatActivity() {
    private lateinit var itemId: String
    private lateinit var progressBar: ProgressBar
    private lateinit var titleTextView: TextView
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var requestQueue: RequestQueue
    private lateinit var item: JSONObject
    private lateinit var viewItemURL: String
    private lateinit var title: String
    private lateinit var convertedCurrentPrice: JSONObject
    private lateinit var currency: String
    private lateinit var searchParamsJson: String
    private lateinit var shippingCost:String
    private var convertedPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details_splash)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progressBar = findViewById(R.id.progressBar)
        titleTextView = findViewById(R.id.textView4)
        itemId = intent.getStringExtra("productId") ?: ""
        searchParamsJson = intent.getStringExtra("searchParams") ?: ""
        shippingCost = intent.getStringExtra("shippingCost").toString()
        val facebookButton = findViewById<ImageButton>(R.id.facebookButton)

        facebookButton.setOnClickListener {
            shareOnFacebook()
        }
        titleTextView.text = "Fetching Product Details ..."
        var titleText = findViewById<TextView>(R.id.titleText)
        titleText.text = intent.getStringExtra("title")?:""
        viewPager = findViewById(R.id.viewPager)
        searchParamsJson = intent.getStringExtra("searchParams") ?: ""
        tabLayout = findViewById(R.id.tabLayout)
        val adapter = TabPagerAdapter(supportFragmentManager,itemId,searchParamsJson,shippingCost)
        viewPager.adapter = adapter

        // Link the TabLayout to the ViewPager
        tabLayout.setupWithViewPager(viewPager)
        adapter.setupTabIcons()
        requestQueue = Volley.newRequestQueue(this)

        makeApiCall()
        Handler().postDelayed(
            {
                progressBar.visibility = View.GONE
                titleTextView.visibility = View.GONE
                viewPager.visibility = View.VISIBLE
            },
            getSplashScreenDuration()
        )

    }

    private inner class TabPagerAdapter(fm: FragmentManager, private val itemId: String, private  val searchParamsJson: String, private val shippingCost: String) : FragmentPagerAdapter(fm) {
        private val tabTitles = arrayOf("Product", "Shipping", "Photos", "Similar")

        private val tabIcons = intArrayOf(R.drawable.information_variant, R.drawable.truck_delivery, R.drawable.google, R.drawable.equal)
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> ProductFragment.newInstance(itemId,shippingCost)
                1 -> ShippingFragment.newInstance(itemId, searchParamsJson, shippingCost)
                2 -> PhotosFragment.newInstance(title)
                3 -> SimilarFragment.newInstance(itemId)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }


        override fun getCount(): Int {
            return tabTitles.size // Number of tabs
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles[position]
        }
        fun setupTabIcons() {
            for (i in 0 until tabIcons.size) {
                val tab = tabLayout.getTabAt(i)
                tab?.setIcon(tabIcons[i])
            }
        }
    }

    private fun shareOnFacebook() {
        // Create a Facebook sharing URL
            val hashtag = Uri.encode("#CSCI571Fall23AndroidApp")
            val facebookUrl =
                "https://www.facebook.com/sharer/sharer.php?u=$viewItemURL"+"&amp;src=sdkpreparse&hashtag=$hashtag"

            // Open the Facebook sharing page in a web browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
            startActivity(intent)

    }
    private fun makeApiCall() {
        // Use the itemId obtained from arguments
        val url = "https://web-tech-hw-3.wl.r.appspot.com/product-details?itemId=$itemId"

        // Create a request using the JsonObjectRequest class
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                 item = response.getJSONObject("Item")
                 viewItemURL = item.getString("ViewItemURLForNaturalSearch")
                 title = item.getString("Title")

                 convertedCurrentPrice = item.getJSONObject("ConvertedCurrentPrice")
                currency = convertedCurrentPrice.getString("CurrencyID")
                convertedPrice = convertedCurrentPrice.getDouble("Value")

                // Now you can use viewItemURL, title, and convertedPrice in your application
            },
            Response.ErrorListener { error ->
                // Handle errors here
             Log.e("error", error.toString())
            }
        )


        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateUpToSearchResults()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun navigateUpToSearchResults() {
        // Create an Intent to navigate up to the SearchResults activity
        val searchResultsIntent = Intent(this, SearchResults::class.java)

        // Add any extras you need to pass to the SearchResults activity
        searchResultsIntent.putExtra("Search_PARAMS", searchParamsJson)

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

        // Start the SearchResults activity
        startActivity(searchResultsIntent)
    }

    private fun getSplashScreenDuration() = 2000L
}