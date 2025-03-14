package io.github.shangor.llm.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
public class OpenAiCompletionRequest {
    private String model;
    private List<Map<String, Object>> messages;
    private Integer max_completion_tokens;
    private Boolean logprobs = false;
    private Boolean stream = true;
    private Double temperature = 1.0;
    private String format = "text";
    private Map<String, Object> stream_options = Map.of("include_usage", true);

    @Data
    public static class RequestMessage {
        protected String role;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class RequestMessageText extends RequestMessage {
        private String content;
    }
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class RequestMessageImage extends RequestMessage {
        private List<Content> content;
    }

    @Data
    public static class Content {
        private String type;
        private String text;
        private String image_url;
    }
}
