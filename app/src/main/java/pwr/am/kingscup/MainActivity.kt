package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import pwr.am.kingscup.databinding.ActivityMainBinding


class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun onClick(view: View) {
        when(view){
            binding.playButton-> {
                // todo create new game in firebase
                val id = "DEVDEV"

                val intent = Intent(this, LobbyActivity::class.java)
                intent.putExtra("ID", id)
                intent.putExtra("OWNER", true)
                startActivity(intent)
            }
            binding.joinButton-> startActivity(Intent(this, JoinActivity::class.java))
            binding.optionsButton-> startActivity(Intent(this, OptionsActivity::class.java))
            binding.aboutButton-> startActivity(Intent(this, AboutActivity::class.java))
        }
    }

}