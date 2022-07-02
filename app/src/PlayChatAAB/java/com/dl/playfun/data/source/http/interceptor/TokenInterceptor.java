package com.dl.playfun.data.source.http.interceptor;

import android.util.Log;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.data.RetrofitHeadersConfig;
import com.dl.playfun.data.source.LocalDataSource;
import com.dl.playfun.data.source.local.LocalDataSourceImpl;
import com.dl.playfun.entity.ApiConfigManagerEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

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
        Request request = builder.build();
        String path = chain.request().url().url().getPath();
        boolean upUrlFlag = false;
        if(request!=null){
            Headers headers = request.headers();
            if(headers!=null){
                //不需要登录token效验
                if(ObjectUtils.isNotEmpty(headers.get(RetrofitHeadersConfig.DEFAULT_API_INIT_URL_KEY))){
                    //初始化API
                    upUrlFlag = true;
                    builder.removeHeader(RetrofitHeadersConfig.DEFAULT_API_INIT_URL_KEY);
                    builder.removeHeader("Authorization");
                    //builder.url(apiServerUrl+path);
                }else if(ObjectUtils.isEmpty(headers.get(RetrofitHeadersConfig.NO_TOKEN_CHECK_KEY))){
                    if (localDataSource != null && localDataSource.readLoginInfo() != null && !StringUtils.isEmpty(localDataSource.readLoginInfo().getToken())) {
                        String token = localDataSource.readLoginInfo().getToken();

                        builder.removeHeader(RetrofitHeadersConfig.NO_TOKEN_CHECK_KEY);
                        builder.addHeader("Authorization", "Bearer " + token);
                    }
                }
            }
        }
        ApiConfigManagerEntity apiServerUrl = localDataSource.readApiConfigManagerEntity();
        if(apiServerUrl!=null && !upUrlFlag){
            try {
                URI customUrl = new URI(apiServerUrl.getPlayFunApiUrl());
                HttpUrl newUrl = request.url().newBuilder()
                        .host(customUrl.getHost())
                        .scheme(customUrl.getScheme())
                        .build();
                        builder.url(newUrl);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
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
