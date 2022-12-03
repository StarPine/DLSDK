package com.dl.download.component

import android.content.Context
import android.util.Log
import com.dl.download.exception.NetworkFailException
import com.dl.download.exception.ResponseBodyEmptyException
import com.dl.download.model.DownloadTask
import com.dl.download.model.DownloadTaskGroup
import com.dl.download.model.DownloadTaskListener
import com.dl.download.utils.StorageUtils
import okhttp3.*
import okio.use
import java.io.IOException

/**
 * 下载池
 *
 * @author Shuotao Gong
 * @time 2022/11/16
 */
internal object DownloadPool {

    private const val TAG = "DOWNLOAD_POOL"

    private const val maxConnection = 5

    private val client = OkHttpClient.Builder().build()

    /** 当前正在下载的任务组 */
    @Volatile
    private var currTaskGroup: DownloadTaskGroup? = null

    /** 任务组中还没开始下载在等待的池中等待队列 */
    private val waiting = ArrayList<DownloadTask>()

    /** 任务组中正在下载的集合 */
    private val downloading = ArrayList<DownloadTask>(maxConnection)

    /** 从池外等待队列中加载任务组 */
    @Synchronized
    fun loadTaskGroup() {
        Log.d(TAG, "Request Load Task Group")
        if (waiting.isEmpty() && downloading.isEmpty()) {
            currTaskGroup = null
            WaitQueue.pollTaskGroup()?.let {
                Log.d(TAG, "TaskGroup[${it.taskGroupName}] load into pool")
                currTaskGroup = it
                waiting.addAll(it.getNeedDownloadTasks())
                loadTask()
            }
        }
    }

    /** 从池中等待队列中加载任务 */
    @Synchronized
    fun loadTask(): Int {
        Log.d(TAG, "Request Load Task")
        if (waiting.isEmpty() && downloading.isEmpty()) {
            loadTaskGroup()
            return 0
        }
        var count = 0
        while (downloading.size < maxConnection && waiting.isNotEmpty()) {
            val task = waiting.removeFirst()
            downloading.add(task)
            download(task)
            count++
        }
        return count
    }

    /** 任务被请求开始下载 */
    fun download(task: DownloadTask) {
        val group = currTaskGroup ?: return
        val context = group.context.get() ?: return

        // 再次检查本地缓存
        if (group.isCache) {
            val dirPath = context.cacheDir.path + "/" + context.javaClass.simpleName
            val filePath = dirPath + "/" + task.hashKey

            if (StorageUtils.exist(filePath)) {
                group.taskInLocal(task, filePath)
                taskDownloadComplete(task)
                return
            }
        }

        // 构建网络请求，开始请求网络
        val request = buildRequest(task)
        Log.d(TAG, "Okhttp Connect Start[${task.url}] @TaskGroup[${group.taskGroupName}]")
        client.newCall(request).enqueue(

            object : Callback {

                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "Okhttp Connect Success[${task.url}] @TaskGroup[${group.taskGroupName}]")
                    response.body?.byteStream()?.let { bs ->
                        val contentLen = response.body!!.contentLength()
                        Decoder.groupTaskDecode(group, task, bs, contentLen)
                    } ?: run {
                        group.taskFail(task, ResponseBodyEmptyException())
                    }
                    taskDownloadComplete(task)
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Okhttp Connect File[${task.url}] @TaskGroup[${group.taskGroupName}]", e)
                    group.taskFail(task, NetworkFailException(e))
                    taskDownloadComplete(task)
                }
            }
        )
    }

    /**
     * 单个下载任务
     */
    fun singleTaskDownload(
        context: Context,
        task: DownloadTask,
        listener: DownloadTaskListener,
        isMonitorProgress: Boolean = false
    ) {
        val request = buildRequest(task)

        client.newCall(request).enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "Okhttp Connect Success[${task.url}] @SingleTask")
                    response.body?.byteStream()?.let { bs ->
                        val contentLen = response.body!!.contentLength()
                        Decoder.singleTaskDecode(context, task, bs, contentLen, isMonitorProgress, listener)
                    } ?: run {
                        task.exception = ResponseBodyEmptyException()
                        listener.onTaskFailure(task)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Okhttp Connect File[${task.url}] @SingleTask", e)
                    task.exception = NetworkFailException(e)
                    listener.onTaskFailure(task)
                }
            }
        )
    }

    /** 构建下载请求 */
    private fun buildRequest(task: DownloadTask): Request {
        return Request.Builder()
            .url(task.url)
            .header("Connect", "close")
            .method("GET", null)
            .build()
    }

    /** 已下载完成的任务，从正在下载的集合中移除 */
    @Synchronized
    private fun taskDownloadComplete(task: DownloadTask) {
        downloading.remove(task)
        loadTask()
    }
}