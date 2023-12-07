package com.example.ebayhw4

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WishListFragment : Fragment(R.layout.fragment_wishlist) {
    private lateinit var wishlistRecyclerView: RecyclerView
    private lateinit var noItemsTextView: TextView
    private lateinit var wishlistCardView: androidx.cardview.widget.CardView
    private lateinit var wishlistAdapter: WishlistAdapter
    private lateinit var itemCountTextView: TextView
    private lateinit var totalTextView: TextView
    private lateinit var linearLayout2: LinearLayout
    override fun onResume() {
        super.onResume()
        // Fetch the latest wishlist items when the fragment is resumed
        WishlistManager.getAllWishlistItemsFromApi()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        WishlistManager.init(requireContext())
        wishlistRecyclerView = view.findViewById(R.id.wishlistRecyclerView)
        noItemsTextView = view.findViewById(R.id.noItemsTextView)
        wishlistCardView = view.findViewById(R.id.wishlistCardView)
        itemCountTextView = view.findViewById(R.id.itemCount)
        totalTextView = view.findViewById(R.id.total)
        linearLayout2 = view.findViewById(R.id.linearLayout2)
        // Observe changes in wishlistLiveData
        WishlistManager.wishlistLiveData.observe(viewLifecycleOwner, Observer { wishlistItems ->
            updateUI(wishlistItems)
        })
        val layoutManager = GridLayoutManager(context, 2)
        wishlistRecyclerView.layoutManager = layoutManager

        // Initialize the wishlistAdapter only once
        wishlistAdapter = WishlistAdapter(ArrayList(WishlistManager.getWishlist())) // Convert to mutable list
        wishlistRecyclerView.adapter = wishlistAdapter
        WishlistManager.getAllWishlistItemsFromApi()
    }


    private fun updateUI(wishlistItems: List<WishlistItem>?) {
        if (wishlistItems?.size!! > 0) {
            linearLayout2.visibility = View.GONE
            wishlistCardView.visibility = View.GONE
            noItemsTextView.visibility = View.GONE
            totalTextView.visibility = View.VISIBLE
            itemCountTextView.visibility = View.VISIBLE
            wishlistAdapter.updateItems(wishlistItems) // Add this line
            val totalCount = wishlistItems.size
            val totalPrice = calculateTotalPrice(wishlistItems)
            itemCountTextView.text = "WishList Total($totalCount items)"
            totalTextView.text = "$$totalPrice"
        } else {
            linearLayout2.visibility = View.VISIBLE
            wishlistCardView.visibility = View.VISIBLE
            noItemsTextView.visibility = View.VISIBLE
            totalTextView.visibility = View.GONE
            itemCountTextView.visibility = View.GONE
        }
    }
    private fun calculateTotalPrice(wishlistItems: List<WishlistItem>): Double {
        var total = 0.0
        for (item in wishlistItems) {
            total += item.price.toDouble()
        }
        return total
    }
}