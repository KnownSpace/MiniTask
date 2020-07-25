package org.knownspace.minitask;

import java.util.function.Supplier;

import org.knownspace.minitask.locks.UniqueFlag;

public interface ITaskFactory {
    public <Result> ITask<Result> startTask(Supplier<Result> fn);

    public ITask<Void> startTask(Runnable fn);

    public UniqueFlag makeUniqueFlag();
}