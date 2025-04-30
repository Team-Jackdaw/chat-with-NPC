package com.jackdaw.chatwithnpc.conversation;

import org.junit.jupiter.api.Test;

public class ConversationHandlerTest {
    @Test
    public void testConversationHandler() {
        String message = "a<foo>d</foo>b<bar>d</bar>c";
        String noTags = message.replaceAll("(?s)<([^>]+)>.*?</\\1>", "");
        System.out.println(noTags);
    }
}
