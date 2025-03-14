package io.github.shangor.llm.impl;

import io.github.shangor.ForTestApp;
import io.github.shangor.llm.EmbeddingFunc;
import io.github.shangor.llm.LlmCompletionFunc;
import io.github.shangor.util.GenUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@SpringBootTest(classes = ForTestApp.class)
@ActiveProfiles({"test", "test-ollama-completion"})
@ConditionalOnProperty(value = "llm.completion.provider", havingValue = "ollama")
class OllamaCompletionFuncTest {
    @Resource
    LlmCompletionFunc llmCompletionFunc;

    @MockitoBean
    EmbeddingFunc func;

    @Test
    void complete() {
        var resp = llmCompletionFunc.completeStream("hello, what can you do for me?");
        resp.subscribe(s -> log.info("Response is: {}", GenUtils.objectToJsonSnake(s.data())));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}