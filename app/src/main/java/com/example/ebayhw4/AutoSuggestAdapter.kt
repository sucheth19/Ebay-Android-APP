package com.example.ebayhw4

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class AutoSuggestAdapter(context: Context, resource: Int) : ArrayAdapter<String>(context, resource) {
    private var suggestionList = mutableListOf<String>()

    fun setSuggestions(suggestions: List<String>) {
        suggestionList.clear()
        suggestionList.addAll(suggestions)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return suggestionList.size
    }

    override fun getItem(index: Int): String? {
        return if (index < suggestionList.size) {
            suggestionList[index]
        } else {
            null
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return resultValue as String
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    // Perform API call or any other logic to get suggestions based on the constraint
                    // For simplicity, we'll just return the entire list for demonstration purposes
                    filterResults.values = suggestionList
                    filterResults.count = suggestionList.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
