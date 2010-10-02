package se.fnord.rt.client.testutil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConstantExceptionFuture<V> implements Future<V> {
    private final Exception exception;

    public ConstantExceptionFuture(final Exception exception) {
        this.exception = exception;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public V get() throws ExecutionException, InterruptedException {
        throw new ExecutionException(exception);
    }

    @Override
    public V get(final long timeout, final TimeUnit unit) throws ExecutionException, InterruptedException {
        throw new ExecutionException(exception);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }
}