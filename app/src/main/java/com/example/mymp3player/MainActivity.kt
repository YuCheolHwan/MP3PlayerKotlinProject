package com.example.mymp3player

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mymp3player.databinding.ActivityMainBinding
import com.example.mymp3player.databinding.UsertabButtonBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    companion object {
        val REQUEST_CODE = 100
        val DB_NAME = "musicDB5"
        val VERSION = 1
        var flag = false
    }

    lateinit var binding: ActivityMainBinding
    lateinit var customViewPagerAdapter: CustomViewPagerAdapter
    private var playList: MutableList<Parcelable>? = null
    private var position: Int = 0
    lateinit var musicData: MusicData
    var musicDataList: MutableList<MusicData>? = mutableListOf<MusicData>()
    private val dbOpenHelper by lazy { DBOpenHelper(this, DB_NAME, VERSION) }
    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //전달해온 intent 값을 가져옴.
        playList = intent.getParcelableArrayListExtra("parcelableList")
        position = intent.getIntExtra("position", -1)
        if (playList != null && position != -1) {
            musicData = playList?.get(position) as MusicData
            binding.tvPlayValueTitle.text = musicData.title
        }
        binding.ivPlay.setOnClickListener {
            when (flag) {
                true -> {
                    flag = false
                    binding.ivPlay.setImageResource(R.drawable.pause)
                }
                false -> {
                    flag = true
                    binding.ivPlay.setImageResource(R.drawable.play)
                }
            }
        }


        //외장메모리 읽기 승인
        var flag = ContextCompat.checkSelfPermission(this, permission[0])
        if (flag == PackageManager.PERMISSION_GRANTED) {
            startProcess()
        } else {
            //승인요청
            ActivityCompat.requestPermissions(this, permission, REQUEST_CODE)
        }
        customViewPagerAdapter = CustomViewPagerAdapter(this)
        customViewPagerAdapter.addListFragment(OneFragment())
        customViewPagerAdapter.addListFragment(TwoFragment())
        customViewPagerAdapter.addListFragment(ThreeFragment())
        binding.viewPager.adapter = customViewPagerAdapter
        // TabLayout , ViewPager 연동
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setCustomView(tabCustomView(position))
        }.attach()

    }

    fun tabCustomView(position: Int): View {
        val binding = UsertabButtonBinding.inflate(layoutInflater)
        when (position) {
            0 -> binding.ivIcon.setImageResource(R.drawable.play)
            1 -> binding.ivIcon.setImageResource(R.drawable.like)
            2 -> binding.ivIcon.setImageResource(R.drawable.playlist)

        }
        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startProcess()
            } else {
                Toast.makeText(this, "권한승인을 해야만 앱을 사용할 수 있어요.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startProcess() {
        // 데이타베이스를 조회해서 음악파일이 있다면, 음원정보를 가져와서 데이타베이스 입력했음을 뜻함
        // 데이타베이스를 조회해서 음악파일이 없다면, 음원정보를 가져와서 데이타베이스 입력하지 않음을 뜻함.
        //1. 데이타베이스에서 음원파일을 가져온다.
        var musicDataDBList: MutableList<MusicData>? = mutableListOf<MusicData>()
        musicDataDBList = dbOpenHelper.selectAllMusicTBL()
        Log.e("MainActivity", "musicDataList.size = ${musicDataDBList?.size}")

        if (musicDataDBList == null || musicDataDBList!!.size <= 0) {
            //start 음원정보를 가져옴 **********************************************
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
            )
            val cursor = contentResolver.query(musicUri, projection, null, null, null)

            if (cursor!!.count <= 0) {
                Toast.makeText(this, "메모리에 음악파일에 없습니다. 다운받아주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val title = cursor.getString(1).replace("'", "")
                val artist = cursor.getString(2).replace("'", "")
                val albumId = cursor.getString(3)
                val duration = cursor.getInt(4)
                val musicData = MusicData(id, title, artist, albumId, duration, 0, 0)
                musicDataList?.add(musicData)
            }
            Log.e("MainActivity", "2 musicDataList.size = ${musicDataList?.size}")
            var size = musicDataList?.size
            if (size != null) {
                for (index in 0..size - 1) {
                    val musicData = musicDataList!!.get(index)
                    dbOpenHelper.insertMusicTBL(musicData)
                }
            }

        } else {
            musicDataList = musicDataDBList
        }
        //Adapter와 recyclerview 연결
        Log.e("MainActivity", "음원정보를 연결해서 정보를 가져옴")

    }
}