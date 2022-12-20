package cl.aarampour.survey.slider

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cl.aarampour.survey.R

class SwiperRecyclerAdapter(val context: Context,val items : ArrayList<SliderItemModel>,val callBack : SwiperActionListener) : RecyclerView.Adapter<SwiperRecyclerAdapter.ViewHolder>() {


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val imageView : ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.image_slider_layout_item,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
val item = items.get(position)
        val bitmap = BitmapFactory.decodeFile(item.path)

        holder.imageView.setImageBitmap(bitmap)

        holder.imageView.setOnClickListener(
            object : View.OnClickListener{
                override fun onClick(p0: View?) {
                    callBack.onPressedItem(item)
                }

            }
        )

    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface SwiperActionListener{
        fun onPressedItem(sliderItemModel: SliderItemModel)
    }
}