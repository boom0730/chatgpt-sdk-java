package cn.bugstack.chatgpt.session;

import cn.bugstack.chatgpt.IOpenAiApi;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;

/**
 * @author 小傅哥，微信：fustack
 * @description 配置信息
 * @github https://github.com/fuzhengwei
 * @Copyright 公众号：bugstack虫洞栈 | 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {
    @Getter
    @Setter
    private IOpenAiApi openAiApi;

    @Getter
    @NotNull
    private String apiKey;

    @Getter
    private String apiHost;

    /**
     * 字段废弃，不在使用
     */
    @Getter
    @Deprecated
//    @NotNull
    private String authToken;

    @Getter
    @Setter
    private OkHttpClient okHttpClient;//客户端 相关信息

    public EventSource.Factory createRequestFactory() {
        //直接放到configuration里面来创建  因为反正要使用
        return EventSources.createFactory(okHttpClient);
    }
}
