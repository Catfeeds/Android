package com.yzy.ebag.student.activity.tools.read

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.SpannableString
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import cn.jzvd.JZUtils
import cn.jzvd.JZVideoPlayer
import cn.jzvd.JZVideoPlayerStandard
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.yzy.ebag.student.R
import com.yzy.ebag.student.bean.ReadDetailBean
import com.yzy.ebag.student.bean.ReadOutBean
import com.yzy.ebag.student.bean.ReadUploadResponseBean
import com.yzy.ebag.student.bean.RecordHistory
import com.yzy.ebag.student.http.StudentApi
import ebag.core.base.BaseActivity
import ebag.core.http.network.MsgException
import ebag.core.http.network.RequestCallBack
import ebag.core.http.network.handleThrowable
import ebag.core.util.*
import ebag.core.widget.RoundView
import ebag.hd.bean.response.UserEntity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_read_detail.*
import java.io.File
import java.util.concurrent.TimeUnit

class FollowReadActivity: BaseActivity() {
    private var readBean: ReadOutBean.OralLanguageBean? = null
    private var playingUrl: String? = null
    private var anim : AnimationDrawable? = null
    private var progressBar : ProgressBar? = null
    private lateinit var basePath: String
    private var readDetailBean: ReadDetailBean? = null
    private lateinit var userId: String
    private lateinit var classId: String
    private var tempUrl = ""
    // 上传文件是否成功
    private var ossSuccess = false
    // 语音识别是否成功
    private var recognizeSuccess = false
    // 是否正在上传到阿里云
    private var isOssUploading = false
    // 是否正在语音识别
    private var isRecognizing = false
    private var resultReadStr = ""
    private var subCode = "yy"
    private val voicePlayer by lazy {
        val voicePlayer = VoicePlayerOnline(this)
        voicePlayer.setOnPlayChangeListener(object : VoicePlayerOnline.OnPlayChangeListener{
            override fun onProgressChange(progress: Int) {
                progressBar?.progress = progress
            }
            override fun onCompletePlay() {
                playingUrl = null
                anim?.stop()
                anim?.selectDrawable(0)
                anim = null
                progressBar?.progress = 0
                progressBar = null
            }
        })
        voicePlayer
    }
    private val adapter = Adapter()
    private var request = object :RequestCallBack<List<ReadDetailBean>>(){
        override fun onStart() {
            stateView.showLoading()
        }
        override fun onSuccess(entity: List<ReadDetailBean>?) {
            if(entity == null || entity.isEmpty())
                stateView.showEmpty()
            else {
                stateView.showContent()
                adapter.setNewData(entity)
                if (StringUtils.isEmpty(entity!![0].languageEn)) {
                    subCode = "yw"
                }
                if (subCode == "yy"){
                    spannableArray = arrayOfNulls(entity.size)
                }
            }
        }

        override fun onError(exception: Throwable) {
            stateView.showError()
        }

    }
    companion object {
        fun jump(context: Context,classId: String, readBean: ReadOutBean.OralLanguageBean?){
            context.startActivity(
                    Intent(context, FollowReadActivity::class.java)
                            .putExtra("classId", classId)
                            .putExtra("oralLanguageBean", readBean)
            )
        }
    }
    private lateinit var spannableArray: Array<SpannableString?>
    private lateinit var loadingTv: TextView
    private lateinit var scoreTv: TextView
    private lateinit var recorderProgressBar: RoundView
    private var score = "0"
    private val iflytekUtil by lazy {
        val util = IflytekUtil.getInstance(this)
        util.onEvaluatingResult = {score, resultSpannable ->
            spannableArray[adapter.selectedPosition] = resultSpannable
            this.score = (score * 20).toInt().toString()
            readDetailBean?.score = (score * 20).toInt().toString()
//            scoreTv.visibility = View.VISIBLE
            loadingTv.visibility = View.GONE
            adapter.notifyItemChanged(adapter.selectedPosition)
            upload()
            tempPosition = -1
            codeDisposable?.dispose()
        }
        util
    }
    private var codeDisposable: Disposable? = null
    private fun startCutDown(){
        codeDisposable = Observable
                .intervalRange(0, 10000, 0, 1, TimeUnit.MILLISECONDS)
                .map { it }
                .doOnSubscribe {
                    recorderProgressBar.visibility = View.VISIBLE
                }
                .observeOn(AndroidSchedulers.mainThread())
                //一秒钟改变一次文字
                .doOnNext {
                    recorderProgressBar.progress = (it / 100).toInt()
                }.doOnComplete {//全部完成之后改变状态
                    if (iflytekUtil.isRecording()){
                        iflytekUtil.stopEvaluating()
                        tempPosition = -1
                        loadingTv.visibility = View.VISIBLE
                        scoreTv.visibility = View.GONE
                        recorderProgressBar.progress = 0
                        recorderProgressBar.visibility = View.GONE
                    }
                }.doOnDispose {//取消这个事件后 将其状态重置
                    recorderProgressBar.progress = 0
                    recorderProgressBar.visibility = View.GONE
                }.subscribe()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_read_detail
    }

    override fun initViews() {
        readBean = intent.getSerializableExtra("oralLanguageBean") as ReadOutBean.OralLanguageBean?
        classId = intent.getStringExtra("classId") ?: ""
        val userEntity = SerializableUtils.getSerializable<UserEntity>(ebag.hd.base.Constants.STUDENT_USER_ENTITY)
        userId = userEntity?.uid ?: "userId"
        basePath = "${FileUtil.getRecorderPath()}/$userId/read"
        val file = File(basePath)
        if(!file.exists()){
            file.mkdirs()
        }

        if(readBean == null){
            T.show(this,"出了一些错误，请稍后再试...")
            return
        }else{
            videoPlayer.setUp(readBean!!.languageUrl
                    , JZVideoPlayerStandard.SCREEN_STATE_ON, readBean!!.fileName)
            if(readBean!!.type == "video"){
                videoPlayer.thumbImageView.loadImage(readBean!!.coveUrl)
            }else{
                videoPlayer.thumbImageView.loadImage(readBean!!.coveUrl)
            }


            titleBar.setTitle(readBean!!.fileName)
            contentRecycler.layoutManager = LinearLayoutManager(this)
            contentRecycler.adapter = adapter

            historyRecycler.layoutManager = LinearLayoutManager(this)
            historyRecycler.adapter = historyAdapter

            adapter.setOnItemClickListener { _, view, position ->
                if(recorderUtil.isRecording || iflytekUtil.isRecording()){
                    T.show(this,"请先结束录音")
                }else{
                    adapter.selectedPosition = position
                }
            }

            stateView.setOnRetryClickListener {
                getContent()
            }

            historyStateView.setOnRetryClickListener {
//                getHistory()
            }


            adapter.setOnItemChildClickListener { _, view, position ->
                readDetailBean = adapter.getItem(position)
                val recordFile = File(readDetailBean?.localPath)
                when(view.id){
                    R.id.playerBtn -> {
                        if(recorderUtil.isRecording || iflytekUtil.isRecording()) {
                            T.show(this, "请先结束录音")
                        }else{
                            val urlStr = view.tag as String?
                            if(StringUtils.isEmpty(urlStr)){
                                return@setOnItemChildClickListener
                            }
                            if(urlStr != playingUrl){
                                if(anim != null) {
                                    anim!!.stop()
                                    anim!!.selectDrawable(0)
                                    progressBar?.progress = 0
                                    progressBar = null
                                }
                                anim = view.background as AnimationDrawable
                                voicePlayer.playUrl(urlStr)
                                anim?.start()
                                playingUrl = urlStr
                            }else{
                                if (voicePlayer.isPlaying && !voicePlayer.isPause){
                                    voicePlayer.pause()
                                    anim?.stop()
                                }else{
                                    voicePlayer.play()
                                    anim?.start()
                                }
                            }
                        }
                    }

                    // 录音
                    R.id.recordBtn -> {
                        // 这次点击的和上次点击的 是同一个
                        if(tempPosition == position){//
                            // 正在录， 停止录音，保存音频文件
                            if(recorderUtil.isRecording || iflytekUtil.isRecording()){
                                if(recorderAnim != null){
                                    recorderAnim?.stop()
                                    recorderAnim?.selectDrawable(0)
                                }
                                if (subCode == "yw") {
                                    recorderUtil.pauseRecord()
                                    recorderUtil.stopPlayRecord()
                                    recorderUtil.finishRecord()
                                    upload()
                                    adapter.notifyItemChanged(position)
                                }else{
                                    iflytekUtil.stopEvaluating()
                                    scoreTv = view.getTag(R.id.scoreTv) as TextView
                                    loadingTv = view.getTag(R.id.loadingTv) as TextView
                                    loadingTv.visibility = View.VISIBLE
                                    scoreTv.visibility = View.GONE
                                    codeDisposable?.dispose()
                                }
                                tempPosition = -1
                            }
                        }else{//
                            recorderAnim = view.background as AnimationDrawable
                            if (subCode == "yw") {
                                recorderUtil.setFinalFileName(recordFile.absolutePath)
                                recorderUtil.startRecord()
                                recorderAnim?.start()
                            }else{
                                iflytekUtil.startEvaluating(this, readDetailBean?.languageEn ?: "", recordFile.absolutePath)
                                scoreTv = view.getTag(R.id.scoreTv) as TextView
                                loadingTv = view.getTag(R.id.loadingTv) as TextView
                                recorderProgressBar = view.getTag(R.id.progressBar) as RoundView
                                startCutDown()
                            }
                            tempPosition = position
                        }
                    }

                    // 播放自己的音频
                    R.id.playSelf -> {
                        if(recorderUtil.isRecording) {
                            T.show(this, "请先结束录音")
                        }else {
                            // 本地不存在录音
                            if(!recordFile.exists()){
                                T.show(this,"暂未找到您的本地录音文件，请重新录制")
                                adapter.notifyItemChanged(position)
                            }else{// 本地存在录音文件
                                if(playingUrl != recordFile.absolutePath){
                                    if(anim != null) {
                                        anim!!.stop()
                                        anim!!.selectDrawable(0)
                                        progressBar?.progress = 0
                                        progressBar = null
                                    }
                                    voicePlayer.playUrl(recordFile.absolutePath)
                                    playingUrl = recordFile.absolutePath
                                }else{
                                    if (voicePlayer.isPlaying && !voicePlayer.isPause){
                                        voicePlayer.pause()
                                        anim?.stop()
                                    }else{
                                        voicePlayer.play()
                                        anim?.start()
                                    }
                                }
                            }
                        }
                    }
                    // 提交录音
                    R.id.submit -> {
                        if(recorderUtil.isRecording) {
                            T.show(this, "请先结束录音")
                        }else {
                            // 服务器存在录音
                            if(readDetailBean?.checkLanguage == "Y"){
                                dialogNetExist.show()
                            }else{
                                upload()
                            }

                        }
                    }
                }
            }

            historyAdapter.setOnItemChildClickListener { _, view, position ->
                if(view.id == R.id.play_id){
                    val recordHistory = historyAdapter.getItem(position)

                    if(recordHistory?.myAudioUrl == null){
                        T.show(this,"对不起，文件丢失，请重新录制上传")
                    }else{
                        if(playingUrl != recordHistory.myAudioUrl){
                            if(anim != null) {
                                anim!!.stop()
                                anim!!.selectDrawable(0)
                                progressBar?.progress = 0
                                progressBar = null
                            }
                            anim = (view.getTag(R.id.image_id) as View).background as AnimationDrawable
                            progressBar = view.getTag(R.id.progress_id) as ProgressBar
                            anim?.start()
                            progressBar?.progress = 0
                            voicePlayer.playUrl(recordHistory.myAudioUrl)
                            playingUrl = recordHistory.myAudioUrl
                        }else{
                            if (voicePlayer.isPlaying && !voicePlayer.isPause){
                                voicePlayer.pause()
                                anim?.stop()
                            }else{
                                voicePlayer.play()
                                anim?.start()
                            }
                        }
                    }
                }
            }
//            getHistory()
            getContent()
        }


    }

    /**
     * 上传文件
     */
    private fun upload(){
        uploadFile()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(voicePlayer.isPlaying){
            voicePlayer.stop()
        }

        if(recorderUtil.isRecording){
            recorderUtil.stopPlayRecord()
            recorderUtil.finishRecord()
        }
        if (iflytekUtil.isRecording()){
            iflytekUtil.stopEvaluating()
        }
        historyRequest.cancelRequest()
        request.cancelRequest()
        uploadRequest.cancelRequest()
    }



    override fun onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if(recorderUtil.isRecording){
            recorderUtil.pauseRecord()
        }
        JZVideoPlayer.releaseAllVideos()
    }

    private var recorderAnim: AnimationDrawable? = null
    private val recorderUtil by lazy {
        RecorderUtil(this)
    }
    private var tempPosition: Int = -1

    /**
     * 本地存在录音的提示
     */
    private val dialogExists by lazy {
        AlertDialog.Builder(this)
                .setMessage("录音已存在，是否重新录制？")
                .setCancelable(false)
                .setNegativeButton("取消", { dialog, _ ->
                    dialog.dismiss()
                })
                .setPositiveButton("重新录音", {_, _ ->
                    if (subCode == "yw") {
                        recorderUtil.setFinalFileName(readDetailBean?.localPath)
                        recorderUtil.startRecord()
                    }else{
                        iflytekUtil.startEvaluating(this, readDetailBean?.languageEn ?: "", readDetailBean?.localPath ?: "")
                    }
                    recorderAnim!!.start()
                })
                .create()
    }

    /**
     * 服务器存在 当前句子录音的提示
     */
    private val dialogNetExist by lazy {
        AlertDialog.Builder(this)
                .setMessage("服务器已存在录音，是否覆盖？")
                .setCancelable(false)
                .setNegativeButton("取消", { dialog, _ ->
                    dialog.dismiss()
                })
                .setPositiveButton("覆盖", {_, _ ->
                    upload()
                })
                .create()
    }

    /**
     * 右侧的adapter
     */
    inner class Adapter: BaseQuickAdapter<ReadDetailBean, BaseViewHolder>(R.layout.item_activity_read_load){

        private var oldPosition = -1
        var selectedPosition = -1
        set(value) {
            if(field != value){
                field = value
                if(oldPosition != -1)
                    notifyItemChanged(oldPosition)
                notifyItemChanged(field)
                oldPosition = value
            }
        }

        override fun convert(helper: BaseViewHolder, item: ReadDetailBean?) {
            val file = File(basePath, "${item?.languageDetailId}.wav")
            item?.localPath = file.absolutePath
            item?.position = helper.adapterPosition
            val isSelected = selectedPosition == helper.adapterPosition
            helper.setText(R.id.tvSecond, item?.languageCn)
                    .setGone(R.id.playerBtn, isSelected)
                    .setGone(R.id.recordBtn, isSelected)
                    .setGone(R.id.playSelf, file.exists() && isSelected)
//                    .setGone(R.id.submit, file.exists() && isSelected)
                    .setTag(R.id.playerBtn, item?.languageUrl)
                    .addOnClickListener(R.id.playerBtn)
                    .addOnClickListener(R.id.recordBtn)
                    .addOnClickListener(R.id.playSelf)
                    .addOnClickListener(R.id.submit)
                    .getView<View>(R.id.layout).isSelected = isSelected
            val recorderBtn = helper.getView<View>(R.id.recordBtn)
            recorderBtn.setTag(R.id.loadingTv, helper.getView(R.id.loadingTv))
            recorderBtn.setTag(R.id.progressBar, helper.getView(R.id.progressBar))
            val scoreTv = helper.getView<TextView>(R.id.scoreTv)
            if (!StringUtils.isEmpty(item?.score)){
                scoreTv.visibility = View.VISIBLE
                scoreTv.text = item?.score
            }else{
                scoreTv.visibility = View.GONE
            }
            recorderBtn.setTag(R.id.scoreTv, scoreTv)
            if (subCode == "yw"){
                scoreTv.visibility = View.GONE
            }
            if (::spannableArray.isInitialized && spannableArray[helper.adapterPosition] != null){
                helper.setText(R.id.tvFirst, spannableArray[helper.adapterPosition])
            }else{
                helper.setText(R.id.tvFirst, item?.languageEn)
            }
        }

    }

    private fun getContent(){
        StudentApi.getReadDetailList(readBean?.id ?: "", request)
    }
    private fun getHistory(){
        StudentApi.recordHistory(readBean?.id ?: "", historyRequest)
    }

    inner class HistoryAdapter: BaseQuickAdapter<RecordHistory, BaseViewHolder>(R.layout.item_activity_record_history){
        var resultPosition = -1
        set(value) {
            field = value
            notifyItemChanged(resultPosition)
        }
        override fun convert(helper: BaseViewHolder, item: RecordHistory?) {
            helper.setText(R.id.tvContent, if (StringUtils.isEmpty(item?.languageEn)) item?.languageCn else item?.languageEn)
                    .setText(R.id.scoreEdit, item?.score ?: "")
                    .addOnClickListener(R.id.play_id)
                    .setTag(R.id.play_id, R.id.image_id, helper.getView(R.id.image_id))
                    .setTag(R.id.play_id, R.id.progress_id, helper.getView(R.id.progress_id))
            if (resultPosition != -1 && resultPosition == helper.adapterPosition && !StringUtils.isEmpty(resultReadStr)){
                val contentTv = helper.getView<TextView>(R.id.tvContent)
                contentTv.setTextColor(Color.parseColor("#2ED33E"))
                contentTv.text = Html.fromHtml(resultReadStr)
            }
        }
    }


//    private lateinit var systemTime: String
    /**
     * 上传阿里云，如果多条历史记录 用systemTime 上传，单挑用ID上传
     */
    private fun uploadFile(){
        if(classId.isEmpty()){
            T.show(this,"现在不能上传您的录音，请返回首页获取，当前班级")
            return
        }
        // 文件上传没成功
        isOssUploading = true
        //上传录音
        tempUrl = ""
        OSSUploadUtils.getInstance().uploadFileToOss(
                this,
                File(readDetailBean?.localPath),
                "personal/$userId/read",
                "${readDetailBean?.languageDetailId}.wav",
                mHandler)
    }

    /**
     * 阿里云 上传文件
     */
    private val mHandler = MyHandler(this)
    class MyHandler(activity: FollowReadActivity): HandlerUtil<FollowReadActivity>(activity){
        override fun handleMessage(activity: FollowReadActivity?, msg: Message?) {
            when {
                msg!!.what == Constants.UPLOAD_SUCCESS ->{//上传文件成功
                    activity?.isOssUploading = false
                    activity?.ossSuccess = true
                    activity?.tempUrl = "${Constants.OSS_BASE_URL}/personal/${activity!!.userId}/read/${activity.readDetailBean?.languageDetailId}.wav"
                    StudentApi.uploadRecord(
                            activity.classId,
                            activity.readDetailBean?.languageId ?: "",
                            activity.readDetailBean?.languageDetailId ?: "",
                            activity.score,
                            activity.tempUrl , activity.uploadRequest)
                }
                msg.what == Constants.UPLOAD_FAIL ->{//上传文件失败
                    activity?.isOssUploading = false
                    if(activity?.isRecognizing == false){
                        LoadingDialogUtil.closeLoadingDialog()
                        T.show(activity, "录音上传失败，请重试")
                    }

                }
            }
        }
    }

    /**
     * 上传录音url
     */
    private val uploadRequest = object: RequestCallBack<ReadUploadResponseBean>(){
        override fun onSuccess(entity: ReadUploadResponseBean?) {
            LoadingDialogUtil.closeLoadingDialog()
            ossSuccess = false
            recognizeSuccess = false
            tempUrl = ""
            resultReadStr = entity?.hightingString ?: ""
//            getHistory()
//            T.show(this@FollowReadActivity, "我的录音上传成功")
        }

        override fun onError(exception: Throwable) {
            LoadingDialogUtil.closeLoadingDialog()
            if(exception is MsgException){
                T.show(this@FollowReadActivity, exception.message ?: "录音上传失败，请重试")
            }else{
                T.show(this@FollowReadActivity, "录音上传失败，请重试")
            }
        }
    }

    private val historyAdapter = HistoryAdapter()
    private val historyRequest = object :RequestCallBack<List<RecordHistory>>(){

        override fun onStart() {
            emptyLayout.visibility = View.VISIBLE
            historyStateView.showLoading()
        }

        override fun onSuccess(entity: List<RecordHistory>?) {
            historyAdapter.setNewData(entity)
            if(entity != null && entity.isNotEmpty()){
                emptyLayout.visibility = View.GONE
                historyStateView.showContent()
            }else{
                emptyLayout.visibility = View.VISIBLE
                historyStateView.showEmpty()
            }
            historyAdapter.resultPosition = 0
        }

        override fun onError(exception: Throwable) {
            emptyLayout.visibility = View.VISIBLE
            if(exception is MsgException){
                historyStateView.showError(exception.message)
            }else{
                historyStateView.showError()
            }
            exception.handleThrowable(this@FollowReadActivity)
        }

    }
    //视频播放退出全屏的时候要保持横屏
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        JZUtils.setRequestedOrientation(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    }

}