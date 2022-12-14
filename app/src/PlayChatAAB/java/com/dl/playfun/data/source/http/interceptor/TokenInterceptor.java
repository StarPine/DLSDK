package com.dl.playfun.data.source.http.interceptor;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.data.RetrofitHeadersConfig;
import com.dl.playfun.data.source.LocalDataSource;
import com.dl.playfun.data.source.local.LocalDataSourceImpl;
import com.dl.playfun.entity.ApiConfigManagerEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        int upUrlFlag = -1;
        ApiConfigManagerEntity apiServerUrl = localDataSource.readApiConfigManagerEntity();
        if(request!=null){
            Headers headers = request.headers();
            if(headers!=null){
                //不需要登录token效验
                if(!ObjectUtils.isEmpty(headers.get(RetrofitHeadersConfig.DEFAULT_API_INIT_URL_KEY))&& upUrlFlag == -1){
                    //初始化API
                    upUrlFlag = 0;
                    builder.removeHeader(RetrofitHeadersConfig.DEFAULT_API_INIT_URL_KEY);
                    builder.removeHeader("Authorization");
                    //builder.url(apiServerUrl+path);
                }
                if(!ObjectUtils.isEmpty(headers.get(RetrofitHeadersConfig.NO_TOKEN_CHECK_KEY)) && upUrlFlag == -1){
                    //登录接口api 不加token
                    upUrlFlag = 3;
                }
                if(!ObjectUtils.isEmpty(headers.get(RetrofitHeadersConfig.PlayChat_API_URL_KEY))){
                    upUrlFlag = 2;
                }
                if(headers.get(RetrofitHeadersConfig.DEFAULT_API_INIT_URL_KEY)==null && headers.get(RetrofitHeadersConfig.NO_TOKEN_CHECK_KEY)==null && headers.get(RetrofitHeadersConfig.PlayChat_API_URL_KEY)==null){
                    upUrlFlag = 1;
                }
                if(upUrlFlag == 1 || upUrlFlag == 2){
                    boolean flagToken = false;
                    UserDataEntity oldLocalUserData = localDataSource.readOldUserData();
                    if(oldLocalUserData != null){
                        if(!StringUtils.isEmpty(oldLocalUserData.getToken())){
                            String token = oldLocalUserData.getToken();
                            builder.removeHeader("Authorization");
                            builder.removeHeader(RetrofitHeadersConfig.NO_TOKEN_CHECK_KEY);
                            token = new String(token.getBytes(), StandardCharsets.UTF_8);
                            token = URLEncoder.encode(token, "utf-8");
                            builder.addHeader("Authorization", "Bearer " + token);
                            flagToken = true;
                        }
                    }

                    TokenEntity tokenEntity = localDataSource.readLoginInfo();
                    if (tokenEntity != null && !flagToken) {
                        if(!StringUtils.isEmpty(tokenEntity.getToken())){
                            builder.removeHeader("Authorization");
                            String token = tokenEntity.getToken();
                            builder.removeHeader(RetrofitHeadersConfig.NO_TOKEN_CHECK_KEY);
                            token = new String(token.getBytes(), StandardCharsets.UTF_8);
                            token = URLEncoder.encode(token, "utf-8");
                            builder.addHeader("Authorization", "Bearer " + token);
                        }
                    }

                }
            }
        }
        if(apiServerUrl!=null){
            try {
                URI customUrl = null;
                switch (upUrlFlag){
                    case -1:
                    case 1:
                    case 3:
                        //
                        customUrl = new URI(apiServerUrl.getPlayFunApiUrl());
                        break;
                    case 0:
                        //不做任何处理
                        break;
                    case 2:
                        //任务中心+福袋页面
                        customUrl = new URI(apiServerUrl.getPlayChatApiUrl());
                        break;
                }
                if(customUrl!=null){
                    HttpUrl newUrl = request.url().newBuilder()
                            .host(customUrl.getHost())
                            .scheme(customUrl.getScheme())
                            .build();
                    builder.url(newUrl);
                }
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
