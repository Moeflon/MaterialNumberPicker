package com.michaelflisar.materialnumberpicker.demo.activities.actvities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.michaelflisar.materialnumberpicker.demo.R
import com.michaelflisar.materialnumberpicker.demo.activities.fragments.DemoFragment1
import com.michaelflisar.materialnumberpicker.demo.activities.fragments.DemoFragment2
import com.michaelflisar.materialnumberpicker.demo.databinding.ActivityDemoBinding
import kotlin.reflect.KClass


class DemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDemoBinding

    private var lastAttachedFragment: KClass<*>? = null
    private var selectedFragment: KClass<*> = DemoFragment1::class

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityDemoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        if (savedInstanceState != null) {
            selectedFragment =
                Class.forName(savedInstanceState.getString("selectedFragment")).kotlin
            lastAttachedFragment = savedInstanceState.getString("lastAttachedFragment")
                ?.let { Class.forName(it).kotlin }
        }

        updateFragment()

        listOf(
            Pair(binding.btFragment1, DemoFragment1::class),
            Pair(binding.btFragment2, DemoFragment2::class)
        ).forEach { data ->
            data.first.setOnClickListener {
                if (selectedFragment != data.second) {
                    selectedFragment = data.second
                    updateFragment()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("selectedFragment", selectedFragment.java.name)
        lastAttachedFragment?.let { outState.putString("lastAttachedFragment", it.java.name) }
    }

    private fun updateFragment() {

        val ft = supportFragmentManager.beginTransaction()

        val tag = selectedFragment.java.name
        val currentTag = lastAttachedFragment?.java?.name
        val id = R.id.fragment

        val currentFragment = currentTag?.let { supportFragmentManager.findFragmentByTag(it) }
        val nextFragment = supportFragmentManager.findFragmentByTag(tag)
            ?: supportFragmentManager.fragmentFactory.instantiate(
                ClassLoader.getSystemClassLoader(),
                selectedFragment.java.name
            )

        if (!nextFragment.isAdded)
            ft.add(id, nextFragment, tag)
        if (currentFragment != null)
            ft.hide(currentFragment)
        ft.show(nextFragment)

        lastAttachedFragment = selectedFragment

        ft.commit()
    }
}