package com.sygic.driving.testapp.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.sygic.driving.testapp.R

class LiveValueView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): LinearLayout(context, attrs, defStyleAttr) {

    constructor(context: Context): this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet): this(context, attrs, 0)

    private val descriptionView: TextView
    private val valueView: TextView

    init {
        val layout = inflate(context, R.layout.item_live_value, this)
        valueView = layout.findViewById(R.id.tvValue)
        descriptionView = layout.findViewById(R.id.tvDescription)

        // FIXME this should be reading attributes from XML but it is not working
        context.theme.obtainStyledAttributes(attrs, R.styleable.LiveValueView, 0, 0).apply {
            try {
                descriptionView.text = getString(R.styleable.LiveValueView_description)
            }
            finally {
                recycle()
            }
        }
    }

    var value: String
        set(value) {
            setValue(value)
        }
        get() {
            return valueView.text.toString()
        }


    fun setValue(value: CharSequence) {
        valueView.text = value
    }

    fun setValue(@StringRes resId: Int) {
        valueView.setText(resId)
    }

    var description: String
    set(text) {
        setDescription(text)
    }
    get() {
        return descriptionView.text.toString()
    }


    fun setDescription(value: CharSequence) {
        descriptionView.text = value
    }

    fun setDescription(@StringRes resId: Int) {
        descriptionView.setText(resId)
    }
}