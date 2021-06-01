package pwr.am.kingscup

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

import pwr.am.kingscup.databinding.ActivityCardChangeBinding
import pwr.am.kingscup.databinding.CardChangeRowBinding

class CardChangeActivity : Activity(){

    private lateinit var database : FirebaseDatabase
    private lateinit var referenceCards : DatabaseReference

    private lateinit var binding : ActivityCardChangeBinding
    private var changed = false

    private lateinit var cards : ArrayList<Int>
    private var cardCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCardChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gameKey = intent.getStringExtra("gameKey").toString()

        database = Firebase.database
        referenceCards = database.getReference("games/$gameKey/card_set")

        loadCards()
    }

    fun loadCards(){
        referenceCards.get().addOnSuccessListener{
            cards = arrayListOf(0,0,0,0,0,0,0,0,0,0,0,0,0)
            for (i in 0..51){
                cards[i%13] += (it.child(i.toString()).value as Long).toInt()
                cardCount += (it.child(i.toString()).value as Long).toInt()
            }
            runOnUiThread{
                binding.cardList.removeAllViews()
                val cardNames = resources.getStringArray(R.array.card_name)
                for(i in 0..12){
                    val row = CardChangeRowBinding.inflate(LayoutInflater.from(this))
                    row.cardName.text = cardNames[i]
                    row.cardCount.text = cards[i].toString()
                    if(i == 8 || i == 9 || i == 10){
                        row.root.alpha = 0.5f
                        row.root.setBackgroundColor(Color.GRAY)
                        row.plusButton.isClickable = false
                        row.minusButton.isClickable = false
                    }else{
                        row.plusButton.setOnClickListener {
                            if(cards[i]<4) {
                                cards[i]++
                                row.cardCount.text = cards[i].toString()
                                cardCount++
                            }
                        }
                        row.minusButton.setOnClickListener {
                            if(cards[i]>0 && cardCount > 1){
                                cards[i]--
                                row.cardCount.text = cards[i].toString()
                                cardCount--
                            }
                        }
                    }
                    binding.cardList.addView(row.root)
                }
            }
        }
    }

    fun back(view: View) {
        val cardList = HashMap<String, Long>()
        for(i in 0..51){
            if(cards[i%13] > i/13) cardList.put(i.toString(), 1)
            else cardList.put(i.toString(), 0)
        }
        referenceCards.setValue(cardList)
        this.setResult(RESULT_OK, intent)
        finish()
    }

}