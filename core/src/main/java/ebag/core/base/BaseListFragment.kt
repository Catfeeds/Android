package ebag.core.base

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import ebag.core.R
import ebag.core.http.network.MsgException
import ebag.core.http.network.RequestCallBack
import ebag.core.http.network.handleThrowable
import ebag.core.widget.empty.StateView
import java.util.*

/**
 * Created by caoyu on 2017/11/30.
 */
abstract class BaseListFragment<Parent, E> : LazyFragment(),
        StateView.OnRetryClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        BaseQuickAdapter.OnItemClickListener,
        BaseQuickAdapter.OnItemChildClickListener,
        BaseQuickAdapter.RequestLoadMoreListener{

    companion object {
        /** 刚进入页面时的状态 */
        val FIRST = 0
        /** 刷新时的状态 */
        val REFRESH = 1
        /** 加载更多时的状态 */
        val LOAD_MORE = 2
        /** 每页默认加载的数量 */
        val DEFAULT_PAGE_SIZE = 10
    }
    protected var mAdapter: BaseQuickAdapter<E,BaseViewHolder>? = null
    protected var mCurrentPage = 1
    protected var needFirstLoad = true
    // 是否进行过网络请求
    var isRequested = false

    /**可刷新*/
    private var canRefresh = true
    /**可加载更多*/
    private var canLoadMore = true

    protected abstract fun loadConfig()

    /**
     *  网络请求
     * @param page
     * @param requestCallBack
     */
    protected abstract fun requestData(page: Int, requestCallBack: RequestCallBack<Parent>)

    protected abstract fun parentToList(isFirstPage: Boolean, parent: Parent?): List<E>?

    /**
     * 设置 recyclerView 的适配器 Adapter
     */
    protected abstract fun getAdapter(): BaseQuickAdapter<E,BaseViewHolder>

    /**
     * 设置 recyclerView 的 LayoutManager
     */
    protected abstract fun getLayoutManager(adapter: BaseQuickAdapter<E,BaseViewHolder>): RecyclerView.LayoutManager?

    /** 每页默认加载的数量 */
    protected open fun getPageSize(): Int = DEFAULT_PAGE_SIZE

    protected fun withFirstPageData(list: List<E>?){
        withFirstPageData(list,false)
    }

    protected fun withFirstPageData(list: List<E>?, enable: Boolean){
        withFirstPageData(list,enable,enable)
    }

    protected fun withFirstPageData(list: List<E>?, canRefresh: Boolean, canLoadMore: Boolean){
        refreshEnabled(canRefresh)
        loadMoreEnabled(canLoadMore)
        //mAdapter != null 是为了防止别人乱改这个  Fragment的顺序
        if(list != null && mAdapter != null){
            needFirstLoad = false
            stateView.showContent()
            firstPageDataLoad(list)
        }else{
            stateView.showEmpty()
        }
    }

    /** 是否有加载更多操作 */
    protected fun loadMoreEnabled(enable: Boolean){
        this.canLoadMore = enable
        mAdapter?.setEnableLoadMore(enable)
    }

    /** 是否有下拉刷新操作 */
    protected fun refreshEnabled(enable: Boolean){
        this.canRefresh = enable
        refreshLayout.isEnabled = enable
    }


    protected fun enableNetWork(enable: Boolean){
        config(enable,enable,enable)
    }

    protected fun config(needFirstLoad: Boolean, canRefresh: Boolean, canLoadMore: Boolean){
        this.needFirstLoad = needFirstLoad
        this.canRefresh = canRefresh
        this.canLoadMore = canLoadMore
    }

    protected fun setPadding(left: Int, top: Int, right: Int, bottom: Int){
        recyclerView.setPadding(left, top, right,bottom)
    }

    /** 当前网络请求所处的状态 */
    private var loadingStatus: Int = FIRST

    override fun getLayoutRes(): Int {
        return R.layout.base_list_view
    }

    lateinit var rootView: RelativeLayout
    lateinit var recyclerView: RecyclerView
    lateinit var stateView: StateView
    lateinit var refreshLayout: SwipeRefreshLayout

    /** 初始化操作 */
    override fun initViews(rootView: View) {

    }

    /**
     * 这个方法里面不能直接用  kotlin的快捷方式取得View 必须findViewById
     */
    override fun initRootView(rootView: View) {
        this.rootView = rootView.findViewById(R.id.layout)
        recyclerView = rootView.findViewById(R.id.recyclerView)
        stateView = rootView.findViewById(R.id.stateView)
        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        refreshLayout.setColorSchemeResources(R.color.blue)
        // 设置 recyclerView 的 Adapter
        mAdapter = getAdapter()
        mAdapter?.enableLoadMoreEndClick(true)
        // 设置 RecyclerView 的 LayoutManager
        recyclerView.layoutManager = getLayoutManager(mAdapter!!) ?: LinearLayoutManager(mContext)
        mAdapter?.bindToRecyclerView(recyclerView)
        //设置 点击监听事件
        mAdapter?.onItemClickListener = this
        mAdapter?.onItemChildClickListener = this
        // 第一次网络请求失败时点击重新加载
        stateView.setOnRetryClickListener(this)
        refreshLayout.setOnRefreshListener(this)
        mAdapter?.setOnLoadMoreListener(this,recyclerView)

        loadConfig()
        readLoadConfig()
    }

    private fun readLoadConfig(){
        loadMoreEnabled(canLoadMore)
        refreshEnabled(canRefresh)
    }

    private val requestDelegate = lazy{ object: RequestCallBack<Parent>(){
            override fun onStart() {
                isLoadMoreEnd = false
                when (loadingStatus) {
                    FIRST -> stateView.showLoading()
                    REFRESH -> {
                        refreshLayout.isRefreshing = true
                    }
                    LOAD_MORE -> { }
                }
            }

            override fun onSuccess(entity: Parent?) {
                isRequested = true
                when (loadingStatus) {
                    /** 刚进入页面，第一次请求成功 */
                    FIRST -> {
                        var result: List<E>? = parentToList(true,entity)
                        if (result == null) {
                            //添加判断，防止异常
                            result = ArrayList()
                        }
                        //第一次请求成功
                        needFirstLoad = false
                        //返回数据不为空时，等待层消失，展示数据
                        stateView.showContent()
                        firstPageDataLoad(result)
                    }
                    /** 刷新的时候数据请求成功 */
                    REFRESH -> {
                        var result: List<E>? = parentToList(true,entity)
                        if (result == null) {
                            //添加判断，防止异常
                            result = ArrayList()
                        }
                        //刷新成功
                        refreshLayout.isRefreshing = false
                        firstPageDataLoad(result)
                    }
                    /** 加载更多成功 */
                    LOAD_MORE -> {
                        var result: List<E>? = parentToList(false,entity)
                        if (result == null) {
                            //添加判断，防止异常
                            result = ArrayList()
                        }
                        //加载更多时 没有数据返回的话  page 不变
                        if(result.isEmpty()){
                            mCurrentPage--
                        }else{
                            mAdapter?.addData(result)
                        }
                        footerState(result)
                    }
                }
            }

            override fun onError(exception: Throwable) {
                exception.handleThrowable(mContext)
                isRequested = true
                when (loadingStatus) {
                    //进入页面第一次加载出现的异常
                    FIRST -> {
                        if(exception is MsgException){
                            stateView.showError(exception.message)
                        }else{
                            stateView.showError()
                        }
                    }
                    //刷新的时候出现的异常
                    REFRESH -> {
                        refreshLayout.isRefreshing = false
                    }
                    //加载更多的时候出现的异常
                    LOAD_MORE -> {
                        mCurrentPage--
                        mAdapter?.loadMoreFail()
                    }
                }
            }
        }
    }

    private var isLoadMoreEnd = false
    /**
     * 加载第一屏数据 的View的改变
     */
    private fun firstPageDataLoad(result: List<E>){
        if (result.isEmpty()) {
            //返回数据为空时，展示无数据
            stateView.showEmpty()
        } else {
            mAdapter?.setNewData(result)
            if(canLoadMore) {
                footerState(result)
            }else {
                mAdapter?.loadMoreEnd(true)
            }
        }
    }

    /**
     * 加载更多的状态
     */
    private fun footerState(result: List<E>){
        //没有更多了
        if (result.size < getPageSize()) {
            isLoadMoreEnd = true
            mAdapter?.loadMoreEnd()
        } else {//还可以加载更多
            mAdapter?.loadMoreComplete()
        }
    }

    /**
     * 网络请求 需要的时候才被加载，运用了懒加载的策略
     */
    private val requestCallBack: RequestCallBack<Parent> by requestDelegate


    override fun lazyLoad() {
        if (needFirstLoad) {
            // 加载各种数据
            onRetryClick()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //判断这个是否进行了初始化
        if(requestDelegate.isInitialized())
            requestCallBack.cancelRequest()
    }

    /**
     * 第一次加载失败，后点击重新加载
     */
    override fun onRetryClick() {
        loadingStatus = FIRST
        mCurrentPage = 1
        requestData(mCurrentPage, requestCallBack)
    }

    fun cancelRequest(){
        requestCallBack.cancelRequest()
    }

    /**
     * 下拉刷新
     */
    override fun onRefresh() {
        loadingStatus = REFRESH
        mCurrentPage = 1
        requestData(mCurrentPage, requestCallBack)
    }

    /**
     * 上拉加载
     */
    override fun onLoadMoreRequested() {
        if(!isLoadMoreEnd){
            loadingStatus = LOAD_MORE
            mCurrentPage++
            requestData(mCurrentPage, requestCallBack)
        }else{
            mAdapter?.loadMoreEnd()
        }
    }

    /**
     * 列表页的点击事件
     * @param view
     * @param position
     * @param t
     */
    override fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {}
    /**
     * 列表页的点击事件
     * @param view
     * @param position
     * @param t
     */
    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {}

}