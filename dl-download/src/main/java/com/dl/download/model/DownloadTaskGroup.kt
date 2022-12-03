package com.dl.download.model

import android.content.Context
import android.util.Log
import com.dl.download.exception.DownloadException
import com.dl.download.exception.WrongConfigException
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * 下载任务组
 * @param context 上下文
 * @param tasks 任务组中的所有任务
 * @param priority 任务组的优先级
 * @param isCache 是否读取缓存
 * @param strategy 入等待队列时的策略
 * @param isMonitorProgress 是否开启进度监听
 * @param singleTaskListener 单个任务的监听
 * @param taskGroupName 任务组名称，仅用于打印日志
 * @param allCompleteListener 任务全部完成时回调，参数为[任务名，成功的任务集合，失败的任务集合]
 *
 * @author Shuotao Gong
 * @time 2022/11/16
 */
data class DownloadTaskGroup(
    internal val context: WeakReference<Context>,
    internal val tasks: Collection<DownloadTask>,
    internal val priority: DownloadTaskPriority = DownloadTaskPriority.MEDIUM,
    internal val isCache: Boolean = false,
    internal val strategy: QueueStrategy = QueueStrategy.FIFO,
    internal val singleTaskListener: DownloadTaskListener = object : DownloadTaskListener {},
    internal val isMonitorProgress: Boolean = false,
    internal var taskGroupName: String = hashCode().toString(),
    private val allCompleteListener: (String, Collection<DownloadTask>, Collection<DownloadTask>) -> Unit
) {

    companion object {
        const val TAG = "DOWNLOAD_TASK_GROUP"
    }

    /** 成功任务的集合 */
    private val successTasks = ArrayList<DownloadTask>(tasks.size)

    /** 失败任务的集合 */
    private val failureTasks = ArrayList<DownloadTask>(tasks.size / 5)

    /** 本地已有资源的集合 */
    private val localExistTasks = ArrayList<DownloadTask>(tasks.size)

    /** 已被标记为完成的任务计数 */
    private val completeCount = AtomicInteger(0)

    /**
     * 标记任务成功
     *
     * @param task 被标记的任务
     * @param path 本地缓存资源路径
     */
    internal fun taskSuccess(task: DownloadTask, path: String) {
        Log.d(TAG, "Task[${task.url}]@TaskGroup[$taskGroupName] Success ")
        synchronized(successTasks) {
            if (task in tasks) {
                task.path = path
                successTasks.add(task)
                singleTaskListener.onTaskSuccess(task)
                taskComplete()
            }
        }
    }

    /**
     * 标记任务失败
     *
     * @param task 被标记的任务
     * @param e 失败异常
     */
    internal fun taskFail(task: DownloadTask, e: DownloadException) {
        Log.e(TAG, "Task[${task.url}]@TaskGroup[$taskGroupName] Fail ", e)
        synchronized(failureTasks) {
            if (task in tasks) {
                task.exception = e
                failureTasks.add(task)
                singleTaskListener.onTaskFailure(task)
                taskComplete()
            }
        }
    }

    /**
     * 标记任务在本地已缓存
     *
     * @param task 被标记的任务
     * @param path 本地缓存资源路径
     */
    internal fun taskInLocal(task: DownloadTask, path: String) {
        Log.d(TAG, "Task[${task.url}]@TaskGroup[$taskGroupName] in Local ")
        synchronized(localExistTasks) {
            if (task in tasks) {
                task.path = path
                localExistTasks.add(task)
                singleTaskListener.onTaskSuccess(task)
                taskComplete()
            }
        }
    }

    /** 获取还需要下载即本地没有的资源列表 */
    internal fun getNeedDownloadTasks(): Collection<DownloadTask> {
        synchronized(localExistTasks) {
            return tasks.filter { it !in localExistTasks }
        }
    }

    /** 判断是否还有需要下载的资源 */
    internal fun isNeedDownload() = completeCount.get() != tasks.size

    /** 用于计数完成任务数量，被标记时调用 */
    private fun taskComplete() {
        if (completeCount.incrementAndGet() == tasks.size) {
            allCompleteListener(taskGroupName, successTasks + localExistTasks, failureTasks)
        }
    }

    /** 构造者 */
    class Builder {
        companion object {

            const val TAG = "DOWNLOAD_TASK_GROUP_BUILD"

        }

        private var context: Context? = null

        private val tasks = ArrayList<DownloadTask>()

        private var priority = DownloadTaskPriority.MEDIUM

        private var isCache = false

        private var allCompleteListener: (String, Collection<DownloadTask>, Collection<DownloadTask>) -> Unit =
            { _, _, _ -> }

        private var singleTaskListener: DownloadTaskListener = object : DownloadTaskListener {}

        private var strategy = QueueStrategy.FIFO

        private var name = ""

        private var isMonitorProgress = false

        /** **必须**：传入上下文 */
        fun withContext(context: Context): Builder {
            this.context = context
            return this
        }

        /** 添加单个任务 */
        fun addTask(task: DownloadTask): Builder {
            tasks.add(task)
            return this
        }

        /** 添加多个任务 */
        fun addTasks(vararg tasks: DownloadTask): Builder {
            tasks.forEach { this.tasks.add(it) }
            return this
        }

        /** 添加所有任务 */
        fun addAllTask(tasks: Collection<DownloadTask>): Builder {
            this.tasks.addAll(tasks)
            return this
        }

        /** 设置优先级 */
        fun setPriority(priority: DownloadTaskPriority): Builder {
            this.priority = priority
            return this
        }

        /** 设置是否读取缓存 */
        fun setIsCache(isCache: Boolean): Builder {
            this.isCache = isCache
            return this
        }

        /** 设置所有任务完成时回调 */
        fun setCompleteListener(allCompleteListener: (String, Collection<DownloadTask>, Collection<DownloadTask>) -> Unit): Builder {
            this.allCompleteListener = allCompleteListener
            return this
        }

        /** 设置单个任务回调 */
        fun setSingleTaskListener(listener: DownloadTaskListener): Builder {
            this.singleTaskListener = listener
            return this
        }

        /** 设置等待策略 */
        fun setQueueStrategy(strategy: QueueStrategy): Builder {
            this.strategy = strategy
            return this
        }

        /** 设置任务组名 */
        fun setGroupName(name: String): Builder {
            this.name = name
            return this
        }

        /** 启用下载进度监听 */
        fun enableMonitorProgress(): Builder {
            isMonitorProgress = true
            return this
        }

        /** 禁用下载进度监听 */
        fun disableMonitorProgress(): Builder {
            isMonitorProgress = false
            return this
        }

        fun build(): DownloadTaskGroup {
            context ?: throw WrongConfigException("Context cannot be null")
            return DownloadTaskGroup(
                WeakReference(context),
                tasks,
                priority,
                isCache,
                strategy,
                singleTaskListener,
                isMonitorProgress,
                name,
                allCompleteListener
            )
        }
    }
}
