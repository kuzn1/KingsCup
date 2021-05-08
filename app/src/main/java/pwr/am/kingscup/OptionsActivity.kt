package pwr.am.kingscup

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import pwr.am.kingscup.databinding.ActivityOptionsBinding

class OptionsActivity : Activity() {
    private lateinit var binding : ActivityOptionsBinding
    private lateinit var config : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        config = getSharedPreferences("KingsCupConfig", MODE_PRIVATE)
        loadConfig()
    }

    override fun onPause() {
        saveConfig()
        super.onPause()
    }

    fun back(view: View) {
        saveConfig()
        finish()
    }

    private fun loadConfig() {
        binding.nickInput.setText(config.getString("nick", "Player"))
    }

    private fun saveConfig() {
        with(config.edit()){
            if(binding.nickInput.text.toString().isNotEmpty())
                this.putString("nick",binding.nickInput.text.toString())
            this.apply()
        }
    }
}