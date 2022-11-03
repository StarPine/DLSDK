package com.dl.rtc.calling

import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore

/**
 *Author: 彭石林
 *Time: 2022/11/3 15:28
 * Description: This is DLRTCServiceInitializer
 */
class DLRTCServiceInitializer : ContentProvider() {

    private val TAG = "ServiceInitializer"

    /**
     * 应用启动时自动调起的初始化方法
     *
     * @param context applicationContext
     */
    fun init(context: Context?) {
        if (context is Application) {
            context.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks {
                private var foregroundActivities = 0
                private var isChangingConfiguration = false
                override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
                override fun onActivityStarted(activity: Activity) {
                    foregroundActivities++
                    if (foregroundActivities == 1 && !isChangingConfiguration) {
                        // 应用切到前台
                        Log.i(TAG, "application enter foreground")
                        //应用回到前台,需要主动去查询是否有未处理的通话请求
                        //例如应用在后台时没有拉起应用的权限,当用户听到铃声,从桌面或通知栏进入应用时,主动查询,拉起通话
                        if (com.tencent.qcloud.tuicore.TUILogin.isUserLogined()) {
                            TUICallingImpl.sharedInstance(context).queryOfflineCalling()
                        }
                    }
                    isChangingConfiguration = false
                }

                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {
                    foregroundActivities--
                    isChangingConfiguration = activity.isChangingConfigurations
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            })
        }
    }

/////////////////////////////////////////////////////////////////////////////////
//                               以下方法无需重写                                 //
/////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(): Boolean {
        val appContext = context!!.applicationContext
        init(appContext)
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}