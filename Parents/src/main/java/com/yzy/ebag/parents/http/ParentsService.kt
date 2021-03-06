package com.yzy.ebag.parents.http

import com.yzy.ebag.parents.bean.*
import ebag.core.bean.QuestionBean
import ebag.core.bean.ResponseBean
import ebag.mobile.bean.MyChildrenBean
import ebag.mobile.bean.NoticeBean
import ebag.mobile.bean.UnitBean
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ParentsService {
    /*查询孩子*/
    @POST("user/searchMyChildren/{version}")
    fun searchChildren(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<List<MyChildrenBean>>>

    /*首页作业*/
    @POST("user/getOnePageInfo/{version}")
    fun getOnePageInfo(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<List<OnePageInfoBean>>>

    /*作业报告*/
    @POST("correctHome/createHomeWorkRep/{version}")
    fun homeReport(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<HomeworkAbstractBean>>

    /*评语*/
    @POST("correctHome/correctComment/{version}")
    fun parentComment(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<String>>

    /*查询小孩劳动任务*/
    @POST("user/searchChildTargetList/{version}")
    fun queryTask(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<List<ExcitationWorkBean>>>

    /*创建劳动任务*/
    @POST("user/createTargetToChild/{version}")
    fun createTask(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<String>>

    /*更新任务*/
    @POST("user/updateChildTargetCompleted/{version}")
    fun updateTask(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<String>>

    /**查询最新公告*/
    @POST("notice/queryNewClassNotice/{version}")
    fun newestNotice(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<NoticeBean>>

    /**自习室-生字总览列表*/
    @POST("util/queryNewWordsTime/{version}")
    fun getLetterRecord(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<List<LetterRecordBaseBean>>>

    /**
     * 随堂 课后作业
     * @return
     */
    @POST("homeWork/getMyHomeWork/{version}")
    fun subjectWorkList(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<ArrayList<SubjectBean>>>

    /**
     * 我的错题
     */
    @POST("homeWork/myErrorHomeWork/{version}")
    fun errorTopic(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<ArrayList<ErrorTopicBean>>>

    @POST("user/createByParent/{version}")
    fun createChildCode(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<String>>

    /**
     * 加入班级
     */
    @POST("clazz/joinByClass/{version}")
    fun joinClazz(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<JoinClazzBean>>

    /**
     * 查科目
     */
    @POST("clazz/getClassCourses/{version}")
    fun getTaughtCourses(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<List<StudentSubjectBean>>>

    @POST("user/getGiftDetail/{version}")
    fun getGiftDetail(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<List<GiftListBean>>>

    /** 查询教科书目录*/
    @POST("data/getBookUnit/{version}")
    fun getBookUnit(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<java.util.ArrayList<UnitBean>>>

    /**预览试卷*/
    @POST("sendHome/queryTestPaperQuestion/{version}")
    fun previewTestPaper(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<List<QuestionBean>>>

    /**智能推送*/
    @POST("question/smartChoice/{version}")
    fun smartPush(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<List<QuestionBean>>>


    /**发布作业*/
    @POST("sendHome/sendHome/{version}")
    fun publishHomework(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<String>>

    @POST("user/queryUserDetail/{version}")
    fun queryUserDetail(@Path("version") version: String, @Body requestBody: RequestBody): Observable<ResponseBean<MyChildrenBean>>
}