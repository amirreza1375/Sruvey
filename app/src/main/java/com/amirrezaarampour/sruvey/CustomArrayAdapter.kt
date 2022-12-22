package com.amirrezaarampour.sruvey

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.json.JSONObject

class CustomArrayAdapter(context: Context,val res: Int, val items: ArrayList<JSONObject>) :
    ArrayAdapter<JSONObject>(context, res, items) {

    val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView :TextView = super.getView(position, convertView, parent) as TextView

        if (items.get(position).has("title")) {
            textView.setText(items.get(position).getString("title"))
        }

        if (items.get(position).has("shop_name")) {
            textView.setText(items.get(position).getString("shop_name"))
        }

        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView :TextView =  super.getDropDownView(position, convertView, parent) as TextView

        if (items.get(position).has("title")) {
            textView.setText(items.get(position).getString("title"))
        }
        if (items.get(position).has("shop_name")) {
            textView.setText(items.get(position).getString("shop_name"))
        }

        return textView
    }

//    override fun getView(position: Int, convertView: View, container: ViewGroup): View {
//        var view: View? = convertView
//        if (view == null) {
//            view = inflater.inflate(android.R.layout.simple_list_item_1, container, false)
//        }
//        (view?.findViewById(android.R.id.text1) as TextView).text = getItem(position)!!.getString("name")
//        return view
//    }

//    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
//        var view: View? = convertView
//        if (view == null) {
//            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
//        }
//        (view?.findViewById(android.R.id.text1) as TextView).text = getItem(position)!!.getString("title")
//        return view
//    }
}