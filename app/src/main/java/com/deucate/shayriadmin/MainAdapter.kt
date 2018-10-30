package com.deucate.shayriadmin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class MainAdapter(private val titles: ArrayList<String>) : RecyclerView.Adapter<MainViewHolder>() {

    var listner: CardClickListener? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MainViewHolder {
        return MainViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.card_main, p0, false))
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(p0: MainViewHolder, p1: Int) {
        p0.textView.text = titles[p1]

        p0.imageView.setOnClickListener {
            listner!!.onClickDelete(p1)
        }

        p0.itemView.setOnClickListener {
            listner!!.onClickCard(p1)
        }
    }

    interface CardClickListener {
        fun onClickDelete(position: Int)
        fun onClickCard(position: Int)
    }
}

class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textView = view.findViewById<TextView>(R.id.cardMainText)!!
    val imageView = view.findViewById<ImageButton>(R.id.cardMainDeleteBtn)!!
}