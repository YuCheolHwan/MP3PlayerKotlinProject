package com.example.mymp3player

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.example.mymp3player.databinding.ActivityPlayBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlayActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityPlayBinding
    lateinit var dbOpenHelper: DBOpenHelper
    val ALBUM_IMAGE_SIZE = 90
    var mediaPlayer: MediaPlayer? = null
    lateinit var musicData: MusicData
    private var playList: MutableList<Parcelable>? = null
    private var position: Int = 0
    var mp3playerJob: Job? = null
    var pauseFlag = false
    var shuffleFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbOpenHelper =
            DBOpenHelper(binding.root.context, MainActivity.DB_NAME, MainActivity.VERSION)

        //전달해온 intent 값을 가져옴.
        playList = intent.getParcelableArrayListExtra("parcelableList")
        position = intent.getIntExtra("position", 0)
        musicData = playList!!.get(position) as MusicData
        runCreate(musicData)
        binding.btnShuffle.setOnClickListener {
            when (shuffleFlag) {
                true -> {
                    shuffleFlag = false
                    binding.btnShuffle.setImageResource(R.drawable.baseline_shuffle_24)
                }
                false -> {
                    shuffleFlag = true
                    binding.btnShuffle.setImageResource(R.drawable.baseline_shuffle_24_2)
                }
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.listButton -> {
                val intent = Intent(binding.root.context, MainActivity::class.java)
                val parcelableList: ArrayList<Parcelable>? = playList as ArrayList<Parcelable>
                intent.putExtra("parcelableList", parcelableList)
                intent.putExtra("position", position)

                binding.root.context.startActivity(intent)
                finish()
            }
            R.id.playButton -> {
                startMusic()
            }
            R.id.nextSongButton -> {
                mp3playerJob?.cancel()
                mediaPlayer?.stop()
                if (position < (playList!!.size) - 1) {
                    musicData = playList?.get(position + 1) as MusicData
                    runCreate(musicData)
                    var lastPosition = position + 1
                    position = lastPosition
                } else {
                    position = 0
                    musicData = playList?.get(position) as MusicData
                    runCreate(musicData)
                }
                Log.e("aaaaaaaaa", "$position")
            }
            R.id.backSongButton -> {
                mediaPlayer?.stop()
                mp3playerJob?.cancel()
                if (position <= playList!!.size - 1 && position > 0) {
                    musicData = playList?.get(position - 1) as MusicData
                    runCreate(musicData)
                    var lastPosition = position - 1
                    position = lastPosition
                } else {
                    position = playList!!.size - 1
                    musicData = playList?.get(position) as MusicData
                    runCreate(musicData)
                }
                binding.playButton.setImageResource(R.drawable.pause)
                Log.e("aaaaaaaaa", "$position")
            }
        }
    }

    fun runCreate(musicData: MusicData) {
        //화면에 바인딩 진행
        binding.albumTitle.text = musicData.title
        binding.albumArtist.text = musicData.artist
        binding.seekBar.progress = 0
        binding.totalDuration.text = SimpleDateFormat("mm:ss").format(musicData.duration)
        Log.e("ddddddddddd", "${musicData.duration}")
        binding.playDuration.text = "00:00"
        val bitmap = musicData.getAlbumBitmap(this, ALBUM_IMAGE_SIZE)
        if (bitmap != null) {
            binding.albumImage.setImageBitmap(bitmap)
        } else {
            binding.albumImage.setImageResource(R.drawable.ic_launcher_background)
        }
        //음악파일객체 가져옴
        mediaPlayer = MediaPlayer.create(this, musicData.getMusicUri())
        //이벤트처리(일시정지, 실행, 돌아가기, 정지, 시크바 조절)
        binding.listButton.setOnClickListener(this)
        binding.playButton.setOnClickListener(this)
        binding.nextSongButton.setOnClickListener(this)
        binding.backSongButton.setOnClickListener(this)
        binding.seekBar.max = mediaPlayer!!.duration
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        startMusic()
    }

    fun startMusic() {
        binding.playButton.setImageResource(R.drawable.pause)
        var currentPosition = 0
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer?.pause()
            binding.playButton.setImageResource(R.drawable.play)
            pauseFlag = true

        } else {
            mediaPlayer?.start()
            binding.playButton.setImageResource(R.drawable.pause)
            pauseFlag = false

            //코루틴으로 음악을 재생
            val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
            mp3playerJob = backgroundScope.launch {
                try {
                    while (mediaPlayer!!.isPlaying) {
                        if (MainActivity.flag == true) {
                            mediaPlayer!!.pause()
                            Log.e("111111111", "${MainActivity.flag}")
                        }
                        currentPosition = mediaPlayer?.currentPosition!!
                        Log.e("qqqqqqqqq", "${currentPosition}")
                        //코루틴속에서 화면의 값을 변동시키고자 할대 runOnUiThread
                        runOnUiThread {
                            binding.seekBar.progress = currentPosition
                            binding.playDuration.text =
                                SimpleDateFormat("mm:ss").format(mediaPlayer?.currentPosition)
                        }
                        try {
                            delay(1000)
                        } catch (e: java.lang.Exception) {
                            Log.e("PlayActivity", "delay 오류발생 ${e.printStackTrace()}")
                        }
                        runOnUiThread {
                            if (binding.seekBar.max - 1000 <= currentPosition) {
                                mediaPlayer!!.stop()
                                if (shuffleFlag == true) {
                                    val randomPosition: Int = (0..playList!!.size).random().toInt()
                                    Log.e("dasdsadasds", "${randomPosition}")
                                    musicData = playList?.get(randomPosition) as MusicData
                                    var lastPosition = randomPosition
                                    position = lastPosition
                                    runCreate(musicData)
//                                    startMusic()
                                } else {
                                    if (position < (playList!!.size) - 1) {
                                        musicData = playList?.get(position + 1) as MusicData
                                        var lastPosition = position + 1
                                        position = lastPosition
                                        runCreate(musicData)
                                    } else {
                                        position = 0
                                        musicData = playList?.get(position) as MusicData
                                        runCreate(musicData)
                                    }
                                }
                            }
                        }

                    }//end of while
                    while (!mediaPlayer!!.isPlaying) {
                        if (MainActivity.flag == false && pauseFlag == false) {
                            mediaPlayer!!.start()
                            Log.e("22222222", "${MainActivity.flag}")

                        }
                    }
                } catch (e: java.lang.Exception) {
                }
            }//end of mp3PlayerJob
        }
    }

    override fun onBackPressed() {
        mp3playerJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        finish()

    }
}