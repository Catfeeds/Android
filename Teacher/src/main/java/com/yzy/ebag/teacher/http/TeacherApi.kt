package com.yzy.ebag.teacher.http

import com.yzy.ebag.teacher.bean.*
import ebag.core.bean.QuestionBean
import ebag.core.http.network.RequestCallBack
import ebag.core.util.StringUtils
import ebag.mobile.bean.BaseClassesBean
import ebag.mobile.bean.BaseStudentBean
import ebag.mobile.bean.NoticeBean
import ebag.mobile.bean.UnitBean
import ebag.mobile.http.EBagApi
import ebag.mobile.http.EBagClient
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by caoyu on 2018/1/8.
 */
object TeacherApi {

    private val teacherService: TeacherService by lazy {
        EBagClient.createRetrofitService(TeacherService::class.java)
    }

    /**首页网络数据*/
    fun firstPage(callback: RequestCallBack<FirstPageBean>){
        val jsonObject = JSONObject()
        jsonObject.put("roleCode", "2")
        jsonObject.put("isHDorPHONE", "PHONE")
        EBagApi.request(teacherService.firstPage("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**布置作业页面*/
    fun assignmentData(type: String, callback: RequestCallBack<AssignmentBean>){
        val jsonObject = JSONObject()
        jsonObject.put("type", type)
        EBagApi.request(teacherService.assignmentData("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**根据年级获取单元 和 题型信息*/
    fun unitAndQuestion(type: String, gradeCode: String, classIds: ArrayList<AssignClassBean>?, bookVersionId: String?, callback: RequestCallBack<AssignmentBean>){
        val jsonObject = JSONObject()
        jsonObject.put("type", type)
        jsonObject.put("gradeCode", gradeCode)
        if(bookVersionId != null)
            jsonObject.put("bookVersionId", bookVersionId)
        if (classIds != null){
            val jsonArray = JSONArray()
            classIds.forEach {
                jsonArray.put(it.classId)
            }
            jsonObject.put("classIds",jsonArray)
        }
        EBagApi.request(teacherService.assignmentData("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**布置作业页面-切换版本请求数据*/
    fun assignDataByVersion(type: String, versionId: String?, subCode: String?, unitCode: String?, level: String?, callback: RequestCallBack<AssignmentBean>){
        val jsonObject = JSONObject()
        if (!StringUtils.isEmpty(level)){
            jsonObject.put("level", level)
        }
        jsonObject.put("type", type)
        jsonObject.put("bookVersionId", versionId)
        jsonObject.put("subCode", subCode)
        if (!StringUtils.isEmpty(unitCode)){
            jsonObject.put("unitCode", unitCode)
        }
        EBagApi.request(teacherService.assignDataByVersion("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**试卷列表*/
    fun testPaperList(testPaperFlag: String, gradeCode: String, unitId: String?, subCode: String?, page: Int, pageSize: Int, callback: RequestCallBack<List<TestPaperListBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("testPaperFlag", testPaperFlag)
        jsonObject.put("gradeCode", gradeCode)
        jsonObject.put("subCode", subCode)
        if (unitId != null)
            jsonObject.put("unitId", unitId)
        jsonObject.put("page",page)
        jsonObject.put("pageSize",pageSize)
        EBagApi.request(teacherService.testPaperList("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**根据班级获取教材版本*/
    fun searchBookVersion(classesId: List<String>, callback: RequestCallBack<BookVersionBean>){
        val jsonObject = JSONObject()
        jsonObject.put("clazzIds", JSONArray(classesId))
        EBagApi.request(teacherService.searchBookVersion("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**查询试题*/
    fun searchQuestion(unitBean: UnitBean.UnitSubBean, difficulty: String?, type: String, gradeCode: String, semeterCode: String, course: String, bookVersionId: String, page: Int, callback: RequestCallBack<List<QuestionBean>>){
        val jsonObject = JSONObject()
        if (unitBean.unitCode != null) {
            if (unitBean.isUnit)
                jsonObject.put("bookUnit", unitBean.unitCode)
            else
                jsonObject.put("bookCatalog", unitBean.unitCode)
        }
        if (difficulty != null)
            jsonObject.put("level",difficulty)
        jsonObject.put("gradeCode",gradeCode)
        jsonObject.put("semesterCode",semeterCode)
        jsonObject.put("course",course)
        jsonObject.put("bookVersionId",bookVersionId)

        jsonObject.put("type",type)
        jsonObject.put("page",page)
        jsonObject.put("pageSize",10)
        EBagApi.request(teacherService.searchQuestion("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**组卷*/
    fun organizePaper(paperName: String, gradeCode: String, unitId: String?, subCode: String, questionList: ArrayList<QuestionBean>, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("name",paperName)
        jsonObject.put("gradeCode",gradeCode)
        jsonObject.put("subCode",subCode)
        if (unitId != null)
            jsonObject.put("unitId",unitId)
        val jsonArray = JSONArray()
        questionList.forEach {
            val jsonObj = JSONObject()
            jsonObj.put("questionId", it.questionId)
            jsonArray.put(jsonObj)
        }
        jsonObject.put("questionVos", jsonArray)
        EBagApi.request(teacherService.organizePaper("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**智能推送*/
    fun smartPush(count: Int, unitBean: UnitBean.UnitSubBean, difficulty: String?, type: String, bookVersionId: String?, callback: RequestCallBack<List<QuestionBean>>){
        val jsonObject = JSONObject()
        if (unitBean.unitCode != null) {
            if (unitBean.isUnit)
                jsonObject.put("bookUnit", unitBean.unitCode)
            else
                jsonObject.put("bookCatalog", unitBean.unitCode)
        }
        if(bookVersionId != null){
            jsonObject.put("bookVersionId", bookVersionId)
        }
        difficulty ?: jsonObject.put("level",difficulty)
        jsonObject.put("type",type)
        jsonObject.put("count", count)
        EBagApi.request(teacherService.smartPush("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**错题反馈*/
    fun wrongQuestionFeedback(content: String, questionId: String, uid: String, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("content", content)
        jsonObject.put("qid", questionId)
        jsonObject.put("uid", uid)
        EBagApi.request(teacherService.wrongQuestionFeedback("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**预览试卷*/
    fun previewTestPaper(paperId: String, callback: RequestCallBack<List<QuestionBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("testPaperId",paperId)
        EBagApi.request(teacherService.previewTestPaper("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**发布作业*/
    fun publishHomework(
            classes: ArrayList<AssignClassBean>,
            groupIds: ArrayList<String>? = null,
            isGroup: Boolean,
            type: String,
            remark: String,
            content: String,
            endTime: String,
            subCode: String,
            bookVersionId: String,
            questionList: ArrayList<QuestionBean>? = null,
            testPaperId: String? = null,
            callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        val classArray = JSONArray()
        classes.forEach {
            classArray.put(it.classId)
        }
        if (groupIds != null){
            val groupArray = JSONArray()
            groupIds.forEach { groupArray.put(it) }
            jsonObject.put("groupIds", groupArray)
        }
        jsonObject.put("clazzIds", classArray)
        if (isGroup)
            jsonObject.put("groupType", "2")
        else
            jsonObject.put("groupType", "1")
        jsonObject.put("content", content)
        jsonObject.put("remark", remark)
        jsonObject.put("type", type)
        jsonObject.put("endTime", endTime)
        jsonObject.put("subCode", subCode)
        jsonObject.put("bookVersionId", bookVersionId)
        if (questionList != null) {
            val questionArray = JSONArray()
            questionList.forEach {
                val questionJson = JSONObject()
                questionJson.put("questionId", it.questionId)
                questionJson.put("questionType", it.type)
                questionArray.put(questionJson)
            }
            jsonObject.put("homeWorkQuestionDtos", questionArray)
        }
        if (testPaperId != null){
            jsonObject.put("testPaperId", testPaperId)
        }
        EBagApi.request(teacherService.publishHomework("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**根据班级查询班级下所有的学习小组*/
    fun studyGroup(classId: String, callback: RequestCallBack<List<GroupBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        EBagApi.request(teacherService.studyGroup("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**打分*/
    fun markScore(homeworkId: String, uid: String?, questionId: String, questionScore: String, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("homeWorkId", homeworkId)
        jsonObject.put("uid", uid)
        jsonObject.put("questionId", questionId)
        jsonObject.put("questionScore", questionScore)
        EBagApi.request(teacherService.markScore("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**班级列表*/
    fun clazzSpace(callback: RequestCallBack<List<SpaceBean>>){
        EBagApi.request(teacherService.clazzSpace("v1"), callback)
    }

    /**添加老师*/
    fun addTeacher(classId: String, ysbCode: String, subCodeList: ArrayList<String>, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("ysbCode",ysbCode)
        jsonObject.put("classId",classId)
        val sb = StringBuilder()
        subCodeList.forEach { sb.append("$it,") }
        jsonObject.put("subCode", sb.deleteCharAt(sb.length - 1))
        EBagApi.request(teacherService.addTeacher("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**根据年级 获取科目*/
    fun getSubject(code: String, callback: RequestCallBack<List<BaseSubjectBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("groupCode", "grade_subject")
        jsonObject.put("parentCode", code)
        EBagApi.request(teacherService.getBaseData("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**查询最新公告*/
    fun newestNotice(classId: String, callback: RequestCallBack<NoticeBean>){
        val jsonObject = JSONObject()
        jsonObject.put("classId",classId)
        EBagApi.request(teacherService.newestNotice("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**
     * 创建学习小组
     */
    fun createGroup(classId: String, groupName: String, list: List<BaseStudentBean>, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        jsonObject.put("groupName", groupName)
        jsonObject.put("studentCount", list.size)
        val jsonArray = JSONArray()
        list.forEach {
            val jsonObj = JSONObject()
            jsonObj.put("uid", it.uid)
            jsonObj.put("duties", it.duties)
            jsonArray.put(jsonObj)
        }
        jsonObject.put("clazzUserGroupVos", jsonArray)
        EBagApi.request(teacherService.createGroup("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**修改小组*/
    fun modifyGroup(groupId: String, classId: String, groupName: String, list: List<BaseStudentBean>, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        jsonObject.put("groupName", groupName)
        jsonObject.put("studentCount", list.size)
        jsonObject.put("groupId", groupId)
        val jsonArray = JSONArray()
        list.forEach {
            val jsonObj = JSONObject()
            jsonObj.put("uid", it.uid)
            jsonObj.put("duties", it.duties)
            jsonArray.put(jsonObj)
        }
        jsonObject.put("clazzUserGroupVos", jsonArray)
        EBagApi.request(teacherService.modifyGroup("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**删除学习小组*/
    fun deleteGroup(classId: String, groupId: String, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        jsonObject.put("groupId", groupId)
        EBagApi.request(teacherService.deleteGroup("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**发布公告*/
    fun publishNotice(classId: String, content: String, urls: String, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        jsonObject.put("content", content)
        jsonObject.put("photoUrl", urls)
        EBagApi.request(teacherService.publishNotice("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**添加所教课程*/
    fun addCourse(classId: String,
                  bookVersionId: String,
                  bookVersionCode: String,
                  bookVersionName: String,
                  bookCode: String,
                  bookName: String,
                  semeterCode: String,
                  semeterName: String,
                  gradeCode: String,
                  callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        jsonObject.put("bookVersionId", bookVersionId)
        jsonObject.put("bookVersionCode", bookVersionCode)
        jsonObject.put("bookVersionName", bookVersionName)
        jsonObject.put("bookCode", bookCode)
        jsonObject.put("bookName", bookName)
        jsonObject.put("semeterCode", semeterCode)
        jsonObject.put("semeterName", semeterName)
        jsonObject.put("gradeCode", gradeCode)
        EBagApi.request(teacherService.addCourse("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**添加所教课程-教材版本数据*/
    fun courseVersionData(classId: String, callback: RequestCallBack<AddCourseTextbookBean>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        EBagApi.request(teacherService.courseVersionData("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**删除所教课程*/
    fun deleteCourse(id: String, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("id", id)
        EBagApi.request(teacherService.deleteCourse("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**查询所教科目*/
    fun searchCourse(classId: String, callback: RequestCallBack<List<MyCourseBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        EBagApi.request(teacherService.searchCourse("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**课堂表现列表*/
    fun classPerformance(classId: String, callback: RequestCallBack<List<PerformanceBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        EBagApi.request(teacherService.classPerformance("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**修改学生课堂表现*/
    fun modifyPerformance(uid: String?, praise: ArrayList<Int>, criticize: ArrayList<Int>, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("uid", uid)
        val praiseArray = JSONArray()
        val criticizeArray = JSONArray()
        for (i in 0 until praise.size){
            praiseArray.put(praise[i])
            criticizeArray.put(criticize[i])
        }
        jsonObject.put("praise", praiseArray)
        jsonObject.put("criticize", criticizeArray)
        EBagApi.request(teacherService.modifyPerformance("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**创建班级*/
    fun createClass(schoolCode: String?, gradeCode: String?, className: String?, subjectCode: String?, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("gradeCode", gradeCode)
        jsonObject.put("className", className)
        jsonObject.put("introduce", "")
        jsonObject.put("school", schoolCode)
        jsonObject.put("subCode", subjectCode)
        EBagApi.request(teacherService.createClass("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**
     * 创建学校
     * @param schoolName 省份代码
     * @param province 市编码
     * @param city 区编码
     */
    fun createSchool(schoolName: String, province: String?, city: String?, county: String?, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("schoolName", schoolName)
        jsonObject.put("province", province)
        jsonObject.put("city", city)
        jsonObject.put("county", county)
        EBagApi.request(teacherService.createSchool("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**
     * 备课文件-首次进入的数据
     * @param lessonType 资源库类型  1 备课 2 校园资源  3 共享资源
     */
    fun prepareBaseData(lessonType: String, callback: RequestCallBack<PrepareBaseBean>){
        val jsonObject = JSONObject()
        jsonObject.put("lessonType", lessonType)
        jsonObject.put("page", 1)
        jsonObject.put("pageSize", 10)
        EBagApi.request(teacherService.prepareBaseData("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**删除指定备课文件*/
    fun deletePrepareFile(id: String, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("id", id)
        EBagApi.request(teacherService.deletePrepareFile("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**备课文件列表*/
    fun prepareList(
            lessonType: String,
            page: Int,
            pageSize: Int,
            callback: RequestCallBack<List<PrepareFileBean>>,
            gradeCode: String?,
            subCode: String?,
            unitId: String?,
            semesterCode: String?,
            versionCode: String?){
        val jsonObject = JSONObject()
        jsonObject.put("lessonType", lessonType)
        if (!StringUtils.isEmpty(gradeCode))
            jsonObject.put("gradeCode", gradeCode)
        if (!StringUtils.isEmpty(subCode))
            jsonObject.put("subCode", subCode)
        if (!StringUtils.isEmpty(unitId))
            jsonObject.put("unitCode", unitId)
        if (!StringUtils.isEmpty(semesterCode))
            jsonObject.put("semesterCode", semesterCode)
        if (!StringUtils.isEmpty(versionCode))
            jsonObject.put("bookVersion", versionCode)
        jsonObject.put("page", page)
        jsonObject.put("pageSize", pageSize)
        EBagApi.request(teacherService.prepareList("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**备课-获取年级科目数据*/
    fun prepareSubject(lessonType: String, callback: RequestCallBack<List<PrepareSubjectBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("lessonType", lessonType)
        EBagApi.request(teacherService.prepareSubject("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**备课-获取版本数据*/
    fun prepareVersion(gradeCode: String, lessonType: String, subCode: String, callback: RequestCallBack<PrepareVersionBean>){
        val jsonObject = JSONObject()
        jsonObject.put("lessonType", lessonType)
        jsonObject.put("subCode", subCode)
        jsonObject.put("gradeCode", gradeCode)
        EBagApi.request(teacherService.prepareVersion("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**
     * 推送备课文件至学生自习
     */
    fun pushPrepareFile(lessionFileId: String, classes: List<BaseClassesBean?>, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("lessionFileId", lessionFileId)
        val jsonArray = JSONArray()
        classes.forEach {
            jsonArray.put(it?.classId)
        }
        jsonObject.put("classIds", jsonArray)
        EBagApi.request(teacherService.pushPrepareFile("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**
     * 保存课件到个人备课
     */
    fun savePrepareFile(fileBean: PrepareFileBean, gradeCode: String?, subCode: String?, unitCode: String?, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("fileName", fileBean.fileName)
        jsonObject.put("fileType", fileBean.fileType)
        jsonObject.put("fileUrl", fileBean.fileUrl)
        jsonObject.put("gradeCode", gradeCode)
        jsonObject.put("subCode", subCode)
        jsonObject.put("unitCode", unitCode)
        EBagApi.request(teacherService.savePrepareFile("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**获取单元*/
    fun getUnit(versionId: String, callback: RequestCallBack<List<UnitBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("bookVersionId", versionId)
        EBagApi.request(teacherService.getUnit("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**自习室-生字总览列表*/
    fun getLetterRecord(unitId: String, classId: String, callback: RequestCallBack<List<LetterRecordBaseBean>>) {
        val jsonObject = JSONObject()
        if (!StringUtils.isEmpty(unitId))
            jsonObject.put("unitId", unitId)
        jsonObject.put("classId", classId)
        EBagApi.request(teacherService.getLetterRecord("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**查询已经布置的作业列表*/
    fun searchPublish(type: String, callback: RequestCallBack<List<CorrectingBean>>, classId: String? = null, subCode: String? = null, page: Int = 1, pageSize: Int = 10){
        val jsonObject = JSONObject()
        jsonObject.put("type", type)
        jsonObject.put("page", page)
        jsonObject.put("pageSize", pageSize)
        if (classId != null)
            jsonObject.put("classId", classId)
        if (subCode != null)
            jsonObject.put("subCode", subCode)
        EBagApi.request(teacherService.searchPublish("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**检查作业*/
    fun correctWork(id: String, type: String, callback: RequestCallBack<List<QuestionBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("id", id)
        jsonObject.put("type", type)
        EBagApi.request(teacherService.correctWork("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**检查作业学生答案*/
    fun correctStudentAnswer(homeworkId: String, questionId: String, callback: RequestCallBack<List<CorrectAnswerBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("homeWorkId", homeworkId)
        jsonObject.put("questionId", questionId)
        EBagApi.request(teacherService.correctStudentAnswer("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**评语列表*/
    fun commentList(homeworkId: String, page: Int, pageSize: Int, callback: RequestCallBack<List<CommentBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("homeWorkId", homeworkId)
        jsonObject.put("page", page)
        jsonObject.put("pageSize", pageSize)
        EBagApi.request(teacherService.commentList("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**
     * 提交评语
     */
    fun uploadComment(homeworkId: String, uid: String, teacherComment: String, callback: RequestCallBack<String>){
        val jsonObject = JSONObject()
        jsonObject.put("homeWorkId", homeworkId)
        jsonObject.put("uid", uid)
        jsonObject.put("teacherComment", teacherComment)
        EBagApi.request(teacherService.uploadComment("v1", EBagApi.createBody(jsonObject)), callback)
    }

}