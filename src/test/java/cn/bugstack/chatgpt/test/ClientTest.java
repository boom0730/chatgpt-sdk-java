package cn.bugstack.chatgpt.test;

import cn.bugstack.chatgpt.common.Constants;
import cn.bugstack.chatgpt.domain.chat.ChatCompletionRequest;
import cn.bugstack.chatgpt.domain.chat.ChatCompletionResponse;
import cn.bugstack.chatgpt.domain.chat.Message;
import cn.bugstack.chatgpt.session.Configuration;
import cn.bugstack.chatgpt.session.OpenAiSession;
import cn.bugstack.chatgpt.session.OpenAiSessionFactory;
import cn.bugstack.chatgpt.session.defaults.DefaultOpenAiSessionFactory;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author 小傅哥，微信：fustack
 * @description 客户端输入测试
 * @github https://github.com/fuzhengwei
 * @Copyright 公众号：bugstack虫洞栈 | 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 */
public class ClientTest {

    public static void main(String[] args) throws InterruptedException {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setApiHost("https://pro-share-aws-api.zcyai.com/");
        configuration.setApiKey("sk-xMfFDTpPdxPiJVbl5cD12543B71f4477B40b24D30a3bDf88");
//        configuration.setAuthToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4ZmciLCJleHAiOjE2ODQ2MzEwNjAsImlhdCI6MTY4NDYyNzQ2MCwianRpIjoiMGU2M2Q3NDctNDk1YS00NDU3LTk1ZTAtOWVjMzkwNTlkNmQzIiwidXNlcm5hbWUiOiJ4ZmcifQ.xX4kaw-Pz2Jm4LBSvADzijud4nlNLFQUOaN6UgxrK9E");

        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        OpenAiSession openAiSession = factory.openSession();

        System.out.println("我是 OpenAI ChatGPT，请输入你的问题：");

        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .messages(new ArrayList<>())//这里就跟之前的不一样了 加入一个list来存放上下文
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                .user("testUser01")
                .build();

        // 3. 等待输入
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String text = scanner.nextLine();
            chatCompletion.getMessages().add(Message.builder().role(Constants.Role.USER).content(text).build());
            ChatCompletionResponse chatCompletionResponse = openAiSession.completions(chatCompletion);
            chatCompletion.getMessages().add(Message.builder().role(Constants.Role.USER).content(chatCompletionResponse.getChoices().get(0).getMessage().getContent()).build());
            // 输出结果
            System.out.println(chatCompletionResponse.getChoices().get(0).getMessage().getContent());
            System.out.println("请输入你的问题：");
        }

    }

}
