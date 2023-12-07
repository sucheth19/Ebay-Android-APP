package com.example.ebayhw4

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.properties.Delegates

class ShippingFragment : Fragment() {
    private var itemId: String? = null
    private var searchParamsJson:String? = null
    private var shippingCost:String? = null
    private lateinit var requestQueue: RequestQueue
    private lateinit var wishlistButton: ImageButton
    private lateinit var product: SearchResultItem
    private var location by Delegates.notNull<Int>()

    companion object {
        private const val ARG_ItemId = "itemId"
        private const val ARG_SEARCH_PARAMS = "searchParamsJson"
        private const val ARG_Shipping = "shippingCost"

        fun newInstance(
            itemId: String,
            searchParams: String,
            shippingCost: String,
            product: SearchResultItem?,
            location: Int
        ): ShippingFragment {
            val fragment = ShippingFragment()
            val args = Bundle()
            args.putString(ARG_ItemId,itemId)
            args.putString(ARG_SEARCH_PARAMS,searchParams)
            args.putString(ARG_Shipping,shippingCost)
            args.putParcelable("product", product)
            args.putInt("location",location)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getString("itemId")
        searchParamsJson = arguments?.getString("searchParamsJson")
        shippingCost = arguments?.getString("shippingCost")
        product = arguments?.getParcelable("product")!!
        location = arguments?.getInt("location")!!
        requestQueue = Volley.newRequestQueue(requireContext())


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the ImageView and apply color filter
        val truckDeliveryImageView = view.findViewById<ImageView>(R.id.imageView2)
        truckDeliveryImageView?.setColorFilter(resources.getColor(android.R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)

        // Now you can proceed with other operations
        fetchShippingDetails()
        fetchProductDetails()
    }
    private fun fetchProductDetails() {
        val productUrl = "https://web-tech-hw-3.wl.r.appspot.com/search-results?searchParams=$searchParamsJson"

        val jsonProductObjectRequest = JsonObjectRequest(
            Request.Method.GET, productUrl, null,
            { response ->
                try {

                    val jsonResponse = JSONObject(response.toString())

                    val searchResults = jsonResponse.getJSONArray("searchResult")
                    Log.d("SearchResults",searchResults.toString())
                    var storeName = ""
                    var storeURL = ""
                    val item = searchResults.getJSONObject(0).getJSONArray("item").getJSONObject(location)
                    Log.d("item",item.toString())
                    val storeInfoArray = item.optJSONArray("storeInfo")
                    if (storeInfoArray != null && storeInfoArray.length() > 0) {
                        storeName = storeInfoArray.getJSONObject(0).optJSONArray("storeName")?.optString(0, "") ?: ""
                        storeURL = storeInfoArray.getJSONObject(0).optJSONArray("storeURL")?.optString(0, "") ?: ""
                    } else {
                        storeName = "USAEasySales"
                        storeURL = "http://stores.ebay.com/USAEasySales"
                    }
                    val firstItem = item
                    val name = view?.findViewById<TextView>(R.id.storeUrl)

                    name?.text = storeName

                    name?.setOnClickListener{
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeURL))
                        startActivity(intent)
                    }
                    Log.d("storeName",storeName)
                    Log.d("StoreUrl",storeURL)

                    Log.d("firstItem",firstItem.toString())
                    val returnsAcceptedArray = firstItem.getJSONArray("returnsAccepted")
                    Log.d("returnsAcceptedArray",returnsAcceptedArray.toString())
                    val returnsAccepted = returnsAcceptedArray.getString(0)
                    // Extracting shippingInfo
                    val shippingInfoArray = firstItem.getJSONArray("shippingInfo")
                    val shippingInfo = shippingInfoArray.getJSONObject(0)
                    val sellerInfoArray = firstItem.getJSONArray("sellerInfo")
                    val sellerInfo = sellerInfoArray.getJSONObject(0)


                    // Extracting specific seller details
                    val sellerUserName = sellerInfo.getJSONArray("sellerUserName").getString(0)
                    val feedbackScore = sellerInfo.getJSONArray("feedbackScore").getString(0)
                    Log.d("feedbackScore",feedbackScore)

                    var score = view?.findViewById<TextView>(R.id.score)
                    if(feedbackScore.isNotEmpty()){
                        score?.text = feedbackScore.toString()
                    }else{
                            score?.visibility = View.GONE

                    }


                    val positiveFeedbackPercent = sellerInfo.getJSONArray("positiveFeedbackPercent").getString(0)

                    val positivePercent = view?.findViewById<TextView>(R.id.popularityscore)
                    positivePercent?.text = positiveFeedbackPercent.toString()
                    val feedbackRatingStar = sellerInfo.getJSONArray("feedbackRatingStar").getString(0)
                    Log.d("feedbackRatingStart",feedbackRatingStar)
                    val topRatedSeller = sellerInfo.getJSONArray("topRatedSeller").getString(0)

                    val feedbackScoreText = view?.findViewById<TextView>(R.id.score)
                    feedbackScoreText?.text = feedbackScore


                    val positiveScore = view?.findViewById<TextView>(R.id.popularityscore)
                    positiveScore?.text = "$positiveFeedbackPercent %"
                    Log.d("positiveFeedback",positiveFeedbackPercent)
                    // Assuming feedbackScore is an integer
                    val feedbackStarImageView = view?.findViewById<ImageView>(R.id.star)
                    val feedbackRatingStarColor = Color.parseColor(feedbackRatingStar)
                    val starDrawable = ContextCompat.getDrawable(requireContext(),R.drawable.star_circle)

                    if(feedbackScore.toInt()>10000){
                        feedbackStarImageView?.setImageResource(R.drawable.star_circle_outline)
                    }
                    starDrawable?.setTint(feedbackRatingStarColor)
                    feedbackStarImageView?.setImageDrawable(starDrawable)
                    val shippingType = shippingInfo.getJSONArray("shippingType").getString(0)
                    val shipToLocations = shippingInfo.getJSONArray("shipToLocations").getString(0)

                    var shipping = view?.findViewById<TextView>(R.id.shippingCostTextView)
                    Log.d("shippingCost",shippingCost.toString())


                    if (shippingCost != null && shippingCost.toString().isNotEmpty() && shippingCost != "Free") {
                        shipping?.text = "$ $shippingCost"
                    } else {
                        shipping?.text = "Free"
                    }



                    val oneDayShippingAvailable = shippingInfo.getJSONArray("oneDayShippingAvailable").getString(0)

                    val handlingTime = shippingInfo.getJSONArray("handlingTime").getString(0)
                    var handlingText = view?.findViewById<TextView>(R.id.handlingTimeInfo)
                    handlingText?.text = handlingTime.toString()

                    if (handlingTime.toString().isEmpty() || handlingTime.isNullOrEmpty()) {
                        handlingText?.text = "1"
                    }
                    Log.d("handlingTime",handlingTime.toString())
                } catch (e: Exception) {
                    Log.e("PhotosFragment", "Error parsing JSON response: ${e.message}")
                }
            },
            { error ->
                Log.e("PhotosFragment", "Error fetching photos: ${error.message}")
            }
        )
        requestQueue.add(jsonProductObjectRequest)
    }

    private fun fetchShippingDetails(){
        val apiUrl = "https://websucheth.wl.r.appspot.com/getSingleData?itemId=$itemId"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            Response.Listener { response ->
                val itemObject = response.optJSONObject("Item")
                val globalShipping = itemObject?.optBoolean("GlobalShipping", false)
                Log.d("global",globalShipping.toString())
                val globalShippingInfoB = view?.findViewById<TextView>(R.id.globalShippingInfo)
                if(globalShipping.toString()=="true"){
                    globalShippingInfoB?.text = "Yes"
                }else{
                    globalShippingInfoB?.text = "No"
                }
                val returnPolicyObject = response.optJSONObject("Item")?.optJSONObject("ReturnPolicy")
                val internationalReturnsAccepted = returnPolicyObject?.optString("InternationalReturnsAccepted", "")
                val refund = returnPolicyObject?.optString("Refund", "")
                val returnsAccepted = returnPolicyObject?.optString("ReturnsAccepted", "")
                Log.d("returnAccepted",returnsAccepted.toString())
                val returnsWithin = returnPolicyObject?.optString("ReturnsWithin", "30 Days")
                Log.d("returnsWithing",returnsWithin.toString())
                val shippingCostPaidBy = returnPolicyObject?.optString("ShippingCostPaidBy", "Seller")

                var policy = view?.findViewById<TextView>(R.id.policyInfo)
                if(returnsAccepted.toString().length>0){
                    policy?.text = returnsAccepted.toString()
                }else{
                    policy?.text="Returns Accepted"
                }

                Log.d("ReturnsAccepted", returnsAccepted ?: "ReturnsAccepted is null or empty")
                var returnsWithinText = view?.findViewById<TextView>(R.id.returnsWithinText)
                returnsWithinText?.text = returnsWithin.toString()
                var refundModes = view?.findViewById<TextView>(R.id.refundMode)

                refundModes?.text = refund.toString()
                if(refund.toString()==""){
                    refundModes?.text = "Money back or repl.."
                }
                var shippedby = view?.findViewById<TextView>(R.id.shippedSeller)
                shippedby?.text = shippingCostPaidBy.toString()
                if(shippingCostPaidBy.toString()==""){
                    shippedby?.text="Seller"
                }
                Log.d("ShippingCostBy",shippingCostPaidBy.toString())

            },
            Response.ErrorListener { error ->
                Log.e("PhotosFragment", "Error fetching photos: ${error.message}")
            }
        )


        requestQueue.add(jsonObjectRequest)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_shipping, container, false)
        wishlistButton = view.findViewById(R.id.fab)
        wishlistButton.setOnClickListener {
            toggleWishlistItem()
        }

        return view  // Return the initialized view
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
            Toast.makeText(requireContext(), "${product.title} was removed from the wishlist",Toast.LENGTH_SHORT
            ).show()
            updateWishlistButtonBackground(WishlistManager.isItemInWishlist(product))

        } else {
            WishlistManager.addItemToWishlist(product)
            Toast.makeText(requireContext(), "${product.title} was added to the wishlist",Toast.LENGTH_SHORT
            ).show()
            updateWishlistButtonBackground(WishlistManager.isItemInWishlist(product))
        }
        updateWishlistButtonBackground(WishlistManager.isItemInWishlist(product))
    }

}