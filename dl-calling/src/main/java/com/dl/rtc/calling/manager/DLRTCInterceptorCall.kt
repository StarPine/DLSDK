package com.dl.rtc.calling.manager

/**
 *Author: 彭石林
 *Time: 2022/11/5 10:25
 * Description: 音视频页面跳转拦截器
 */
class DLRTCInterceptorCall {
    /**
     * 语音通话跳转页面 :: 页面绝对路由
     */
    var audioCallActivity = "com.dl.playfun.kl.view.DialingAudioActivity"

    /**
     *  视频通话跳转页面 :: 页面绝对路由
     */
    var videoCallActivity = "com.dl.playfun.kl.view.CallingVideoActivity"

    /**
     * 拦截处理页面规则
     */
     var interceptorActivity : HashMap<String,Any> ? = null

    fun addDelegate(vararg mClass : Class<*>){
        if(mClass.isNotEmpty()){
            mClass.apply {
                //如果为空::创建对象
                if(interceptorActivity.isNullOrEmpty()){
                    interceptorActivity = HashMap()
                }
                mClass.iterator().forEach {
                    interceptorActivity!![it.simpleName] = it.canonicalName as Any
                }
            }
        }

    }

    /**
     * 判断是否存在指定的class
     */
    fun containsActivity(mClass: Class<*>): Boolean{
        if(interceptorActivity.isNullOrEmpty()){
            return false
        }
        var result = false
        interceptorActivity!!.iterator().forEach {
            if(it.key == mClass.simpleName && it.value == mClass.canonicalName){
                result = true
            }
        }
        return result
    }

    /**
     * 单列模式
     */
    companion object{
        val instance by lazy {
             DLRTCInterceptorCall()
        }
    }

}