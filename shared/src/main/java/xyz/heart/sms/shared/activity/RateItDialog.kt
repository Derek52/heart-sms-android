package xyz.heart.sms.shared.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import xyz.heart.sms.api.implementation.firebase.AnalyticsHelper
import xyz.heart.sms.shared.R
import xyz.heart.sms.shared.data.Settings
import xyz.heart.sms.shared.util.DensityUtil
import xyz.heart.sms.shared.util.isDarkColor
//TODO these are in a jar, I'm not sure how to change these
import xyz.klinker.android.floating_tutorial.FloatingTutorialActivity
import xyz.klinker.android.floating_tutorial.TutorialFinishedListener
import xyz.klinker.android.floating_tutorial.TutorialPage

class RateItDialog : FloatingTutorialActivity(), TutorialFinishedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsHelper.rateItPromptShown(this)
    }

    override fun onTutorialFinished() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            AnalyticsHelper.rateItClicked(this)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Couldn't launch the Play Store!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getPages(): List<TutorialPage> {
        return listOf(object : TutorialPage(this@RateItDialog) {
            override fun initPage() {
                setContentView(R.layout.page_rate_it)
                setNextButtonText(R.string.rate_it)

                val topText = findViewById<View>(R.id.top_text) as TextView
                val primaryColor = Settings.mainColorSet.color

                topText.setBackgroundColor(primaryColor)
                if (!primaryColor.isDarkColor()) {
                    topText.setTextColor(resources.getColor(R.color.tutorial_light_background_indicator))
                }
            }

            override fun animateLayout() {
                val startTime: Long = 300

                quickViewReveal(findViewById<View>(R.id.bottom_text_1), startTime)
                quickViewReveal(findViewById<View>(R.id.bottom_text_2), startTime + 75)

                quickViewReveal(findViewById<View>(R.id.star_1), startTime)
                quickViewReveal(findViewById<View>(R.id.star_2), startTime + 50)
                quickViewReveal(findViewById<View>(R.id.star_3), startTime + 100)
                quickViewReveal(findViewById<View>(R.id.star_4), startTime + 150)
                quickViewReveal(findViewById<View>(R.id.star_5), startTime + 200)
            }
        })
    }

    private fun quickViewReveal(view: View, delay: Long) {
        view.translationX = (-1 * DensityUtil.toDp(this, 16)).toFloat()
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
                .translationX(0f)
                .alpha(1f)
                .setStartDelay(delay)
                .start()
    }
}