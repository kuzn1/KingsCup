package pwr.am.kingscup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import pwr.am.kingscup.databinding.ActivityCardViewBinding

class CardViewActivity : Activity(){

    private lateinit var database : FirebaseDatabase
    private lateinit var referenceCards : DatabaseReference
    private var gameKey = ""

    private lateinit var binding: ActivityCardViewBinding
    private lateinit var lobby: Lobby

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCardViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.getBooleanExtra("OWNER", false)){
            binding.cardsButton.visibility = View.VISIBLE
        }

        gameKey = intent.getStringExtra("gameKey").toString()

        lobby = Lobby(gameKey)
        lobby.playerKey = intent.getStringExtra("playerKey").toString()
        lobby.addServerTickListener(this)
        lobby.addListenerToPlayer(this)

        database = Firebase.database
        referenceCards =  database.getReference("games/$gameKey/card_set")

        setCards()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setCards()
    }

    private fun setCards(){
        referenceCards.get().addOnSuccessListener{
            val cardIdList : ArrayList<Int> = ArrayList()
            for (i in 0..51){
                repeat( (it.child(i.toString()).value as Long).toInt() ){
                    cardIdList.add(i)
                }
            }
            runOnUiThread{binding.viewPager.adapter = CardPagerAdapter(this, cardIdList)}
        }
    }

    fun back(view: View) {
        lobby.removeServerTickListener()
        lobby.removeListenerToPlayer()
        val intent = Intent()
        intent.putExtra("result", "back")
        this.setResult(RESULT_OK, intent)
        finish()
    }
    override fun onBackPressed() {
        lobby.removeServerTickListener()
        lobby.removeListenerToPlayer()
        val intent = Intent()
        intent.putExtra("result", "back")
        this.setResult(RESULT_OK, intent)
        finish()
    }

    fun changeCards(view: View) {
        startActivityForResult(
            Intent(this, CardChangeActivity::class.java).putExtra("gameKey", gameKey),
            0
        )
    }
    //TODO on destroy
}