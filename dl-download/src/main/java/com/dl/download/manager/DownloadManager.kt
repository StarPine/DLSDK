package com.dl.download.manager

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dl.download.component.DownloadPool
import com.dl.download.component.WaitQueue
import com.dl.download.model.DownloadTask
import com.dl.download.model.DownloadTaskGroup
import com.dl.download.model.DownloadTaskListener
import com.dl.download.utils.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 下载任务(组)接收者
 *
 * @author Shuotao Gong
 * @time 2022/11/17
 */
object DownloadManager {

    private const val TAG = "DOWNLOAD_TASK_MANAGER"

    /**
     * 接收下载任务组并开始下载
     *
     * @param group 被下载的任务组
     */
    @JvmStatic
    fun startTaskGroup(group: DownloadTaskGroup) {

        val context = group.context.get() ?: return

        if (context is LifecycleOwner) {
            context.lifecycleScope.launch(Dispatchers.IO) {
                // 建立缓存目录
                val dirPath = context.cacheDir.path + "/" + context.javaClass.simpleName
                File(dirPath).mkdirs()

                // 检查本地缓存
                if (group.isCache) {
                    Log.d(TAG, "TaskGroup[${group.taskGroupName}] is viewing cache")
                    group.tasks.forEach {
                        val filePath = dirPath + "/" + it.hashKey
                        if (StorageUtils.exist(filePath)) {
                            group.taskInLocal(it, filePath)
                        }
                    }
                    // 判断是否还需要下载
                    if (!group.isNeedDownload()) {
                        return@launch
                    }
                }

                // 请求进入等待队列
                WaitQueue.offerTaskGroup(group)
            }
        }
    }

    /**
     * 接收单个下载任务并开始下载
     *
     * @param context 上下文
     * @param task 被下载的任务
     * @param listener 下载回调；参数分别为原任务和是否下载成功
     * @param isCache 是否读取缓存
     * @param isMonitorProgress 是否监听下载进度
     */
    @JvmOverloads
    @JvmStatic
    fun startSingleTask(
        context: Context,
        task: DownloadTask,
        listener: DownloadTaskListener = object : DownloadTaskListener {},
        isCache: Boolean = true,
        isMonitorProgress: Boolean = false
    ) {
        if (context is LifecycleOwner) {
            context.lifecycleScope.launch(Dispatchers.IO) {

                val dirPath = context.cacheDir.path + "/" + context.javaClass.simpleName
                File(dirPath).mkdirs()

                if (isCache) {
                    Log.d(TAG, "Task[${task.url}] is viewing cache")
                    val filePath = dirPath + "/" + task.hashKey
                    if (StorageUtils.exist(filePath)) {
                        task.path = filePath
                        listener.onTaskSuccess(task)
                        return@launch
                    }
                }

                DownloadPool.singleTaskDownload(context, task, listener, isMonitorProgress)
            }
        }
    }

    /**
     * 对此上下文的指定或所有缓存进行清除
     *
     * @param context 上下文
     * @param url 需要被清除的缓存，不传入此参数清除全部缓存
     */
    @JvmStatic
    fun clearLocalCache(context: Context, url: String = "") {

        if (context is LifecycleOwner) {
            context.lifecycleScope.launch(Dispatchers.IO) {
                val dirPath = context.cacheDir.path + "/" + context.javaClass.simpleName + "/" + url
                val file = File(dirPath)
                if (file.isDirectory) {
                    File(dirPath).list()?.forEach {
                        File(dirPath, it).delete()
                    }
                } else file.delete()
            }
        }
    }
}