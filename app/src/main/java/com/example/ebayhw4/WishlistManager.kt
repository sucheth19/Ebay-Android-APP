package com.example.ebayhw4

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


data class WishlistItem(
    val itemId: String,
    val title: String,
    val galleryUrl: String,
    val price: String,
    val shippingPrice: String,
    val zipCode: String,
    val shippingInfo: JSONObject, // Change to List or Array
    val returnsAccepted: Boolean
)
object WishlistManager {
    val wishlistLiveData = MutableLiveData<List<WishlistItem>>()

    private val wishlist: MutableList<WishlistItem> = mutableListOf()
    private var requestQueue: RequestQueue? = null

    fun init(context: Context) {
        requestQueue = Volley.newRequestQueue(context.applicationContext)

        getAllWishlistItemsFromApi()
    }

    fun addItemToWishlist(searchResultItem: SearchResultItem) {
        if (!isItemInWishlist(searchResultItem)) {
            try {
                val shippingInfoList = JSONObject(mapOf("shippingCondition" to searchResultItem.shippingCondition))
                val wishlistItem = WishlistItem(
                    itemId = searchResultItem.itemId,
                    title = searchResultItem.title,
                    galleryUrl = searchResultItem.galleryUrl,
                    price = searchResultItem.price,
                    shippingPrice = searchResultItem.shippingPrice,
                    zipCode = searchResultItem.zipCode,
                    shippingInfo = shippingInfoList,
                    returnsAccepted = true
                )
                addToWishlistApiCall(wishlistItem)
                wishlist.add(wishlistItem)
                wishlistLiveData.postValue(wishlist)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
    private fun addToWishlistApiCall( wishlistItem: WishlistItem) {

        val fullUrl = "https://web-tech-hw-3.wl.r.appspot.com/products?itemId=${wishlistItem.itemId}&title=${wishlistItem.title}&galleryUrl=${wishlistItem.galleryUrl}&price=${wishlistItem.price}&shippingPrice=${wishlistItem.shippingPrice}&zipCode=${wishlistItem.zipCode}&shippingInfo=${wishlistItem.shippingInfo}&returnsAccepted=${wishlistItem.returnsAccepted}"
        Log.d("fullUrl",fullUrl)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            fullUrl,
            null,
            { response ->
                Log.d("API response", response.toString())
            },
            { error ->
                Log.e("Push error", error.toString())
            }
        )

        requestQueue?.add(jsonObjectRequest)
        getAllWishlistItemsFromApi()
    }

    fun getAllWishlistItemsFromApi() {
        val apiUrl = "https://web-tech-hw-3.wl.r.appspot.com/all-products"
        val jsonObjectRequest = JsonArrayRequest(
            Request.Method.GET,
            apiUrl,
            null,
            { response ->

                val wishlistItems = parseWishlistItems(response)
                Log.d("live data",wishlistItems.toString())
                wishlistLiveData.postValue(wishlistItems)
            },
            { error ->
                Log.e("Get error", error.toString())
//                completion(null)
            }
        )

        // Add the request to the RequestQueue
        requestQueue?.add(jsonObjectRequest)
    }

    fun parseWishlistItems(response: JSONArray): List<WishlistItem> {
        val wishlistItems = mutableListOf<WishlistItem>()

        try {
            for (i in 0 until response.length()) {
                val productObject = response.getJSONObject(i)

                val shippingInfoObject = productObject.optJSONObject("shippingInfo") ?: JSONObject()

                val wishlistItem = WishlistItem(
                    itemId = productObject.getString("itemId"),
                    title = productObject.getString("title"),
                    galleryUrl = productObject.getString("galleryURL"),
                    price = productObject.getString("price"),
                    shippingPrice = productObject.getString("shippingPrice"),
                    zipCode = productObject.getString("zipCode"),
                    shippingInfo = shippingInfoObject,
                    returnsAccepted = productObject.getBoolean("returnsAccepted")
                )

                wishlistItems.add(wishlistItem)
            }

            // Update the local wishlist list with the parsed items
            wishlist.clear()
            wishlist.addAll(wishlistItems)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return wishlistItems
    }
    fun getWishlist(): List<WishlistItem> {
        return wishlist
    }
    fun removeWishlistItem(context: Context, item: WishlistItem) {
        removeItemFromWishlist(item)
        removeFromWishlistInApi(context, item.itemId)
    }

    fun removeItemFromWishlist(item: WishlistItem) {
        wishlist.remove(item)
        wishlistLiveData.postValue(wishlist)
    }
    fun isItemInWishlist(searchResultItem: SearchResultItem?): Boolean {
        return searchResultItem != null && wishlist.any { it.itemId == searchResultItem.itemId }
    }
     fun removeFromWishlistInApi(context: Context, itemId: String) {
        // Instantiate the RequestQueue
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        // Formulate the request URL
        val url = "https://web-tech-hw-3.wl.r.appspot.com/products/$itemId"

        // Create a DELETE request
        val request = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            { response ->
                // Handle successful API response
                Log.d("API", "Item removed successfully")
            },
            { error ->
                // Handle unsuccessful API response
                Log.e("API", "Failed to remove item from API: ${error.networkResponse?.statusCode}")
            }
        )

        // Add the request to the RequestQueue
        requestQueue.add(request)
    }

}


