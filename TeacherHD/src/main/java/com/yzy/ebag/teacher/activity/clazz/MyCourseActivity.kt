package com.yzy.ebag.teacher.activity.clazz

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.yzy.ebag.teacher.R
import com.yzy.ebag.teacher.bean.MyCourseBean
import com.yzy.ebag.teacher.http.TeacherApi
import com.yzy.ebag.teacher.widget.AddCourseDialog
import ebag.core.http.network.RequestCallBack
import ebag.core.http.network.handleThrowable
import ebag.core.util.DateUtil
import ebag.core.util.LoadingDialogUtil
import ebag.core.util.T
import ebag.core.util.loadImage
import ebag.hd.base.BaseListActivity
import java.util.*

class MyCourseActivity : BaseListActivity<List<MyCourseBean>, MyCourseBean>() {
    private val adapter by lazy { MyAdapter() }
    private lateinit var classId: String
    private var gradeCode = "1"
    private val addCourseDialog by lazy {
        val dialog = AddCourseDialog(this)
        dialog.onAddCourseSuccess = {
            onRetryClick()
        }
        dialog
    }
    private var currentPosition = -1
    private val deleteCourseDialog by lazy {
        val dialog = AlertDialog.Builder(this)
                .setMessage("删除当前所教课程？")
                .setPositiveButton("删除", {_, _ ->
                    TeacherApi.deleteCourse(adapter.data[currentPosition].id, deleteRequest)
                })
                .setNegativeButton("取消", null)
                .create()
        dialog
    }
    private val deleteRequest = object : RequestCallBack<String>(){
        override fun onStart() {
            LoadingDialogUtil.showLoading(this@MyCourseActivity, "正在删除...")
        }

        override fun onSuccess(entity: String?) {
            LoadingDialogUtil.closeLoadingDialog()
            T.show(this@MyCourseActivity, "删除成功")
            adapter.remove(currentPosition)
        }

        override fun onError(exception: Throwable) {
            LoadingDialogUtil.closeLoadingDialog()
            exception.handleThrowable(this@MyCourseActivity)
        }
    }
    companion object {
        fun jump(activity: Activity, classId: String, gradeCode: String){
            activity.startActivity(Intent(activity, MyCourseActivity::class.java)
                    .putExtra("classId", classId)
                    .putExtra("gradeCode", gradeCode)
            )
        }
    }
    override fun loadConfig(intent: Intent) {
        loadMoreEnabled(false)
        refreshEnabled(false)
        titleBar.setTitle(R.string.course_teaching)
        titleBar.setRightText(resources.getString(R.string.add), {
            addCourseDialog.show(classId, gradeCode)
        })
        classId = intent.getStringExtra("classId")
        gradeCode = intent.getStringExtra("gradeCode")
        adapter.setOnItemLongClickListener { adapter, view, position ->
            currentPosition = position
            deleteCourseDialog.show()
            true
        }
    }

    override fun requestData(page: Int, requestCallBack: RequestCallBack<List<MyCourseBean>>) {
        TeacherApi.searchCourse(classId, requestCallBack)
    }

    override fun parentToList(isFirstPage: Boolean, parent: List<MyCourseBean>?): List<MyCourseBean>? {
        if(parent != null && !parent.isEmpty()){
            gradeCode = parent[0].gradeCode
        }
        return parent
    }

    override fun getAdapter(): BaseQuickAdapter<MyCourseBean, BaseViewHolder> {
        return adapter
    }

    override fun getLayoutManager(adapter: BaseQuickAdapter<MyCourseBean, BaseViewHolder>): RecyclerView.LayoutManager? {
        return GridLayoutManager(this, 3)
    }

    inner class MyAdapter: BaseQuickAdapter<MyCourseBean, BaseViewHolder>(R.layout.item_my_course){
        override fun convert(helper: BaseViewHolder, item: MyCourseBean) {
            val imageView = helper.getView<ImageView>(R.id.ivBook)
            imageView.loadImage("")
            helper.setText(R.id.tvEdition,item.bookVersionName)
                    .setText(R.id.tvTime,"[添加时间:${DateUtil.getFormatDateTime(Date(item.createDate), "yyyy-MM-dd HH:mm")}]")
                    .setText(R.id.tvSemester,item.semeterName)
                    .setText(R.id.tvSubject,item.bookName)
                    .setText(R.id.tvClass,item.gradeName)
        }
    }
}
