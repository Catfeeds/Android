package com.yzy.ebag.teacher.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.yzy.ebag.teacher.R
import com.yzy.ebag.teacher.bean.CorrectAnswerBean
import com.yzy.ebag.teacher.http.TeacherApi
import com.yzy.ebag.teacher.widget.MarkDialog
import ebag.core.base.BaseActivity
import ebag.core.bean.QuestionBean
import ebag.core.bean.QuestionTypeUtils
import ebag.core.http.network.RequestCallBack
import ebag.core.http.network.handleThrowable
import ebag.core.util.*
import ebag.core.xRecyclerView.adapter.OnItemChildClickListener
import ebag.core.xRecyclerView.adapter.RecyclerViewHolder
import ebag.hd.widget.FlowLayout
import ebag.hd.widget.questions.*
import ebag.hd.widget.questions.base.BaseQuestionView
import kotlinx.android.synthetic.main.activity_correcting_desc.*
import java.util.*

class CorrectingDescActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_correcting_desc
    }
    companion object {
        fun jump(context: Context, homeworkId: String, type: String){
            context.startActivity(
                    Intent(context, CorrectingDescActivity::class.java)
                            .putExtra("homeworkId", homeworkId)
                            .putExtra("type", type)
            )
        }
    }
    private var isFirstRequest: Boolean = true
    private var homeworkId: String = ""
    private var type = ""
    private var questionList : ArrayList<QuestionBean>? = null
    private var currentQuestionIndex = 0
    private val answerAdapter by lazy { StudentAnswerAdapter() }
    private val markDialog by lazy { MarkDialog(this) }
    private val questionRequest = object : RequestCallBack<List<QuestionBean>>(){
        override fun onStart() {
            stateView.showLoading()
            isFirstRequest = true
        }
        override fun onSuccess(entity: List<QuestionBean>?) {
            if (entity == null || entity.isEmpty()){
                stateView.showEmpty()
                return
            }
            TeacherApi.correctStudentAnswer(homeworkId, entity[0].questionId, answerRequest)
            questionList = entity as ArrayList<QuestionBean>
            questionList?.forEach {
                it.answer = it.rightAnswer
            }
            showQuestion(entity[0])
            stateView.showContent()
            questionNum.text = "第${currentQuestionIndex + 1}题/共${questionList?.size}题"
        }

        override fun onError(exception: Throwable) {
            stateView.showError()
            exception.handleThrowable(this@CorrectingDescActivity)
        }
    }
    private val answerRequest = object : RequestCallBack<List<CorrectAnswerBean>>(){
        override fun onStart() {
            if (!isFirstRequest){
                LoadingDialogUtil.showLoading(this@CorrectingDescActivity)
            }
        }

        override fun onSuccess(entity: List<CorrectAnswerBean>?) {
            isFirstRequest = false
            LoadingDialogUtil.closeLoadingDialog()
            if (entity == null || entity.isEmpty()){
                return
            }
            entity.forEach {
                it.setType(QuestionTypeUtils.getIntType(questionList!![currentQuestionIndex]))
            }
            answerAdapter.setNewData(entity)
        }

        override fun onError(exception: Throwable) {
            LoadingDialogUtil.closeLoadingDialog()
        }
    }
    override fun initViews() {
        homeworkId = intent.getStringExtra("homeworkId") ?: ""
        type = intent.getStringExtra("type") ?: ""
        TeacherApi.correctWork(homeworkId, type, questionRequest)
        nextQuestion.setOnClickListener {
            if (questionList == null || currentQuestionIndex >= questionList!!.size -1){
                return@setOnClickListener
            }
            currentQuestionIndex ++
            setAnswerDesc()
        }
        previewQuestion.setOnClickListener {
            if (questionList == null || currentQuestionIndex <= 0){
                return@setOnClickListener
            }
            currentQuestionIndex --
            setAnswerDesc()
        }
        answerRecycler.adapter = answerAdapter
        answerRecycler.layoutManager = LinearLayoutManager(this)

        titleBar.setOnRightClickListener {
            CommentActivity.jump(this@CorrectingDescActivity, homeworkId, type)
        }

        answerAdapter.setOnItemChildClickListener { adapter, view, position ->
            voicePlaySetting(view)
        }
    }
    private fun setAnswerDesc(){
        questionNum.text = "第${currentQuestionIndex + 1}题/共${questionList?.size}题"
        showQuestion(questionList!![currentQuestionIndex])
        TeacherApi.correctStudentAnswer(homeworkId, questionList!![currentQuestionIndex].questionId, answerRequest)
        titleBar.setTitle(QuestionTypeUtils.getTitle(questionList!![currentQuestionIndex].type))
    }
    inner class StudentAnswerAdapter: BaseMultiItemQuickAdapter<CorrectAnswerBean, BaseViewHolder>(null){
        init {
            addItemType(QuestionTypeUtils.QUESTIONS_CLASSIFICATION, R.layout.item_correcting_answer_classification)
            addItemType(QuestionTypeUtils.QUESTIONS_DRAW_LINE, R.layout.item_correcting_answer_connection)
            addItemType(QuestionTypeUtils.QUESTIONS_FOLLOW_READ, R.layout.item_correcting_answer_follow_read)
            addItemType(QuestionTypeUtils.QUESTIONS_CHINESE_READ_UNDERSTAND, R.layout.item_correcting_answer_mark)
            addItemType(QuestionTypeUtils.QUESTION_MATH_APPLICATION, R.layout.item_correcting_answer_mark)
            addItemType(QuestionTypeUtils.QUESTIONS_WRITE_COMPOSITION_BY_PIC, R.layout.item_correcting_answer_mark)

            addItemType(QuestionTypeUtils.QUESTIONS_CHOOSE_PIC_BY_WORD, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_CHOOSE_WORD_BY_PIC, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_WRITE_WORD_BY_PIC, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_COMPLETION_BY_VOICE, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_EN_ORDER_SENTENCE, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_CN_ORDER_SENTENCE, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_JUDGE, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_CHOICE, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_CHOOSE_BY_VOICE, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_COMPLETION, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTION_MATH_VERTICAL, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTION_MATH_EQUATION, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTION_MATH_FRACTION, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_CHINESE_SENTENCE, R.layout.item_correcting_answer_normal)
            addItemType(QuestionTypeUtils.QUESTIONS_CHINESE_WRITE_BY_VOICE, R.layout.item_correcting_answer_normal)
        }
        override fun convert(helper: BaseViewHolder, item: CorrectAnswerBean) {
            val isComplete = false
            val studentAnswer = item.studentAnswer
            helper.setText(R.id.studentName, item.studentName)
                    .setText(R.id.bagId, "书包号：${item.ysbCode}")
            if(StringUtils.isEmpty(studentAnswer)){
                helper.getView<TextView>(R.id.commitTime).visibility = View.GONE
                helper.setText(R.id.answerTv, "未完成")
//                helper.getView<TextView>(R.id.correctIcon).visibility = View.GONE
                return
            }
            helper.getView<TextView>(R.id.commitTime).visibility = View.VISIBLE
//            helper.getView<TextView>(R.id.correctIcon).visibility = View.VISIBLE
            helper.setText(R.id.commitTime, "提交时间：${DateUtil.getFormatDateTime(Date(item.endTime.toLong()), "yyyy-MM-dd HH:mm:ss")}")
            helper.setText(R.id.answerTv, "学生答案：")
            when(helper.itemViewType){
                //纯文字
                QuestionTypeUtils.QUESTIONS_CHOOSE_PIC_BY_WORD,
                QuestionTypeUtils.QUESTIONS_CHOOSE_WORD_BY_PIC,
                QuestionTypeUtils.QUESTIONS_EN_ORDER_SENTENCE,
                QuestionTypeUtils.QUESTIONS_CN_ORDER_SENTENCE,
                QuestionTypeUtils.QUESTIONS_JUDGE,
                QuestionTypeUtils.QUESTIONS_CHOICE,
                QuestionTypeUtils.QUESTIONS_CHOOSE_BY_VOICE,
                QuestionTypeUtils.QUESTIONS_CHINESE_SENTENCE,
                QuestionTypeUtils.QUESTION_MATH_EQUATION,
                QuestionTypeUtils.QUESTION_MATH_VERTICAL,
                QuestionTypeUtils.QUESTION_MATH_FRACTION,
                QuestionTypeUtils.QUESTIONS_CHINESE_WRITE_BY_VOICE
                ->{
                    helper.setText(R.id.studentAnswer, studentAnswer)
                }
                //#R#分隔
                QuestionTypeUtils.QUESTIONS_WRITE_WORD_BY_PIC,
                QuestionTypeUtils.QUESTIONS_COMPLETION_BY_VOICE,
                QuestionTypeUtils.QUESTIONS_COMPLETION
                ->{
                    helper.setText(R.id.studentAnswer, studentAnswer.replace("#R#","、"))
                }
                //连线题
                QuestionTypeUtils.QUESTIONS_DRAW_LINE ->{
                    val connectionView = helper.getView<ConnectionView>(R.id.connectionView)
                    val questionBean = questionList!![currentQuestionIndex].clone() as QuestionBean
                    questionBean.answer = studentAnswer
                    connectionView.setData(questionBean)
                    connectionView.show(false)
                    connectionView.showResult()
                }
                //分类题
                QuestionTypeUtils.QUESTIONS_CLASSIFICATION ->{
                    val linearLayout = helper.getView<LinearLayout>(R.id.classifyLayout)
                    linearLayout.removeAllViews()
                    val splitCategory = studentAnswer.split(";")
                    splitCategory.forEach {
                        val category = it.split("#R#")
                        val categoryName = category[0]
                        val elementSplit = category[1].split(",")

                        val answerLayout = LinearLayout(this@CorrectingDescActivity)
                        answerLayout.orientation = LinearLayout.VERTICAL
                        val answerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        answerLayout.layoutParams = answerParams

                        val categoryTv = TextView(this@CorrectingDescActivity)
                        val categoryTvParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        categoryTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.tv_normal))
                        categoryTv.setTextColor(resources.getColor(R.color.tv_normal))
                        categoryTvParams.topMargin = resources.getDimensionPixelSize(R.dimen.y15)
                        categoryTv.text = categoryName

                        answerLayout.addView(categoryTv)

                        val elementLayout = FlowLayout(this@CorrectingDescActivity)
                        val elementLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        elementLayout.layoutParams = elementLayoutParams
                        if (elementSplit[0].startsWith("http")){    //图片
                            elementSplit.forEach {
                                val imageView = ImageView(this@CorrectingDescActivity)
                                imageView.setPadding(
                                        resources.getDimension(R.dimen.x5).toInt(),
                                        resources.getDimension(R.dimen.x5).toInt(),
                                        resources.getDimension(R.dimen.x5).toInt(),
                                        resources.getDimension(R.dimen.x5).toInt())
                                imageView.setBackgroundResource(ebag.hd.R.drawable.classify_element_bg)
                                val elementImgParams = LinearLayout.LayoutParams(
                                        resources.getDimension(R.dimen.x80).toInt(),
                                        resources.getDimension(R.dimen.x80).toInt())
                                elementImgParams.leftMargin = resources.getDimension(R.dimen.x8).toInt()
                                elementImgParams.topMargin = resources.getDimension(R.dimen.y5).toInt()
                                elementImgParams.rightMargin = resources.getDimension(R.dimen.x8).toInt()
                                elementImgParams.bottomMargin = resources.getDimension(R.dimen.y10).toInt()
                                imageView.layoutParams = elementImgParams
                                imageView.loadImage(it)

                                elementLayout.addView(imageView)
                            }
                            answerLayout.addView(elementLayout)
                        }else{  //文字
                            val elementTv = TextView(this@CorrectingDescActivity)
                            elementTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.tv_normal))
                            elementTv.setTextColor(resources.getColor(R.color.tv_normal))
                            elementTv.text = category[1]
                            answerLayout.addView(elementTv)
                        }
                        linearLayout.addView(answerLayout)
                    }
                }
                //跟读
                QuestionTypeUtils.QUESTIONS_FOLLOW_READ,QuestionTypeUtils.QUESTIONS_CHINESE_WRITE_BY_VOICE ->{
                    val linearLayout = helper.getView<LinearLayout>(R.id.play_id)
                    val imageView = helper.getView<ImageView>(R.id.image_id)
                    val progressBar = helper.getView<ProgressBar>(R.id.progress_id)
                    val drawable = imageView.background as AnimationDrawable
                    linearLayout.setTag(ebag.hd.R.id.image_id, drawable)
                    linearLayout.setTag(ebag.hd.R.id.progress_id, progressBar)
                    linearLayout.setTag(ebag.hd.R.id.play_id, "#M#${item.studentAnswer}")
                    helper.addOnClickListener(R.id.play_id)
                }
                QuestionTypeUtils.QUESTIONS_CHINESE_READ_UNDERSTAND,    //阅读理解
                QuestionTypeUtils.QUESTION_MATH_APPLICATION,            //应用题
                QuestionTypeUtils.QUESTIONS_WRITE_COMPOSITION_BY_PIC    //作文
                ->{
                    val markBtn = helper.getView<TextView>(R.id.markBtn)
                    val score = item.questionScore
                    if (StringUtils.isEmpty(score)){
                        markBtn.setOnClickListener {
                            markDialog.show(data, helper.adapterPosition, questionList!![currentQuestionIndex].questionId)
                        }
                    }else{
                        markBtn.isEnabled = false
                        helper.setText(R.id.score, score)
                    }
                }
            }
        }
    }
    private val questionClickListener : QuestionItemChildClickListener by lazy { QuestionItemChildClickListener() }
    private val voicePlayer : VoicePlayerOnline by lazy {
        val player = VoicePlayerOnline(this)
        player.setOnPlayChangeListener(object : VoicePlayerOnline.OnPlayChangeListener{
            override fun onProgressChange(progress: Int) {
                progressBar!!.progress = progress
            }
            override fun onCompletePlay() {
                tempUrl = null
                anim!!.stop()
                anim!!.selectDrawable(0)
                progressBar!!.progress = 0
            }
        })
        player
    }
    private var anim : AnimationDrawable? = null
    private var progressBar : ProgressBar? = null
    private var tempUrl: String? = null

    inner class QuestionItemChildClickListener : OnItemChildClickListener {
        override fun onItemChildClick(holder: RecyclerViewHolder, view: View, position: Int) {
            voicePlaySetting(view)
        }
    }
    private fun voicePlaySetting(view: View){
        var url : String = view.getTag(R.id.play_id) as String
        url = url.substring(3, url.length)
        if (StringUtils.isEmpty(url))
            return
        if (url != tempUrl){
            if(anim != null) {
                anim!!.stop()
                anim!!.selectDrawable(0)
                progressBar!!.progress = 0
            }
            anim = view.getTag(R.id.image_id) as AnimationDrawable
            progressBar = view.getTag(R.id.progress_id) as ProgressBar
            voicePlayer.playUrl(url)
            anim!!.start()
            tempUrl = url
        }else{
            if (voicePlayer.isPlaying && !voicePlayer.isPause){
                voicePlayer.pause()
                anim!!.stop()
            }else{
                voicePlayer.play()
                anim!!.start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (voicePlayer.isPlaying && !voicePlayer.isPause) {
            voicePlayer.pause()
            anim!!.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voicePlayer.stop()
    }

    private fun showQuestion(questionBean: QuestionBean){
        questionLayout.removeAllViews()

        var questionView: BaseQuestionView? = null
        when(QuestionTypeUtils.getIntType(questionBean)){
            QuestionTypeUtils.QUESTIONS_CHOOSE_PIC_BY_WORD,
            QuestionTypeUtils.QUESTIONS_CHOOSE_WORD_BY_PIC,
            QuestionTypeUtils.QUESTIONS_JUDGE,
            QuestionTypeUtils.QUESTIONS_CHOICE,
            QuestionTypeUtils.QUESTIONS_CHOOSE_BY_VOICE->{
                questionView = ChoiceView(this)
            }
            QuestionTypeUtils.QUESTIONS_WRITE_WORD_BY_PIC,
            QuestionTypeUtils.QUESTIONS_COMPLETION_BY_VOICE,
            QuestionTypeUtils.QUESTIONS_COMPLETION->{
                questionView = CompleteView(this)
            }
            QuestionTypeUtils.QUESTIONS_EN_ORDER_SENTENCE->{
                questionView = SortHorizontalView(this)
            }
            QuestionTypeUtils.QUESTIONS_CN_ORDER_SENTENCE->{
                questionView = SortVerticalView(this)
            }
            QuestionTypeUtils.QUESTIONS_DRAW_LINE->{
                questionView = ConnectionView(this)
            }
            QuestionTypeUtils.QUESTIONS_CLASSIFICATION->{
                questionView = ClassificationView(this)
            }
            QuestionTypeUtils.QUESTIONS_READ_ALOUD, QuestionTypeUtils.QUESTIONS_FOLLOW_READ->{
                questionView = RecorderView(this)
                questionView.setOnItemChildClickListener(questionClickListener)
            }
            QuestionTypeUtils.QUESTIONS_WRITE_COMPOSITION_BY_PIC,
            QuestionTypeUtils.QUESTIONS_CHINESE_SENTENCE,
            QuestionTypeUtils.QUESTIONS_CHINESE_WRITE_BY_VOICE,
            QuestionTypeUtils.QUESTION_MATH_APPLICATION->{
                questionView = SentenceView(this)
            }
            QuestionTypeUtils.QUESTION_MATH_VERTICAL->{
                questionView = MathVerticalView(this)
            }
            QuestionTypeUtils.QUESTION_MATH_EQUATION->{
                questionView = MathEquationView(this)
            }
            QuestionTypeUtils.QUESTION_MATH_FRACTION->{
                questionView = MathFractionView(this)
            }
            QuestionTypeUtils.QUESTIONS_CHINESE_READ_UNDERSTAND->{
                questionView = UnderstandView(this)
            }
            QuestionTypeUtils.QUESTIONS_WRITE->{
                questionView = WriteView(this)
            }
        }
        questionLayout.addView(questionView)

        questionView?.setData(questionBean)
        questionView?.show(false)
        analyseTv.text = if (StringUtils.isEmpty(questionBean.analytical)) "暂无" else questionBean.analytical
    }
}
