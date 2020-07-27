package org.knownspace.minitask.functions;

@FunctionalInterface
public interface Callback {
    public void call() throws Exception;
}