package lat.pam.kopidannyemil

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = findViewById<View>(R.id.main)
        val topBar = findViewById<View>(R.id.topBar)
        val loginContent = findViewById<View>(R.id.loginContent)
        val loginScroll = findViewById<ScrollView>(R.id.loginScroll)

        val topBarBasePadding = resources.getDimensionPixelSize(
            R.dimen.spacing_16
        )

        val contentBasePaddingBottom = resources.getDimensionPixelSize(
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
                top = topBarBasePadding + systemBars.top
            )

            loginContent.updatePadding(
                bottom = contentBasePaddingBottom + bottomInset
            )

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                scrollFocusedFieldIntoView(loginScroll)
            }

            insets
        }

        ViewCompat.requestApplyInsets(root)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.tvRegisterLink).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePassword = findViewById<ImageButton>(R.id.btnTogglePassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        var isPasswordVisible = false

        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            val selection = etPassword.selectionEnd
            val typeface = etPassword.typeface

            if (isPasswordVisible) {
                etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()

                btnTogglePassword.setImageResource(
                    R.drawable.ic_eye
                )
            } else {
                etPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()

                btnTogglePassword.setImageResource(
                    R.drawable.ic_eye_off
                )
            }

            etPassword.typeface = typeface

            if (selection >= 0) {
                etPassword.setSelection(
                    selection.coerceAtMost(etPassword.text.length)
                )
            }
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            val preferences = getSharedPreferences(
                "kopi_dan_nyemil",
                MODE_PRIVATE
            )

            val accountKey = "account_$username"

            val savedPassword = preferences.getString(
                "${accountKey}_password",
                null
            )

            val savedFullName = preferences.getString(
                "${accountKey}_full_name",
                null
            )

            when {
                username.isBlank() -> {
                    etUsername.error = getString(
                        R.string.error_username_required
                    )

                    etUsername.requestFocus()
                }

                password.isBlank() -> {
                    etPassword.error = getString(
                        R.string.error_password_required
                    )

                    etPassword.requestFocus()
                }

                savedPassword == null -> {
                    Toast.makeText(
                        this,
                        getString(R.string.login_no_account),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                password == savedPassword -> {
                    preferences.edit {
                        putString("current_username", username)
                        putString(
                            "current_full_name",
                            savedFullName ?: username
                        )
                    }

                    val intent = Intent(
                        this,
                        HomeActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                }

                else -> {
                    Toast.makeText(
                        this,
                        getString(R.string.login_invalid_credentials),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun scrollFocusedFieldIntoView(scrollView: ScrollView) {
        scrollView.post {
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
}