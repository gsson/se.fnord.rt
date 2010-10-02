package se.fnord.rt.client.testutil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConstantValueFuture<V> implements Future<V> {
    private final V value;

    public ConstantValueFuture(final V value) {
        this.value = value;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public V get() throws ExecutionException, InterruptedException {
        return value;
    }

    @Override
    public V get(final long timeout, final TimeUnit unit) throws ExecutionException, InterruptedException {
        return value;
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