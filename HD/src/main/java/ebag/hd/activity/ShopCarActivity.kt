package ebag.hd.activity

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import ebag.core.base.BaseActivity
import ebag.core.http.network.RequestCallBack
import ebag.core.util.LoadingDialogUtil
import ebag.core.util.T
import ebag.hd.R
import ebag.hd.adapter.ShopCarAdapter
import ebag.hd.bean.ShopListBean
import ebag.hd.http.EBagApi
import ebag.hd.util.ActivityUtils
import kotlinx.android.synthetic.main.activity_shop_car.*

/**
 * Created by fansan on 2018/3/16.
 */
class ShopCarActivity : BaseActivity() {
    private lateinit var mAdapter: ShopCarAdapter
    private var mDatas: MutableList<ShopListBean.ListBean> = mutableListOf()
    private var totalPrice: Int = 0
    override fun getLayoutId(): Int = R.layout.activity_shop_car
    private val selectedCount: HashSet<Int> = hashSetOf()
    private lateinit var _intent: Intent
    override fun initViews() {
        ActivityUtils.addActivity(this)
        shopcar_recyclerview.layoutManager = LinearLayoutManager(this)
        mAdapter = ShopCarAdapter(this, mDatas, object : OnCheckChange {
            override fun getPosition(position: Int, isChecked: Boolean) {
                if (isChecked) {
                    totalPrice += mDatas[position].discountPrice.toInt() * mDatas[position].numbers
                    tv_total_price.text = "¥ $totalPrice"
                    selectedCount.add(position)
                    if (selectedCount.size == mDatas.size) {
                        all_check.isChecked = true
                    }
                } else {
                    totalPrice -= mDatas[position].discountPrice.toInt()* mDatas[position].numbers
                    tv_total_price.text = "¥ $totalPrice"
                    selectedCount.remove(position)
                    if (all_check.isChecked && selectedCount.isEmpty()) {
                        all_check.isChecked = false
                    }
                }

                mDatas[position].isChecked = isChecked
            }

        })
        shopcar_recyclerview.adapter = mAdapter
        shopcar_recyclerview.addItemDecoration(ebag.core.xRecyclerView.manager.DividerItemDecoration(DividerItemDecoration.VERTICAL, 1, Color.parseColor("#e0e0e0")))
        all_check.setOnCheckedChangeListener({ _, isChecked ->
            mDatas.forEach {
                it.isChecked = isChecked
                mAdapter.notifyDataSetChanged()
            }

        })

        mAdapter.setOnItemChildClickListener { _, view, position ->

            when (view.id) {
                R.id.add -> {
                    mDatas[position].numbers = mDatas[position].numbers + 1
                    updateShopCar(mDatas[position].shopCartId.toString(), mDatas[position].numbers.toString())
                    if (mDatas[position].isChecked) {
                        totalPrice += mDatas[position].discountPrice.toInt()
                        tv_total_price.text = "¥ $totalPrice"
                    }
                    mAdapter.notifyDataSetChanged()
                }
                R.id.lower -> {
                    val array = arrayListOf<String>()
                    if (mDatas[position].numbers <= 1) {
                        AlertDialog.Builder(this@ShopCarActivity).setMessage("移除此商品吗?").setNeutralButton("是", { dialog, which ->
                            array.add(mDatas[position].shopCartId.toString())
                            EBagApi.removeShopCar(array, object : RequestCallBack<String>() {
                                override fun onSuccess(entity: String?) {
                                    totalPrice -= mDatas[position].discountPrice.toInt()
                                    mDatas.removeAt(position)
                                    mAdapter.notifyItemRemoved(position)
                                    selectedCount.remove(position)
                                    all_check.isChecked = false
                                }

                                override fun onError(exception: Throwable) {
                                    T.show(this@ShopCarActivity, "移除商品失败")
                                }

                            })
                        }).setNegativeButton("否", { dialog, which ->

                        }).show()
                        return@setOnItemChildClickListener
                    }
                    mDatas[position].numbers = mDatas[position].numbers - 1
                    if (mDatas[position].isChecked) {
                        totalPrice -= mDatas[position].discountPrice.toInt()
                        tv_total_price.text = "¥ $totalPrice"
                    }
                    updateShopCar(mDatas[position].shopCartId.toString(), mDatas[position].numbers.toString())
                    mAdapter.notifyDataSetChanged()
                }
            }

        }

        btn_pay.setOnClickListener {
            if (selectedCount.size == 0) {
                T.show(this, "还没有选中商品")
                return@setOnClickListener
            }
            val data: ArrayList<ShopListBean.ListBean> = arrayListOf()
            var f = 0
            for (i in selectedCount) {
                f += (mDatas[i].freight?:"0").toInt()
                data.add(mDatas[i])
            }
            _intent = Intent(this, OrderDetailsActivity::class.java)
            _intent.putExtra("datas", data)
            _intent.putExtra("freight", f.toString())
            createOrderNo()

        }

        request()
        shopcar_stateview.setOnRetryClickListener { request() }
    }

    private fun createOrderNo() {
        EBagApi.createShopOrderNo(object : RequestCallBack<String>() {

            override fun onStart() {
                super.onStart()
                LoadingDialogUtil.showLoading(this@ShopCarActivity, "正在生成订单...")
            }

            override fun onSuccess(entity: String?) {
                _intent.putExtra("number", entity)
                startActivity(_intent)
                LoadingDialogUtil.closeLoadingDialog()
            }

            override fun onError(exception: Throwable) {
                LoadingDialogUtil.closeLoadingDialog()
                T.show(this@ShopCarActivity, "提交失败")
            }

        })
    }

    private fun request() {
        EBagApi.queryShopCar(object : RequestCallBack<MutableList<ShopListBean.ListBean>>() {

            override fun onStart() {
                super.onStart()
                shopcar_stateview.showLoading()

            }

            override fun onSuccess(entity: MutableList<ShopListBean.ListBean>?) {
                if (entity!!.isNotEmpty()) {
                    mDatas.addAll(entity)
                    mDatas.forEach {
                        it.isChecked = false
                    }
                    mAdapter.notifyDataSetChanged()
                    shopcar_stateview.showContent()
                } else {
                    shopcar_stateview.showEmpty()
                }
            }

            override fun onError(exception: Throwable) {
                shopcar_stateview.showError()
            }


        })
    }

    private fun updateShopCar(id: String, num: String) {

        EBagApi.updateShopCar(num, id, object : RequestCallBack<String>() {

            override fun onSuccess(entity: String?) {
                Log.d("shopCarUpdate", "success")
            }

            override fun onError(exception: Throwable) {
                Log.d("shopCarUpdate", "failed")
            }

        })
    }


    interface OnCheckChange {

        fun getPosition(position: Int, isChecked: Boolean)
    }

}