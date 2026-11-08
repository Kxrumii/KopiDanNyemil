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

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = findViewById<View>(R.id.main)
        val topBar = findViewById<View>(R.id.topBar)
        val registerContent = findViewById<View>(R.id.registerContent)
        val registerScroll = findViewById<ScrollView>(R.id.registerScroll)

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

            registerContent.updatePadding(
                bottom = contentBasePaddingBottom + bottomInset
            )

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                scrollFocusedFieldIntoView(registerScroll)
            }

            insets
        }

        ViewCompat.requestApplyInsets(root)

        findViewById<View>(R.id.btnRegisterBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.tvRegisterLogin).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )
        }

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnTogglePassword = findViewById<ImageButton>(R.id.btnTogglePassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

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

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            when {
                fullName.isBlank() -> {
                    etFullName.error = getString(
                        R.string.error_full_name_required
                    )

                    etFullName.requestFocus()
                }

                username.isBlank() -> {
                    etUsername.error = getString(
                        R.string.error_username_required
                    )

                    etUsername.requestFocus()
                }

                username.length < 4 -> {
                    etUsername.error = getString(
                        R.string.error_username_short
                    )

                    etUsername.requestFocus()
                }

                password.isBlank() -> {
                    etPassword.error = getString(
                        R.string.error_password_required
                    )

                    etPassword.requestFocus()
                }

                password.length < 6 -> {
                    etPassword.error = getString(
                        R.string.error_password_short
                    )

                    etPassword.requestFocus()
                }

                else -> {
                    val preferences = getSharedPreferences(
                        "kopi_dan_nyemil",
                        MODE_PRIVATE
                    )

                    val accountKey = "account_$username"

                    if (preferences.contains("${accountKey}_username")) {
                        etUsername.error = "Username sudah digunakan."
                        etUsername.requestFocus()
                    } else {
                        preferences.edit {
                            putString(
                                "${accountKey}_full_name",
                                fullName
                            )

                            putString(
                                "${accountKey}_username",
                                username
                            )

                            putString(
                                "${accountKey}_password",
                                password
                            )
                        }

                        Toast.makeText(
                            this,
                            getString(R.string.register_success),
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )

                        finish()
                    }
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