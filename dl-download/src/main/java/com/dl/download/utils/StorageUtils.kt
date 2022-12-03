package com.dl.download.utils

import java.io.File

/**
 * @author Shuotao Gong
 * @time 2022/11/15
 */
internal object StorageUtils {

    fun exist(path: String): Boolean {
        return File(path).exists()
    }
}