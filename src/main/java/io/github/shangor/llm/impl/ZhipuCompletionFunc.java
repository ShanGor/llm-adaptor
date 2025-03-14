package io.github.shangor.llm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shangor.llm.LlmCompletionFunc;
import io.github.shangor.llm.pojo.OpenAiLlmResult;
import io.github.shangor.llm.pojo.OpenAiLlmStreamResult;
import io.github.shangor.llm.service.HttpService;
import io.github.shangor.util.GenUtils;
import io.github.shangor.util.TimeKeeper;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static io.github.shangor.util.DateTimeUtils.STD_GMT_FMT;


@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@Service
@ConditionalOnProperty(value = "rag.llm.completion.provider", havingValue = "zhipu")
public class ZhipuCompletionFunc extends LlmCompletionFunc {
    @Value("${rag.llm.completion.model}")
    private String model;
    @Value("${rag.llm.completion.url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private URI url;
    private final Map<String, String> headers;
    @Resource
    private ObjectMapper objectMapper;

    public ZhipuCompletionFunc(@Value("${rag.llm.completion.api-key}") String apiKey) {
        this.headers = Map.of("Authorization", "Bearer " + apiKey,
                "Content-Type", "application/json");
    }

    @Resource
    HttpService httpService;

    @Override
    public OpenAiLlmResult complete(List<CompletionMessage> messages, Options options) {
        log.info("Zhipu request with messages list {}", messages.size());
        var tk = TimeKeeper.start();
        var result = httpService.post(url, headers,
                Map.of("model", LlmCompletionFunc.getModel(options, model),
                        "messages", messages,
                        "temperature", options.getTemperature(),
                        "stream", options.isStream()),
                OpenAiLlmResult.class);
        log.info("Zhipu response from {} in {} seconds", url, tk.elapsedSeconds());
        return result;
    }

    @Override
    public Flux<ServerSentEvent<OpenAiLlmStreamResult>> completeStream(List<CompletionMessage> messages, Options options) {
        log.info("Zhipu request with messages list {}", messages.size());
        AtomicReference<String> requestId = new AtomicReference<>("");
        AtomicReference<OpenAiLlmResult.Usage> usage = new AtomicReference<>(null);
        return httpService.postSeverSentEvent(url, headers,
                Map.of("model", LlmCompletionFunc.getModel(options, model),
                        "messages", messages,
                        "temperature", options.getTemperature(),
                        "stream", true), false)
                .mapNotNull(sse -> {
                    var data = sse.data();
                    if ("[DONE]".equals(data)) {
                        return null;
                    }
                    return GenUtils.jsonToObject(data, OpenAiLlmStreamResult.class);
        }).map(s -> ServerSentEvent.builder(s).id(requestId.get()).event("llm").build());
    }

    /**
     * @param epochTimeAtSeconds unix timestamp
     * @return 2025-02-06T13:12:50.8303797Z
     */
    public static String convertCreated(long epochTimeAtSeconds) {
        var time = LocalDateTime.ofEpochSecond(epochTimeAtSeconds, 0, ZoneOffset.UTC);
        return STD_GMT_FMT.format(time);
    }

    public static Pattern KW_JSON_PATTERN = Pattern.compile("\\{[\\s\\S]*}");



}
