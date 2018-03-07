package com.yzy.ebag.teacher.ui.activity

import android.content.Context
import android.content.Intent
import android.view.View
import com.yzy.ebag.teacher.R
import com.yzy.ebag.teacher.base.Constants
import com.yzy.ebag.teacher.http.TeacherApi
import ebag.core.base.BaseActivity
import ebag.core.http.network.MsgException
import ebag.core.http.network.RequestCallBack
import ebag.core.http.network.handleThrowable
import ebag.core.util.DateUtil
import ebag.hd.bean.response.NoticeBean
import kotlinx.android.synthetic.main.activity_space.*
import java.util.*

class SpaceActivity : BaseActivity(), View.OnClickListener {
    override fun getLayoutId(): Int {
        return R.layout.activity_space
    }
    private val noticeRequest = object : RequestCallBack<NoticeBean>(){
        override fun onStart() {

        }

        override fun onSuccess(entity: NoticeBean?) {
            showContent(false, entity)
        }
        override fun onError(exception: Throwable) {
            if(exception is MsgException)
                showContent(true, null)
            else
                exception.handleThrowable(this@SpaceActivity)
        }
    }
    lateinit var classId: String
    companion object {
        fun jump(context: Context, classId: String, className: String){
            context.startActivity(
                    Intent(context, SpaceActivity::class.java)
                            .putExtra("classId", classId)
                            .putExtra("className", className)
            )
        }
    }

    override fun initViews() {
        classId = intent.getStringExtra("classId")
        val className = intent.getStringExtra("className")
        chat.text = className

        course.setOnClickListener(this)
        noticeHistoryBtn.setOnClickListener(this)
        studyGroup.setOnClickListener(this)
        publishNoticeBtn.setOnClickListener(this)

        TeacherApi.newestNotice(classId, noticeRequest)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.course ->{
                MyCourseActivity.jump(this, classId)
            }
            R.id.noticeHistoryBtn ->{
                NoticeHistoryActivity.jump(this, classId)
            }
            R.id.studyGroup ->{
                StudyGroupActivity.jump(this, classId)
            }
            R.id.publishNoticeBtn ->{
                PublishContentActivity.jump(this, classId)
            }
        }
    }

    private fun showContent(isEmpty: Boolean, entity: NoticeBean?){
        if (isEmpty) {
            empty_content.visibility = View.VISIBLE
        }else{
            if (entity != null) {
                empty_content.visibility = View.INVISIBLE
                teacherName.text = entity.name
                noticeTime.text = getString(R.string.notice_time, DateUtil.getFormatDateTime(Date(entity.createDate), "yyyy-MM-dd"))
                descTv.text = entity.content
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.PUBLISH_REQUEST && resultCode == Constants.PUBLISH_RESULT)
            TeacherApi.newestNotice(classId, noticeRequest)
    }

}
