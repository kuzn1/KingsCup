package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import pwr.am.kingscup.databinding.ActivityEndGameBinding

class EndGameActivity : Activity() {

    private lateinit var binding : ActivityEndGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEndGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun lobby(view: View) {
        //todo rejoin game lobby
    }

    fun leave(view: View) {
        //todo leave game
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }
}