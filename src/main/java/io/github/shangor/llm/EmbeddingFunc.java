package io.github.shangor.llm;

public interface EmbeddingFunc {
    int getDimension();
    int getMaxTokenSize();
    int getConcurrentLimit();
    float[] convert(String input);
}
