package cn.bugstack.chatgpt.session.defaults;

import cn.bugstack.chatgpt.IOpenAiApi;
import cn.bugstack.chatgpt.interceptor.OpenAiInterceptor;
import cn.bugstack.chatgpt.session.Configuration;
import cn.bugstack.chatgpt.session.OpenAiSession;
import cn.bugstack.chatgpt.session.OpenAiSessionFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author 小傅哥，微信：fustack
 * @description OpenAi API Factory 会话工厂
 * @github https://github.com/fuzhengwei
 * @Copyright 公众号：bugstack虫洞栈 | 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class DefaultOpenAiSessionFactory implements OpenAiSessionFactory {

    private final Configuration configuration;

    public DefaultOpenAiSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public OpenAiSession openSession() {
        // 1. 日志配置
        // HttpLoggingInterceptor是OkHttp3提供的一个拦截器，
        // 主要用于记录HTTP请求和响应的详细信息，包括请求的URL、方法、头部信息，以及响应的状态码、响应体等内容。
        //置拦截器的日志记录级别为 BODY。HttpLoggingInterceptor.Level.BODY 是最详细的日志记录级别，它记录请求和响应的头部、主体和元数据。
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 2. 开启 Http 客户端
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new OpenAiInterceptor(configuration.getApiKey(), configuration.getAuthToken()))
                .connectTimeout(450, TimeUnit.SECONDS)
                .writeTimeout(450, TimeUnit.SECONDS)
                .readTimeout(450, TimeUnit.SECONDS)
                //.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 21284)))
                .build();

        // 3. 创建 API 服务
        IOpenAiApi openAiApi = new Retrofit.Builder()
                .baseUrl(configuration.getApiHost())//设置 API 的基础 URL
                .client(okHttpClient)//设置用于网络请求的 HTTP 客户端，这里使用之前配置的 OkHttpClient 实例。
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())//JSON 数据  Java 对象的互相转化
                .build().create(IOpenAiApi.class);

        return new DefaultOpenAiSession(openAiApi);
    }

}
