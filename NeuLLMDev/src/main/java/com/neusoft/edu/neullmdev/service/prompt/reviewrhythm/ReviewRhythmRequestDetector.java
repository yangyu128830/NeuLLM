package com.neusoft.edu.neullmdev.service.prompt.reviewrhythm;

/** 识别「复习节奏表」请求，供 {@link com.neusoft.edu.neullmdev.service.llm.KimiChatServiceImpl} 挂载专答提示。 */
public final class ReviewRhythmRequestDetector {

    private ReviewRhythmRequestDetector() {
    }

    public static boolean matches(String userInput) {
        if (userInput == null || userInput.isBlank()) {
            return false;
        }
        String s = userInput.strip();
        if (s.startsWith("【复习节奏表单】")) {
            return true;
        }
        return s.contains("科目/主题：")
                && s.contains("备考天数：")
                && (s.contains("复习节奏表")
                || s.contains("按天拆解")
                || s.contains("复习节奏"));
    }
}
