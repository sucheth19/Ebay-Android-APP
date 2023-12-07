    package com.example.ebayhw4

    import android.content.Intent
    import android.net.Uri
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.AdapterView
    import android.widget.ArrayAdapter
    import android.widget.ImageButton
    import android.widget.ImageView
    import android.widget.LinearLayout
    import android.widget.Spinner
    import android.widget.TextView
    import android.widget.Toast
    import androidx.cardview.widget.CardView
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
        var timeLeft: String,
        val buyItNowPrice: String,
        val viewItemURL: String
    )

    class SimilarFragment : Fragment() {
        private var itemId: String? = null
        private lateinit var view: View
        private var noresult: TextView? = null
        private var similarSortSpinner: Spinner? = null
        private lateinit var requestQueue: RequestQueue
        private var similarProductList: MutableList<SimilarProduct> = mutableListOf()
        val sortItems = arrayOf("Default", "Name", "Price", "Days")
        private val orderItems = arrayOf("Ascending", "Descending")
        private var currentSortItemPosition: Int = 0
        private var currentOrderItemPosition: Int = 0
        private var masterProductList: MutableList<SimilarProduct> = mutableListOf()
        private var displayProductList: MutableList<SimilarProduct> = mutableListOf()
        private lateinit var wishlistButton: ImageButton
        private lateinit var product: SearchResultItem
        companion object {
            fun newInstance(itemId: String, product: SearchResultItem?): SimilarFragment {
                val fragment = SimilarFragment()
                val args = Bundle()
                args.putString("itemId", itemId)
                args.putParcelable("product", product)
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
            noresult = view?.findViewById(R.id.no_result)  // Use safe call
            product = arguments?.getParcelable("product")!!
             requestQueue = Volley.newRequestQueue(requireContext())
            wishlistButton = view.findViewById(R.id.fab)
            wishlistButton.setOnClickListener {
                toggleWishlistItem()
            }
            return view
        }
        private fun updateWishlistButtonBackground(isInWishlist: Boolean) {
            // Update the background of the button based on the wishlist status
            val drawableResource =
                if (isInWishlist) R.drawable.cart_remove else R.drawable.cart_plus
            wishlistButton.setImageResource(drawableResource)

        }
        private fun toggleWishlistItem() {
            val isInWishlist = WishlistManager.isItemInWishlist(product)

            if (isInWishlist) {
                WishlistManager.removeFromWishlistInApi(requireContext(), product.itemId)
                Toast.makeText(requireContext(), "${product.title} was removed from the wishlist",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                WishlistManager.addItemToWishlist(product)
                Toast.makeText(requireContext(), "${product.title} was added to the wishlist",
                    Toast.LENGTH_SHORT
                ).show()
                }
            updateWishlistButtonBackground(WishlistManager.isItemInWishlist(product))
        }
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            noresult?.visibility = View.INVISIBLE

            val similarItemSpinner: Spinner = view.findViewById(R.id.similar_content)
            similarSortSpinner = view.findViewById(R.id.similar_sort)
            val productImageView = view.findViewById<ImageView>(R.id.product_images)

            val sortAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                sortItems
            )
            sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            similarItemSpinner?.adapter = sortAdapter

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
            super.onViewCreated(view, savedInstanceState)


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
                    if (sortItems[position] == "Default" || sortItems[position]!="Price") {
                        similarSortSpinner?.isEnabled = false
                    } else {
                        similarSortSpinner?.isEnabled = true
                        sortAndDisplayData(
                            sortItems[position],
                            orderItems[similarSortSpinner?.selectedItemPosition ?: 0]
                        )
                    }
                    currentSortItemPosition = position

                    // Enable similarSortSpinner when an option other than "Default" is selected
                    similarSortSpinner?.isEnabled = sortItems[position] != "Default"
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing here
                }
            }

            setSimilarSortSpinnerListener(view)
        }
        private fun sortAndDisplayData(sortField: String, sortOrder: String) {
            // Update the displayProductList based on the selected sort criteria and order
            displayProductList = when (sortField) {
                "Name" -> {
                    if (sortOrder == "Ascending") masterProductList.sortedBy { it.title }
                    else masterProductList.sortedByDescending { it.title }
                }
                "Price" -> {
                    if (sortOrder == "Ascending") masterProductList.sortedBy { it.buyItNowPrice.toDoubleOrNull() ?: 0.0 }
                    else masterProductList.sortedByDescending { it.buyItNowPrice.toDoubleOrNull() ?: 0.0 }
                }
                "Days" -> {
                     masterProductList.sortedByDescending { if(sortOrder=="Ascending") it.timeLeft.toIntOrNull() ?: Int.MAX_VALUE else -it.timeLeft.toIntOrNull()!!
                         ?: Int.MAX_VALUE }
                }
                else -> masterProductList // Default or unknown sort field, maintain the original order
            }.toMutableList()
            Log.d("masterProduct",displayProductList.toString())
            // Update the order position
            currentOrderItemPosition = if (sortOrder == "Descending") 1 else 0

            // Log the sorted list for debugging


            // Update the UI with the sorted data
            updateProductList(view, displayProductList)
        }

        private fun updateProductList(view: View, sortedList: List<SimilarProduct>) {
            val productContainer = view.findViewById<LinearLayout>(R.id.card)
            val inflater = LayoutInflater.from(requireContext())

            productContainer?.removeAllViews()


            for (product in sortedList) {
                // Inflate the layout for each item
                val productItemView = inflater.inflate(R.layout.product_layout_item, productContainer, false)

                val productImageView = productItemView.findViewById<ImageView>(R.id.product_images)
                val productTitleTextView = productItemView.findViewById<TextView>(R.id.product_title)
                val shippingInfoTextView = productItemView.findViewById<TextView>(R.id.shipping_info)
                val productPriceTextView = productItemView.findViewById<TextView>(R.id.product_price)
                val productDateTextView: TextView = productItemView.findViewById<TextView>(R.id.days_left)

                // Load the image using Glide
                if (!product.imageURL.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(product.imageURL)
                        .placeholder(R.drawable.wishlist)
                        .error(R.drawable.wishlist)
                        .into(productImageView)
                } else {
                    productImageView.setImageResource(R.drawable.wishlist)
                }

                // Set text for other TextViews
                productTitleTextView.text = product.title
                shippingInfoTextView.text = product.shippingCost
                productPriceTextView.text = product.buyItNowPrice
                if(product.timeLeft.isNullOrBlank() or product.timeLeft.isNullOrEmpty()){
                    productDateTextView.text  = "0 Days Left"

                }
                    productDateTextView.text  = "${product.timeLeft} Days Left"


                productContainer?.addView(productItemView)
            }
        }

        private fun setSimilarSortSpinnerListener(view: View) {
            similarSortSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Ensure that similarItemSpinner is not null before accessing its selected item position
                    val selectedItemPosition = similarSortSpinner?.selectedItemPosition ?: 0

                    val sortField = sortItems[selectedItemPosition]
                    val sortOrder = orderItems[position]


                    if (sortField != "Default") {
                        similarSortSpinner?.isEnabled = true
                        // If any other option is selected, update the display based on the selected sort criteria and order
                        sortAndDisplayData(sortField, sortOrder)

                    }else{
                        similarSortSpinner?.isEnabled = false
                        sortAndDisplayData(sortItems[currentSortItemPosition], orderItems[currentOrderItemPosition])

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
                        masterProductList.clear()
                        displayProductList.clear()
                        val rootView = view.findViewById<LinearLayout>(R.id.product_list_container)
                        val inflater = LayoutInflater.from(requireContext())

                        for (i in 0 until response.length()) {
                            val item = response.getJSONObject(i)
                            val imageURL = item.optString("imageURL", "")
                            val title = item.optString("title", "N/A")
                            val viewItemURL = item.optString("viewItemURL", "https://www.ebay.com/")
                            val shippingCost =
                                item.getJSONObject("shippingCost").optString("__value__", "N/A")
                            var timeLeft = item.optString("timeLeft", "3")
                            var Pattern = Regex("""P(\d+)D""")

                            val Result = Pattern.find(timeLeft)

                            val extracted = Result?.groups?.get(1)?.value

                            timeLeft = extracted
                            var timeLeftInt = timeLeft.toIntOrNull()
                            if (timeLeftInt == null) {
                                // If parsing fails, try to extract a numeric value using a regex pattern
                                val numberPattern = Regex("""(\d+)""")
                                val matchResult = numberPattern.find(timeLeft)
                                timeLeftInt = matchResult?.groups?.get(1)?.value?.toIntOrNull()
                            }
                            if (timeLeftInt == null) {
                                timeLeftInt = Int.MAX_VALUE
                            }

                            var buyItNowPrice =
                                item.getJSONObject("buyItNowPrice").optString("__value__", "N/A")


                            // Create a SimilarProduct object and add it to the list
                            val similarProduct =
                                SimilarProduct(imageURL, title, shippingCost, timeLeft.toString(), buyItNowPrice, viewItemURL)
                            masterProductList.add(similarProduct)

                            // Inflate the layout for each item
                            val productItemView =
                                inflater.inflate(R.layout.product_layout_item, rootView, false)

                            // Find views in the inflated layout
                            val productImageView =
                                productItemView.findViewById<ImageView>(R.id.product_images)
                            val productTitleTextView =
                                productItemView.findViewById<TextView>(R.id.product_title)
                            val shippingInfoTextView =
                                productItemView.findViewById<TextView>(R.id.shipping_info)
                            val productPriceTextView =
                                productItemView.findViewById<TextView>(R.id.product_price)
                            val dateTextView: TextView =
                                productItemView.findViewById<TextView>(R.id.days_left)

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
                            val numberPattern = Regex("""P(\d+)D""")

                            val matchResult = numberPattern.find(timeLeft)

                            val extractedNumber = matchResult?.groups?.get(1)?.value
                            // Inflate the layout for each item
                            if(extractedNumber==null){
                                dateTextView.text = "1 Days Left"
                            }else{
                                dateTextView.text = "$extractedNumber Days Left"
                            }

                            // Set the click listener for the CardView
                            val cardView: CardView = productItemView.findViewById(R.id.product_card)
                            cardView.setOnClickListener {
                                // Open the website using an Intent
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewItemURL))
                                startActivity(intent)
                            }

                            // Add the inflated layout to the main container
                            rootView.addView(productItemView)
                        }
                        Log.d("master",masterProductList.toString())
                        // Now that the UI is updated, set the item selected listener for similarSortSpinner
                        setSimilarSortSpinnerListener(view)
                    } catch (e: Exception) {

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