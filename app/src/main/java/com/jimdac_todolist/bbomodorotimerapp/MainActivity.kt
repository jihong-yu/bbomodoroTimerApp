package com.jimdac_todolist.bbomodorotimerapp

import android.annotation.SuppressLint
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity() {

    private val remainSecondsTextView : TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }

    private val remainMinutesTextView : TextView by lazy {
        findViewById(R.id.remainMinutesTextView)
    }

    private val seekBar:SeekBar by lazy {
        findViewById(R.id.seekbar)
    }
    
    //SoundPool 은 알림사운드,게임효과등 짧은 사운드클립에 적합
    //MediaPlayer 는 노래와 같이 더 큰 사운드파일을 재생할 때 적합합니다.
    //째깍째깍과 같은 짧은 사운드 클립을 이용할것이기 때문에 soundPool사용
    private val soundPool = SoundPool.Builder().build()

    //시간이 흐름에 따라 발생하는 효과음을 적용할 2개의 변수
    private var tickingSoundId : Int? = null
    private var bellSoundId:Int? = null
    
    //카운트타이머 변수
    private var currentCountDownTimer:CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSounds()
        bindViews()
    }
    
    //시크바에 타이머 적용(뷰에 기능추가)
    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    updateRemainTime(progress * 60 * 1000L)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopCountDown()
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar ?: return

                if (seekBar.progress == 0){
                    stopCountDown()
                } else{
                    //사용자가 손 때자 말자 카운트다운 시작
                    startCountDown()
                }
            }
        })
    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        
        //모든 사운드풀에 있는 노래 정지
        soundPool.autoPause()
    }

    private fun startCountDown() {
        currentCountDownTimer = createCountDownTimer()
        currentCountDownTimer?.start()

        //thickingSoundId가 만약 null일때를 처리하기 위한 let사용
        tickingSoundId?.let {
            soundPool.play(it,1.0F,1.0F,0,0,1.0F)
        } //play는 streamID를 반환한다.(음악을 정지,시작할때 필요함) load는 SoundId를 반환
    }
    
    //CountTimer 익명클래스 생성
    private fun createCountDownTimer() : CountDownTimer{

        //1st : 카운트 다운 총 시간, 2nd: 카운트다운 주기
        return object : CountDownTimer(seekBar.progress * 60 * 1000L,1000L){

            //2nd인자인 1초 주기마다 해당 메서드 실행(인자로는 총 시간에서 남은시간까지의 시간이 계속 전달된다.)
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTime(millisUntilFinished)
                //시크바도 계속해서 왼쪽으로 줄어들어야 하기때문에 업데이트 해준다.
                updateSeekBar(millisUntilFinished)

            }
            //완료된 후 처리
            override fun onFinish() {
                completeCountDown()
            }
        }

    }

    private fun completeCountDown() {
        updateRemainTime(0)
        updateSeekBar(0)

        bellSoundId?.let {
            soundPool.play(it,1.0F,1.0F,1,-1,1.0F)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateRemainTime(remainMillis: Long) {

        val remainSeconds =remainMillis / 1000
        //특정자리수가 비어있다면 0을 사용하면 0으로 채워준다.
        //%02d -> 0 : 채워질 문자 , 2: 총자리수 , d: 십진수로 된 정수 ex)소수점표현땐 %.3f 와같이사용
        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60)
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60)
    }

    private fun updateSeekBar(remainMillis: Long){
        seekBar.progress = (remainMillis.toInt() / 1000) / 60
    }

    private fun initSounds(){
        tickingSoundId = soundPool.load(this@MainActivity,R.raw.timer_ticking,1)
        bellSoundId = soundPool.load(this@MainActivity,R.raw.timer_bell,1)
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        //soundPoll과 같은 기능들은 메모리를 많이 먹기 때문에 앱이 꺼진다면 메모리를 없애주는게 좋음
        soundPool.release()
    }
}