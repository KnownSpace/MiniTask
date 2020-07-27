package org.knownspace.minitask;

import org.knownspace.minitask.functions.Callback;
import org.knownspace.minitask.functions.Provider;
import org.knownspace.minitask.locks.ReadWriteFlag;
import org.knownspace.minitask.locks.SharedFlag;
import org.knownspace.minitask.locks.UniqueFlag;

public interface ITaskFactory {
    public <Result> ITask<Result> startTask(Provider<Result> fn);

    public ITask<Void> startTask(Callback fn);

    public UniqueFlag makeUniqueFlag();

    public <Result> ITaskCompletionEvent<Result> makeCompletionEvent();

    public SharedFlag makeSharedFlag(int count);

    public ReadWriteFlag makeReadWriteFlag();
}