package com.example.ebayhw4
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale


class ApiCallTask(private val listener: ApiCallListener) : AsyncTask<String, Void, String>() {

    private var apiResult: String = ""

    override fun doInBackground(vararg params: String): String {
        try {
            val url = URL(params[0])
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
            apiResult = bufferedReader.use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return apiResult
    }

    override fun onPostExecute(result: String) {
        // Store the result
        apiResult = result
        // Notify the listener
        listener.onApiCallComplete(apiResult)
    }

    // Add a method to get the stored result
    fun getApiResult(): String {
        return apiResult
    }
}

interface ApiCallListener {
    fun onApiCallComplete(result: String)
}


data class SearchParameters(
    val keyword: String,
    val category: String,
    val new: Boolean,
    val used: Boolean,
    val unspecified: Boolean,
    val local: Boolean,
    val freeshipping: Boolean,
    val distance: Int,
    val location: String,
    val zipCode: Any?
)

class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var autoSuggestAdapter: AutoSuggestAdapter
    private lateinit var requestQueue: RequestQueue
    private lateinit var spinner: Spinner
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var currentLocation: Location? = null
    private var currentZipCode: String? = null
    private var liveLocation: String? = null
    private val MIN_TIME_BETWEEN_UPDATES = 1000L // 1 second
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES = 1f
    private var editTextZipCode: AutoCompleteTextView? = null
    val categories = arrayOf(
        "All", "Art", "Baby", "Books", "Clothing,Shoes and Accessories",
        "Computer, Tablets and Network", "Health and Beauty", "Music", "Video, Games and Consoles"
    )

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Find the Spinner in the layout
        spinner = view.findViewById(R.id.spinner)
        val radioButtonCurrentLocation =
            view.findViewById<RadioButton>(R.id.radioButtonCurrentLocation)
        val radioButtonZipCode = view.findViewById<RadioButton>(R.id.radioButton3)
        radioButtonCurrentLocation.isChecked = true
        getLocation()
        radioButtonCurrentLocation.setOnClickListener {
            view?.post {
                radioButtonCurrentLocation.isChecked = true
            }
            radioButtonZipCode.isChecked = false
        }
        val editTextZipCode = view.findViewById<AutoCompleteTextView>(R.id.editTextText4)
        autoSuggestAdapter = AutoSuggestAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line)
        editTextZipCode?.setAdapter(autoSuggestAdapter)
        setupZipCodeAutoComplete()
        radioButtonZipCode.setOnClickListener {
            radioButtonCurrentLocation.isChecked = false
            radioButtonZipCode.isChecked = true
            makeZipCodeRequest(editTextZipCode.text.toString(), editTextZipCode)
        }
        val checkBox: CheckBox = view.findViewById(R.id.checkBox13)
        val layout = view.findViewById<LinearLayout>(R.id.myLinearLayout)
        val searchButton = view.findViewById<Button>(R.id.searchButton)
        val clearButton = view.findViewById<Button>(R.id.clearButton)
        clearButton.setOnClickListener {
            clearForm()
        }

        val editTextKeyword = view.findViewById<EditText>(R.id.editTextText)
        val textView8 = view.findViewById<TextView>(R.id.textView8)

        layout.visibility = if (checkBox.isChecked) View.VISIBLE else View.GONE

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            layout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinner.adapter = adapter

        // Set up a listener for spinner item selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item (category) here
                val selectedCategory = categories[position]
                // You can perform actions based on the selected category
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing here, optional implementation
            }
        }



        searchButton.setOnClickListener {
            val warning = view?.findViewById<TextView>(R.id.warning)
            val keyword = editTextKeyword?.text.toString().trim()
            val currentRadioButton = view?.findViewById<RadioButton>(R.id.radioButtonCurrentLocation)
            val zipRadioButton = view?.findViewById<RadioButton>(R.id.radioButton3)

            val zip = editTextZipCode?.text.toString().trim()
            // Check if either keyword or zip code is empty
            if (zipRadioButton != null) {
                if(zipRadioButton.isChecked().toString()=="true"){
                    if (zip.isEmpty() && keyword.isEmpty()) {
                        warning?.visibility = View.VISIBLE
                        textView8?.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            "Please fix all fields with errors",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                   else if (zip.isEmpty()) {
                        warning?.visibility = View.VISIBLE
                        Toast.makeText(
                            requireContext(),
                            "Please fix all fields with errors",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
            if (keyword.isEmpty()) {
                textView8?.visibility = View.VISIBLE
                Toast.makeText(
                    requireContext(),
                    "Please fix all fields with errors",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                warning?.visibility = View.GONE
                textView8?.visibility = View.GONE
                // Continue with the search
                // ... (rest of the method)
                onSearchButtonClick(keyword)
            }


        }
    }

    private fun getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            getCurrentLocation()
        }
    }


    private fun getCurrentLocation() {
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses: List<Address>? =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (addresses != null && addresses.isNotEmpty()) {
                        currentZipCode = addresses[0]?.postalCode

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                // Remove updates only when necessary
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }

        try {
            // Request location updates with GPS_PROVIDER for accuracy
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES, // Minimum time interval between updates (adjust as needed)
                MIN_DISTANCE_CHANGE_FOR_UPDATES, // Minimum distance between updates (adjust as needed)
                locationListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Inside the clearForm function
    private fun clearForm() {
        val editTextKeyword = view?.findViewById<EditText>(R.id.editTextText)
        editTextKeyword?.text?.clear()
        val textView8 = view?.findViewById<TextView>(R.id.textView8)
        textView8?.visibility = View.GONE
        val warning = view?.findViewById<TextView>(R.id.warning)
        warning?.visibility = View.GONE

        // Correctly reference the RadioButton
        val radioButtonCurrentLocation =
            view?.findViewById<RadioButton>(R.id.radioButtonCurrentLocation)
        radioButtonCurrentLocation?.isChecked = true

        // Set the spinner to the first item ("All")
        val spinner = view?.findViewById<Spinner>(R.id.spinner)
        spinner?.setSelection(0)

        // Uncheck all checkboxes
        val checkBoxNew = view?.findViewById<CheckBox>(R.id.checkBox7)
        checkBoxNew?.isChecked = false

        val checkBoxUsed = view?.findViewById<CheckBox>(R.id.checkBox8)
        checkBoxUsed?.isChecked = false

        val checkBoxUnspecified = view?.findViewById<CheckBox>(R.id.checkBox9)
        checkBoxUnspecified?.isChecked = false

        val checkBoxLocal = view?.findViewById<CheckBox>(R.id.checkBox11)
        checkBoxLocal?.isChecked = false

        val checkBoxFreeShipping = view?.findViewById<CheckBox>(R.id.checkBox12)
        checkBoxFreeShipping?.isChecked = false

        val enableNearBySearch = view?.findViewById<CheckBox>(R.id.checkBox13)
        enableNearBySearch?.isChecked = false

        // Set visibility of the layout to GONE
        val layout = view?.findViewById<LinearLayout>(R.id.myLinearLayout)
        layout?.visibility = View.GONE

        // Clear the distance EditText
        val editTextDistance = view?.findViewById<EditText>(R.id.editTextText2)
        editTextDistance?.text?.clear()

        // Uncheck the radio buttons
        val radioButtonZipCode = view?.findViewById<RadioButton>(R.id.radioButton3)
        radioButtonZipCode?.isChecked = false

        // Clear the zip code EditText
        editTextZipCode = view?.findViewById<AutoCompleteTextView>(R.id.editTextText4)
        editTextZipCode?.text?.clear()
    }

    private fun updateUI(apiResult: String) {
        try {
            // Parse the JSON response and update your UI
            val gson = Gson()
            val searchParameters = gson.fromJson(apiResult, SearchParameters::class.java)

            // Check if searchParameters is not null before accessing its properties
            if (searchParameters != null) {
                val keyword = searchParameters.keyword
                // Do whatever you need with the keyword or other properties
            } else {
                Log.e("SearchResults", "SearchParameters object is null")
            }
        } catch (e: Exception) {
            Log.e("SearchResults", "Error updating UI", e)
        }
    }

    private fun onSearchButtonClick(keyword: String) {
        try {
            val editTextKeyword = view?.findViewById<EditText>(R.id.editTextText)
            val keyword = editTextKeyword?.text.toString().trim()
            val spinner = view?.findViewById<Spinner>(R.id.spinner)
            val selectedCategory = categories[spinner?.selectedItemPosition ?: 0]

            val checkBoxNew = view?.findViewById<CheckBox>(R.id.checkBox7)
            val newChecked = checkBoxNew?.isChecked ?: false

            val checkBoxUsed = view?.findViewById<CheckBox>(R.id.checkBox8)
            val usedChecked = checkBoxUsed?.isChecked ?: false

            val checkBoxUnspecified = view?.findViewById<CheckBox>(R.id.checkBox9)
            val unspecifiedChecked = checkBoxUnspecified?.isChecked ?: false

            val checkBoxLocal = view?.findViewById<CheckBox>(R.id.checkBox11)
            val localChecked = checkBoxLocal?.isChecked ?: false

            val checkBoxFreeShipping = view?.findViewById<CheckBox>(R.id.checkBox12)
            val freeShippingChecked = checkBoxFreeShipping?.isChecked ?: false

            val editTextDistance = view?.findViewById<EditText>(R.id.editTextText2)
            val distance = editTextDistance?.text.toString().toIntOrNull() ?: 10

            val radioButtonCurrentLocation =
                view?.findViewById<RadioButton>(R.id.radioButtonCurrentLocation)
            val radioButtonZipCode = view?.findViewById<RadioButton>(R.id.radioButton3)
            val editTextZipCode = view?.findViewById<EditText>(R.id.editTextText4)
            val zipCode =
                if (editTextZipCode?.text.toString().length > 0) editTextZipCode?.text.toString() else currentZipCode

            // Create SearchParameters instance
            val searchParams = SearchParameters(
                keyword = keyword,
                category = if (selectedCategory == "All") "All Categories" else selectedCategory,
                new = newChecked,
                used = usedChecked,
                unspecified = unspecifiedChecked,
                local = localChecked,
                freeshipping = freeShippingChecked,
                distance = distance,
                location = if (radioButtonCurrentLocation?.isChecked == true) "current" else "zip",
                zipCode = zipCode.toString()
            )


            // Convert to JSON using Gson
            val searchParamsJson = Gson().toJson(searchParams)
            // Construct the API URL
            val apiUrl =
                "https://web-tech-hw-3.wl.r.appspot.com/search-results?searchParams=$searchParamsJson"

            val splashIntent = Intent(requireContext(), LoadScreen::class.java)

            // Put the search parameters as extras to the intent
            splashIntent.putExtra("Search_PARAMS", searchParamsJson)

            // Start the SplashActivity
            startActivity(splashIntent)

        } catch (e: Exception) {
            Log.e("SearchFragment", "Error during search button click", e)
        }
    }

    class ApiCallTask(private val listener: ApiCallListener) {

        fun execute(apiUrl: String) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val result = makeApiCall(apiUrl)
                    withContext(Dispatchers.Main) {
                        listener.onApiCallComplete(result)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun makeApiCall(apiUrl: String): String {
            val url = URL(apiUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
            return bufferedReader.use { it.readText() }
        }
    }

    private fun getCurrentLocationZipCode(): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address>? =
                geocoder.getFromLocation(
                    currentLocation?.latitude ?: 0.0,
                    currentLocation?.longitude ?: 0.0,
                    1
                )
            if (addresses != null && addresses.isNotEmpty()) {
                addresses[0]?.postalCode ?: ""
            } else {
                ""
            }

        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
    private fun setupZipCodeAutoComplete() {
        val autoCompleteZipCode = view?.findViewById<AutoCompleteTextView>(R.id.editTextText4)

        autoCompleteZipCode?.addTextChangedListener(object : TextWatcher {
            private var debounceTimer: CountDownTimer? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                debounceTimer?.cancel()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel the previous timer
                debounceTimer?.cancel()

                // Set up a new timer to wait for the user to finish typing
                debounceTimer = object : CountDownTimer(500, 500) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        // Make the API call to get suggestions
                        Log.d("string",s.toString())
                        makeZipCodeRequest(s.toString(), autoCompleteZipCode)
                    }
                }.start()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        autoCompleteZipCode?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                // Handle item click here if needed
                val selectedItem = parent.getItemAtPosition(position) as String
                Toast.makeText(
                    requireContext(),
                    "Selected Zip Code: $selectedItem",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun makeZipCodeRequest(zipCode: String, autoCompleteTextView: AutoCompleteTextView) {
        val url = "https://web-tech-hw-3.wl.r.appspot.com/zip-suggestions?zipCode=$zipCode"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Parse the JSON object
                val zipCodeList = mutableListOf<String>()
                try {
                    // Parse the response and extract ZIP codes
                    val jsonObject = JSONObject(response)
                    val postalCodesArray = jsonObject.getJSONArray("postalCodes")
                    for (i in 0 until postalCodesArray.length()) {
                        val postalCodeObject = postalCodesArray.getJSONObject(i)
                        zipCodeList.add(postalCodeObject.getString("postalCode"))
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                // Extract the "postalCodes" array

                // Use postalCodeList as needed, for example, setting it to the AutoCompleteTextView
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    zipCodeList
                )
                autoCompleteTextView.setAdapter(adapter)
                autoCompleteTextView.showDropDown()
            },
            Response.ErrorListener { error ->
                Log.e("Volley", "Error: $error")
            }
        )
        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }



}

