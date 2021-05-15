package pwr.am.kingscup

import android.app.Activity
import android.os.Bundle
import android.view.View
import pwr.am.kingscup.databinding.ActivityCardViewBinding

class CardViewActivity : Activity(){
    private lateinit var binding: ActivityCardViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCardViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        finish()
    }
}