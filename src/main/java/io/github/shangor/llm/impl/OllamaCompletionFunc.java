package io.github.shangor.llm.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shangor.llm.LlmCompletionFunc;
import io.github.shangor.llm.pojo.OpenAiCompletionRequest;
import io.github.shangor.llm.pojo.OpenAiLlmResult;
import io.github.shangor.llm.pojo.OpenAiLlmStreamResult;
import io.github.shangor.llm.service.HttpService;
import io.github.shangor.util.DateTimeUtils;
import io.github.shangor.util.GenUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@Service
@ConditionalOnProperty(value = "llm.completion.provider", havingValue = "ollama")
public class OllamaCompletionFunc extends LlmCompletionFunc {
    @Value("${llm.completion.model}")
    private String model;
    @Value("${llm.embedding.url:http://localhost:11434/api/chat}")
    private URI url;
    private Map<String, String> headers = Map.of("Content-Type", "application/json;charset=utf-8");

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    HttpService httpService;

    @Override
    public OpenAiLlmResult complete(List<CompletionMessage> messages, Options options) {
        var ollamaResult = httpService.post(url, headers, Map.of("model", LlmCompletionFunc.getModel(options, model),
                "messages", messages,
                "stream", options.isStream(),
                "options", options), OllamaResult.class);
        var result = new OpenAiLlmResult();
        var usage = new OpenAiLlmResult.Usage();
        usage.setTotal_tokens(ollamaResult.getEvalCount() + ollamaResult.getPromptEvalCount());
        usage.setPrompt_tokens(ollamaResult.getPromptEvalCount());
        usage.setCompletion_tokens(ollamaResult.getEvalCount());
        result.setUsage(usage);
        var choice = new OpenAiLlmResult.Choice();
        choice.setIndex(0);
        choice.setMessage(ollamaResult.getMessage());
        choice.setFinish_reason(ollamaResult.getDoneReason());
        result.setModel(model);
        result.setCreated(DateTimeUtils.parseOllamaDateTime(ollamaResult.getCreatedAt()));
        return result;
    }

    @Override
    public Flux<ServerSentEvent<OpenAiLlmStreamResult>> completeStream(List<CompletionMessage> messages, Options options) {
        options.setStream(true);
        var requestId = UUID.randomUUID().toString();
        return httpService.postSeverSentEvent(url, null, Map.of("model", LlmCompletionFunc.getModel(options, model),
                "messages", messages,
                "stream", true,
                "options", options), true).mapNotNull(sse -> {
                    var data = sse.data();
                    if (StringUtils.isBlank(data)) {
                        return null;
                    }
                    var obj = GenUtils.jsonToObject(data, OllamaStreamResult.class);
                    var res = new OpenAiLlmStreamResult();
                    res.setId(requestId);
                    res.setModel(obj.getModel());
                    res.setCreated(DateTimeUtils.parseOllamaDateTime(obj.getCreatedAt()));
                    if (obj.isDone()) {
                        var usage = new OpenAiLlmStreamResult.Usage();
                        usage.setTotal_tokens(obj.getEvalCount() + obj.getPromptEvalCount());
                        usage.setPrompt_tokens(obj.getPromptEvalCount().intValue());
                        usage.setCompletion_tokens(obj.getEvalCount().intValue());
                        res.setUsage(usage);
                    }
                    var choice = new OpenAiLlmStreamResult.Choice();
                    choice.setIndex(0);
                    choice.setDelta(obj.getMessage());
                    choice.setFinish_reason(obj.getDoneReason());
                    res.setChoices(List.of(choice));

                    return res;
        }).map(s -> ServerSentEvent.builder(s).id(requestId).event("llm").build());
    }

    /**
     * {
     *   "model": "llama3.2",
     *   "created_at": "2023-12-12T14:13:43.416799Z",
     *   "message": {
     *     "role": "assistant",
     *     "content": "Hello! How are you today?"
     *   },
     *   "done": true,
     *   "total_duration": 5191566416,
     *   "load_duration": 2154458,
     *   "prompt_eval_count": 26,
     *   "prompt_eval_duration": 383809000,
     *   "eval_count": 298,
     *   "eval_duration": 4799921000
     * }
     */


    @Data
    public static class OllamaResult{
        private String model;
        private CompletionMessage message;
        private boolean done;
        @JsonProperty("created_at")
        protected String createdAt;
        @JsonProperty("done_reason")
        protected String doneReason;
        @JsonProperty("total_duration")
        protected long totalDuration;
        @JsonProperty("load_duration")
        protected long loadDuration;
        @JsonProperty("prompt_eval_count")
        protected long promptEvalCount;
        @JsonProperty("prompt_eval_duration")
        protected long promptEvalDuration;
        @JsonProperty("eval_count")
        protected long evalCount;
        @JsonProperty("eval_duration")
        protected long evalDuration;
    }

    @Data
    public static class OllamaStreamResult {
        protected String model;
        @JsonProperty("created_at")
        protected String createdAt;
        protected CompletionMessage message;
        protected boolean done;
        @JsonProperty("done_reason")
        protected String doneReason;
        @JsonProperty("total_duration")
        protected Long totalDuration;
        @JsonProperty("load_duration")
        protected Long loadDuration;
        @JsonProperty("prompt_eval_count")
        protected Long promptEvalCount;
        @JsonProperty("prompt_eval_duration")
        protected Long promptEvalDuration;
        @JsonProperty("eval_count")
        protected Long evalCount;
        @JsonProperty("eval_duration")
        protected Long evalDuration;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class OllamaOptions extends Options {
        private Integer num_predict;
    }

    @Data
    public static class OllamaRequest {
        private String model;
        private List<CompletionMessage> messages;
        private String format = "text"; // could be "json" or "text"
        private boolean stream;
        private Options options;

        public static OllamaRequest fromOpenAiRequest(OpenAiCompletionRequest request) {
            var ollamaRequest = new OllamaRequest();
            var options = new OllamaOptions();
            ollamaRequest.setOptions(options);
            if (request.getTemperature() != null) {
                options.setTemperature(request.getTemperature());
            }
            if (request.getMax_completion_tokens() != null) {
                options.setNum_predict(request.getMax_completion_tokens());
            }

            ollamaRequest.setModel(request.getModel());

            ollamaRequest.setFormat(request.getFormat());
            if (null == request.getStream()) {
                ollamaRequest.setStream(false);
            } else {
                ollamaRequest.setStream(request.getStream());
            }

            List<CompletionMessage> messages = new ArrayList<>();
            ollamaRequest.setMessages(messages);
            for (var messageReq : request.getMessages()) {
                CompletionMessage message = new CompletionMessage();
                message.setRole((String) messageReq.get("role"));
                var content = messageReq.get("content");
                var imageList = new LinkedList<String>();
                if (content instanceof String messageText) {
                    message.setContent(messageText);
                } else if (content instanceof List contents) {
                    for (var item : contents) {
                        if (item instanceof Map contentItem) {
                            String contentType = (String) contentItem.get("type");
                            if ("text".equals(contentType)) {
                                message.setContent((String) contentItem.get("text"));
                            } else if ("image_url".equals(contentType)) {
                                var list = contentItem.get("image_url");
                                if (list != null && list instanceof Map imageUrl) {
                                    var url = (String) imageUrl.get("url");
                                    if (url.startsWith("data:")) {
                                        imageList.add(url.split(",")[1]);
                                    } else {
                                        imageList.add(url);
                                    }
                                }
                            }
                        }

                    }
                }
                if (!imageList.isEmpty()) {
                    message.setImages(imageList);
                }

                messages.add(message);
            }

            return ollamaRequest;

        }
    }
}
