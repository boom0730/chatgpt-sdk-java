package cn.bugstack.chatgpt.test;

import cn.bugstack.chatgpt.common.Constants;
import cn.bugstack.chatgpt.domain.billing.BillingUsage;
import cn.bugstack.chatgpt.domain.billing.Subscription;
import cn.bugstack.chatgpt.domain.chat.ChatCompletionRequest;
import cn.bugstack.chatgpt.domain.chat.ChatCompletionResponse;
import cn.bugstack.chatgpt.domain.chat.Message;
import cn.bugstack.chatgpt.domain.edits.EditRequest;
import cn.bugstack.chatgpt.domain.edits.EditResponse;
import cn.bugstack.chatgpt.domain.embedd.EmbeddingResponse;
import cn.bugstack.chatgpt.domain.files.DeleteFileResponse;
import cn.bugstack.chatgpt.domain.files.UploadFileResponse;
import cn.bugstack.chatgpt.domain.images.ImageEnum;
import cn.bugstack.chatgpt.domain.images.ImageRequest;
import cn.bugstack.chatgpt.domain.images.ImageResponse;
import cn.bugstack.chatgpt.domain.other.OpenAiResponse;
import cn.bugstack.chatgpt.session.Configuration;
import cn.bugstack.chatgpt.session.OpenAiSession;
import cn.bugstack.chatgpt.session.OpenAiSessionFactory;
import cn.bugstack.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * @author 小傅哥，微信：fustack
 * @description 单元测试
 * @github https://github.com/fuzhengwei
 * @Copyright 公众号：bugstack虫洞栈 | 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
@Slf4j
public class ApiTest {

    private OpenAiSession openAiSession;

    @Before
    public void test_OpenAiSessionFactory() {
        // 1. 配置文件【如果你从小傅哥获取key 会给你提供apihost，你可以分别替换下】
        Configuration configuration = new Configuration();
        configuration.setApiHost("https://pro-share-aws-api.zcyai.com/");
        configuration.setApiKey("sk-xMfFDTpPdxPiJVbl5cD12543B71f4477B40b24D30a3bDf88");
        // 可以根据课程首页评论置顶说明获取 apihost、apikey；https://t.zsxq.com/0d3o5FKvc
        //configuration.setAuthToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4ZmciLCJleHAiOjE2ODMyODE2NzEsImlhdCI6MTY4MzI3ODA3MSwianRpIjoiMWUzZTkwYjYtY2UyNy00NzNlLTk5ZTYtYWQzMWU1MGVkNWE4IiwidXNlcm5hbWUiOiJ4ZmcifQ.YgQRJ2U5-9uydtd6Wbkg2YatsoX-y8mS_OJ3FdNRaX0");
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        this.openAiSession = factory.openSession();
    }
    /**
     * 此对话模型 3.5 接近于官网体验 & 流式应答
     */
    @Test
    public void test_chat_completions_stream() throws JsonProcessingException, InterruptedException {
        // 1. 创建参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build()))
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .build();
        // 2. 发起请求
        EventSource eventSource = openAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                log.info("测试结果：{}", data);
            }
        });
        // 等待 服务器发送回来的事件 真实使用的时候这个不会用到
        new CountDownLatch(1).await();
    }

    /**
     * 上下文对话
     */
    @Test
    public void test_chat_completions_context() {
        // 1-1. 创建参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .messages(new ArrayList<>())
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .user("testUser01")
                .build();
        // 写入请求信息
        chatCompletion.getMessages().add(Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build());

        // 1-2. 发起请求
        ChatCompletionResponse chatCompletionResponse01 = openAiSession.completions(chatCompletion);
        log.info("测试结果：{}", chatCompletionResponse01.getChoices());

        // 写入请求信息
        chatCompletion.getMessages().add(Message.builder().role(Constants.Role.USER).content(chatCompletionResponse01.getChoices().get(0).getMessage().getContent()).build());
        chatCompletion.getMessages().add(Message.builder().role(Constants.Role.USER).content("换一种写法").build());

        ChatCompletionResponse chatCompletionResponse02 = openAiSession.completions(chatCompletion);
        log.info("测试结果：{}", chatCompletionResponse02.getChoices());
    }
    /**
     * 文本修复
     */
    @Test
    public void test_edit() {
        // 文本请求
        EditRequest textRequest = EditRequest.builder()
                .input("码农会锁")
                .instruction("帮我修改错字")
                .model(EditRequest.Model.TEXT_DAVINCI_EDIT_001.getCode()).build();
        EditResponse textResponse = openAiSession.edit(textRequest);
        log.info("测试结果：{}", textResponse);

        // 代码请求
        EditRequest codeRequest = EditRequest.builder()
                // j <= 10 应该修改为 i <= 10
                .input("for (int i = 1; j <= 10; i++) {\n" +
                        "    System.out.println(i);\n" +
                        "}")
                .instruction("这段代码执行时报错，请帮我修改").model(EditRequest.Model.CODE_DAVINCI_EDIT_001.getCode()).build();
        EditResponse codeResponse = openAiSession.edit(codeRequest);
        log.info("测试结果：{}", codeResponse);
    }

    /**
     * 生成图片
     */
    @Test
    public void test_genImages() {
        // 方式1，简单调用
        ImageResponse imageResponse01 = openAiSession.genImages("画一个996加班的程序员");
        log.info("测试结果：{}", imageResponse01);

        // 方式2，调参调用
        ImageRequest imageRequest = ImageRequest.builder()
                .prompt("画一个996加班的程序员")
                //Base64 编码的 JSON 格式进行传输。这在需要传输二进制数据时特别有用，因为 JSON 本身只支持文本数据。
                .size(ImageEnum.Size.size_256.getCode())
                .responseFormat(ImageEnum.ResponseFormat.B64_JSON.getCode()).build();
        ImageResponse imageResponse02 = openAiSession.genImages(imageRequest);
        log.info("测试结果：{}", imageResponse02);
    }

    /**
     * 修改图片，有3个方法，入参不同。
     */
    @Test
    public void test_editImages() throws IOException {
        ImageResponse imageResponse = openAiSession.editImages(new File("/Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/chatgpt-sdk-java/docs/images/996.png"), "去除图片中的文字");
        log.info("测试结果：{}", imageResponse);
    }

    @Test
    public void test_embeddings() {
        EmbeddingResponse embeddingResponse = openAiSession.embeddings("哈喽", "嗨", "hi!");
        log.info("测试结果：{}", embeddingResponse);
    }

    @Test
    public void test_files() {
        OpenAiResponse<File> openAiResponse = openAiSession.files();
        log.info("测试结果：{}", openAiResponse);
    }

    @Test
    public void test_uploadFile() {
        UploadFileResponse uploadFileResponse = openAiSession.uploadFile(new File("/Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/chatgpt-sdk-java/docs/files/introduce.md"));
        log.info("测试结果：{}", uploadFileResponse);
    }

    @Test
    public void test_deleteFile() {
        DeleteFileResponse deleteFileResponse = openAiSession.deleteFile("file id 上传后才能获得");
        log.info("测试结果：{}", deleteFileResponse);
    }

    @Test
    public void test_subscription() {
        Subscription subscription = openAiSession.subscription();
        log.info("测试结果：{}", subscription);
    }

    @Test
    public void test_billingUsage() {
        BillingUsage billingUsage = openAiSession.billingUsage(LocalDate.of(2023, 3, 20), LocalDate.now());
        log.info("测试结果：{}", billingUsage.getTotalUsage());
    }
}
