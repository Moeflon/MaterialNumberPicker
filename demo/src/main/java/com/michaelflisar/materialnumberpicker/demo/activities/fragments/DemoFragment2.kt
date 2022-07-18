package com.michaelflisar.materialnumberpicker.demo.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michaelflisar.materialnumberpicker.demo.activities.classes.ItemWithPickerAdapter
import com.michaelflisar.materialnumberpicker.demo.databinding.Fragment2Binding

class DemoFragment2 : Fragment() {

    private lateinit var binding: Fragment2Binding
    private lateinit var items: ArrayList<ItemWithPickerAdapter.Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        items = savedInstanceState?.getParcelableArrayList("ITEMS")
            ?: ArrayList((0..100).map { ItemWithPickerAdapter.Item(it.toFloat(), it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = Fragment2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.recycler.adapter = ItemWithPickerAdapter(requireContext(), items)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("ITEMS", items)
    }

}