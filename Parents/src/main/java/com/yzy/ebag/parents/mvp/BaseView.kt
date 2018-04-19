package com.yzy.ebag.parents.mvp



interface BaseView {

    fun showLoading()

    fun <T> showContent(data: T?): Unit = TODO("no impl")

    fun showError(e: Throwable?): Unit = TODO("no impl")


    interface BaseListView:BaseView{

        fun showContents(data:List<*>)

        fun showMoreComplete()

        fun loadmoreEnd()

        fun loadmoreFail()
    }

}