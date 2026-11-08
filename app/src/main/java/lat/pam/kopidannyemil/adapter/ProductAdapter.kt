package lat.pam.kopidannyemil.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import lat.pam.kopidannyemil.R
import lat.pam.kopidannyemil.data.Product
import lat.pam.kopidannyemil.util.PriceFormatter

class ProductAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product, ImageView) -> Unit,
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val ivImage =
            itemView.findViewById<ImageView>(R.id.ivProductImage)

        private val tvEmoji =
            itemView.findViewById<TextView>(R.id.tvProductEmoji)

        private val tvName =
            itemView.findViewById<TextView>(R.id.tvProductName)

        private val tvDescription =
            itemView.findViewById<TextView>(R.id.tvProductDescription)

        private val tvPrice =
            itemView.findViewById<TextView>(R.id.tvProductPrice)

        private val btnAdd =
            itemView.findViewById<ImageButton>(R.id.btnAddProduct)

        fun bind(product: Product) {
            tvName.text = product.name
            tvDescription.text = product.description
            tvPrice.text = PriceFormatter.format(product.price)

            if (product.imageResId != 0) {
                ivImage.visibility = View.VISIBLE
                ivImage.setImageResource(product.imageResId)
                ivImage.clipToOutline = true
                tvEmoji.visibility = View.GONE
            } else {
                ivImage.visibility = View.GONE
                tvEmoji.visibility = View.VISIBLE
                tvEmoji.text = product.emoji
            }

            itemView.setOnClickListener {
                animateAddButton()
                onProductClick(product, ivImage)
            }

            btnAdd.setOnClickListener {
                animateAddButton()
                onProductClick(product, ivImage)
            }
        }

        private fun animateAddButton() {
            btnAdd.animate().cancel()

            btnAdd.scaleX = 1f
            btnAdd.scaleY = 1f

            btnAdd.animate()
                .scaleX(0.82f)
                .scaleY(0.82f)
                .setDuration(80)
                .withEndAction {
                    btnAdd.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220)
                        .setInterpolator(OvershootInterpolator(3f))
                        .start()
                }
                .start()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)

        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int,
    ) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}