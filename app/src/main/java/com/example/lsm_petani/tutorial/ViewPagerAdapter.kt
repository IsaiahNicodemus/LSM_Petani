package com.example.lsm_petani

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

class ViewPagerAdapter(private val context: Context) : PagerAdapter() {

    private val sliderAllImages = arrayOf(
        R.drawable.uploadimage,
        R.drawable.fillform2,
        R.drawable.farmers
    )

    private val sliderAllTitle = arrayOf(
        R.string.screen1,
        R.string.screen2,
        R.string.screen3
    )

    private val sliderAllDesc = arrayOf(
        R.string.screen1desc,
        R.string.screen2desc,
        R.string.screen3desc
    )

    override fun getCount(): Int {
        return sliderAllTitle.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.slider_screen, container, false)

        val sliderImage: ImageView = view.findViewById(R.id.sliderImage)
        val sliderTitle: TextView = view.findViewById(R.id.sliderTitle)
        val sliderDesc: TextView = view.findViewById(R.id.sliderDesc)

        sliderImage.setImageResource(sliderAllImages[position])
        sliderTitle.setText(sliderAllTitle[position])
        sliderDesc.setText(sliderAllDesc[position])

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as LinearLayout)
    }
}