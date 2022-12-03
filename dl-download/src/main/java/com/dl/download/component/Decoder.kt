package com.dl.download.component

import android.content.Context
import android.util.Log
import com.dl.download.exception.DecodeFailException
import com.dl.download.model.DownloadTask
import com.dl.download.model.DownloadTaskGroup
import com.dl.download.model.DownloadTaskListener
import okio.use
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * 转码器
 *
 * @author Shuotao Gong
 * @time 2022/11/17
 */
internal object Decoder {

    private const val TAG = "DOWNLOAD_DECODER"

    private val threadNum = AtomicInteger(0)

    private val threadPoolExecutor = Executors.newCachedThreadPool { r ->
        Thread(r, "Decoder-Thread-${threadNum.getAndIncrement()}")
    }

    /** 组中任务转码 */
    fun groupTaskDecode(
        group: DownloadTaskGroup,
        task: DownloadTask, input: InputStream, contentLength: Long
    ) {
        val context = group.context.get() ?: return
        val path = context.cacheDir.path + "/" + context.javaClass.simpleName
        val file = File(path, task.hashKey)
        threadPoolExecutor.execute {
            try {
                output(task, input, contentLength, group.isMonitorProgress, group.singleTaskListener, file)
            } catch (e: Exception) {
                Log.e(TAG, "Create File[${file.path}] Fail [${task.url}] @TaskGroup[${group.taskGroupName}] ", e)
                group.taskFail(task, DecodeFailException(e))
                return@execute
            }
            Log.d(TAG, "File[${file.path}] Create Success [${task.url}] @TaskGroup[${group.taskGroupName}]")
            group.taskSuccess(task, file.path)
        }
    }

    /** 单个任务转码 */
    fun singleTaskDecode(
        context: Context,
        task: DownloadTask, input: InputStream, contentLength: Long,
        isMonitorProgress: Boolean, listener: DownloadTaskListener
    ) {

        val path = context.cacheDir.path + "/" + context.javaClass.simpleName
        val file = File(path, task.hashKey)
        threadPoolExecutor.execute {
            try {
                output(task, input, contentLength, isMonitorProgress, listener, file)
            } catch (e: Exception) {
                Log.e(TAG, "Create File[${file.path}] Fail [${task.url}] @SingleTask ", e)
                task.exception = DecodeFailException(e)
                listener.onTaskFailure(task)
                return@execute
            }
            Log.d(TAG, "File[${file.path}] Create Success [${task.url}] @SingleTask")
            task.path = file.path
            listener.onTaskSuccess(task)
        }
    }

    private fun output(
        task: DownloadTask, input: InputStream, contentLength: Long,
        isMonitorProgress: Boolean, listener: DownloadTaskListener, file: File
    ) {
        file.takeIf { !it.exists() }?.createNewFile()
        val pool = ByteArray(2048)
        FileOutputStream(file).use { fos ->
            var streamLen: Int
            var sum = 0L
            while (run {
                    streamLen = input.read(pool)
                    streamLen != -1
                }) {
                fos.write(pool, 0, streamLen)
                if (isMonitorProgress) {
                    sum += streamLen
                    listener.onTaskDownloading(task, sum.toDouble() / contentLength)
                }
            }
        }
    }

}