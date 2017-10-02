package com.ubiqsmart.app.ui.onboarding.steps

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ubiqsmart.R
import com.ubiqsmart.app.ui.base.BaseFragment
import kotlinx.android.synthetic.main.tos_layout.*

class TOSFragment : BaseFragment() {

  private var listener: ((v: View, toggled: Boolean) -> Unit)? = null

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater?.inflate(R.layout.tos_layout, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    tos_webview.apply {
      loadUrl("file:///android_asset/html/license.html")
      setBackgroundColor(Color.TRANSPARENT)
      setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
      webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
          progress_bar_view.visibility = View.GONE
          tos_webview.visibility = View.VISIBLE
        }
      }
    }

    read_checkbox_view.setOnClickListener {
      listener?.apply {
        invoke(it, read_checkbox_view.isChecked)
      }
    }
  }

  fun setOnTosButtonClickListener(listener: (v: View, toggled: Boolean) -> Unit) {
    this.listener = listener
  }

  fun isTOSButtonChecked() = read_checkbox_view.isChecked

}