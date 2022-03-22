package com.dl.playfun.data.source.http.interceptor;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.data.source.LocalDataSource;
import com.dl.playfun.data.source.local.LocalDataSourceImpl;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author wulei
 */
public class TokenInterceptor implements Interceptor {

    private final LocalDataSource localDataSource;

    public TokenInterceptor() {
        localDataSource = LocalDataSourceImpl.getInstance();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request()
                .newBuilder();
        String path = chain.request().url().url().getPath();
        if (!path.equals("/api/login") && !path.equals("/api/auth/login") && !path.equals("/api/register")) {
            if (localDataSource != null && localDataSource.readLoginInfo() != null && !StringUtils.isEmpty(localDataSource.readLoginInfo().getToken())) {
                String token = localDataSource.readLoginInfo().getToken();
                builder.addHeader("Authorization", "Bearer " + token).build();
            }
        }
        return chain.proceed(builder.build());
    }
}
