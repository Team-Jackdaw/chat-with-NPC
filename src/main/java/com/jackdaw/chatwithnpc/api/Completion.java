package com.jackdaw.chatwithnpc.api;

import org.jetbrains.annotations.NotNull;
import com.jackdaw.chatwithnpc.api.json.CompletionRequest;
import com.jackdaw.chatwithnpc.api.json.CompletionResponse;

class Completion {
    static @NotNull CompletionResponse completionRequest(@NotNull String url, @NotNull String model, @NotNull String prompt) throws Exception {
        CompletionRequest completionRequest = new CompletionRequest();
        completionRequest.model = model;
        completionRequest.prompt = prompt;
        completionRequest.stream = false;
        String res = Request.sendRequest(completionRequest.toJson(), url + "/api/generate", Header.buildDefault(), Request.Action.POST);
        return CompletionResponse.fromJson(res);
    }

    static @NotNull CompletionResponse completionRequest(@NotNull String url, @NotNull CompletionRequest request) throws Exception {
        String res = Request.sendRequest(request.toJson(), url + "/api/generate", Header.buildDefault(), Request.Action.POST);
        return CompletionResponse.fromJson(res);
    }
}
