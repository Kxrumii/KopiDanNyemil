package lat.pam.kopidannyemil

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import kotlin.math.hypot
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var revealOverlay: View
    private lateinit var btnStart: Button

    @Suppress("DEPRECATION")
    private fun disableLegacyTransition() {
        overridePendingTransition(0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val root = findViewById<View>(R.id.main)
        val brandHeader = findViewById<View>(R.id.brandHeader)
        val welcomeContent = findViewById<View>(R.id.welcomeContent)
        revealOverlay = findViewById(R.id.revealOverlay)
        btnStart = findViewById(R.id.btnStart)

        val headerBasePadding = resources.getDimensionPixelSize(R.dimen.spacing_24)
        val contentBasePadding = resources.getDimensionPixelSize(R.dimen.spacing_24)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars(),
            )

            brandHeader.updatePadding(
                top = headerBasePadding + systemBars.top,
            )

            welcomeContent.updatePadding(
                bottom = contentBasePadding + systemBars.bottom,
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)

        btnStart.setOnClickListener {
            btnStart.isEnabled = false

            val buttonLocation = IntArray(2)
            btnStart.getLocationOnScreen(buttonLocation)
            val buttonCenterX = buttonLocation[0] + btnStart.width / 2
            val buttonCenterY = buttonLocation[1] + btnStart.height / 2

            val overlayLocation = IntArray(2)
            revealOverlay.getLocationOnScreen(overlayLocation)
            val centerX = (buttonCenterX - overlayLocation[0]).coerceIn(0, revealOverlay.width)
            val centerY = (buttonCenterY - overlayLocation[1]).coerceIn(0, revealOverlay.height)

            val furthestX = max(centerX, revealOverlay.width - centerX).toDouble()
            val furthestY = max(centerY, revealOverlay.height - centerY).toDouble()
            val finalRadius = hypot(furthestX, furthestY).toFloat()

            revealOverlay.visibility = View.VISIBLE

            val revealAnim = ViewAnimationUtils.createCircularReveal(
                revealOverlay,
                centerX,
                centerY,
                0f,
                finalRadius,
            )

            revealAnim.duration = 600L
            revealAnim.interpolator = DecelerateInterpolator()
            revealAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val intent = Intent(
                        this@MainActivity,
                        SignUpActivity::class.java,
                    )
                    startActivity(intent)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                    } else {
                        disableLegacyTransition()
                    }
                }
            })

            revealAnim.start()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::revealOverlay.isInitialized) {
            revealOverlay.visibility = View.INVISIBLE
        }
        if (::btnStart.isInitialized) {
            btnStart.isEnabled = true
        }
    }
}
