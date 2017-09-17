package com.ubiqsmart.ui.onboarding

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.ubiqsmart.R
import com.ubiqsmart.ui.base.BaseFragment

class ToSFragment : BaseFragment() {

    private var read: CheckBox? = null
    var isToSChecked: Boolean = false
        private set

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val tos = view!!.findViewById<TextView>(R.id.tostext)
        tos.text = Html.fromHtml(activity.resources.getString(R.string.tos))
        read = view?.findViewById(R.id.checkBox)

        read?.setOnClickListener { isToSChecked = read!!.isChecked }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.tos_layout, container, false)
    }
}