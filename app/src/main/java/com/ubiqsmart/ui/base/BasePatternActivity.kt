package com.ubiqsmart.ui.base

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ubiqsmart.R
import me.zhanghai.android.patternlock.PatternView

abstract class BasePatternActivity : BaseActivity() {

    protected lateinit var messageText: TextView
    protected lateinit var patternView: PatternView
    protected var buttonContainer: LinearLayout? = null
    protected var leftButton: Button? = null
    protected var rightButton: Button? = null

    private val clearPatternRunnable = Runnable { patternView.clearPattern() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.app_lock_activity)

        messageText = findViewById(R.id.pl_message_text)
        patternView = findViewById(R.id.pl_pattern)
        buttonContainer = findViewById(R.id.pl_button_container)
        leftButton = findViewById(R.id.pl_left_button)
        rightButton = findViewById(R.id.pl_right_button)
    }

    protected fun removeClearPatternRunnable() {
        patternView.removeCallbacks(clearPatternRunnable)
    }

    protected fun postClearPatternRunnable() {
        removeClearPatternRunnable()
        patternView.postDelayed(clearPatternRunnable, CLEAR_PATTERN_DELAY_MILLI.toLong())
    }

    companion object {

        private const val CLEAR_PATTERN_DELAY_MILLI = 2000
    }
}