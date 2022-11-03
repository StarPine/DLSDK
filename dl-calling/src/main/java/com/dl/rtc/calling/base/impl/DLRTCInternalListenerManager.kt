package com.dl.rtc.calling.base.impl

import com.dl.rtc.calling.base.DLRTCCallingDelegate
import com.tencent.trtc.TRTCCloudDef
import java.lang.ref.WeakReference

/**
 *Author: 彭石林
 *Time: 2022/11/3 14:51
 * Description: This is DLRTCInternalListenerManager
 */
class DLRTCInternalListenerManager : DLRTCCallingDelegate {

    private var mWeakReferenceList: MutableList<WeakReference<DLRTCCallingDelegate>>? = null

    fun TRTCInternalListenerManager() {
        mWeakReferenceList = java.util.ArrayList<WeakReference<DLRTCCallingDelegate>>()
    }

    fun addDelegate(listener: DLRTCCallingDelegate) {
        val listenerWeakReference: WeakReference<DLRTCCallingDelegate> =
            WeakReference<DLRTCCallingDelegate>(listener)
        mWeakReferenceList!!.add(listenerWeakReference)
    }

    fun removeDelegate(listener: DLRTCCallingDelegate) {
        val iterator: MutableIterator<*> = mWeakReferenceList!!.iterator()
        while (iterator.hasNext()) {
            val reference: WeakReference<DLRTCCallingDelegate?> =
                iterator.next() as WeakReference<DLRTCCallingDelegate?>
            if (reference.get() == null) {
                iterator.remove()
                continue
            }
            if (reference.get() === listener) {
                iterator.remove()
            }
        }
    }

    override fun onError(code: Int, msg: String?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onError(code, msg)
        }
    }

    override fun onInvited(
        sponsor: String?,
        userIdList: List<String?>?,
        isFromGroup: Boolean,
        callType: Int
    ) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onInvited(sponsor, userIdList, isFromGroup, callType)
        }
    }

    override fun onGroupCallInviteeListUpdate(userIdList: List<String?>?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onGroupCallInviteeListUpdate(userIdList)
        }
    }

    override fun onUserEnter(userId: String?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onUserEnter(userId)
        }
    }

    override fun onUserLeave(userId: String?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onUserLeave(userId)
        }
    }

    override fun onReject(userId: String?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onReject(userId)
        }
    }

    override fun onNoResp(userId: String?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onNoResp(userId)
        }
    }

    override fun onLineBusy(userId: String?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onLineBusy(userId)
        }
    }

    override fun onCallingCancel() {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onCallingCancel()
        }
    }

    override fun onCallingTimeout() {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onCallingTimeout()
        }
    }

    override fun onCallEnd() {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onCallEnd()
        }
    }

    override fun onUserVideoAvailable(userId: String?, isVideoAvailable: Boolean) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onUserVideoAvailable(userId, isVideoAvailable)
        }
    }

    override fun onUserAudioAvailable(userId: String?, isVideoAvailable: Boolean) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onUserAudioAvailable(userId, isVideoAvailable)
        }
    }

    override fun onUserVoiceVolume(volumeMap: Map<String?, Int?>?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onUserVoiceVolume(volumeMap)
        }
    }

    override fun onNetworkQuality(
        localQuality: TRTCCloudDef.TRTCQuality?,
        remoteQuality: ArrayList<TRTCCloudDef.TRTCQuality?>?
    ) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onNetworkQuality(localQuality,remoteQuality)
        }
    }

    override fun onSwitchToAudio(success: Boolean, message: String?) {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onSwitchToAudio(success,message)
        }
    }

    override fun onTryToReconnect() {
        for (reference in mWeakReferenceList!!) {
            val listener: DLRTCCallingDelegate? = reference.get()
            listener?.onTryToReconnect()
        }
    }
}