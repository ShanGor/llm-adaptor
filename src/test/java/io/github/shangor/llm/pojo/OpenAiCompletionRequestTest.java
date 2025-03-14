package io.github.shangor.llm.pojo;

import io.github.shangor.llm.impl.OllamaCompletionFunc;
import io.github.shangor.util.GenUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiCompletionRequestTest {
    @Test
    void testDeserialize() {
        var text = """
                {
                    "model": "gpt-4o",
                    "messages": [
                      {
                        "role": "user",
                        "content": [
                          {
                            "type": "text",
                            "text": "What is in this image?"
                          },
                          {
                            "type": "image_url",
                            "image_url": {
                              "url": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg"
                            }
                          }
                        ]
                      }
                    ],
                    "max_tokens": 300
                  }
                """;
        var request = GenUtils.jsonToObject(text, OpenAiCompletionRequest.class);
        var o = OllamaCompletionFunc.OllamaRequest.fromOpenAiRequest(request);
        assertNotNull(o);
        text = """
                {
                    "model": "gpt-4o",
                    "messages": [
                      {
                        "role": "developer",
                        "content": "You are a helpful assistant."
                      },
                      {
                        "role": "user",
                        "content": "Hello!"
                      }
                    ]
                  }
                """;
        request = GenUtils.jsonToObject(text, OpenAiCompletionRequest.class);
        o = OllamaCompletionFunc.OllamaRequest.fromOpenAiRequest(request);
        assertNotNull(o);
    }

}