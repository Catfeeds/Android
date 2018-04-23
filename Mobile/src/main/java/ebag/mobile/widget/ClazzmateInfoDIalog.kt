package ebag.mobile.widget

import android.os.Bundle
import android.view.View
import ebag.core.base.BaseFragmentDialog
import ebag.core.util.loadHead
import ebag.mobile.R
import ebag.mobile.bean.ClassMemberBean
import kotlinx.android.synthetic.main.dialog_classmateinfo.*

/**
 * Created by fansan on 2018/4/8.
 */

class ClazzmateInfoDIalog: BaseFragmentDialog(){

    private lateinit var datas: ClassMemberBean.StudentsBean

    companion object {
        fun newInstance(): ClazzmateInfoDIalog{
            return ClazzmateInfoDIalog()
        }
    }

    override fun getBundle(bundle: Bundle?) {
            @Suppress("UNCHECKED_CAST")
            datas = bundle?.get("data") as ClassMemberBean.StudentsBean

    }

    override fun getLayoutRes(): Int = R.layout.dialog_classmateinfo

    override fun initView(view: View) {

        clazzmate_iv.loadHead(datas.headUrl)
        clazzmate_name.text = datas.name
        clazzmate_info.text = "${when(datas.sex){"1" -> "男" "2" -> "女" else -> ""}} ${datas.county?:""}"
        clazzmate_bagnum.text = "书包号：${datas.ysbCode}"
        clazzmate_phone.text = "电话：${datas.phone}"
        clazzmate_school.text = "学校：${datas.schoolName}"

    }

}