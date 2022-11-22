package com.dl.rtc.calling.manager

import com.dl.rtc.calling.base.DLRTCCalling
import com.dl.rtc.calling.model.DLRTCDataMessageType
import java.lang.ref.WeakReference

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

    private var interceptorCallListener : MutableList<WeakReference<InterceptorCall>>? = null

    /**
     * 添加拦截类
     */
    fun addDelegateActivity(vararg mClass : Class<*>){
        if(mClass.isNotEmpty()){
            //如果为空::创建对象
            if(interceptorActivity.isNullOrEmpty()){
                interceptorActivity = HashMap()
            }
            mClass.iterator().forEach {
                interceptorActivity!![it.simpleName] = it.canonicalName as Any
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
             DLRTCInterceptorCall().apply {
                 interceptorCallListener = ArrayList()
             }
        }
    }

    fun notifyInterceptorCall(acceptUserId: String,inviteUserId : String, type: DLRTCDataMessageType.DLInviteRTCType, roomId: Int, data: String?){
        for (reference in interceptorCallListener!!) {
            val listener: InterceptorCall? = reference.get()
            listener?.receiveCall(acceptUserId, inviteUserId,type, roomId, data)
        }
    }

    /**
     * 添加注册监听
     */
    fun addDelegateInterceptorCall(listener: InterceptorCall) {
        val listenerWeakReference: WeakReference<InterceptorCall> = WeakReference<InterceptorCall>(listener)
        interceptorCallListener!!.add(listenerWeakReference)
    }

    /**
     * 移除注册监听
     */
    fun removeDelegateInterceptorCall(listener: InterceptorCall) {
        val iterator: MutableIterator<*> = interceptorCallListener!!.iterator()
        while (iterator.hasNext()) {
            val reference: WeakReference<InterceptorCall?> =
                iterator.next() as WeakReference<InterceptorCall?>
            if (reference.get() == null) {
                iterator.remove()
                continue
            }
            if (reference.get() === listener) {
                iterator.remove()
            }
        }
    }

    /**
     * 接听拦截回调
     */
    interface InterceptorCall{
        fun receiveCall(acceptUserId: String,inviteUserId : String, type: DLRTCDataMessageType.DLInviteRTCType, roomId: Int, data: String?)
    }

}