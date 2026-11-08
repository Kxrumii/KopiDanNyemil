package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import lat.pam.kopidannyemil.util.PriceFormatter
import org.json.JSONObject

class ConfirmationActivity : AppCompatActivity() {

    private lateinit var activeUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmation)

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val preferences = getSharedPreferences(
            "kopi_dan_nyemil",
            MODE_PRIVATE
        )

        activeUsername = preferences.getString(
            "current_username",
            null
        ).orEmpty()

        if (activeUsername.isBlank()) {
            goToHome()
            return
        }

        val savedSnapshot = preferences.getString(
            "last_order_$activeUsername",
            null
        )

        if (savedSnapshot.isNullOrBlank()) {
            goToHome()
            return
        }

        val snapshot = try {
            JSONObject(savedSnapshot)
        } catch (_: Exception) {
            goToHome()
            return
        }

        val expectedOrderNumber = intent.getStringExtra(
            EXTRA_ORDER_NUMBER
        ).orEmpty()

        val savedOrderNumber = snapshot.optString(
            "orderNumber"
        )

        if (
            savedOrderNumber.isBlank() ||
            savedOrderNumber != expectedOrderNumber ||
            snapshot.optJSONArray("items") == null
        ) {
            goToHome()
            return
        }

        setupWindowInsets()
        displayOrder(snapshot)
        setupNavigation()
    }

    private fun setupWindowInsets() {
        val root = findViewById<View>(R.id.main)
        val meta = findViewById<View>(R.id.tvConfirmationMeta)
        val actionBar = findViewById<View>(
            R.id.confirmationActionBar
        )

        val topBasePadding = resources.getDimensionPixelSize(
            R.dimen.spacing_16
        )

        val bottomBasePadding = resources.getDimensionPixelSize(
            R.dimen.spacing_16
        )

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            meta.updatePadding(
                top = topBasePadding + systemBars.top
            )

            actionBar.updatePadding(
                bottom = bottomBasePadding + systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)
    }

    private fun displayOrder(snapshot: JSONObject) {
        findViewById<TextView>(
            R.id.tvConfirmationOrderNumber
        ).text = snapshot.optString("orderNumber")

        val items = snapshot.optJSONArray("items")
        val itemLines = mutableListOf<String>()

        if (items != null) {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue

                val name = item.optString("name")
                val quantity = item.optInt("quantity")
                val lineTotal = item.optLong("lineTotal")

                if (name.isNotBlank() && quantity > 0) {
                    itemLines.add(
                        "$name × $quantity\n${PriceFormatter.format(lineTotal)}"
                    )
                }
            }
        }

        findViewById<TextView>(
            R.id.tvConfirmationItems
        ).text = itemLines.joinToString("\n\n")

        findViewById<TextView>(
            R.id.tvConfirmationSubtotal
        ).text = PriceFormatter.format(
            snapshot.optLong("subtotal")
        )

        findViewById<TextView>(
            R.id.tvConfirmationDeliveryFee
        ).text = PriceFormatter.format(
            snapshot.optLong("deliveryFee")
        )

        findViewById<TextView>(
            R.id.tvConfirmationTotal
        ).text = PriceFormatter.format(
            snapshot.optLong("total")
        )

        findViewById<TextView>(
            R.id.tvConfirmationRecipient
        ).text = snapshot.optString("recipientName")

        findViewById<TextView>(
            R.id.tvConfirmationAddress
        ).text = snapshot.optString("deliveryAddress")

        val landmark = snapshot.optString("landmark")

        val tvLandmark = findViewById<TextView>(
            R.id.tvConfirmationLandmark
        )

        if (landmark.isBlank()) {
            tvLandmark.visibility = View.GONE
        } else {
            tvLandmark.text = getString(
                R.string.confirmation_landmark_format,
                landmark
            )

            tvLandmark.visibility = View.VISIBLE
        }
    }

    private fun setupNavigation() {
        findViewById<View>(
            R.id.btnConfirmationHome
        ).setOnClickListener {
            goToHome()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goToHome()
                }
            }
        )
    }

    private fun goToHome() {
        val intent = Intent(
            this,
            HomeActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_ORDER_NUMBER = "confirmation_order_number"
    }
}