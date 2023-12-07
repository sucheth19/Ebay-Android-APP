
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.ebayhw4.R
import com.example.ebayhw4.SearchResultItem
import com.example.ebayhw4.WishlistManager


class PhotosFragment : Fragment() {
    private var title: String? = null
    private lateinit var requestQueue: RequestQueue
    private lateinit var wishlistButton: ImageButton
    private lateinit var product: SearchResultItem
    companion object {
        fun newInstance(title: String, product: SearchResultItem?): PhotosFragment {
            val fragment = PhotosFragment()
            val args = Bundle()
            args.putString("title",title)
            args.putParcelable("product", product)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString("title")
        product = arguments?.getParcelable("product")!!
            requestQueue = Volley.newRequestQueue(requireContext())
            fetchPhotos()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photos, container, false)
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
    private fun fetchPhotos() {
        val apiUrl = "https://web-tech-hw-3.wl.r.appspot.com/photo?title=$title"
        Log.d("apiUrl",apiUrl)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            Response.Listener { response ->

                val photoList = mutableListOf<PhotoItem>()
                val items = response.getJSONArray("items")

                for (i in 0 until minOf(items.length(), 8)) {
                    val item = items.getJSONObject(i)
                    val imageUrl = item.getString("link")
                    photoList.add(PhotoItem(imageUrl))
                }

                Log.d("photoList", photoList.toString())

                // Set up RecyclerView with the adapter
                val recyclerView = view?.findViewById<RecyclerView>(R.id.photoRecyclerView)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = PhotoAdapter(photoList)
            },
            Response.ErrorListener { error ->
                Log.e("PhotosFragment", "Error fetching photos: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }


    data class PhotoItem(val imageUrl: String)

    class PhotoAdapter(private val photoList: List<PhotosFragment.PhotoItem>) :
        RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

        class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.photoImageView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo, parent, false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val photoItem = photoList[position]
            Glide.with(holder.itemView.context)
                .load(photoItem.imageUrl)
                .into(holder.imageView)
        }

        override fun getItemCount(): Int {
            return photoList.size
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the FAB by its ID
        val fab = view.findViewById<ImageButton>(R.id.fab)

        // Set a click listener for the FAB
        fab.setOnClickListener {
           toggleWishlistItem()
        }
    }
}
