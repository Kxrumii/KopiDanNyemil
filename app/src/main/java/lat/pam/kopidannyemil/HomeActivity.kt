package lat.pam.kopidannyemil

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lat.pam.kopidannyemil.adapter.ProductAdapter
import lat.pam.kopidannyemil.data.CartManager
import lat.pam.kopidannyemil.data.Product
import lat.pam.kopidannyemil.data.ProductCatalog

class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: ProductAdapter
    private lateinit var preferences: SharedPreferences
    private lateinit var cartManager: CartManager
    private lateinit var activeUsername: String

    private var allProducts: List<Product> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        preferences = getSharedPreferences(
            "kopi_dan_nyemil",
            MODE_PRIVATE
        )

        activeUsername = preferences.getString(
            "current_username",
            null
        ).orEmpty()

        if (activeUsername.isBlank()) {
            redirectToWelcome()
            return
        }

        cartManager = CartManager(
            preferences,
            activeUsername
        )

        val root = findViewById<View>(R.id.main)
        val topBar = findViewById<View>(R.id.topBar)
        val cartContainer = findViewById<View>(
            R.id.cartButtonContainer
        )

        val topBarBasePadding = resources.getDimensionPixelSize(
            R.dimen.spacing_16
        )

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            topBar.updatePadding(
                top = topBarBasePadding + systemBars.top
            )

            cartContainer.setPadding(
                cartContainer.paddingLeft,
                cartContainer.paddingTop,
                cartContainer.paddingRight,
                systemBars.bottom
            )

            insets
        }

        val fullName = preferences.getString(
            "current_full_name",
            "Pelanggan"
        ) ?: "Pelanggan"

        findViewById<TextView>(R.id.tvGreeting).text =
            getString(
                R.string.home_greeting,
                fullName
            )

        allProducts = ProductCatalog.products

        val rvProducts = findViewById<RecyclerView>(
            R.id.rvProducts
        )

        rvProducts.layoutManager = LinearLayoutManager(this)

        adapter = ProductAdapter(allProducts) { product, sourceImage ->
            cartManager.addProduct(product.id)

            updateCartBadge(animate = true)
            animateProductToCart(sourceImage)

            Toast.makeText(
                this,
                getString(
                    R.string.product_added_to_cart,
                    product.name
                ),
                Toast.LENGTH_SHORT
            ).show()
        }

        rvProducts.adapter = adapter

        setupCategoryFilters()

        findViewById<View>(R.id.ivProfile).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )
        }

        val deliveryRow = findViewById<View>(
            R.id.rowDeliveryLocation
        )

        deliveryRow.setOnClickListener {
            openOrder(
                OrderActivity.STEP_ADDRESS,
                deliveryRow
            )
        }

        val btnCart = findViewById<View>(
            R.id.btnCart
        )

        btnCart.setOnClickListener {
            openOrder(
                OrderActivity.STEP_CART,
                btnCart
            )
        }

        updateCartBadge()
        updateDeliveryAddress()
    }

    private fun setupCategoryFilters() {
        val btnCatAll = findViewById<TextView>(R.id.btnCatAll)
        val btnCatCoffee = findViewById<TextView>(R.id.btnCatCoffee)
        val btnCatNonCoffee = findViewById<TextView>(R.id.btnCatNonCoffee)
        val btnCatSnacks = findViewById<TextView>(R.id.btnCatSnacks)
        val btnCatBakery = findViewById<TextView>(R.id.btnCatBakery)

        val categoryButtons = listOf(
            btnCatAll to "Semua",
            btnCatCoffee to "Coffee",
            btnCatNonCoffee to "Non-Coffee",
            btnCatSnacks to "Snacks",
            btnCatBakery to "Bakery"
        )

        fun selectCategory(
            selectedButton: TextView,
            categoryName: String
        ) {
            categoryButtons.forEach { (button, _) ->
                if (button == selectedButton) {
                    button.setBackgroundResource(
                        R.drawable.bg_category_chip_selected
                    )

                    button.setTextColor(
                        getColor(R.color.pure_white)
                    )
                } else {
                    button.setBackgroundResource(
                        R.drawable.bg_category_chip_unselected
                    )

                    button.setTextColor(
                        getColor(R.color.espresso)
                    )
                }
            }

            val filteredList = if (categoryName == "Semua") {
                allProducts
            } else {
                allProducts.filter {
                    it.category.equals(
                        categoryName,
                        ignoreCase = true
                    )
                }
            }

            adapter.updateData(filteredList)
        }

        categoryButtons.forEach { (button, categoryName) ->
            button.setOnClickListener {
                selectCategory(button, categoryName)
            }
        }
    }

    private fun animateProductToCart(sourceImage: ImageView) {
        if (
            !sourceImage.isAttachedToWindow ||
            sourceImage.visibility != View.VISIBLE ||
            sourceImage.width <= 0 ||
            sourceImage.height <= 0
        ) {
            return
        }

        val root = findViewById<ViewGroup>(R.id.main)
        val cartButton = findViewById<View>(R.id.btnCart)
        val drawable = sourceImage.drawable ?: return

        val rootLocation = IntArray(2)
        val sourceLocation = IntArray(2)
        val targetLocation = IntArray(2)

        root.getLocationOnScreen(rootLocation)
        sourceImage.getLocationOnScreen(sourceLocation)
        cartButton.getLocationOnScreen(targetLocation)

        val size = minOf(
            sourceImage.width,
            sourceImage.height
        )

        val overlay = ImageView(this)

        val imageDrawable = drawable.constantState
            ?.newDrawable()
            ?.mutate() ?: drawable

        overlay.setImageDrawable(imageDrawable)

        overlay.background = sourceImage.background
            ?.constantState
            ?.newDrawable()
            ?.mutate()

        overlay.scaleType = sourceImage.scaleType
        overlay.clipToOutline = sourceImage.clipToOutline
        overlay.elevation = 16f * resources.displayMetrics.density

        root.addView(
            overlay,
            ViewGroup.LayoutParams(size, size)
        )

        val startX = (
                sourceLocation[0] - rootLocation[0]
                ).toFloat()

        val startY = (
                sourceLocation[1] - rootLocation[1]
                ).toFloat()

        val endX = (
                targetLocation[0] - rootLocation[0] +
                        cartButton.width / 2f - size / 2f
                )

        val endY = (
                targetLocation[1] - rootLocation[1] +
                        cartButton.height / 2f - size / 2f
                )

        overlay.translationX = startX
        overlay.translationY = startY

        val animation = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(
                    overlay,
                    View.TRANSLATION_X,
                    startX,
                    endX
                ),
                ObjectAnimator.ofFloat(
                    overlay,
                    View.TRANSLATION_Y,
                    startY,
                    endY
                ),
                ObjectAnimator.ofFloat(
                    overlay,
                    View.SCALE_X,
                    1f,
                    0.2f
                ),
                ObjectAnimator.ofFloat(
                    overlay,
                    View.SCALE_Y,
                    1f,
                    0.2f
                ),
                ObjectAnimator.ofFloat(
                    overlay,
                    View.ALPHA,
                    1f,
                    0.35f
                )
            )

            duration = 420L
            interpolator = AccelerateInterpolator()

            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        root.removeView(overlay)
                    }
                }
            )
        }

        animation.start()
    }

    private fun openOrder(
        step: String,
        originView: View,
    ) {
        val intent = Intent(
            this,
            OrderActivity::class.java
        ).apply {
            putExtra(
                OrderActivity.EXTRA_STEP,
                step
            )
        }

        if (
            originView.isLaidOut &&
            originView.width > 0 &&
            originView.height > 0
        ) {
            val options = ActivityOptions.makeClipRevealAnimation(
                originView,
                0,
                0,
                originView.width,
                originView.height
            )

            startActivity(
                intent,
                options.toBundle()
            )
        } else {
            startActivity(intent)
        }
    }

    private fun updateCartBadge(animate: Boolean = false) {
        val badge = findViewById<TextView>(
            R.id.tvCartBadge
        )

        val totalQuantity = cartManager.getTotalQuantity()

        if (totalQuantity == 0) {
            badge.animate().cancel()
            badge.scaleX = 1f
            badge.scaleY = 1f
            badge.visibility = View.GONE
            return
        }

        val wasVisible = badge.visibility == View.VISIBLE

        badge.text = if (totalQuantity > 99) {
            "99+"
        } else {
            totalQuantity.toString()
        }

        badge.visibility = View.VISIBLE

        if (animate) {
            badge.animate().cancel()

            badge.scaleX = if (wasVisible) 0.75f else 0.5f
            badge.scaleY = if (wasVisible) 0.75f else 0.5f

            badge.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260)
                .setInterpolator(OvershootInterpolator(2.5f))
                .start()
        } else {
            badge.scaleX = 1f
            badge.scaleY = 1f
        }
    }

    private fun updateDeliveryAddress() {
        val savedAddress = preferences.getString(
            "account_${activeUsername}_delivery_address",
            null
        ).orEmpty()

        findViewById<TextView>(R.id.tvDeliveryAddress).text =
            savedAddress.ifBlank {
                getString(R.string.delivery_placeholder)
            }
    }

    private fun redirectToWelcome() {
        val intent = Intent(
            this,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()

        if (
            !::preferences.isInitialized ||
            !::cartManager.isInitialized
        ) {
            return
        }

        val fullName = preferences.getString(
            "current_full_name",
            "Pelanggan"
        ) ?: "Pelanggan"

        findViewById<TextView>(R.id.tvGreeting)?.text =
            getString(
                R.string.home_greeting,
                fullName
            )

        updateCartBadge()
        updateDeliveryAddress()
    }
}