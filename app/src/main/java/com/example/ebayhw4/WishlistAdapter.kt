package com.example.ebayhw4

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class WishlistAdapter(private val wishlistItems: MutableList<WishlistItem>) :
    RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        private val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        private val itemPrice: TextView = itemView.findViewById(R.id.price)
        private val zipCode: TextView = itemView.findViewById(R.id.zipCode)
        private val shippingInfo:TextView = itemView.findViewById(R.id.shippingCondition)
        private val shippingCost: TextView = itemView.findViewById(R.id.shippingCost)

        fun bind(item: WishlistItem) {
            itemTitle.text = item.title
            itemPrice.text = item.price
            Picasso.get().load(item.galleryUrl).into(itemImage)
            val zip = item.zipCode
            zipCode.text = "zip: $zip"
            shippingInfo.text = "Used"
            if(item.shippingPrice=="0.0"){
                shippingCost.text = "Free"
            }else{
                shippingCost.text = item.shippingPrice
            }


        }

        init {
            val removeButton: ImageButton = itemView.findViewById(R.id.remove_item)
            removeButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val removedItem = wishlistItems[position]
                    removeItem(position)
                    removeItemFromWishlistApiCall(itemView.context, removedItem)
                }
            }
        }
    }

    fun updateItems(newItems: List<WishlistItem>) {
        wishlistItems.clear()
        wishlistItems.addAll(newItems)
        notifyDataSetChanged()
    }
  fun removeItemFromWishlistApiCall(context: Context, removedItem: WishlistItem) {
        WishlistManager.removeWishlistItem(context, removedItem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wishlist_item, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = wishlistItems[position]
        holder.bind(item)
    }
    override fun getItemCount(): Int {
        return wishlistItems.size
    }
    private fun removeItem(position: Int) {
        val updatedList = wishlistItems.toMutableList()
        updatedList.removeAt(position)
        notifyItemRemoved(position)
    }

}