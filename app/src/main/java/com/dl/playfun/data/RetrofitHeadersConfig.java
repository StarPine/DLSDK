package com.dl.playfun.data;

/**
 * Author: 彭石林
 * Time: 2022/7/2 14:53
 * Description: This is RetrofitHeadersConfig
 */
public interface RetrofitHeadersConfig {
    //不需要添加token api
    String NO_TOKEN_CHECK = "NO_TOKEN_CHECK:NO_TOKEN_CHECK";
    String NO_TOKEN_CHECK_KEY = "NO_TOKEN_CHECK";
    //初始化api
    String DEFAULT_API_INIT_URL = "DEFAULT_API_INIT_URL:DEFAULT_API_INIT_URL";
    String DEFAULT_API_INIT_URL_KEY = "DEFAULT_API_INIT_URL";
    //任务中心模块
    String TASK_CENTER_URL = "TASK_CENTER_URL:TASK_CENTER_URL";
    String TASK_CENTER_URL_KEY = "TASK_CENTER_URL";
}
