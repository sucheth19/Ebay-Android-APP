package com.example.ebayhw4

import SearchResultsAdapter
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

data class SearchResultItem(
    val itemId: String,
    val title: String,
    val galleryUrl: String,
    val price: String,
    val shippingPrice: String,
    val zipCode: String,
    val shippingCondition: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemId)
        parcel.writeString(title)
        parcel.writeString(galleryUrl)
        parcel.writeString(price)
        parcel.writeString(shippingPrice)
        parcel.writeString(zipCode)
        parcel.writeString(shippingCondition)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchResultItem> {
        override fun createFromParcel(parcel: Parcel): SearchResultItem {
            return SearchResultItem(parcel)
        }

        override fun newArray(size: Int): Array<SearchResultItem?> {
            return arrayOfNulls(size)
        }
    }
}

class SearchResults : AppCompatActivity() {
    private var mRequestQueue: RequestQueue? = null
    private var url: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultsAdapter
    private var count: Number? = null
    private var resultsList = mutableListOf<SearchResultItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the back arrow in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Retrieve the SearchParameters JSON from the intent
        val searchParamsJson = intent.getStringExtra("Search_PARAMS")
        url = "https://web-tech-hw-3.wl.r.appspot.com/search-results?searchParams=$searchParamsJson"

        mRequestQueue = Volley.newRequestQueue(this)


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // Set the number of columns
        adapter = SearchResultsAdapter(resultsList,searchParamsJson)
        recyclerView.adapter = adapter



// String Request initialized
        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                // Display the response on screen or handle it as needed
                parseApiResponse(response)
            },
            Response.ErrorListener { error ->
                // Handle errors
                Log.e("ApiError", "Error: ${error.toString()}")
                Toast.makeText(applicationContext, "Error: ${error.toString()}", Toast.LENGTH_LONG).show()

            })

        mRequestQueue?.add(stringRequest)
    }

    private fun parseApiResponse(response: String) {
        resultsList.clear()
        val result = JSONObject(response)
        try {
            // Assuming result is a JSONArray
            val resultArray = result.getJSONArray("searchResult")

            // Access the first item in the array
            val firstItem = resultArray.getJSONObject(0)

            // Access the value of @count
            val countValue = firstItem.getString("@count")
            count = countValue.toInt()
        } catch (e: JSONException) {
            e.printStackTrace()

        }

        try {
            if(response.isNullOrEmpty()){
                var ll = findViewById<LinearLayout>(R.id.linearLayout2)
                ll.visibility = View.VISIBLE
                val cardView = findViewById<CardView>(R.id.wishlistCardView)
                cardView.visibility = View.VISIBLE
                val tt = findViewById<TextView>(R.id.noItemsTextView)
                tt.visibility = View.VISIBLE
            }
            val jsonObject = JSONObject(response)
            val searchResultArray = jsonObject.getJSONArray("searchResult")
            for (i in 0 until searchResultArray.length()) {
                val searchResultItem = searchResultArray.getJSONObject(i)

                // Extract @count and item array from each search result item
                val countValue = searchResultItem.getString("@count")
                val itemArray = searchResultItem.getJSONArray("item")

                // Iterate through the item array
                for (j in 0 until itemArray.length()) {
                    val itemObject = itemArray.getJSONObject(j)
                    val shippingCost = itemObject.getJSONArray("shippingInfo").getJSONObject(0)
                        .getJSONArray("shippingServiceCost").getJSONObject(0).getString("__value__") ?: "N/A"
                    val rawTitle = itemObject.getJSONArray("title").getString(0) ?: "N/A"
                    val truncatedTitle = if (rawTitle.length > 30) {
                        "${rawTitle.substring(0, 30)}..."
                    } else {
                        rawTitle
                    }

                    var condition = itemObject.getJSONArray("condition").getJSONObject(0).getJSONArray("conditionDisplayName")[0].toString()

                    val currentItem = SearchResultItem(
                        itemId = itemObject.getJSONArray("itemId").getString(0) ?: "N/A",
                        title = truncatedTitle,
                        galleryUrl = itemObject.getJSONArray("galleryURL").getString(0) ?: "N/A",
                        price = itemObject.getJSONArray("sellingStatus").getJSONObject(0).getJSONArray("currentPrice").getJSONObject(0).get("__value__").toString(),
                        shippingPrice = if (shippingCost == "0.0") "Free" else shippingCost,
                        zipCode = (itemObject.getJSONArray("postalCode").getString(0) ?: "N/A"),
                        shippingCondition = condition,

                    )

                    WishlistManager.init(applicationContext)

                    resultsList.add(currentItem)
                }
            }
            }
         catch (e: Exception) {
            e.printStackTrace()
            Log.e("JsonParsingError", "Error parsing JSON: ${e.message}")


        }

        adapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateUpToMain()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateUpToMain() {
        // Create an Intent to navigate up to the parent activity (SearchFragment)
        val upIntent = NavUtils.getParentActivityIntent(this)

        // If upIntent is not null and the activity is not in the back stack, create a new task
        if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(upIntent)
                .startActivities()
        } else {
            // If the activity is in the back stack, simply navigate up
            if (upIntent != null) {
                NavUtils.navigateUpTo(this, upIntent)
            }
        }
    }
}
