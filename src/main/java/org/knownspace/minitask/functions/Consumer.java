package org.knownspace.minitask.functions;

@FunctionalInterface
public interface Consumer<Arg> {
    public void call(Arg arg) throws Exception;
}