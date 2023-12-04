package com.example.ebayhw4

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide

data class SimilarProduct(
    val imageURL: String,
    val title: String,
    val shippingCost: String,
    val timeLeft: String,
    val buyItNowPrice: String
)

class SimilarFragment : Fragment() {
    private var itemId: String? = null
    private lateinit var view: View
    private var noresult: TextView? = null
    private var similarSortSpinner: Spinner? = null
    private lateinit var requestQueue: RequestQueue
    private var similarProductList: MutableList<SimilarProduct> = mutableListOf()
    companion object {
        fun newInstance(itemId: String): SimilarFragment {
            val fragment = SimilarFragment()
            val args = Bundle()
            args.putString("itemId", itemId)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        itemId = arguments?.getString("itemId")
        view = inflater.inflate(R.layout.fragment_similar, container, false)
        requestQueue = Volley.newRequestQueue(requireContext())
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noresult?.visibility = View.INVISIBLE

        val similarItemSpinner: Spinner = view.findViewById(R.id.similar_content)
        similarSortSpinner = view.findViewById(R.id.similar_sort)
        val productImageView = view.findViewById<ImageView>(R.id.product_images)
        val sortItems = arrayOf("Default", "Name", "Price", "Days")
        val sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortItems
        )
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        similarItemSpinner.adapter = sortAdapter

        val orderItems = arrayOf("Ascending", "Descending")
        val orderAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            orderItems
        )
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        similarSortSpinner?.adapter = orderAdapter

        makeApiCall()

        // Replace "imageUrlFromApi" with the actual URL from your JSON response
        val imageUrlFromApi =
            "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.salveinternational.org%2Fcould-you-become-an-ebay-seller-for-s-a-l-v-e%2F&psig=AOvVaw08fxaoViMNBZiKEhfgc8vz&ust=1701636387651000&source=images&cd=vfe&ved=0CBEQjRxqFwoTCJDm1NXP8YIDFQAAAAAdAAAAABAI"

        // Load the image using Glide
        if (productImageView != null) {
            Glide.with(this)
                .load(imageUrlFromApi)
                .placeholder(R.drawable.wishlist)
                .error(R.drawable.wishlist)
                .into(productImageView)
        } else {
            Log.e("Glide", "productImageView is null")
        }

        similarItemSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Check if "Default" is selected

                if (sortItems[position] == "Default") {
                    // Disable the "Order" spinner
                    similarSortSpinner?.isEnabled = false
                } else {
                    // Enable the "Order" spinner
                    similarSortSpinner?.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing here
            }
        }
         fun updateProductList(sortedList: List<SimilarProduct>) {
            val rootView = view?.findViewById<LinearLayout>(R.id.product_list_container)
            val inflater = LayoutInflater.from(requireContext())

            for (product in sortedList) {
                // Inflate the layout for each item
                val productItemView = inflater.inflate(R.layout.product_layout_item, rootView, false)

                // Find views in the inflated layout
                val productImageView = productItemView.findViewById<ImageView>(R.id.product_images)
                val productTitleTextView = productItemView.findViewById<TextView>(R.id.product_title)
                val shippingInfoTextView = productItemView.findViewById<TextView>(R.id.shipping_info)
                val productPriceTextView = productItemView.findViewById<TextView>(R.id.product_price)
                val productDateTextView:TextView = productItemView.findViewById<TextView>(R.id.days_left)

                // Load the image using Glide
                if (!product.imageURL.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(product.imageURL)
                        .placeholder(R.drawable.wishlist)
                        .error(R.drawable.wishlist)
                        .into(productImageView)
                } else {
                    // Handle the case where imageURL is null or empty
                    // For example, you might want to set a default image
                    productImageView.setImageResource(R.drawable.wishlist)
                }

                // Set text for other TextViews
                productTitleTextView.text = product.title
                shippingInfoTextView.text = product.shippingCost
                productPriceTextView.text = product.buyItNowPrice
                productDateTextView.text = "${product.timeLeft} Days Left"
                rootView?.addView(productItemView)
            }
        }
        similarSortSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ensure that similarItemSpinner is not null before accessing its selected item position
                val selectedItemPosition = similarItemSpinner?.selectedItemPosition ?: 0

                val sortedList = when (sortItems[selectedItemPosition]) {
                    "Name" -> similarProductList.sortedBy { it.title }
                    "Price" -> similarProductList.sortedBy { it.buyItNowPrice.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: Double.MAX_VALUE }
                    "Days" -> similarProductList.sortedBy { it.timeLeft }
                    else -> similarProductList
                }

                val finalList = if (orderItems[position] == "Descending") sortedList.reversed() else sortedList
               val rootView = view?.findViewById<LinearLayout>(R.id.product_list_container)
                if (rootView != null) {
                    rootView.removeAllViews()
                    updateProductList(finalList)
                } else {
                    Log.e("RootView", "RootView is null")
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing here
            }
        }


    }
    private fun makeApiCall() {
        val url = "https://web-tech-hw-3.wl.r.appspot.com/similar-products/$itemId"
        val jsonProductObjectRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    similarProductList.clear()
                    for (i in 0 until response.length()) {
                        val item = response.getJSONObject(i)

                        // Extract required fields with fallback to "N/A" for missing values
                        val imageURL = item.optString("imageURL", "")
                        val title = item.optString("title", "N/A")
                        val shippingCost = item.getJSONObject("shippingCost").optString("__value__", "N/A")
                        val timeLeft = item.optString("timeLeft", "N/A")
                        val buyItNowPrice = item.getJSONObject("buyItNowPrice").optString("__value__", "N/A")

                        // Create a SimilarProduct object and add it to the list
                        val similarProduct = SimilarProduct(imageURL, title, shippingCost, timeLeft, buyItNowPrice)
                        similarProductList.add(similarProduct)
                    }

                    val rootView = view.findViewById<LinearLayout>(R.id.product_list_container)
                    val inflater = LayoutInflater.from(requireContext())
                    for (i in 0 until response.length()) {
                        val item = response.getJSONObject(i)

                        // Extract required fields with fallback to "N/A" for missing values
                        val imageURL = item.optString("imageURL", "")
                        val title = item.optString("title", "N/A")
                        val shippingCost = item.getJSONObject("shippingCost").optString("__value__", "N/A")
                        val timeLeft = item.optString("timeLeft", "N/A")
                        val buyItNowPrice = item.getJSONObject("buyItNowPrice").optString("__value__", "N/A")
                        val numberPattern = Regex("""P(\d+)D""")

                        val matchResult = numberPattern.find(timeLeft)

                        val extractedNumber = matchResult?.groups?.get(1)?.value
                        // Inflate the layout for each item
                        val productItemView = inflater.inflate(R.layout.product_layout_item, rootView, false)

                        // Find views in the inflated layout
                        val productImageView = productItemView.findViewById<ImageView>(R.id.product_images)
                        val productTitleTextView = productItemView.findViewById<TextView>(R.id.product_title)
                        val shippingInfoTextView = productItemView.findViewById<TextView>(R.id.shipping_info)
                        val productPriceTextView = productItemView.findViewById<TextView>(R.id.product_price)
                        val dateTextView:TextView = productItemView.findViewById<TextView>(R.id.days_left)

                        // Load the image using Glide
                        if (!imageURL.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageURL)
                                .placeholder(R.drawable.wishlist)
                                .error(R.drawable.wishlist)
                                .into(productImageView)
                        } else {
                            // Handle the case where imageURL is null or empty
                            // For example, you might want to set a default image
                            productImageView.setImageResource(R.drawable.wishlist)
                        }

                        // Set text for other TextViews
                        productTitleTextView.text = title
                        shippingInfoTextView.text = "$shippingCost"
                        productPriceTextView.text = "$buyItNowPrice"
                        dateTextView.text = "$extractedNumber Days Left"
                        // Add the inflated layout to the main container
                        rootView.addView(productItemView)
                    }
                    Log.d("similar after api call",similarProductList.toString())
                } catch (e: Exception) {
                    // Handle JSON parsing error
                    Log.d("failed","")
                    e.printStackTrace()
                }
            },

            { error ->
                // Handle errors here
                Log.e("responseSimilar", "Error: ${error.message}")
            }
        )


        requestQueue.add(jsonProductObjectRequest)
    }
}