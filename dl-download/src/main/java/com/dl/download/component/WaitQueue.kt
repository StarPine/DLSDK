package com.dl.download.component

import android.util.Log
import com.dl.download.model.DownloadTaskGroup
import com.dl.download.model.QueueStrategy
import java.util.PriorityQueue

/**
 * 等待队列
 *
 * @author Shuotao Gong
 * @time 2022/11/16
 */
internal object WaitQueue {

    private const val TAG = "DOWNLOAD_WAIT_QUEUE"

    private val queue = ArrayList<DownloadTaskGroup>()

    /** 进入等待队列
     *
     * @param taskGroup 需要加入的任务组
     */
    @Synchronized
    fun offerTaskGroup(taskGroup: DownloadTaskGroup) {
        Log.d(TAG, "TaskGroup[${taskGroup.taskGroupName}] offer into queue")
        // 空队列直接进入
        if (queue.isEmpty()) queue.add(taskGroup)
        else if (taskGroup.strategy == QueueStrategy.FIFO) {
            // 先进先出从队头开始遍历，直到遇到相同或更高优先级任务，或者到队尾直接插入队尾
            for (i in 0..queue.size) {
                if (i == queue.size || queue[i].priority >= taskGroup.priority) {
                    queue.add(i, taskGroup)
                    break
                }
            }
        } else {
            // 后进先出从队尾开始遍历，直到遇到相同或更高优先级任务，或者到队头直接插入队头
            for (i in queue.lastIndex downTo -1) {
                if (i == -1 || queue[i].priority <= taskGroup.priority) {
                    queue.add(i + 1, taskGroup)
                    break
                }
            }
        }
        DownloadPool.loadTaskGroup()
    }

    /**
     * 从等待队列队尾中移除并返回任务组，
     *
     * @return 任务组，如果队列空为null
     */
    @Synchronized
    fun pollTaskGroup(): DownloadTaskGroup? = queue.removeLastOrNull()

    @Synchronized
    fun isEmpty(): Boolean = queue.isEmpty()

    @Synchronized
    fun isNotEmpty(): Boolean = queue.isNotEmpty()

}