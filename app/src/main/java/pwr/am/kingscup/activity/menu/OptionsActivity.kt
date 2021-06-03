package pwr.am.kingscup.activity.menu

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import pwr.am.kingscup.R
import pwr.am.kingscup.databinding.ActivityOptionsBinding

class OptionsActivity : Activity() {
    private lateinit var binding : ActivityOptionsBinding
    private lateinit var config : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        config = getSharedPreferences("KingsCupConfig", MODE_PRIVATE)

        initItems()

        loadConfig()
    }

    private fun initItems() {
        ArrayAdapter.createFromResource(
                this,
            R.array.card_texture_name_array,
                android.R.layout.simple_spinner_item
            ).also{ adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.textureSpinner.adapter = adapter
            }

        ArrayAdapter.createFromResource(
            this,
            R.array.texture_quality_name_array,
            android.R.layout.simple_spinner_item
        ).also{ adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.qualitySpinner.adapter = adapter
        }

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
        //Nickname
        binding.nickInput.setText(config.getString("nick", "Player"))

        //Gender
        if(config.getString("gender", "male")=="male")
            onGenderCheck(binding.maleCheckBox)
        else
            onGenderCheck(binding.femaleCheckBox)

        //Texture
        binding.textureSpinner.setSelection(
            resources.getTextArray(R.array.card_texture_name_array)
                .indexOf(config.getString("texture", "Default")))

        //Quality
        binding.qualitySpinner.setSelection(
            resources.getTextArray(R.array.texture_quality_name_array)
                .indexOf(config.getString("quality", "Low")))

    }

    private fun saveConfig() {
        with(config.edit()){
            //Nickname
            if(binding.nickInput.text.toString().isNotEmpty())
                this.putString("nick",binding.nickInput.text.toString())

            //Gender
            if(binding.maleCheckBox.isChecked)
                this.putString("gender", "male")
            else
                this.putString("gender", "female")

            //Texture
            this.putString("texture",
                binding.textureSpinner.selectedItem as String
            )

            //Quality
            this.putString("quality",
                binding.qualitySpinner.selectedItem as String
            )
            val index = resources.getTextArray(R.array.texture_quality_name_array)
                .indexOf(config.getString("quality", "Low"))

            this.putInt("card_height", resources.getIntArray(R.array.card_height)[index])
            this.putInt("card_width", resources.getIntArray(R.array.card_width)[index])

            this.apply()
        }
    }

    fun onGenderCheck(view: View) {
        when (view) {
            binding.femaleCheckBox -> {
                binding.femaleCheckBox.isChecked = true
                binding.maleCheckBox.isChecked = false
            }
            binding.maleCheckBox -> {
                binding.maleCheckBox.isChecked = true
                binding.femaleCheckBox.isChecked = false
            }
        }
    }
}