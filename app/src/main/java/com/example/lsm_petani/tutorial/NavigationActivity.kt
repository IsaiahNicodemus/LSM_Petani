package com.example.lsm_petani.tutorial

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.example.lsm_petani.MainActivity
import com.example.lsm_petani.R
import com.example.lsm_petani.ViewPagerAdapter

class NavigationActivity : AppCompatActivity() {

    private lateinit var slideViewPager: ViewPager
    private lateinit var dotIndicator: LinearLayout
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var skipButton: Button
    private lateinit var dots: Array<TextView>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private val viewPagerListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            setDotIndicator(position)

            backButton.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
            nextButton.text = if (position == 2) "Finish" else "Next"
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
        skipButton = findViewById(R.id.skipButton)
        slideViewPager = findViewById(R.id.slideViewPager)
        dotIndicator = findViewById(R.id.dotIndicator)

        viewPagerAdapter = ViewPagerAdapter(this)
        slideViewPager.adapter = viewPagerAdapter

        setDotIndicator(0)
        slideViewPager.addOnPageChangeListener(viewPagerListener)

        backButton.setOnClickListener {
            if (getItem(0) > 0) {
                slideViewPager.setCurrentItem(getItem(-1), true)
            }
        }

        nextButton.setOnClickListener {
            if (getItem(0) < 2) {
                slideViewPager.setCurrentItem(getItem(1), true)
            } else {
                val intent = Intent(this, GetStarted::class.java)
                startActivity(intent)
                finish()
            }
        }

        skipButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setDotIndicator(position: Int) {
        dots = Array(3) { TextView(this) }
        dotIndicator.removeAllViews()

        for (i in dots.indices) {
            dots[i].apply {
                text = Html.fromHtml("&#8226", Html.FROM_HTML_MODE_LEGACY)
                textSize = 35f
                setTextColor(ContextCompat.getColor(this@NavigationActivity, R.color.grey))
            }
            dotIndicator.addView(dots[i])
        }
        dots[position].setTextColor(ContextCompat.getColor(this, R.color.lavender))
    }

    private fun getItem(i: Int): Int {
        return slideViewPager.currentItem + i
    }
}