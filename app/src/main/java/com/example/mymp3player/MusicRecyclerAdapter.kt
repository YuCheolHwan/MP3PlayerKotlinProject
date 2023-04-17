package com.example.mymp3player

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.mymp3player.databinding.ItemRecyclerBinding
import java.text.SimpleDateFormat

class MusicRecyclerAdapter(val context:Context, val musicList:MutableList<MusicData>):
    RecyclerView.Adapter<MusicRecyclerAdapter.CustomViewHolder>() {
    val ALBUM_IMAGE_SIZE = 90

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemRecyclerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int = musicList.size

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        // 이미지, artist, title, duration binding
        val bitmap = musicList.get(position).getAlbumBitmap(context, ALBUM_IMAGE_SIZE)
        if(bitmap != null){
            binding.ivAlbumArt.setImageBitmap(bitmap)
        }else{
            binding.ivAlbumArt.setImageResource(R.drawable.ic_launcher_background)
        }
        binding.tvArtist.text = musicList.get(position).artist
        binding.tvTitle.text = musicList.get(position).title
        binding.tvDuration.text = SimpleDateFormat("mm:ss").format(musicList.get(position).duration)
        when(musicList.get(position).likes){
            0 -> binding.ivItemLike.setImageResource(R.drawable.unlike)
            1 -> binding.ivItemLike.setImageResource(R.drawable.like)
        }
        // 아이템항목 클릭 시 PlayActivity MusicData 전달
        binding.root.setOnClickListener {
            musicList.get(position).playValue = 1
            val db = DBOpenHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
            var errorFlag = db.updatePlayValue(musicList.get(position))
            if(errorFlag){
                Toast.makeText(context,"updatePlayValue 실패",Toast.LENGTH_SHORT).show()
            } else {
                this.notifyDataSetChanged()
            }
            val intent = Intent(binding.root.context,PlayActivity::class.java)
            val parcelableList:ArrayList<Parcelable>? = musicList as ArrayList<Parcelable>
            intent.putExtra("parcelableList", parcelableList)
            intent.putExtra("position", position)
            binding.root.context.startActivity(intent)
        }
        binding.ivItemLike.setOnClickListener {
            when(musicList.get(position).likes){
                0 -> {
                    musicList.get(position).likes = 1
                    binding.ivItemLike.setImageResource(R.drawable.like)
                }
                1 -> {
                    musicList.get(position).likes = 0
                    binding.ivItemLike.setImageResource(R.drawable.unlike)
                }
            }
            val db = DBOpenHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
            var errorFlag = db.updateLike(musicList.get(position))
            if(errorFlag){
                Toast.makeText(context,"updateLike 실패",Toast.LENGTH_SHORT).show()
            } else {
                this.notifyDataSetChanged()
            }

        }
    }
    inner class CustomViewHolder(val binding:ItemRecyclerBinding):RecyclerView.ViewHolder(binding.root)
}