package com.dl.download.model

import com.dl.download.exception.DownloadException
import com.dl.download.utils.HashKeyUtils

/**
 * 单个下载任务
 *
 * @param url 任务远程资源地址
 *
 * @author Shuotao Gong
 * @time 2022/11/16
 */
data class DownloadTask(
    val url: String
) {
    /** 任务成功后表示本地资源路径 */
    var path = ""

    /** 任务失败后表示失败异常 */
    var exception: DownloadException? = null

    /** 对远程地址的哈希计算结果 */
    internal val hashKey = HashKeyUtils.getSHA256(url)
}
