package com.dl.playfun.data.source.http.interceptor;

import android.util.Log;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.data.source.LocalDataSource;
import com.dl.playfun.data.source.local.LocalDataSourceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author 彭石林
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
                builder.addHeader("Authorization", "Bearer " + token);
                builder.build();
            }
        }
        String apiServerUrl = localDataSource.readKeyValue(AppConfig.KEY_API_SERVER_URL);
        if(apiServerUrl!=null){
            builder.url(apiServerUrl+path);
            builder.build();
        }
//        Response response = chain.proceed(builder.build());
//        // 输出返回结果
//        try {
//            Charset charset;
//            charset = Charset.forName("UTF-8");
//            ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
//            Reader jsonReader = new InputStreamReader(responseBody.byteStream(), charset);
//            BufferedReader reader = new BufferedReader(jsonReader);
//            StringBuilder sbJson = new StringBuilder();
//            String line = reader.readLine();
//            do {
//                sbJson.append(line);
//                line = reader.readLine();
//            } while (line != null);
//            Log.e("请求地址拦截: " ,path+"==\t"+ sbJson.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return chain.proceed(builder.build());
    }
}
