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
    private lateinit var layoutNoCard: LinearLayout
    private lateinit var wishlistAdapter: WishlistAdapter
    private lateinit var itemCountTextView: TextView
    private lateinit var totalTextView: TextView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        WishlistManager.init(requireContext())
        wishlistRecyclerView = view.findViewById(R.id.wishlistRecyclerView)
        noItemsTextView = view.findViewById(R.id.noItemsTextView)
        wishlistCardView = view.findViewById(R.id.wishlistCardView)
        layoutNoCard = view.findViewById<LinearLayout>(R.id.linearLayout2)
        itemCountTextView = view.findViewById(R.id.itemCount)
        totalTextView = view.findViewById(R.id.total)
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
        if (wishlistItems != null && wishlistItems.isNotEmpty()) {
            wishlistCardView.visibility = View.VISIBLE
            noItemsTextView.visibility = View.GONE
            layoutNoCard.visibility = View.GONE
            itemCountTextView.visibility = View.VISIBLE
            totalTextView.visibility = View.VISIBLE
            // Set the adapter for the RecyclerView
            wishlistAdapter.updateItems(wishlistItems) // Add this line
            val totalCount = wishlistItems.size
            val totalPrice = calculateTotalPrice(wishlistItems)
            itemCountTextView.text = "WishList Total($totalCount items)"
            totalTextView.text = "$$totalPrice"
        } else {
            wishlistCardView.visibility = View.VISIBLE
            noItemsTextView.visibility = View.VISIBLE
            layoutNoCard.visibility = View.VISIBLE
            itemCountTextView.visibility = View.GONE
            totalTextView.visibility = View.GONE
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