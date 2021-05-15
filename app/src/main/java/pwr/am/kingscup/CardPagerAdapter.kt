package pwr.am.kingscup

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap

import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import pwr.am.kingscup.databinding.CardViewPageBinding


class CardPagerAdapter(private val context: Context, private val cardIdList : ArrayList<Int>) : PagerAdapter() {

    private var cardResource = R.drawable.deck_default_320x498
    private var cardWidth = 320
    private var cardHeight = 498

    private var coverBitmap : Bitmap? = null
    private val cardBitmapArray : Array<Bitmap?> = arrayOfNulls(cardIdList.size)

    init {
        // todo change to asynchronous image loading (load every image using single RegionDecoder)
        val config = context.getSharedPreferences("KingsCupConfig", Activity.MODE_PRIVATE)
        cardWidth = config.getInt("card_width", 320)
        cardHeight = config.getInt("card_height", 498)

        when(config.getString("texture", "Default")){
            "Default"->when(config.getString("quality", "Low")){
                "Low" -> cardResource = R.drawable.deck_default_320x498
                "Medium" -> cardResource = R.drawable.deck_default_480x747
                "High" -> cardResource = R.drawable.deck_default_720x1121
            }
            "Dark"->when(config.getString("quality", "Low")){
                "Low" -> cardResource = R.drawable.deck_dark_320x498
                "Medium" -> cardResource = R.drawable.deck_dark_480x747
                "High" -> cardResource = R.drawable.deck_dark_720x1121
            }
        }
    }

    override fun getCount(): Int = cardIdList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = (view == `object`)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val binding = CardViewPageBinding.inflate(LayoutInflater.from(context), container, false)

        // todo change to asynchronous image loading (load every image using single RegionDecoder)
        if(cardBitmapArray[position] == null) {
            val input = context.resources.openRawResource(cardResource)
            val decoder = BitmapRegionDecoder.newInstance(input, false)

            // todo get map position by given id
            val cardMapPosition = cardIdList[position]
            val x = (cardMapPosition%13)*cardWidth
            val y = (cardMapPosition/13)*cardHeight
            cardBitmapArray[position] = decoder.decodeRegion(Rect(x,y,x+cardWidth,y+cardHeight),null)

            if (coverBitmap == null)
                coverBitmap = decoder.decodeRegion(Rect(0, 4 * cardHeight, cardWidth, 5 * cardHeight), null)
        }

        binding.cardImage.setImageBitmap(cardBitmapArray[position])
        binding.cardCover.setImageBitmap(coverBitmap)

        // todo get description position by given id
        val cardDescriptionPosition = 1
        binding.cardDescription.text = context.resources.getStringArray(R.array.card_description_array)[cardDescriptionPosition]

        container.addView(binding.root)

        binding.root.setOnClickListener {
            if(binding.cardCover.visibility == View.VISIBLE) {
                binding.cardCover.visibility = View.INVISIBLE
                binding.cardDescription.visibility = View.INVISIBLE
            }else {
                binding.cardCover.visibility = View.VISIBLE
                binding.cardDescription.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

    override fun destroyItem(parent: ViewGroup, position: Int, `object`: Any) {
        parent.removeView(`object` as View)
    }
}