
package lat.pam.kopidannyemil

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

class ProfileActivity : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences
    private lateinit var activeUsername: String
    private lateinit var etFullName: EditText
    private lateinit var tvProfileName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

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

        val accountKey = "account_$activeUsername"

        if (!preferences.contains("${accountKey}_username")) {
            preferences.edit {
                remove("current_username")
                remove("current_full_name")
            }

            redirectToWelcome()
            return
        }

        loadProfileData()
        setupWindowInsets()

        val profileContent = findViewById<View>(
            R.id.profileContent
        )

        profileContent.isFocusableInTouchMode = true
        profileContent.requestFocus()

        val profileScroll =
            findViewById<androidx.core.widget.NestedScrollView>(
                R.id.profileScroll
            )

        profileScroll.post {
            profileScroll.scrollTo(0, 0)
        }

        setupBackButton()
        setupSaveChanges()
        setupLogout()
    }

    private fun loadProfileData() {
        val accountKey = "account_$activeUsername"

        val fullName = preferences.getString(
            "${accountKey}_full_name",
            "Pelanggan"
        ) ?: "Pelanggan"

        val username = preferences.getString(
            "${accountKey}_username",
            activeUsername
        ) ?: activeUsername

        tvProfileName = findViewById(R.id.tvProfileName)
        etFullName = findViewById(R.id.etProfileFullName)

        val tvProfileUsername =
            findViewById<TextView>(R.id.tvProfileUsername)

        val tvProfileUsernameValue =
            findViewById<TextView>(R.id.tvProfileUsernameValue)

        tvProfileName.text = fullName
        etFullName.setText(fullName)

        tvProfileUsername.text = "@$username"
        tvProfileUsernameValue.text = username
    }

    private fun setupBackButton() {
        findViewById<View>(R.id.btnProfileBack).setOnClickListener {
            finish()
        }
    }

    private fun setupSaveChanges() {
        findViewById<View>(R.id.btnSaveProfile).setOnClickListener {
            val newFullName = etFullName.text.toString().trim()

            if (newFullName.isBlank()) {
                etFullName.error = getString(
                    R.string.profile_error_name_required
                )
                etFullName.requestFocus()
                return@setOnClickListener
            }

            preferences.edit {
                putString(
                    "account_${activeUsername}_full_name",
                    newFullName
                )
                putString("current_full_name", newFullName)
            }

            tvProfileName.text = newFullName
            etFullName.error = null

            Toast.makeText(
                this,
                getString(R.string.profile_save_success),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupLogout() {
        findViewById<View>(R.id.btnProfileLogout).setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.profile_logout_title)
            .setMessage(R.string.profile_logout_message)
            .setNegativeButton(R.string.profile_logout_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.profile_logout_confirm) { _, _ ->
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        preferences.edit {
            remove("current_username")
            remove("current_full_name")
        }

        Toast.makeText(
            this,
            getString(R.string.profile_logout_success),
            Toast.LENGTH_SHORT
        ).show()

        redirectToWelcome()
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

    private fun setupWindowInsets() {
        val contentRoot = findViewById<View>(
            android.R.id.content
        )

        val topBar = findViewById<View>(R.id.topBar)
        val profileContent = findViewById<View>(R.id.profileContent)

        val topBarBasePadding = resources.getDimensionPixelSize(
            R.dimen.spacing_16
        )

        val contentBasePaddingBottom = resources.getDimensionPixelSize(
            R.dimen.spacing_32
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            contentRoot
        ) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            topBar.updatePadding(
                top = topBarBasePadding + systemBars.top
            )

            profileContent.updatePadding(
                bottom = contentBasePaddingBottom + systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(contentRoot)
    }
}
