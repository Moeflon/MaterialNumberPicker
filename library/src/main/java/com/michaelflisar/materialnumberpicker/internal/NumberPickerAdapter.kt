package com.michaelflisar.materialnumberpicker.internal

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker
import com.michaelflisar.materialnumberpicker.R

internal class NumberPickerAdapter<T, Picker>(
    val picker: Picker,
    private val clickListener: (vh: ViewHolderItem<T, Picker>, item: T) -> Unit,
    visibleItemsAboveBelow: Int
) : RecyclerView.Adapter<NumberPickerAdapter.ViewHolder<T, Picker>>() where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

    enum class Layout {
        Horizontal,
        HorizontalEmpty,
        Vertical,
        VerticalEmpty,
    }

    abstract class ViewHolder<T, Picker>(
        private val adapter: NumberPickerAdapter<T, Picker>,
        view: View
    ) : RecyclerView.ViewHolder(view),
        CenterZoomLinearLayoutManager.ScalableViewHolder where T : Number, T : Comparable<T>, Picker : com.michaelflisar.materialnumberpicker.AbstractMaterialNumberPicker<T, Picker> {

        abstract val textView: TextView
        abstract val viewToScale: View

        override fun scaleOnScroll(factor: Float, isCenter: Boolean) {
            viewToScale.apply {
                scaleX = factor
                scaleY = factor
            }
            textView.alpha = factor
            val newTypeface = if (isCenter) {
                Typeface.DEFAULT_BOLD
            } else {
                Typeface.DEFAULT
            }
            if (newTypeface != textView.typeface) {
                textView.typeface = newTypeface
            }
        }
    }

    class ViewHolderItem<T, Picker>(
        val adapter: NumberPickerAdapter<T, Picker>,
        view: View
    ) : ViewHolder<T, Picker>(
        adapter,
        view
    ) where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {

        override val textView = view.findViewById<TextView>(R.id.text)
        override val viewToScale = textView

        fun bind(item: T) {
            textView.text = adapter.picker.setup.formatter(item)
            itemView.setOnClickListener {
                adapter.clickListener(this, item)
            }
            Log.d("Scroller", "bind = $item | ${textView.text}")
        }
    }

    class ViewHolderEmpty<T, Picker>(
        adapter: NumberPickerAdapter<T, Picker>,
        view: View
    ) : ViewHolder<T, Picker>(
        adapter,
        view
    ) where T : Number, T : Comparable<T>, Picker : AbstractMaterialNumberPicker<T, Picker> {
        override val textView = view as TextView
        override val viewToScale = textView
    }

    private var items: List<T> = picker.setup.allValidValuesSorted
    var visibleOffsetItems = visibleItemsAboveBelow
        private set

    fun reload() {
        items = picker.setup.allValidValuesSorted
        visibleOffsetItems = picker.setup.scrollerVisibleOffsetItems
        notifyDataSetChanged()
    }

    fun getItemIndex(item: T): Int {
        return items.indexOf(item) + visibleOffsetItems
    }

    fun getItem(index: Int): T? {
        return items.getOrNull(index - visibleOffsetItems)
    }

    override fun getItemViewType(position: Int): Int {
        val layout =
            if (position < visibleOffsetItems || position >= items.size + visibleOffsetItems) {
                if (picker.orientation == LinearLayout.HORIZONTAL) Layout.HorizontalEmpty else Layout.VerticalEmpty
            } else if (picker.orientation == LinearLayout.HORIZONTAL) Layout.Horizontal else Layout.Vertical
        return layout.ordinal
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder<T, Picker> {
        val layout = Layout.values()[viewType]
        return when (layout) {
            Layout.Horizontal -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.mnp_scroller_item_horizontal, viewGroup, false)
                ViewHolderItem(this, view)
            }
            Layout.Vertical -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.mnp_scroller_item_vertical, viewGroup, false)
                ViewHolderItem(this, view)
            }
            Layout.HorizontalEmpty -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.mnp_scroller_item_horizontal_empty, viewGroup, false)
                ViewHolderEmpty(this, view)
            }
            Layout.VerticalEmpty -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.mnp_scroller_item_vertical_empty, viewGroup, false)
                ViewHolderEmpty(this, view)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<T, Picker>, position: Int) {
        Log.d("Scroller", "onBindViewHolder = $holder")
        if (holder is ViewHolderItem<T, Picker>) {
            val pos = position - visibleOffsetItems
            val item = items[pos]
            holder.bind(item)
        } else {
            // nothing to do
        }
    }

    override fun onViewRecycled(holder: ViewHolder<T, Picker>) {
        super.onViewRecycled(holder)
        holder.itemView.setOnClickListener(null)
    }

    override fun getItemCount(): Int {
        return items.size + visibleOffsetItems + visibleOffsetItems
    }
}
