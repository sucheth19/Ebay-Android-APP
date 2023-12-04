package com.example.ebayhw4

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WishList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val wishlistItems: MutableList<WishlistItem> = WishlistManager.getWishlist().toMutableList()
        val adapter = WishlistAdapter(wishlistItems)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        for (item in wishlistItems) {
            Log.d("WishList after", "Wishlist item: $item")
        }

    }
}