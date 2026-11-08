package lat.pam.kopidannyemil.data

import lat.pam.kopidannyemil.R

object ProductCatalog {

    val products = listOf(
        Product(
            id = 1,
            name = "Kopi Susu Gula Aren",
            description = "Espresso, susu, gula aren",
            price = 22000L,
            emoji = "☕",
            imageResId = R.drawable.product_kopi_susu_aren,
            category = "Coffee",
        ),
        Product(
            id = 2,
            name = "Cappuccino",
            description = "Espresso dengan foam susu lembut",
            price = 25000L,
            emoji = "☕",
            imageResId = R.drawable.product_cappuccino,
            category = "Coffee",
        ),
        Product(
            id = 3,
            name = "Matcha Latte",
            description = "Matcha Jepang dengan susu segar",
            price = 27000L,
            emoji = "🍵",
            imageResId = R.drawable.product_matcha_latte,
            category = "Non-Coffee",
        ),
        Product(
            id = 4,
            name = "Donat Cokelat",
            description = "Donat empuk dengan glaze cokelat",
            price = 12000L,
            emoji = "🍩",
            imageResId = R.drawable.product_donat_cokelat,
            category = "Snacks",
        ),
        Product(
            id = 5,
            name = "Sourdough Loaf",
            description = "Roti fermentasi alami",
            price = 35000L,
            emoji = "🍞",
            imageResId = R.drawable.product_sourdough,
            category = "Bakery",
        ),
    )
}