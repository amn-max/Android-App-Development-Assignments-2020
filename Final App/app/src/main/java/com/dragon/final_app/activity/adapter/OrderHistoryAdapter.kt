package com.dragon.final_app.activity.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.dragon.final_app.R
import com.dragon.final_app.activity.model.CartItems
import com.dragon.final_app.activity.model.OrderHistoryRestaurant
import com.dragon.final_app.activity.util.ConnectionManager
import org.json.JSONException

class OrderHistoryAdapter(
    val context: Context,
    val orderedRestaurantList: ArrayList<OrderHistoryRestaurant>
) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolderOrderHistoryRestaurant>() {
    lateinit var sharedPreferences: SharedPreferences

    class ViewHolderOrderHistoryRestaurant(view: View) : RecyclerView.ViewHolder(view) {
        val textViewResturantName: TextView = view.findViewById(R.id.textViewResturantName)
        val textViewDate: TextView = view.findViewById(R.id.textViewDate)
        val recyclerViewItemsOrdered: RecyclerView =
            view.findViewById(R.id.recyclerViewItemsOrdered)


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderOrderHistoryRestaurant {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_history_single_row, parent, false)

        return ViewHolderOrderHistoryRestaurant(view)
    }

    override fun getItemCount(): Int {
        return orderedRestaurantList.size
    }

    override fun onBindViewHolder(holder: ViewHolderOrderHistoryRestaurant, position: Int) {
        val restaurantObject = orderedRestaurantList[position]


        holder.textViewResturantName.text = restaurantObject.restaurantName
        var formatDate = restaurantObject.orderPlacedAt
        formatDate = formatDate.replace("-", "/")
        formatDate = formatDate.substring(0, 6) + "20" + formatDate.substring(6, 8)
        holder.textViewDate.text = formatDate


        var layoutManager = LinearLayoutManager(context)
        var orderedItemAdapter: CartRecyclerAdapter

        if (ConnectionManager().checkConnectivity(context)) {

            try {

                val orderItemsPerRestaurant = ArrayList<CartItems>()

                sharedPreferences =
                    context.getSharedPreferences("Restaurant Preferences", Context.MODE_PRIVATE)

                val user_id = sharedPreferences.getString("user_id", "")

                val queue = Volley.newRequestQueue(context)

                val url = "http://13.235.250.119/v2/orders/fetch_result/$user_id"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {

                        val responseJsonObjectData = it.getJSONObject("data")

                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {

                            val data = responseJsonObjectData.getJSONArray("data")

                            val fetechedRestaurantJsonObject = data.getJSONObject(position)

                            orderItemsPerRestaurant.clear()

                            val foodOrderedJsonArray =
                                fetechedRestaurantJsonObject.getJSONArray("food_items")

                            for (j in 0 until foodOrderedJsonArray.length()) {
                                val eachFoodItem = foodOrderedJsonArray.getJSONObject(j)
                                val itemObject = CartItems(
                                    eachFoodItem.getString("food_item_id"),
                                    eachFoodItem.getString("name"),
                                    eachFoodItem.getString("cost"),
                                    "000"
                                )

                                orderItemsPerRestaurant.add(itemObject)

                            }

                            orderedItemAdapter = CartRecyclerAdapter(
                                context,
                                orderItemsPerRestaurant
                            )

                            holder.recyclerViewItemsOrdered.adapter =
                                orderedItemAdapter

                            holder.recyclerViewItemsOrdered.layoutManager = layoutManager


                        }
                    },
                    Response.ErrorListener {
                        Toast.makeText(
                            context,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "e168e28418726a"
                        return headers
                    }
                }

                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    context,
                    "Some Unexpected error occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }


    }
}