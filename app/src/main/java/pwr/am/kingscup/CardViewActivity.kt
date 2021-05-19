package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import pwr.am.kingscup.databinding.ActivityCardViewBinding

class CardViewActivity : Activity(){
    private lateinit var binding: ActivityCardViewBinding
    private lateinit var lobby: Lobby

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCardViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lobby = Lobby(intent.getStringExtra("gameKey").toString())
        lobby.playerKey = intent.getStringExtra("playerKey").toString()
        lobby.addServerTickListener(this)
        lobby.addListenerToPlayer(this)

        setCards()
    }

    private fun setCards(){
        // TODO get cards from firebase
        val cardIdList : ArrayList<Int> = ArrayList()
        for (i in 0..54)
            cardIdList.add(i)

        binding.viewPager.adapter = CardPagerAdapter(this, cardIdList)
    }

    fun back(view: View) {
        lobby.removeServerTickListener()
        lobby.removeListenerToPlayer()
        val intent = Intent()
        intent.putExtra("result", "back");
        this.setResult(RESULT_OK, intent)
        finish()
    }
    override fun onBackPressed() {
        lobby.removeServerTickListener()
        lobby.removeListenerToPlayer()
        val intent = Intent()
        intent.putExtra("result", "back");
        this.setResult(RESULT_OK, intent)
        finish()
    }
    //TODO on destroy
}