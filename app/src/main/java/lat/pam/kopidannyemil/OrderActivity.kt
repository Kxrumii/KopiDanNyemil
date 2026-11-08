package lat.pam.kopidannyemil

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import lat.pam.kopidannyemil.adapter.CartAdapter
import lat.pam.kopidannyemil.data.CartEntry
import lat.pam.kopidannyemil.data.CartManager
import lat.pam.kopidannyemil.data.ProductCatalog
import lat.pam.kopidannyemil.util.PriceFormatter
import org.json.JSONArray
import org.json.JSONObject

class OrderActivity : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences
    private lateinit var cartManager: CartManager
    private lateinit var cartAdapter: CartAdapter
    private lateinit var activeUsername: String

    private lateinit var etRecipientName: EditText
    private lateinit var etDeliveryAddress: EditText
    private lateinit var etLandmark: EditText

    private var currentStep = STEP_CART
    private var hasInitializedStep = false
    private var isStepTransitionRunning = false

    private var stepTransitionAnimator: AnimatorSet? = null

    private val allProducts = ProductCatalog.products

    private val amountAnimators = mutableMapOf<Int, ValueAnimator>()
    private val displayedAmounts = mutableMapOf<Int, Long>()
    private val visibilityTargets = mutableMapOf<Int, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setContentView(R.layout.activity_order)

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

        setupWindowInsets()
        setupCartRecyclerView()
        setupAddressFields()
        setupNavigation()

        val requestedStep = intent.getStringExtra(EXTRA_STEP)

        showStep(
            if (requestedStep == STEP_ADDRESS) {
                STEP_ADDRESS
            } else {
                STEP_CART
            },
            animate = false
        )
    }

    private fun setupWindowInsets() {
        val root = findViewById<View>(R.id.main)
        val topBar = findViewById<View>(R.id.orderTopBar)
        val actionBar = findViewById<View>(R.id.orderActionBar)
        val addressContent = findViewById<View>(R.id.addressContent)

        val orderScroll = findViewById<NestedScrollView>(
            R.id.orderScroll
        )

        val topBasePadding = resources.getDimensionPixelSize(
            R.dimen.spacing_16
        )

        val bottomBasePadding = resources.getDimensionPixelSize(
            R.dimen.spacing_16
        )

        val addressBasePadding = resources.getDimensionPixelSize(
            R.dimen.spacing_32
        )

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val imeInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )

            val bottomInset = maxOf(
                systemBars.bottom,
                imeInsets.bottom
            )

            topBar.updatePadding(
                top = topBasePadding + systemBars.top
            )

            actionBar.updatePadding(
                bottom = bottomBasePadding + systemBars.bottom
            )

            addressContent.updatePadding(
                bottom = addressBasePadding + bottomInset
            )

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                orderScroll.post {
                    val focusedView = currentFocus ?: return@post

                    if (focusedView.width <= 0 || focusedView.height <= 0) {
                        return@post
                    }

                    focusedView.requestRectangleOnScreen(
                        Rect(
                            0,
                            0,
                            focusedView.width,
                            focusedView.height
                        ),
                        true
                    )
                }
            }

            insets
        }

        ViewCompat.requestApplyInsets(root)
    }

    private fun setupCartRecyclerView() {
        val rvCartItems = findViewById<RecyclerView>(
            R.id.rvCartItems
        )

        cartAdapter = CartAdapter(
            entries = emptyList(),
            onQuantityChange = { entry, quantity ->
                cartManager.updateQuantity(
                    entry.product.id,
                    quantity
                )

                refreshCart(animateChanges = true)
            },
            onRemove = { entry ->
                cartManager.removeProduct(
                    entry.product.id
                )

                refreshCart(animateChanges = true)
            }
        )

        rvCartItems.layoutManager = LinearLayoutManager(this)
        rvCartItems.adapter = cartAdapter
        rvCartItems.itemAnimator = null
    }

    private fun setupAddressFields() {
        etRecipientName = findViewById(
            R.id.etOrderRecipientName
        )

        etDeliveryAddress = findViewById(
            R.id.etOrderDeliveryAddress
        )

        etLandmark = findViewById(
            R.id.etOrderLandmark
        )

        val accountKey = "account_$activeUsername"

        etRecipientName.setText(
            preferences.getString(
                "${accountKey}_recipient_name",
                ""
            )
        )

        etDeliveryAddress.setText(
            preferences.getString(
                "${accountKey}_delivery_address",
                ""
            )
        )

        etLandmark.setText(
            preferences.getString(
                "${accountKey}_delivery_landmark",
                ""
            )
        )
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.btnOrderBack).setOnClickListener {
            handleBackNavigation()
        }

        findViewById<View>(R.id.btnOrderPrimary).setOnClickListener {
            if (isStepTransitionRunning) return@setOnClickListener

            if (currentStep == STEP_CART) {
                if (cartManager.getTotalQuantity() == 0) {
                    finish()
                } else {
                    showStep(STEP_ADDRESS)
                }
            } else {
                submitAddress()
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackNavigation()
                }
            }
        )
    }

    private fun handleBackNavigation() {
        if (isStepTransitionRunning) return

        if (currentStep == STEP_ADDRESS) {
            showStep(STEP_CART)
        } else {
            finish()
        }
    }

    private fun showStep(
        step: String,
        animate: Boolean = true,
    ) {
        if (isStepTransitionRunning) return

        val targetStep = if (step == STEP_ADDRESS) {
            STEP_ADDRESS
        } else {
            STEP_CART
        }

        val cartContent = findViewById<View>(R.id.cartContent)
        val addressContent = findViewById<View>(R.id.addressContent)

        val isCartStep = targetStep == STEP_CART

        updateStepHeader(isCartStep)

        if (!hasInitializedStep) {
            currentStep = targetStep
            hasInitializedStep = true

            cartContent.visibility =
                if (isCartStep) View.VISIBLE else View.GONE

            addressContent.visibility =
                if (isCartStep) View.GONE else View.VISIBLE

            resetContentProperties(cartContent)
            resetContentProperties(addressContent)

            refreshCart()
            updatePrimaryButton()
            resetOrderScroll()

            return
        }

        if (targetStep == currentStep) return

        hideKeyboard()

        val outgoingContent = if (isCartStep) {
            addressContent
        } else {
            cartContent
        }

        val incomingContent = if (isCartStep) {
            cartContent
        } else {
            addressContent
        }

        currentStep = targetStep

        refreshCart()
        updatePrimaryButton()

        if (!animate) {
            outgoingContent.visibility = View.GONE
            incomingContent.visibility = View.VISIBLE

            resetContentProperties(outgoingContent)
            resetContentProperties(incomingContent)

            updatePrimaryButton()
            resetOrderScroll()

            return
        }

        val density = resources.displayMetrics.density
        val distance = 24f * density
        val direction = if (isCartStep) -1f else 1f

        incomingContent.visibility = View.VISIBLE
        incomingContent.alpha = 0f
        incomingContent.translationX = direction * distance

        outgoingContent.alpha = 1f
        outgoingContent.translationX = 0f

        isStepTransitionRunning = true
        updatePrimaryButton()

        val animator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(
                    outgoingContent,
                    View.ALPHA,
                    1f,
                    0f
                ),
                ObjectAnimator.ofFloat(
                    outgoingContent,
                    View.TRANSLATION_X,
                    0f,
                    -direction * distance
                ),
                ObjectAnimator.ofFloat(
                    incomingContent,
                    View.ALPHA,
                    0f,
                    1f
                ),
                ObjectAnimator.ofFloat(
                    incomingContent,
                    View.TRANSLATION_X,
                    direction * distance,
                    0f
                )
            )

            duration = 300L
            interpolator = DecelerateInterpolator()

            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        outgoingContent.visibility = View.GONE

                        resetContentProperties(outgoingContent)
                        resetContentProperties(incomingContent)

                        isStepTransitionRunning = false
                        stepTransitionAnimator = null

                        updatePrimaryButton()
                    }
                }
            )
        }

        stepTransitionAnimator = animator
        animator.start()

        resetOrderScroll()
    }

    private fun updateStepHeader(isCartStep: Boolean) {
        findViewById<TextView>(R.id.tvOrderMetaSubtitle).text =
            getString(
                if (isCartStep) {
                    R.string.order_meta_cart
                } else {
                    R.string.order_meta_address
                }
            )
    }

    private fun resetContentProperties(view: View) {
        view.animate().cancel()
        view.alpha = 1f
        view.translationX = 0f
        view.translationY = 0f
        view.scaleX = 1f
        view.scaleY = 1f
    }

    private fun resetOrderScroll() {
        findViewById<NestedScrollView>(
            R.id.orderScroll
        ).post {
            findViewById<NestedScrollView>(
                R.id.orderScroll
            ).scrollTo(0, 0)
        }
    }

    private fun hideKeyboard() {
        currentFocus?.clearFocus()

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).hide(WindowInsetsCompat.Type.ime())
    }

    private fun refreshCart(animateChanges: Boolean = false) {
        val storedItems = cartManager.getItems()

        val invalidItems = storedItems.filter { item ->
            allProducts.none { product ->
                product.id == item.productId
            }
        }

        invalidItems.forEach { item ->
            cartManager.removeProduct(item.productId)
        }

        val entries = cartManager.getItems().mapNotNull { item ->
            val product = allProducts.firstOrNull {
                it.id == item.productId
            }

            product?.let {
                CartEntry(
                    product = it,
                    quantity = item.quantity
                )
            }
        }

        cartAdapter.updateData(entries)

        val isEmpty = entries.isEmpty()

        setAnimatedVisibility(
            findViewById(R.id.emptyCartState),
            isEmpty,
            animateChanges
        )

        setAnimatedVisibility(
            findViewById(R.id.rvCartItems),
            !isEmpty,
            animateChanges
        )

        setAnimatedVisibility(
            findViewById(R.id.orderSummary),
            !isEmpty,
            animateChanges
        )

        val subtotal = cartManager.getSubtotal(allProducts)

        val deliveryFee = if (isEmpty) {
            0L
        } else {
            DELIVERY_FEE
        }

        val total = subtotal + deliveryFee

        updateAnimatedAmount(
            R.id.tvOrderSubtotal,
            subtotal,
            animateChanges
        )

        updateAnimatedAmount(
            R.id.tvOrderDeliveryFee,
            deliveryFee,
            animateChanges
        )

        updateAnimatedAmount(
            R.id.tvOrderTotal,
            total,
            animateChanges
        )

        updateAnimatedAmount(
            R.id.tvOrderActionTotal,
            total,
            animateChanges
        )

        val addressSummary = getString(
            R.string.order_address_summary,
            cartManager.getTotalQuantity(),
            PriceFormatter.format(total)
        )

        updateSummaryText(
            findViewById(R.id.tvAddressCartSummary),
            addressSummary,
            animateChanges
        )

        updatePrimaryButton()
    }

    private fun updateAnimatedAmount(
        viewId: Int,
        newAmount: Long,
        animate: Boolean,
    ) {
        val textView = findViewById<TextView>(viewId)

        val previousAnimator = amountAnimators[viewId]
        val runningValue = previousAnimator?.animatedValue as? Int

        previousAnimator?.cancel()

        val previousAmount =
            runningValue?.toLong() ?: displayedAmounts[viewId]

        displayedAmounts[viewId] = newAmount

        if (
            !animate ||
            previousAmount == null ||
            previousAmount == newAmount
        ) {
            textView.text = PriceFormatter.format(newAmount)
            amountAnimators.remove(viewId)
            return
        }

        val animator = ValueAnimator.ofInt(
            previousAmount.toInt(),
            newAmount.toInt()
        )

        animator.duration = 320L
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val amount = (animation.animatedValue as Int).toLong()
            textView.text = PriceFormatter.format(amount)
        }

        animator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (amountAnimators[viewId] === animator) {
                        amountAnimators.remove(viewId)
                    }
                }
            }
        )

        amountAnimators[viewId] = animator
        animator.start()
    }

    private fun updateSummaryText(
        textView: TextView,
        newText: String,
        animate: Boolean,
    ) {
        if (textView.text.toString() == newText) return

        textView.animate().cancel()

        if (!animate || textView.visibility != View.VISIBLE) {
            textView.text = newText
            textView.alpha = 1f
            return
        }

        textView.animate()
            .alpha(0.25f)
            .setDuration(90L)
            .withEndAction {
                textView.text = newText

                textView.animate()
                    .alpha(1f)
                    .setDuration(160L)
                    .start()
            }
            .start()
    }

    private fun setAnimatedVisibility(
        view: View,
        shouldShow: Boolean,
        animate: Boolean,
    ) {
        val currentTarget = visibilityTargets[view.id]
            ?: (view.visibility == View.VISIBLE)

        if (currentTarget == shouldShow) return

        visibilityTargets[view.id] = shouldShow

        view.animate().cancel()

        if (shouldShow) {
            view.visibility = View.VISIBLE

            if (!animate) {
                resetContentProperties(view)
                return
            }

            val distance = 12f * resources.displayMetrics.density

            view.alpha = 0f
            view.translationY = distance
            view.scaleX = 0.98f
            view.scaleY = 0.98f

            view.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        } else {
            if (!animate || view.visibility != View.VISIBLE) {
                view.visibility = View.GONE
                resetContentProperties(view)
                return
            }

            val distance = -8f * resources.displayMetrics.density

            view.animate()
                .alpha(0f)
                .translationY(distance)
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(200L)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    if (visibilityTargets[view.id] == false) {
                        view.visibility = View.GONE
                        resetContentProperties(view)
                    }
                }
                .start()
        }
    }

    private fun updatePrimaryButton() {
        val totalQuantity = cartManager.getTotalQuantity()

        val buttonText = when {
            currentStep == STEP_CART && totalQuantity == 0 ->
                R.string.order_browse_menu

            currentStep == STEP_CART ->
                R.string.order_continue

            totalQuantity == 0 ->
                R.string.order_save_address

            else ->
                R.string.order_submit
        }

        val button = findViewById<TextView>(
            R.id.btnOrderPrimary
        )

        button.text = getString(buttonText)
        button.isEnabled = !isStepTransitionRunning
        button.alpha = if (isStepTransitionRunning) 0.7f else 1f
    }

    private fun submitAddress() {
        val recipientName = etRecipientName.text.toString().trim()
        val deliveryAddress = etDeliveryAddress.text.toString().trim()
        val landmark = etLandmark.text.toString().trim()

        if (recipientName.isBlank()) {
            etRecipientName.error = getString(
                R.string.order_recipient_error
            )
            etRecipientName.requestFocus()
            return
        }

        if (deliveryAddress.isBlank()) {
            etDeliveryAddress.error = getString(
                R.string.order_address_error
            )
            etDeliveryAddress.requestFocus()
            return
        }

        saveAddress(
            recipientName,
            deliveryAddress,
            landmark
        )

        if (cartManager.getTotalQuantity() == 0) {
            Toast.makeText(
                this,
                getString(R.string.order_address_saved),
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        val orderNumber = saveOrderSnapshot(
            recipientName,
            deliveryAddress,
            landmark
        )

        cartManager.clear()

        val intent = Intent(
            this,
            ConfirmationActivity::class.java
        ).apply {
            putExtra(
                ConfirmationActivity.EXTRA_ORDER_NUMBER,
                orderNumber
            )
        }

        startActivity(intent)
        finish()
    }

    private fun saveAddress(
        recipientName: String,
        deliveryAddress: String,
        landmark: String
    ) {
        val accountKey = "account_$activeUsername"

        preferences.edit {
            putString(
                "${accountKey}_recipient_name",
                recipientName
            )

            putString(
                "${accountKey}_delivery_address",
                deliveryAddress
            )

            putString(
                "${accountKey}_delivery_landmark",
                landmark
            )
        }
    }

    private fun saveOrderSnapshot(
        recipientName: String,
        deliveryAddress: String,
        landmark: String
    ): String {
        val orderNumber = "KN-${
            System.currentTimeMillis().toString().takeLast(8)
        }"

        val cartItems = cartManager.getItems()
        val itemsJson = JSONArray()

        cartItems.forEach { item ->
            val product = allProducts.firstOrNull {
                it.id == item.productId
            } ?: return@forEach

            itemsJson.put(
                JSONObject().apply {
                    put("productId", product.id)
                    put("name", product.name)
                    put("unitPrice", product.price)
                    put("quantity", item.quantity)
                    put(
                        "lineTotal",
                        product.price * item.quantity.toLong()
                    )
                }
            )
        }

        val subtotal = cartManager.getSubtotal(allProducts)
        val deliveryFee = DELIVERY_FEE

        val orderSnapshot = JSONObject().apply {
            put("orderNumber", orderNumber)
            put("createdAt", System.currentTimeMillis())
            put("items", itemsJson)
            put("recipientName", recipientName)
            put("deliveryAddress", deliveryAddress)
            put("landmark", landmark)
            put("subtotal", subtotal)
            put("deliveryFee", deliveryFee)
            put("total", subtotal + deliveryFee)
        }

        preferences.edit {
            putString(
                "last_order_$activeUsername",
                orderSnapshot.toString()
            )
        }

        return orderNumber
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

    override fun onDestroy() {
        stepTransitionAnimator?.cancel()

        amountAnimators.values.forEach {
            it.cancel()
        }

        amountAnimators.clear()

        super.onDestroy()
    }

    companion object {
        const val EXTRA_STEP = "order_step"
        const val STEP_CART = "cart"
        const val STEP_ADDRESS = "address"

        private const val DELIVERY_FEE = 8000L
    }
}