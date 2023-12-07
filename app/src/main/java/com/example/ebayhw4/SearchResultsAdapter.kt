
import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.ebayhw4.R
import com.example.ebayhw4.SearchResultItem
import com.example.ebayhw4.WishlistItem
import com.example.ebayhw4.WishlistManager
import com.example.ebayhw4.itemDetailsSplash
import com.squareup.picasso.Picasso
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
class SearchResultsAdapter(
    private val productList: List<SearchResultItem>,
    private val searchParamsJson: String?
) :
    RecyclerView.Adapter<SearchResultsAdapter.ProductViewHolder>() {
    private var latestClickedPosition: Int = RecyclerView.NO_POSITION
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val product = productList[position]

        holder.titleTextView.text = product.title
        holder.zipCodeTextView.text ="Zip: ${product.zipCode}"
        holder.shippingCostTextView.text = product.shippingPrice
        holder.conditon.text = product.shippingCondition
        holder.price.text = product.price
        var itemId:Any = product.itemId
        val wishlistItem = WishlistItem(
            itemId = product.itemId,
            title = product.title,
            galleryUrl = product.galleryUrl,
            price = product.price,
            shippingPrice = product.shippingPrice,
            zipCode = product.zipCode,
            shippingInfo = JSONObject(),
            returnsAccepted = true
        )
        Picasso.get().load(product.galleryUrl).into(holder.imageView)
        val isItemInWishlist = WishlistManager.isItemInWishlist(product)
        updateWishlistButtonBackground(holder.imageButton, isItemInWishlist )
        holder.imageButton.setOnClickListener {
            if (isItemInWishlist) {
                WishlistManager.removeItemFromWishlist(wishlistItem)
                Toast.makeText(
                    holder.itemView.context,
                    "${product.title} was removed from the wishlist",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                WishlistManager.addItemToWishlist(product)
                Toast.makeText(
                    holder.itemView.context,
                    "${product.title} was added to the wishlist",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Update the background of the button after toggling
            updateWishlistButtonBackground(holder.imageButton, !isItemInWishlist)
        }

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, itemDetailsSplash::class.java)
            latestClickedPosition = position
            intent.putExtra("productId",product.itemId)
            intent.putExtra("title",product.title)
            intent.putExtra("searchParams",searchParamsJson)
            intent.putExtra("shippingCost",product.shippingPrice)
            intent.putExtra("product", product)
            intent.putExtra("position", latestClickedPosition)
            holder.itemView.context.startActivity(intent)
        }

    }
    private fun updateWishlistButtonBackground(button: AppCompatImageButton, isInWishlist: Boolean) {
        // Update the background of the button based on the wishlist status
        val drawableResource =
            if (isInWishlist) R.drawable.wishlist_remove else R.drawable.ic_launcher_wishlist_round
        button.setBackgroundResource(drawableResource)
    }
    override fun getItemCount(): Int {
        return productList.size
    }



    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val zipCodeTextView: TextView = itemView.findViewById(R.id.zipCodeTextView)
        val shippingCostTextView: TextView = itemView.findViewById(R.id.shippingCostTextView)
        val imageButton = itemView.findViewById<AppCompatImageButton>(R.id.addToWishlistButton)
        val conditon: TextView = itemView.findViewById(R.id.conditionTextView)
        val price:TextView = itemView.findViewById(R.id.productCostTextView)
    }
}
