package com.example.epub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.recyclerview.widget.RecyclerView

class WebViewAdapter(var xHTMLList: ArrayList<String>) :
    RecyclerView.Adapter<WebViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.web_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return xHTMLList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var webView: WebView
        fun bind(position: Int) {
            webView.loadDataWithBaseURL(
                null,
                xHTMLList[position], "application/xhtml+xml", "utf-8", null
            )
        }

        init {
            webView = itemView.findViewById(R.id.webView)
        }
    }
}
