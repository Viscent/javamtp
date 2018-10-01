package io.github.viscent.mtpattern.ch13.pipeline.example;

import java.io.IOException;

public interface RecordSource {

    void close() throws IOException;

    boolean hasNext();

    Record next();

}