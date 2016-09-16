package rcms.utilities.daqexpert.processing;

/**
 * Priority future of the job
 * 
 */
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PriorityFuture<T> implements RunnableFuture<T> {

	private RunnableFuture<T> runnableFuture;
	private int priority;

	public PriorityFuture(RunnableFuture<T> other, int priority) {
		this.runnableFuture = other;
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		return runnableFuture.cancel(mayInterruptIfRunning);
	}

	public boolean isCancelled() {
		return runnableFuture.isCancelled();
	}

	public boolean isDone() {
		return runnableFuture.isDone();
	}

	public T get() throws InterruptedException, ExecutionException {
		return runnableFuture.get();
	}

	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return runnableFuture.get();
	}

	public void run() {
		runnableFuture.run();
	}

}