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
    }

    //todo implement viewPager and card fragment

    fun back(view: View) {
        finish()
    }
}