package io.github.shangor.llm;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.shangor.llm.pojo.OpenAiLlmResult;
import io.github.shangor.llm.pojo.OpenAiLlmStreamResult;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public abstract class LlmCompletionFunc {
    protected String model;
    protected URI url;
    protected Options options = new Options();
    public OpenAiLlmResult complete(List<CompletionMessage> messages) {
        return complete(messages, options);
    }

    public static String getModel(Options options, String model) {
        if (StringUtils.isBlank(options.getModel())) {
            return model;
        }
        return options.getModel();
    }

    public abstract OpenAiLlmResult complete(List<CompletionMessage> messages, Options options);
    public OpenAiLlmResult complete(String prompt, List<CompletionMessage> historyMessages, Options options) {
        var messages = new LinkedList<>(historyMessages);
        messages.add(CompletionMessage.builder()
                .role("user")
                .content(prompt).build()
        );
        return complete(messages, options);
    }
    public OpenAiLlmResult complete(String prompt, List<CompletionMessage> historyMessages) {
        return complete(prompt, historyMessages, options);
    }

    public abstract Flux<ServerSentEvent<OpenAiLlmStreamResult>> completeStream(List<CompletionMessage> messages, Options options) ;
    public Flux<ServerSentEvent<OpenAiLlmStreamResult>> completeStream(String prompt) {
        return completeStream(prompt, Collections.emptyList(), options);
    }
    public Flux<ServerSentEvent<OpenAiLlmStreamResult>> completeStream(String prompt, List<CompletionMessage> historyMessages) {
        return completeStream(prompt, historyMessages, options);
    }
    public Flux<ServerSentEvent<OpenAiLlmStreamResult>> completeStream(String prompt, List<CompletionMessage> historyMessages, Options options) {
        var messages = new LinkedList<>(historyMessages);
        messages.add(CompletionMessage.builder()
                .role("user")
                .content(prompt).build()
        );
        return completeStream(messages, options);
    }

    public OpenAiLlmResult complete(String prompt) {
        return complete(prompt, Collections.emptyList(), options);
    }

    @Data
    public static class Options {
        protected Double temperature = 1.0;
        protected boolean stream = false;
        protected String model;
        protected List<ToolCall> tools;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionMessage {
        private String role;
        private String content;
        private List<String> images;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        private String id;
        private String type;
        private String name;
        private Map<String, Object> arguments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallResponse {
        private ToolCallFunction function;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallFunction {
        private String name;
        private Map<String, Object> arguments;
    }

    public abstract OpenAiLlmResult completeWithTools(List<CompletionMessage> messages, Options options);
}
