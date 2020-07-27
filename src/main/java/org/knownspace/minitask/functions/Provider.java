package org.knownspace.minitask.functions;

@FunctionalInterface
public interface Provider<Result> {
    public Result provide() throws Exception;
}