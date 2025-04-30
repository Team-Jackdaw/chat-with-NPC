package com.jackdaw.chatwithnpc.api;

import com.jackdaw.chatwithnpc.SettingManager;
import com.jackdaw.chatwithnpc.api.json.*;

import java.util.List;

public interface Ollama {
    /**
     * Request completion from the Ollama API. Please update the API_URL, CHAT_MODEL before you use the API.
     * @param prompt The prompt to be completed.
     * @return CompletionResponse
     * @throws Exception If the request fails.
     */
    static CompletionResponse completion(String prompt) throws Exception {
        return Completion.completionRequest(SettingManager.apiURL, SettingManager.chat_model, prompt);
    }

    /**
     * Request completion from the Ollama API. Please update the API_URL, CHAT_MODEL before you use the API.
     * @param request The completion request.
     * @return CompletionResponse
     * @throws Exception If the request fails.
     */
    static CompletionResponse completion(CompletionRequest request) throws Exception {
        return Completion.completionRequest(SettingManager.apiURL, request);
    }

    /**
     * Permanently run the model. Please update the API_URL, CHAT_MODEL before you use the API.
     * @return CompletionResponse
     * @throws Exception If the request fails.
     */
    static CompletionResponse runModel() throws Exception {
        CompletionRequest completionRequest = new CompletionRequest();
        completionRequest.model = SettingManager.chat_model;
        completionRequest.prompt = "";
        completionRequest.keep_alive = -1;
        return completion(completionRequest);
    }

    /**
     * Shutdown the model. Please update the API_URL, CHAT_MODEL before you use the API.
     * @return CompletionResponse
     * @throws Exception If the request fails.
     */
    static CompletionResponse stopModel() throws Exception {
        CompletionRequest completionRequest = new CompletionRequest();
        completionRequest.model = SettingManager.chat_model;
        completionRequest.prompt = "";
        completionRequest.keep_alive = 0;
        return completion(completionRequest);
    }

    /**
     * Request completion from the Ollama API. Please update the API_URL, CHAT_MODEL before you use the API.
     * @param messages The messages to be sent to the chat model. (use the messageBuilder to create messages)
     * @param tools The tools (functions) that can be used in the chat model.
     * @return ChatResponse
     * @throws Exception If the request fails.
     */
    static ChatResponse chat(List<Message> messages, List<Tool> tools) throws Exception {
        return ChatCompletion.chatRequest(SettingManager.apiURL, SettingManager.chat_model, messages, tools);
    }

    /**
     * Create a message builder to create messages for the chat API.
     * @return ChatCompletion.MessageBuilder
     */
    static MessageBuilder messageBuilder() {
        return ChatCompletion.messageBuilder();
    }

    /**
     * Create a message builder from exist messages.
     * @param messages The messages to be added to the builder.
     * @return ChatCompletion.MessageBuilder
     */
    static MessageBuilder messageBuilder(List<Message> messages) {
        return ChatCompletion.messageBuilder(messages);
    }

    /**
     * Embed the input texts to vectors. Please update the API_URL and EMBEDDING_MODEL before you use the API.
     * @param input The input to be embedded.
     * @return EmbeddingResponse
     * @throws Exception If the request fails.
     */
    static EmbeddingResponse embed(List<String> input) throws Exception {
        return Embedding.embedRequest(SettingManager.apiURL, SettingManager.embedding_model, input);
    }

    /**
     * Embed the input texts to vectors. Please update the API_URL and EMBEDDING_MODEL before you use the API.
     * @param request The embedding request.
     * @return EmbeddingResponse
     * @throws Exception If the request fails.
     */
    static EmbeddingResponse embed(EmbeddingRequest request) throws Exception {
        return Embedding.embedRequest(SettingManager.apiURL, request);
    }
}
