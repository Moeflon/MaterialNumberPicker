package com.michaelflisar.materialnumberpicker.demo.activities.classes

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.michaelflisar.materialnumberpicker.demo.databinding.RowItemWithPickerBinding
import com.michaelflisar.materialnumberpicker.picker.FloatPicker
import com.michaelflisar.materialnumberpicker.picker.IntPicker
import com.michaelflisar.materialnumberpicker.setup.NumberPickerSetupMinMax

class ItemWithPickerAdapter internal constructor(
    context: Context,
    val items: List<Item>
) : RecyclerView.Adapter<ItemWithPickerAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(this, RowItemWithPickerBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        //holder.unbind()
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount() = items.size

    private fun updatePickerSetups(index: Int, pickerWeight: FloatPicker, pickerReps: IntPicker) {
        val offsetItems = 1
        val weightUnit = "kg"
        pickerWeight.setup = NumberPickerSetupMinMax(
            50f,
            0f,
            2500f,
            1f,
            1f,
            { "$it$weightUnit" },
            { it.replace(weightUnit, "").toFloatOrNull() },
            offsetItems
        )
        val repsUnit = "x"
        pickerReps.setup = NumberPickerSetupMinMax(
            10,
            0,
            100,
            1,
            1,
            { "$it$repsUnit" },
            { it.replace(repsUnit, "").toIntOrNull() },
            offsetItems
        )
    }

    inner class ViewHolder internal constructor(
        val adapter: ItemWithPickerAdapter,
        val binding: RowItemWithPickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item, position: Int) {

            val smooth = false
            val notify = false

            //updatePickerSetups(position, holder.binding.pickerWeight, holder.binding.pickerReps)

            binding.tvLabel.text = "pos = $position"

            binding.pickerWeight.setValue(item.weight, smooth, notify)
            binding.pickerReps.setValue(item.reps, smooth, notify)

            Log.d("BIND", "pos = $position | weight = ${item.weight} | reps = ${item.reps}")

            binding.pickerWeight.onValueChangedListener = { picker, value, fromUser ->
                val pos = this.adapterPosition
                val item = adapter.items[pos]
                item.weight = value
                Log.d("CHANGE", "pos = $pos | weight = $value")
            }
            binding.pickerReps.onValueChangedListener = { picker, value, fromUser ->
                val pos = this.adapterPosition
                val item = adapter.items[pos]
                item.reps = value
                Log.d("CHANGE", "pos = $pos | reps = $value")
            }
        }

        fun unbind() {
            binding.tvLabel.text = "--"
            binding.pickerWeight.onValueChangedListener = null
            binding.pickerReps.onValueChangedListener = null
            Log.d("UNBIND", "pos = $adapterPosition")
        }
    }

    class Item(
        var weight: Float,
        var reps: Int
    )
}