package com.dl.download.exception

/**
 * 下载异常
 *
 * @author Shuotao Gong
 * @time 2022/11/17
 */
sealed class DownloadException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

/** 网络请求结果正文为空异常 */
class ResponseBodyEmptyException internal constructor(message: String = "Response Body is Empty") :
    DownloadException(message)

/** 网络请求失败异常 */
class NetworkFailException internal constructor(
    message: String = "Download Fail",
    cause: Throwable? = null
) :
    DownloadException(message, cause) {
    constructor(cause: Throwable) : this("Download Fail", cause)
}

/** 转码失败异常 */
class DecodeFailException internal constructor(
    message: String = "Decode Fail",
    cause: Throwable? = null
) :
    DownloadException(message, cause) {
    constructor(cause: Throwable) : this("Decode Fail", cause)
}

/** 无效或错误配置异常 */
class WrongConfigException internal constructor(message: String) : DownloadException(message)