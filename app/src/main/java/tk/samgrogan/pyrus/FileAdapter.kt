package tk.samgrogan.pyrus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.improvelectronics.sync.obex.OBEXFtpFolderListingItem
import kotlinx.android.synthetic.main.file_item.view.*

class FileAdapter(val listener: (OBEXFtpFolderListingItem) -> Unit): RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    var folderItems: List<OBEXFtpFolderListingItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return folderItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { listener(folderItems[position]) }
        holder.nameFile.text = folderItems[position].name
        //folderItems[position].data
        //holder.date.text = folderItems[position].time.toString()
    }

    fun swapData(list: List<OBEXFtpFolderListingItem>) {
        folderItems = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameFile = itemView.nameFile
        //val date = itemView.date
    }
}