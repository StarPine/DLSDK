package com.dl.download.model

/**
 * 单个下载任务监听器
 *
 * @author Shuotao Gong
 * @time 2022/11/18
 */
@JvmDefaultWithCompatibility
interface DownloadTaskListener {

    /**
     * 任务成功回调
     *
     * @param task 当前任务
     */
    fun onTaskSuccess(task: DownloadTask) {}

    /**
     * 任务失败回调
     *
     * @param task 当前任务
     */
    fun onTaskFailure(task: DownloadTask) {}

    /**
     * 任务下载进度回调
     *
     * **注意：配置时如未传入isMonitorProgress为true值，将不会监听进度也不会回调**
     *
     * @param task 当前任务
     * @param process 当前进度
     */
    fun onTaskDownloading(task: DownloadTask, process: Double) {}

}