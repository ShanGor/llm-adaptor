package io.github.shangor.llm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shangor.llm.service.HttpService;
import io.github.shangor.llm.service.impl.DefaultHttpServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmAutoConfig {
    @Bean
    @ConditionalOnMissingBean(HttpService.class)
    public HttpService defaultHttpService(ObjectMapper objectMapper) {
        return new DefaultHttpServiceImpl(objectMapper);
    }
}
