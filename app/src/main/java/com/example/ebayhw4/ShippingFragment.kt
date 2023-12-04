package com.example.ebayhw4

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class ShippingFragment : Fragment() {
    private var itemId: String? = null
    private var searchParamsJson:String? = null
    private var shippingCost:String? = null
    private lateinit var requestQueue: RequestQueue
    companion object {
        private const val ARG_ItemId = "itemId"
        private const val ARG_SEARCH_PARAMS = "searchParamsJson"
        private const val ARG_Shipping = "shippingCost"
        fun newInstance(itemId: String, searchParams: String, shippingCost:String): ShippingFragment {
            val fragment = ShippingFragment()
            val args = Bundle()
            args.putString(ARG_ItemId,itemId)
            args.putString(ARG_SEARCH_PARAMS,searchParams)
            args.putString(ARG_Shipping,shippingCost)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getString("itemId")
        searchParamsJson = arguments?.getString("searchParamsJson")
        shippingCost = arguments?.getString("shippingCost")
        requestQueue = Volley.newRequestQueue(requireContext())
        fetchShippingDetails()
        fetchProductDetails()
    }
    private fun fetchProductDetails() {
        val productUrl = "https://web-tech-hw-3.wl.r.appspot.com/search-results?searchParams=$searchParamsJson"

        val jsonProductObjectRequest = JsonObjectRequest(
            Request.Method.GET, productUrl, null,
            Response.Listener { response ->
                try {
                    Log.d("seartcchResposne",response.toString())
                    val jsonResponse = JSONObject(response.toString())
                    val searchResults = jsonResponse.getJSONArray("searchResult")
                    val firstItem = searchResults.getJSONObject(0).getJSONArray("item").getJSONObject(0)
                    val returnsAcceptedArray = firstItem.getJSONArray("returnsAccepted")
                    val returnsAccepted = returnsAcceptedArray.getString(0)
                    // Extracting shippingInfo
                    val shippingInfoArray = firstItem.getJSONArray("shippingInfo")
                    val shippingInfo = shippingInfoArray.getJSONObject(0)
                    val sellerInfoArray = firstItem.getJSONArray("sellerInfo")
                    val sellerInfo = sellerInfoArray.getJSONObject(0)

                    // Extracting specific seller details
                    val sellerUserName = sellerInfo.getJSONArray("sellerUserName").getString(0)
                    val feedbackScore = sellerInfo.getJSONArray("feedbackScore").getString(0)
                    var score = view?.findViewById<TextView>(R.id.score)
                    score?.text = feedbackScore.toString()

                    val positiveFeedbackPercent = sellerInfo.getJSONArray("positiveFeedbackPercent").getString(0)
                    val positivePercent = view?.findViewById<TextView>(R.id.popularityscore)
                    positivePercent?.text = positiveFeedbackPercent.toString()
                    val feedbackRatingStar = sellerInfo.getJSONArray("feedbackRatingStar").getString(0)

                    val topRatedSeller = sellerInfo.getJSONArray("topRatedSeller").getString(0)

                    val feedbackScoreText = view?.findViewById<TextView>(R.id.score)
                    feedbackScoreText?.text = feedbackScore
                    val positiveScore = view?.findViewById<TextView>(R.id.popularityscore)
                    positiveScore?.text = positiveFeedbackPercent
                    // Assuming feedbackScore is an integer

                    val feedbackStarImageView = view?.findViewById<ImageView>(R.id.star)

                    val shippingType = shippingInfo.getJSONArray("shippingType").getString(0)
                    val shipToLocations = shippingInfo.getJSONArray("shipToLocations").getString(0)


                    val storeInfoArray = firstItem.getJSONArray("storeInfo")
                    val storeInfo = storeInfoArray.getJSONObject(0)
                    // Extracting storeURL
                    var storeName = storeInfo.getJSONArray("storeName").getString(0)
                    var name = view?.findViewById<TextView>(R.id.storeUrl)
                    name?.text = storeName
                    Log.d("name",storeName)

                    val storeURL = storeInfo.getJSONArray("storeURL").getString(0)
                    name?.setOnClickListener{
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storeURL))
                        startActivity(intent)
                    }
                    var shipping = view?.findViewById<TextView>(R.id.shippingCostTextView)
                    Log.d("shippingCost",shippingCost.toString())
                    if(shippingCost.toString()=="Free"){
                        shipping?.text = "Free Shipping"
                    }else{
                        shipping?.text = "$"+shippingCost
                    }

                    val oneDayShippingAvailable = shippingInfo.getJSONArray("oneDayShippingAvailable").getString(0)

                    val handlingTime = shippingInfo.getJSONArray("handlingTime").getString(0)
                    var handlingText = view?.findViewById<TextView>(R.id.handlingTimeInfo)
                    handlingText?.text = handlingTime.toString()
                    Log.d("handlingTime",handlingTime.toString())
                } catch (e: Exception) {
                    Log.e("PhotosFragment", "Error parsing JSON response: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
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
                Log.d("responseItemId",response.toString())
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
                val returnsWithin = returnPolicyObject?.optString("ReturnsWithin", "")
                val shippingCostPaidBy = returnPolicyObject?.optString("ShippingCostPaidBy", "")

                   var policy = view?.findViewById<TextView>(R.id.policyInfo)
                    policy?.text = returnsAccepted.toString()
                var returnsWithinText = view?.findViewById<TextView>(R.id.returnsWithinText)
                returnsWithinText?.text = returnsWithin.toString()
                var refundModes = view?.findViewById<TextView>(R.id.refundMode)
               refundModes?.text = refund.toString()
                var shippedby = view?.findViewById<TextView>(R.id.shippedSeller)
               shippedby?.text = shippingCostPaidBy.toString()

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
        return inflater.inflate(R.layout.fragment_shipping, container, false)
    }

}