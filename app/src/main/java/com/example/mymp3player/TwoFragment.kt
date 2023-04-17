package com.example.mymp3player

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymp3player.databinding.FragmentTwoBinding

class TwoFragment : Fragment() {
    lateinit var binding: FragmentTwoBinding
    lateinit var twoContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        twoContext = context
    }

    override fun onResume() {
        super.onResume()
        notifyLike()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTwoBinding.inflate(inflater)
        notifyLike()
        return binding.root
    }

    fun notifyLike() {
        try {
            val dbOpenHelper = DBOpenHelper(
                twoContext.applicationContext,
                MainActivity.DB_NAME,
                MainActivity.VERSION
            )
            val musicList = dbOpenHelper.selectMusicLike()
            val linearLayoutManager = LinearLayoutManager(twoContext.applicationContext)
            binding.recyclerView.layoutManager = linearLayoutManager
            val likeAdapter = LikeAdapter(twoContext.applicationContext, this, musicList!!)
            binding.recyclerView.adapter = likeAdapter
        } catch (e: java.lang.NullPointerException) {
            Toast.makeText(context, "좋아요 리스트가 없습니다. ", Toast.LENGTH_SHORT).show()
            Log.e("TwoFragment", "${e.printStackTrace()}")
        }
    }

}