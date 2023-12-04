
import android.content.Intent
import android.util.Log
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
import com.example.ebayhw4.WishlistManager
import com.example.ebayhw4.itemDetailsSplash
import com.squareup.picasso.Picasso

class SearchResultsAdapter(
    private val productList: List<SearchResultItem>,
    private val searchParamsJson: String?
) :
    RecyclerView.Adapter<SearchResultsAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        Log.d("product",product.toString())
        holder.titleTextView.text = product.title
        holder.zipCodeTextView.text = product.zipCode
        holder.shippingCostTextView.text = product.shippingPrice
        holder.conditon.text = product.shippingCondition
        holder.price.text = product.price
        Picasso.get().load(product.galleryUrl).into(holder.imageView)
        holder.imageButton.setOnClickListener {
            WishlistManager.addItemToWishlist(
                product
            )

            Toast.makeText(
                holder.itemView.context,
                "${product.title} was added to the wishlist",
                Toast.LENGTH_SHORT
            ).show()
        }
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, itemDetailsSplash::class.java)
            intent.putExtra("productId",product.itemId)
            intent.putExtra("title",product.title)
            intent.putExtra("searchParams",searchParamsJson)
            intent.putExtra("shippingCost",product.shippingPrice)
            holder.itemView.context.startActivity(intent)
        }

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
