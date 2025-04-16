package com.jackdaw.chatwithnpc.api.json;

import java.util.ArrayList;

public class RequiredAction {
    public String type;
    public SubmitToolOutputs submit_tool_outputs;

    public static class SubmitToolOutputs {
        public ArrayList<ToolCall> tool_calls;
    }
}
