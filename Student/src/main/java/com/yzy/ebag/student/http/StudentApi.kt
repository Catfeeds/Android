package com.yzy.ebag.student.http

import com.alibaba.fastjson.JSON
import com.yzy.ebag.student.bean.*
import ebag.core.http.network.RequestCallBack
import ebag.mobile.bean.request.CommitQuestionVo
import ebag.mobile.http.EBagApi
import ebag.mobile.http.EBagApi.createBody
import ebag.mobile.http.EBagClient
import org.json.JSONObject

/**
 * Created by YZY on 2018/5/14.
 */
object StudentApi {
    private val studentService: StudentService by lazy {
        EBagClient.createRetrofitService(StudentService::class.java)
    }
    /**首页*/
    fun mainInfo(classId: String, callback: RequestCallBack<ClassesInfoBean>) {
        val jsonObj = JSONObject()
        jsonObj.put("classId", classId)
        jsonObj.put("roleCode", "1")
        EBagApi.request(studentService.mainInfo("v1", EBagApi.createBody(jsonObj)), callback)
    }

    /**加入班级*/
    fun joinClass(code: String, callback: RequestCallBack<String>, role: String = "student") {
        val jsonObject = JSONObject()
        jsonObject.put("inviteCode", code)
        EBagApi.request(studentService.joinClass("v1", createBody(jsonObject)), callback)
    }

    /**班级*/
    fun clazzSpace(callback: RequestCallBack<List<SpaceBean>>) {
        val jsonObject = JSONObject()
        EBagApi.request(studentService.clazzSpace("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**学习小组列表*/
    fun groups(classId: String, callback: RequestCallBack<ArrayList<GroupBean>>) {
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        EBagApi.request(studentService.groups("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**小组成员*/
    fun groupMember(groupId: String, callback: RequestCallBack<ArrayList<GroupUserBean>>) {
        val jsonObject = JSONObject()
        jsonObject.put("groupId", groupId)
        EBagApi.request(studentService.groupMember("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**劳动任务*/
    fun labourTasks(page: Int, pageSize: Int, callback: RequestCallBack<ArrayList<LabourBean>>) {
        val jsonObject = JSONObject()
        jsonObject.put("page", page)
        jsonObject.put("pageSize", pageSize)
        EBagApi.request(studentService.labourTasks("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**作业列表*/
    fun subjectWorkList(type: String, classId: String?, subCode: String, page: Int, pageSize: Int, callback: RequestCallBack<ArrayList<SubjectBean>>) {
        val jsonObj = JSONObject()
        jsonObj.put("classId", classId)
        jsonObj.put("type", type)
        jsonObj.put("subCode", subCode)
        jsonObj.put("page", page)
        jsonObj.put("pageSize", pageSize)
        EBagApi.request(studentService.subjectWorkList("v1", EBagApi.createBody(jsonObj)), callback)
    }

    /**我的错题*/
    fun errorTopic(classId: String, subCode: String, page: Int, pageSize: Int, callback: RequestCallBack<ArrayList<ErrorTopicBean>>) {
        val jsonObj = JSONObject()
        jsonObj.put("classId", classId)
        jsonObj.put("subCode", subCode)
        jsonObj.put("page", page)
        jsonObj.put("pageSize", pageSize)
        EBagApi.request(studentService.errorTopic("v1", EBagApi.createBody(jsonObj)), callback)
    }

    /**查询家长*/
    fun searchFamily(callback: RequestCallBack<List<ParentBean>>) {
        val jsonObject = JSONObject()
        EBagApi.request(studentService.searchFamily("1", EBagApi.createBody(jsonObject)), callback)
    }

    /**绑定家长*/
    fun bindParent(ysbCode: String, relationType: String, callback: RequestCallBack<String>) {
        val jsonObject = JSONObject()
        jsonObject.put("ysbCode", ysbCode)
        jsonObject.put("relationType", relationType)
        EBagApi.request(studentService.bindParent("1", EBagApi.createBody(jsonObject)), callback)
    }

    /**上报位置历史列表*/
    fun searchLocation(page: Int, callback: RequestCallBack<LocationBean>) {
        val jsonObject = JSONObject()
        jsonObject.put("page", page)
        jsonObject.put("pageSize", 10)
        EBagApi.request(studentService.searchLocation("1", EBagApi.createBody(jsonObject)), callback)
    }

    /**上报位置*/
    fun uploadLocation(address: String, remark: String, longitude: String, latitude: String, callback: RequestCallBack<String>) {
        val jsonObject = JSONObject()
        jsonObject.put("address", address)
        jsonObject.put("remark", remark)
        jsonObject.put("longitude", longitude)
        jsonObject.put("latitude", latitude)
        EBagApi.request(studentService.uploadLocation("1", EBagApi.createBody(jsonObject)), callback)
    }

    /**数学公式*/
    fun formula(formulaId: String, page: Int, pageSize: Int, callback: RequestCallBack<ArrayList<FormulaTypeBean>>) {
        val jsonObject = JSONObject()
        jsonObject.put("formulaId", formulaId)
        jsonObject.put("page", page)
        jsonObject.put("pageSize", pageSize)
        EBagApi.request(studentService.formula("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**获取练字列表*/
    fun getWordsList(unitCode: String, callback: RequestCallBack<WordsBean>) {
        val jsonObject = JSONObject()
        jsonObject.put("unitCode", unitCode)
        EBagApi.request(studentService.getWordsList("v1", EBagApi.createBody(jsonObject)), callback)
    }

    /**练字记录*/
    fun wordRecord(classId: String, pageSize: Int, page: Int, callback: RequestCallBack<WordRecordBean>) {
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        jsonObject.put("pageSize", pageSize)
        jsonObject.put("page", page)
        EBagApi.request(studentService.wordRecord("1", EBagApi.createBody(jsonObject)), callback)
    }

    /**上传练习生字*/
    fun uploadWord(classId: String, words: String, unitId: String, wordUrl: String, callback: RequestCallBack<String>) {
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        jsonObject.put("words", words)
        jsonObject.put("unitId", unitId)
        jsonObject.put("wordUrl", wordUrl)
        EBagApi.request(studentService.uploadWord("1", EBagApi.createBody(jsonObject)), callback)
    }

    /**提交作业*/
    fun commitHomework(commitQuestionVo: CommitQuestionVo, callback: RequestCallBack<String>) {
        EBagApi.request(studentService.commitHomework("v1", EBagApi.createBody(JSON.toJSONString(commitQuestionVo))), callback)
    }

    /**错题纠正*/
    fun errorCorrection(commitQuestionVo: CommitQuestionVo, callback: RequestCallBack<String>) {
        EBagApi.request(studentService.errorCorrection("v1", EBagApi.createBody(JSON.toJSONString(commitQuestionVo))), callback)
    }

    /**备课文件预习*/
    fun prepareList(classId: String, callback: RequestCallBack<List<PrepareFileBean>>){
        val jsonObject = JSONObject()
        jsonObject.put("classId", classId)
        EBagApi.request(studentService.prepareList("v1", EBagApi.createBody(jsonObject)), callback)
    }

    fun searchUserGrowthList(page: Int, pageSize: Int, gradeCode: String, type: String, callback: RequestCallBack<List<Diary>>) {
        val jsonObject = JSONObject()
        jsonObject.put("page", page)
        jsonObject.put("pageSize", pageSize)
        jsonObject.put("gradeCode", gradeCode)
        jsonObject.put("type", type)
        EBagApi.request(studentService.searchUserGrowthList("1", EBagApi.createBody(jsonObject)), callback)
    }


    fun addUserGrowth(className: String, type: String, title: String, content: String, image: String, gradeCode: String, callback: RequestCallBack<String>) {
        val jsonObject = JSONObject()
        jsonObject.put("className", className)
        jsonObject.put("type", type)
        jsonObject.put("title", title)
        jsonObject.put("content", content)
        jsonObject.put("image", image)
        jsonObject.put("gradeCode", gradeCode)
        EBagApi.request(studentService.addUserGrowth("1", EBagApi.createBody(jsonObject)), callback)
    }


    fun learningProcess(gradeCode:String,call: RequestCallBack<List<LeaningProgressBean>>) {
        val jsonObject = JSONObject()
        jsonObject.put("gradeCode",gradeCode)
        EBagApi.request(studentService.learningProcess("1", EBagApi.createBody(jsonObject)), call)
    }
}