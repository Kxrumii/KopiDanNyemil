package lat.pam.kopidannyemil.data

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Long,
    val emoji: String,
    val imageResId: Int = 0,
    val category: String = "Coffee",
)