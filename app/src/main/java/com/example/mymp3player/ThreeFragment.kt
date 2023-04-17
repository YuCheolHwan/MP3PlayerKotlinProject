package com.example.mymp3player

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymp3player.databinding.FragmentThreeBinding

class ThreeFragment : Fragment() {
    lateinit var binding : FragmentThreeBinding
    lateinit var dbOpenHelper : DBOpenHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThreeBinding.inflate(inflater)
        dbOpenHelper = DBOpenHelper(binding.root.context, MainActivity.DB_NAME, MainActivity.VERSION)
        binding.recyclerView.adapter = MusicRecyclerAdapter(binding.root.context, dbOpenHelper.selectMusicPlayValue()!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        return binding.root
    }
}