package com.neusoft.edu.neullmdev.config.llm;

import org.springframework.web.reactive.function.client.WebClientResponseException;

/** 将 WebClient / Moonshot 异常转成对用户可见的口语化中文（智学伴语气）。 */
public final class LlmFriendlyErrors {

    private LlmFriendlyErrors() {
    }

    public static String chatUserMessage(Throwable error) {
        if (error instanceof WebClientResponseException ex) {
            int code = ex.getStatusCode().value();
            if (code == 429) {
                return "哎呀，模型那边这会儿有点「排队爆满」（429 限流）…你先缓个一两分钟，喝口水再来点一次，我陪你重试，好不？";
            }
            if (code >= 400 && code < 500) {
                return "模型接口刚回了 " + code + "，多半是密钥、额度或请求太密。你先检查一下配置或稍后再试，我在这儿等你～";
            }
            if (code >= 500) {
                return "模型服务那边暂时不太稳，过一会儿再叫我好不？咱不着急这一会儿。";
            }
        }
        String msg = error.getMessage() == null ? "" : error.getMessage();
        if (msg.contains("429") || msg.contains("Too Many Requests")) {
            return "哎呀，模型那边这会儿有点「排队爆满」（429 限流）…你先缓个一两分钟，再来点一次，我陪你重试，好不？";
        }
        return "我刚才跟模型连麦没连上（" + shorten(msg, 100) + "）。你方便的时候再发我一次，我认真听。";
    }

    private static String shorten(String m, int max) {
        String t = m.replace('\n', ' ').trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
