package ebag.hd.adapter

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import ebag.hd.R
import ebag.hd.bean.QueryOrderBean

/**
 * Created by fansan on 2018/3/15.
 */
class OrderListAdapter(private val context: Context, layout: Int, data: MutableList<QueryOrderBean.ResultOrderVosBean>) : BaseQuickAdapter<QueryOrderBean.ResultOrderVosBean, BaseViewHolder>(layout, data) {
    private var statusText: String = ""
    private var statusSubtext: String = ""


    override fun convert(helper: BaseViewHolder, item: QueryOrderBean.ResultOrderVosBean?) {
        helper.setText(R.id.order_number_id, "订单编号${item?.oid}")
        when (item!!.status) {
            "0" -> {
                statusText = "未付款"
                statusSubtext = "付款"
            }
            "1" -> {
                statusText = "待发货"
                statusSubtext = "提醒卖家"
            }
            "2" -> {
                statusText = "待收货"
                statusSubtext = "确认收货"
            }
        }

        helper.setText(R.id.state_tv_id, statusText)
                .setText(R.id.order_btn,statusSubtext)
                .setText(R.id.paid_tv_id,"￥${item.allPrice}")

        val datas = item.orderProductVOs
        datas.forEach {
            val view = View.inflate(context,R.layout.item_order_goodlayout,null)
            view.findViewById<TextView>(R.id.commodity_name_tv_id).text = it.shopName
            view.findViewById<TextView>(R.id.price_tv_id).text = "￥${it.price}"
            view.findViewById<TextView>(R.id.count_tv_id).text = "X ${it.numbers}"
            helper.getView<LinearLayout>(R.id.good_layout).addView(view)
        }
    }
}