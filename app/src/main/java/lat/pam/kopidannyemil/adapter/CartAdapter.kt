package lat.pam.kopidannyemil.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import lat.pam.kopidannyemil.R
import lat.pam.kopidannyemil.data.CartEntry
import lat.pam.kopidannyemil.util.PriceFormatter

class CartAdapter(
    private var entries: List<CartEntry>,
    private val onQuantityChange: (CartEntry, Int) -> Unit,
    private val onRemove: (CartEntry) -> Unit,
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    companion object {
        private const val PAYLOAD_QUANTITY = "quantity"
    }

    inner class CartViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val ivImage =
            itemView.findViewById<ImageView>(R.id.ivCartProductImage)

        private val tvEmoji =
            itemView.findViewById<TextView>(R.id.tvCartProductEmoji)

        private val tvName =
            itemView.findViewById<TextView>(R.id.tvCartProductName)

        private val tvDescription =
            itemView.findViewById<TextView>(R.id.tvCartProductDescription)

        private val tvPrice =
            itemView.findViewById<TextView>(R.id.tvCartProductPrice)

        private val tvQuantity =
            itemView.findViewById<TextView>(R.id.tvCartQuantity)

        private val btnDecrease =
            itemView.findViewById<TextView>(R.id.btnCartDecrease)

        private val btnIncrease =
            itemView.findViewById<TextView>(R.id.btnCartIncrease)

        private val btnRemove =
            itemView.findViewById<TextView>(R.id.tvCartRemove)

        private var boundEntry: CartEntry? = null
        private var priceAnimator: ValueAnimator? = null
        private var isRemoving = false

        fun bind(
            entry: CartEntry,
            animateQuantityChange: Boolean = false,
        ) {
            val previousEntry = boundEntry

            isRemoving = false

            itemView.animate().cancel()
            itemView.alpha = 1f
            itemView.translationX = 0f
            itemView.isEnabled = true

            val product = entry.product

            tvName.text = product.name
            tvDescription.text = product.description

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

            val canAnimateQuantity =
                animateQuantityChange &&
                        previousEntry != null &&
                        previousEntry.product.id == product.id &&
                        previousEntry.quantity != entry.quantity

            if (canAnimateQuantity) {
                animatePriceChange(previousEntry!!, entry)
                animateQuantityText(entry.quantity)
            } else {
                priceAnimator?.cancel()

                tvPrice.text = PriceFormatter.format(
                    product.price * entry.quantity
                )

                tvQuantity.text = entry.quantity.toString()
                tvQuantity.scaleX = 1f
                tvQuantity.scaleY = 1f
            }

            boundEntry = entry

            btnDecrease.setOnClickListener {
                if (isRemoving) return@setOnClickListener

                if (entry.quantity <= 1) {
                    animateRemove(entry)
                } else {
                    animatePress(btnDecrease)
                    onQuantityChange(entry, entry.quantity - 1)
                }
            }

            btnIncrease.setOnClickListener {
                if (isRemoving) return@setOnClickListener

                animatePress(btnIncrease)
                onQuantityChange(entry, entry.quantity + 1)
            }

            btnRemove.setOnClickListener {
                if (!isRemoving) {
                    animateRemove(entry)
                }
            }
        }

        private fun animatePress(view: View) {
            view.animate().cancel()

            view.scaleX = 1f
            view.scaleY = 1f

            view.animate()
                .scaleX(0.82f)
                .scaleY(0.82f)
                .setDuration(75L)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220L)
                        .setInterpolator(OvershootInterpolator(3f))
                        .start()
                }
                .start()
        }

        private fun animateQuantityText(quantity: Int) {
            tvQuantity.animate().cancel()

            tvQuantity.text = quantity.toString()
            tvQuantity.scaleX = 0.72f
            tvQuantity.scaleY = 0.72f

            tvQuantity.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(240L)
                .setInterpolator(OvershootInterpolator(2.5f))
                .start()
        }

        private fun animatePriceChange(
            oldEntry: CartEntry,
            newEntry: CartEntry,
        ) {
            priceAnimator?.cancel()

            val startPrice =
                oldEntry.product.price * oldEntry.quantity

            val endPrice =
                newEntry.product.price * newEntry.quantity

            priceAnimator = ValueAnimator.ofInt(
                startPrice.toInt(),
                endPrice.toInt()
            ).apply {
                duration = 280L
                interpolator = AccelerateInterpolator()

                addUpdateListener { animation ->
                    val amount = (animation.animatedValue as Int).toLong()
                    tvPrice.text = PriceFormatter.format(amount)
                }

                start()
            }
        }

        private fun animateRemove(entry: CartEntry) {
            if (isRemoving) return

            isRemoving = true

            btnDecrease.isEnabled = false
            btnIncrease.isEnabled = false
            btnRemove.isEnabled = false

            val distance = itemView.width * 0.22f

            itemView.animate().cancel()

            itemView.animate()
                .translationX(distance)
                .alpha(0f)
                .setDuration(230L)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    onRemove(entry)
                }
                .start()
        }

        fun cancelAnimations() {
            priceAnimator?.cancel()
            itemView.animate().cancel()
            tvQuantity.animate().cancel()
            btnDecrease.animate().cancel()
            btnIncrease.animate().cancel()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_cart,
            parent,
            false,
        )

        return CartViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int,
    ) {
        holder.bind(entries[position])
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.contains(PAYLOAD_QUANTITY)) {
            holder.bind(
                entries[position],
                animateQuantityChange = true,
            )
        } else {
            holder.bind(entries[position])
        }
    }

    override fun onViewRecycled(holder: CartViewHolder) {
        holder.cancelAnimations()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = entries.size

    fun updateData(newEntries: List<CartEntry>) {
        if (entries == newEntries) return

        val oldEntries = entries

        val diff = DiffUtil.calculateDiff(
            object : DiffUtil.Callback() {

                override fun getOldListSize(): Int =
                    oldEntries.size

                override fun getNewListSize(): Int =
                    newEntries.size

                override fun areItemsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int,
                ): Boolean {
                    return oldEntries[oldItemPosition].product.id ==
                            newEntries[newItemPosition].product.id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int,
                ): Boolean {
                    return oldEntries[oldItemPosition] ==
                            newEntries[newItemPosition]
                }

                override fun getChangePayload(
                    oldItemPosition: Int,
                    newItemPosition: Int,
                ): Any? {
                    val oldEntry = oldEntries[oldItemPosition]
                    val newEntry = newEntries[newItemPosition]

                    return if (
                        oldEntry.product == newEntry.product &&
                        oldEntry.quantity != newEntry.quantity
                    ) {
                        PAYLOAD_QUANTITY
                    } else {
                        null
                    }
                }
            }
        )

        entries = newEntries
        diff.dispatchUpdatesTo(this)
    }
}