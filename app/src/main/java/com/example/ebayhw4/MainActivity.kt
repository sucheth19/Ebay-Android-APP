package com.example.ebayhw4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the ViewPager and TabLayout in your layout
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        // Create an instance of the adapter and set it on the ViewPager
        val adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

        // Link the TabLayout with the ViewPager
        tabLayout.setupWithViewPager(viewPager)

    }
}