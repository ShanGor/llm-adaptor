package io.github.shangor.llm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test", "test-zhipu-completion"})
@ConditionalOnProperty(value = "llm.completion.provider", havingValue = "zhipu")
class ZhipuCompletionFuncTest {
    @Resource
    LlmCompletionFunc llmCompletionFunc;
    @Resource
    ObjectMapper objectMapper;

    @MockitoBean
    EmbeddingFunc func;

    @Test
    void testComplete() {
        var resp = llmCompletionFunc.complete("hello, what can you do for me?");
        assertNotNull(resp);
        log.info("Response is: {}", resp);
    }

    @Test
    void testCompleteStream() {
        var resp = llmCompletionFunc.completeStream("hello, what can you do for me?");
        resp.subscribe(s -> log.info("Response is: {}", GenUtils.objectToJsonSnake(s.data())));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}