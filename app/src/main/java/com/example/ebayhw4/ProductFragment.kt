
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.squareup.picasso.Picasso

class ProductFragment : Fragment() {

    private var itemId: String? = null
    private lateinit var requestQueue: RequestQueue
    private var pictureUrls: List<String>? = null
    private lateinit var imageRecyclerView: RecyclerView
    private var title:String? = null
    private var currentPrice: String? = null
    private var shippingCost:String? = null
    private var brand:String?= null
    private var specifics: List<String>?=null
    private lateinit var imageContainer: LinearLayout
    companion object {
        fun newInstance(itemId: String, shippingCost:String): ProductFragment {
            val fragment = ProductFragment()
            val args = Bundle()
            args.putString("itemId", itemId)
            args.putString("shippingCost",shippingCost)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = arguments?.getString("itemId")
        shippingCost = arguments?.getString("shippingCost")
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
        makeApiCall()
        return view
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

        requestQueue.add(jsonObjectRequest)
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
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
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
