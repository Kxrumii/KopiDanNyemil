package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

class SignUpActivity : AppCompatActivity() {

    private fun prepareStaggeredViews(
        heroCoffee: View,
        organicWave: View,
        headlineContainer: View,
        description: View,
        btnRegister: View,
        loginRow: View,
    ) {
        heroCoffee.alpha = 0f
        heroCoffee.scaleX = 0.94f
        heroCoffee.scaleY = 0.94f

        val density = resources.displayMetrics.density

        organicWave.alpha = 0f
        organicWave.translationY = 24f * density

        headlineContainer.alpha = 0f
        headlineContainer.translationY = 20f * density

        description.alpha = 0f
        description.translationY = 16f * density

        btnRegister.alpha = 0f
        btnRegister.translationY = 16f * density

        loginRow.alpha = 0f
        loginRow.translationY = 16f * density
    }

    private fun playStaggeredEntrance(
        heroCoffee: View,
        organicWave: View,
        headlineContainer: View,
        description: View,
        btnRegister: View,
        loginRow: View,
    ) {
        val interpolator = DecelerateInterpolator()

        heroCoffee.animate()
            .alpha(1f)
            .scaleX(1.06f)
            .scaleY(1.06f)
            .setDuration(400L)
            .setInterpolator(interpolator)
            .start()

        organicWave.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400L)
            .setStartDelay(80L)
            .setInterpolator(interpolator)
            .start()

        headlineContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(380L)
            .setStartDelay(160L)
            .setInterpolator(interpolator)
            .start()

        description.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(350L)
            .setStartDelay(240L)
            .setInterpolator(interpolator)
            .start()

        btnRegister.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(350L)
            .setStartDelay(320L)
            .setInterpolator(interpolator)
            .start()

        loginRow.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(350L)
            .setStartDelay(380L)
            .setInterpolator(interpolator)
            .start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = findViewById<View>(R.id.main)
        val topBar = findViewById<View>(R.id.topBar)
        val signupContent = findViewById<View>(R.id.signupContent)

        val topBarBasePadding = resources.getDimensionPixelSize(R.dimen.spacing_16)
        val contentBasePaddingBottom = resources.getDimensionPixelSize(R.dimen.spacing_32)

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            topBar.updatePadding(top = topBarBasePadding + systemBars.top)
            signupContent.updatePadding(bottom = contentBasePaddingBottom + systemBars.bottom)
            insets
        }

        val heroCoffee = findViewById<View>(R.id.ivSignupCoffee)
        val organicWave = findViewById<View>(R.id.ivSignupOrganicWave)
        val headlineContainer = findViewById<View>(R.id.headlineContainer)
        val description = findViewById<View>(R.id.tvSignupDescription)
        val btnRegister = findViewById<View>(R.id.btnRegister)
        val loginRow = findViewById<View>(R.id.loginRow)
        val btnLogin = findViewById<View>(R.id.btnLogin)

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        prepareStaggeredViews(
            heroCoffee,
            organicWave,
            headlineContainer,
            description,
            btnRegister,
            loginRow,
        )

        root.post {
            playStaggeredEntrance(
                heroCoffee,
                organicWave,
                headlineContainer,
                description,
                btnRegister,
                loginRow,
            )
        }
    }
}
