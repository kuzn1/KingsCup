package pwr.am.kingscup.activity.menu

import android.app.Activity
import android.os.Bundle
import android.view.View
import pwr.am.kingscup.databinding.ActivityAboutBinding

class AboutActivity : Activity() {
    lateinit var binding : ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun back(view: View) {
        finish()
    }
}