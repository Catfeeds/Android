package com.yzy.ebag.student.activity.center.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.yzy.ebag.student.R
import com.yzy.ebag.student.bean.SpaceBean
import com.yzy.ebag.student.dialog.ClassJoinDialog
import com.yzy.ebag.student.http.StudentApi
import ebag.core.base.BaseListFragment
import ebag.core.http.network.RequestCallBack
import ebag.core.util.loadHead
import ebag.hd.activity.album.AlbumActivity
import ebag.hd.bean.ClassMemberBean
import ebag.hd.dialog.ClazzmateInfoDIalog

/**
 * @author caoyu
 * @date 2018/1/17
 * @description
 */
class ClassesFragment : BaseListFragment<List<SpaceBean>, SpaceBean>() {

    companion object {
        fun newInstance(): ClassesFragment {
            return ClassesFragment()
        }
    }

    override fun getBundle(bundle: Bundle?) {
    }

    override fun isPagerFragment(): Boolean {
        return false
    }

    override fun loadConfig() {
        loadMoreEnabled(false)
        val button = Button(mContext)
        button.text = "加入班级"
        button.setTextColor(resources.getColor(R.color.white))
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.tv_normal))
        button.setBackgroundResource(R.drawable.my_class_btn_add)
        button.setPadding(resources.getDimensionPixelOffset(R.dimen.x24), 0,
                resources.getDimensionPixelOffset(R.dimen.x24), 0)
        val p = RelativeLayout.LayoutParams(resources.getDimensionPixelOffset(R.dimen.x108),
                resources.getDimensionPixelOffset(R.dimen.x108))
        p.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
        p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rootView.addView(button, p)
        rootView.setPadding(0, resources.getDimensionPixelOffset(R.dimen.x20), 0, 0)
        button.setOnClickListener {
            joinDialog.show(childFragmentManager, "joinDialog")
        }
    }

    override fun requestData(page: Int, requestCallBack: RequestCallBack<List<SpaceBean>>) {
        StudentApi.clazzSpace(requestCallBack)
    }

    override fun parentToList(isFirstPage: Boolean, parent: List<SpaceBean>?): List<SpaceBean>? {
        return parent
    }

    override fun getAdapter(): BaseQuickAdapter<SpaceBean, BaseViewHolder> {
        return Adapter()
    }

    override fun getLayoutManager(adapter: BaseQuickAdapter<SpaceBean, BaseViewHolder>): RecyclerView.LayoutManager? {
        return null
    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
        if (view?.id == R.id.class_photo_btn)
            AlbumActivity.jump(mContext, (adapter as Adapter).getItem(position)?.classId ?: "")
    }


    private val joinDialog by lazy {
        val dialog = ClassJoinDialog.newInstance()
        dialog.successListener = {
            onRetryClick()
            dialog.dismiss()
        }
        dialog
    }

    inner class Adapter : BaseQuickAdapter<SpaceBean, BaseViewHolder>(R.layout.item_fragment_class) {

        override fun convert(helper: BaseViewHolder, item: SpaceBean?) {
            helper.setText(R.id.class_name_id, item?.clazzName)
                    .setText(R.id.class_desc_id, "邀请码: ${item?.inviteCode}\n班级学生: ${item?.studentCount}")
                    .addOnClickListener(R.id.class_photo_btn)
            val recyclerView = helper.getView<RecyclerView>(R.id.recyclerView)
            if (recyclerView.layoutManager == null)
                recyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            val adapter = PersonAdapter()
            recyclerView.adapter = adapter
            adapter.setOnItemChildClickListener { adapter, view, position ->

                val b = adapter.getItem(position) as SpaceBean.ClazzUserVoListBean
                val bean = ClassMemberBean.SubMemberBean()
                bean.city = b.city
                bean.birthday = b.birthday
                bean.county = b.county
                bean.headUrl = b.headUrl
                bean.phone = b.phone
                bean.name = b.name
                bean.schoolName = b.schoolName
                bean.ysbCode = b.ysbCode
                bean.sex = b.sex

                val bundle = Bundle()
                bundle.putSerializable("data", bean)
                val f = ClazzmateInfoDIalog.newInstance()
                f.arguments = bundle
                f.show(fragmentManager, "clazzinfoDialog")

            }
            adapter.setNewData(item?.clazzUserVoList)
        }

    }

    inner class PersonAdapter : BaseQuickAdapter<SpaceBean.ClazzUserVoListBean, BaseViewHolder>(R.layout.item_fragment_class_member) {
        override fun convert(helper: BaseViewHolder, item: SpaceBean.ClazzUserVoListBean?) {
            helper.getView<ImageView>(R.id.img_head).loadHead(item?.headUrl)
            helper.setText(R.id.name_id, item?.name)
            helper.addOnClickListener(R.id.img_head)
            ClazzmateInfoDIalog
        }
    }


}