package com.jackdaw.chatwithnpc.api;

import org.jetbrains.annotations.NotNull;
import com.jackdaw.chatwithnpc.api.json.EmbeddingRequest;
import com.jackdaw.chatwithnpc.api.json.EmbeddingResponse;

import java.util.List;

class Embedding {
    static EmbeddingResponse embedRequest(@NotNull String url, @NotNull String model, @NotNull List<String> input) throws Exception {
        EmbeddingRequest request = new EmbeddingRequest();
        request.model = model;
        request.input = input;
        String res = Request.sendRequest(request.toJson(), url + "/api/embed", Header.buildDefault(), Request.Action.POST);
        return EmbeddingResponse.fromJson(res);
    }

    static EmbeddingResponse embedRequest(@NotNull String url, @NotNull EmbeddingRequest request) throws Exception {
        String res = Request.sendRequest(request.toJson(), url + "/api/embed", Header.buildDefault(), Request.Action.POST);
        return EmbeddingResponse.fromJson(res);
    }
}
