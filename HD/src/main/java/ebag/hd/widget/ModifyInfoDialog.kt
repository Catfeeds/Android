package ebag.hd.widget

import android.content.Context
import android.text.InputType
import ebag.core.base.BaseDialog
import ebag.core.util.EditTextLimitUtil
import ebag.core.util.StringUtils
import ebag.core.util.T
import ebag.hd.R
import kotlinx.android.synthetic.main.dialog_modify_info.*

/**
 * Created by YZY on 2018/2/24.
 */
class ModifyInfoDialog(context: Context): BaseDialog(context) {
    override fun getLayoutRes(): Int {
        return R.layout.dialog_modify_info
    }
    override fun setWidth(): Int {
        return context.resources.getDimensionPixelSize(R.dimen.x600)
    }
    override fun setHeight(): Int {
        return context.resources.getDimensionPixelSize(R.dimen.y400)
    }

    init {
        EditTextLimitUtil.setEditMaxLength(countEdit, 40)
    }

    var onConfirmClickListener: ((count: String) -> Unit)? = null
    init {
        confirmBtn.setOnClickListener {
            val content = countEdit.text.toString()
            if (StringUtils.isEmpty(content)){
                T.show(context, "输入内容不能为空")
                return@setOnClickListener
            }

            onConfirmClickListener?.invoke(content)
        }
    }

    fun show(hint: String, isNumberHint: Boolean = false) {
        countEdit.setText("")
        countEdit.hint = hint
        if (isNumberHint)
            countEdit.inputType = InputType.TYPE_CLASS_NUMBER
        else
            countEdit.inputType = InputType.TYPE_CLASS_TEXT
        super.show()
    }
}