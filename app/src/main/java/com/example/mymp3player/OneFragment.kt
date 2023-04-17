package com.example.mymp3player

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymp3player.databinding.FragmentOneBinding

class OneFragment : Fragment() {
    lateinit var binding : FragmentOneBinding
    lateinit var dbOpenHelper : DBOpenHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOneBinding.inflate(inflater)
        dbOpenHelper = DBOpenHelper(binding.root.context, MainActivity.DB_NAME, MainActivity.VERSION)
        binding.recyclerView.adapter = MusicRecyclerAdapter(binding.root.context,dbOpenHelper.selectAllMusicTBL()!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        return binding.root
    }

}