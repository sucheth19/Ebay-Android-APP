
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageButton
    import android.widget.ImageView
    import android.widget.LinearLayout
    import android.widget.TextView
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.PagerSnapHelper
    import androidx.recyclerview.widget.RecyclerView
    import com.android.volley.Request
    import com.android.volley.RequestQueue
    import com.android.volley.Response
    import com.android.volley.toolbox.JsonObjectRequest
    import com.android.volley.toolbox.Volley
    import com.example.ebayhw4.R
    import com.example.ebayhw4.SearchResultItem
    import com.example.ebayhw4.WishlistItem
    import com.example.ebayhw4.WishlistManager
    import com.squareup.picasso.Picasso
    import org.json.JSONObject

    class ProductFragment : Fragment() {

        private var itemId: String? = null
        private lateinit var requestQueue: RequestQueue
        private var pictureUrls: List<String>? = null
        private lateinit var imageRecyclerView: RecyclerView
        private var title:String? = null
        private var currentPrice: String? = null
        private var shippingCost:String? = null
        private var brand:String?= null
        private lateinit var imageContainer: LinearLayout
        private lateinit var wishlistItem: WishlistItem
        companion object {
            fun newInstance(itemId: String, shippingCost: String, product: SearchResultItem?): ProductFragment {
                val fragment = ProductFragment()
                val args = Bundle()
                args.putString("itemId", itemId)
                args.putString("shippingCost",shippingCost)
                args.putParcelable("product", product)
                fragment.arguments = args
                return fragment
            }
        }

        private lateinit var wishlistButton:ImageButton
        private lateinit var product: SearchResultItem

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            itemId = arguments?.getString("itemId")
            shippingCost = arguments?.getString("shippingCost")
            product = arguments?.getParcelable("product")!!

            // Use the product as needed in your fragment
            Log.d("productFragment Product", product.toString())
            Log.d("shippingCost",shippingCost.toString())
            // Initialize the RequestQueue
            requestQueue = Volley.newRequestQueue(requireContext())
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_product, container, false)
            imageRecyclerView = view.findViewById(R.id.imageRecyclerView)
            imageRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            imageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                }
            })

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(imageRecyclerView)
            wishlistButton = view.findViewById(R.id.fab)
            wishlistButton.setOnClickListener {
                toggleWishlistItem()
            }
            val shippingInfoList = JSONObject(mapOf("shippingCondition" to product?.shippingCondition.orEmpty()))

            wishlistItem = WishlistItem(
                itemId = product?.itemId.orEmpty(),
                title = product?.title.orEmpty(),
                galleryUrl = product?.galleryUrl.orEmpty(),
                price = product?.price.orEmpty(),
                shippingPrice = product?.shippingPrice.orEmpty(),
                zipCode = product?.zipCode.orEmpty(),
                shippingInfo = shippingInfoList,
                returnsAccepted = true
            )
            return view
        }
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            makeApiCall()
        }
        private fun toggleWishlistItem() {
            val isInWishlist = WishlistManager.isItemInWishlist(product)

            if (isInWishlist) {
                WishlistManager.removeFromWishlistInApi(requireContext(),wishlistItem.itemId)
                Toast.makeText(
                    requireContext(),
                    "${product.title} was removed from the wishlist",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                WishlistManager.addItemToWishlist(product)
                Toast.makeText(
                    requireContext(),
                    "${product.title} was added to the wishlist",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Update the background of the button after toggling
            updateWishlistButtonBackground(!isInWishlist)
        }
        private fun updateWishlistButtonBackground(isInWishlist: Boolean) {
            // Update the background of the button based on the wishlist status
            val drawableResource =
                if (isInWishlist) R.drawable.cart_remove else R.drawable.cart_plus
            wishlistButton.setImageResource(drawableResource)
//            wishlistButton.foreground = if (isInWishlist) ContextCompat.getDrawable(requireContext(), R.drawable.orange_wishlist_removal) else ContextCompat.getDrawable(requireContext(), R.drawable.ic_launcher_wishlist_float)

        }

        private fun makeApiCall() {

            val url = "https://web-tech-hw-3.wl.r.appspot.com/product-details?itemId=$itemId"
            Log.d("url",url)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                  pictureUrls = response.getJSONObject("Item").getJSONArray("PictureURL")
                        ?.let { 0.until(it.length()).map { i -> it.optString(i) } }
                        ?: emptyList()
                    val adapter = ImageAdapter(pictureUrls!!)
                    imageRecyclerView.adapter = adapter
                    title = response.getJSONObject("Item").getString("Title")
                    var titleTextView = view?.findViewById<TextView>(R.id.productTitle)
                    titleTextView?.text = title
                     currentPrice = response.getJSONObject("Item").getJSONObject("CurrentPrice").getString("Value")
                    var shipCost = view?.findViewById<TextView>(R.id.priceShipping)
                    if(shippingCost=="Free"){
                        shippingCost = "With Free Shipping"
                    }else{
                        shippingCost = "With $"+shippingCost+" Shipping"
                    }
                    shipCost?.text = "$"+currentPrice.toString()+" "+shippingCost.toString()
                    var cost = view?.findViewById<TextView>(R.id.price)
                    cost?.text = "$"+currentPrice.toString()
                    val itemSpecificsArray = response.getJSONObject("Item")
                        .getJSONObject("ItemSpecifics")
                        .getJSONArray("NameValueList")
                    for (i in 0 until itemSpecificsArray.length()) {
                        val nameValue = itemSpecificsArray.getJSONObject(i)
                        val name = nameValue.getString("Name")

                        if (name == "Brand") {
                            val brandArray = nameValue.getJSONArray("Value")
                            brand = if (brandArray.length() > 0) brandArray.getString(0) else null
                            break
                        }
                    }
                    var b = view?.findViewById<TextView>(R.id.brand)
                    b?.text = brand.toString()
                    val itemSpecificsObject = response.getJSONObject("Item").getJSONObject("ItemSpecifics")
                    val nameValueListArray = itemSpecificsObject.getJSONArray("NameValueList")

                    // Initialize the brand value
                    var brandValue = ""

                    // Dynamically add TextView for each "ItemSpecifics" value
                    for (i in 0 until nameValueListArray.length()) {
                        val nameValue = nameValueListArray.getJSONObject(i)
                        val name = nameValue.optString("Name")
                        val valueArray = nameValue.optJSONArray("Value")

                        // Skip the row if details are missing
                        if (name.isNullOrEmpty() || valueArray == null || valueArray.length() == 0) {
                            continue
                        }

                        // Extracting and capitalizing the first letter of each value
                        for (j in 0 until valueArray.length()) {
                            val value = valueArray.getString(j)
                            val capitalizedValue = value.toLowerCase().capitalize()

                            // Set the brand value for later use
                            if (name.equals("Brand", ignoreCase = true)) {
                                brandValue = capitalizedValue
                            }

                            // Dynamically add TextView for each value
                            addTextViewToContainer("* "+capitalizedValue)
                        }
                    }


                },
                Response.ErrorListener { error ->
                    Log.d("error",error.toString())
                }
            )
            val product: SearchResultItem? = arguments?.getParcelable("product")

            requestQueue.add(jsonObjectRequest)
            updateWishlistButtonBackground(WishlistManager.isItemInWishlist(product))
        }
        private fun addTextViewToContainer(text: String) {
            val textView = TextView(requireContext())
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textView.text = text
            view?.findViewById<LinearLayout>(R.id.specificationsContainer)?.addView(textView)
        }

        class ImageAdapter(private val pictureUrls: List<String>) :
            RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
                val imageView = ImageView(parent.context)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.FIT_XY
                return ImageViewHolder(imageView)
            }



            override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
                Picasso.get().load(pictureUrls[position]).into(holder.imageView)
            }

            override fun getItemCount(): Int {
                return pictureUrls.size
            }

            class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
        }
    }
