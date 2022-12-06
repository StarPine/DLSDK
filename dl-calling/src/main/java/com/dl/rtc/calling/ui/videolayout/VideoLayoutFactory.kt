package com.dl.rtc.calling.ui.videolayout

import android.content.Context
import android.view.View
import com.dl.rtc.calling.model.bean.DLRTCLayoutEntity
import java.util.*

/**
 *Author: 彭石林
 *Time: 2022/11/5 15:13
 * Description: This is VideoLayoutFactory
 */
class VideoLayoutFactory(context: Context) {
    private var mContext = context
    var mLayoutEntityList: LinkedList<DLRTCLayoutEntity>? = null
    init {
        mLayoutEntityList = LinkedList<DLRTCLayoutEntity>()
    }

    /**
     * 根据 userId 找到已经分配的 View
     *
     * @param userId
     * @return
     */
    fun findUserLayout(userId: String?): DLRTCVideoLayout? {
        if (userId == null) return null
        mLayoutEntityList?.let {
            for (layoutEntity in it) {
                if (layoutEntity.userId == userId) {
                    return layoutEntity.layout
                }
            }
        }
        return null
    }

    /**
     * 根据 userId 分配对应的 view
     *
     * @param userId
     * @return
     */
    fun allocUserLayout(
        userIds: String?,
        layouts: DLRTCVideoLayout?
    ): DLRTCVideoLayout? {
        if (userIds == null) return null
        val layoutEntity = DLRTCLayoutEntity().apply {
            userId = userIds
            layout = layouts
            layout?.visibility = View.VISIBLE
        }
        mLayoutEntityList!!.add(layoutEntity)
        return layoutEntity.layout
    }

    /**
     * 根据 userId 和 视频类型，回收对应的 view
     *
     * @param userId
     */
    fun recyclerCloudViewView(userId: String?) {
        if (userId == null) return
        val iterator: MutableIterator<*> = mLayoutEntityList!!.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next() as DLRTCLayoutEntity
            if (item.userId == userId) {
                iterator.remove()
                break
            }
        }
    }
}