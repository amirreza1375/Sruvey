package cl.aarampour.survey.slider

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import cl.aarampour.survey.R

/**
 * This view will get image and show them inside slider
 */

class ImageSliderView(context: Context?) : LinearLayout(context) {

    private lateinit var images : ArrayList<SliderItemModel>
    private var currentPageNumber = 0
    private lateinit var callBack :SwiperRecyclerAdapter.SwiperActionListener

    constructor(context: Context?, images: ArrayList<SliderItemModel>,callBack : SwiperRecyclerAdapter.SwiperActionListener) : this(context) {
        this.images = images
        this.callBack = callBack
        init()

    }

    private fun init() {

        val view = LayoutInflater.from(context).inflate(R.layout.image_slider_view_layout,this,false)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val counterContainer = view.findViewById<LinearLayout>(R.id.counterContainer)
        val counterTxt = view.findViewById<TextView>(R.id.counterTxt)

        addView(view)

        val adapter = SwiperRecyclerAdapter(context,images,callBack)

        viewPager.adapter = adapter

        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.offscreenPageLimit = 3
        viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer(object : ViewPager2.PageTransformer{
            override fun transformPage(page: View, position: Float) {
//                Toast.makeText(applicationContext,"POS : "+page.numberTxt,Toast.LENGTH_SHORT).show()
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f+r*0.15f

            }

        })
        viewPager.setPageTransformer(compositePageTransformer)

        if (images.size <= 1){
            counterContainer.visibility = GONE
        }

        counterTxt.setText("${currentPageNumber+1} / ${images.size}")

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {

                currentPageNumber = position

                counterTxt.setText("${currentPageNumber+1} / ${images.size}")

            }
        })

    }


}