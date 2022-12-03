package com.dl.download.model

/**
 * 等待队列进入策略
 *
 * @author Shuotao Gong
 * @time 2022/11/17
 */
enum class QueueStrategy {
    /** 先进先出，将排在同优先级的任务的最后面 */
    FIFO,
    /** 后进先出，将排在同优先级的任务的最前面 */
    LIFO
}