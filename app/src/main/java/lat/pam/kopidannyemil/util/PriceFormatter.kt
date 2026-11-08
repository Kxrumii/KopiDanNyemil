package lat.pam.kopidannyemil.util

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {
    fun format(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(
            Locale("id", "ID")
        )

        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0

        return "Rp ${formatter.format(amount)}"
    }
}