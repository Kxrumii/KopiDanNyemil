package lat.pam.kopidannyemil.data

import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class CartManager(
    private val preferences: SharedPreferences,
    username: String,
) {

    private val cartKey = "cart_$username"

    fun getItems(): List<CartItem> {
        val savedCart = preferences.getString(cartKey, null)
            ?: return emptyList()

        return try {
            val jsonArray = JSONArray(savedCart)
            val items = mutableListOf<CartItem>()

            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(index) ?: continue
                val productId = item.optInt("productId", -1)
                val quantity = item.optInt("quantity", 0)

                if (productId > 0 && quantity > 0) {
                    items.add(
                        CartItem(
                            productId = productId,
                            quantity = quantity,
                        )
                    )
                }
            }

            items
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addProduct(productId: Int) {
        val items = getItems().toMutableList()
        val existingIndex = items.indexOfFirst {
            it.productId == productId
        }

        if (existingIndex >= 0) {
            val existingItem = items[existingIndex]

            items[existingIndex] = existingItem.copy(
                quantity = existingItem.quantity + 1,
            )
        } else {
            items.add(
                CartItem(
                    productId = productId,
                    quantity = 1,
                )
            )
        }

        saveItems(items)
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeProduct(productId)
            return
        }

        val items = getItems().toMutableList()
        val existingIndex = items.indexOfFirst {
            it.productId == productId
        }

        if (existingIndex >= 0) {
            items[existingIndex] = items[existingIndex].copy(
                quantity = quantity,
            )

            saveItems(items)
        }
    }

    fun removeProduct(productId: Int) {
        val items = getItems().filterNot {
            it.productId == productId
        }

        saveItems(items)
    }

    fun clear() {
        preferences.edit {
            remove(cartKey)
        }
    }

    fun getTotalQuantity(): Int {
        return getItems().sumOf { it.quantity }
    }

    fun getSubtotal(products: List<Product>): Long {
        return getItems().sumOf { item ->
            val product = products.firstOrNull {
                it.id == item.productId
            }

            (product?.price ?: 0L) * item.quantity.toLong()
        }
    }

    private fun saveItems(items: List<CartItem>) {
        val jsonArray = JSONArray()

        items.forEach { item ->
            jsonArray.put(
                JSONObject().apply {
                    put("productId", item.productId)
                    put("quantity", item.quantity)
                }
            )
        }

        preferences.edit {
            putString(cartKey, jsonArray.toString())
        }
    }
}