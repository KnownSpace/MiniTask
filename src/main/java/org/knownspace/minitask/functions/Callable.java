package org.knownspace.minitask.functions;

@FunctionalInterface
public interface Callable<Result,Arg> {
    public Result call(Arg arg) throws Exception;
}