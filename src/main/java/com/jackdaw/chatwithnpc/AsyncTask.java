package com.jackdaw.chatwithnpc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AsyncTask {
    public static final ConcurrentLinkedQueue<TaskResult> taskQueue = new ConcurrentLinkedQueue<>();

    /**
     * Call the task asynchronously. Then call the result in main thread if it is callable.
     * @param task The task to call.
     */
    public static void call(@NotNull Task task) {
        CompletableFuture<TaskResult> future = CompletableFuture.supplyAsync(task::execute);
        future.thenAccept(result -> {
            if (result.isCallable()) {
                taskQueue.add(result);
            }
        });
    }

    /**
     * Check if the task queue is empty.
     * @return True if the task queue is empty.
     */
    public static boolean isTaskQueueEmpty() {
        return taskQueue.isEmpty();
    }

    /**
     * Poll the task queue.
     * @return The task result.
     */
    public static TaskResult pollTaskQueue() {
        return taskQueue.poll();
    }

    /**
     * Create a task result that does need to continue.
     * @return The task that does nothing.
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull TaskResult nothingToDo() {
        return new NoCallResult();
    }

    /**
     * Task interface
     */
    public interface Task {
        /**
         * Execute the task and return the result.
         */
        @NotNull TaskResult execute();
    }

    /**
     * Task result interface
     */
    public interface TaskResult {
        /**
         * Execute the result.
         */
        void execute();

        /**
         * Check if the result is callable.
         * @return True if the result is callable.
         */
        boolean isCallable();
    }

    /**
     * Task result that does need to continue.
     */
    public static class NoCallResult implements TaskResult {

        /**
         * Do nothing.
         */
        @Override
        public void execute() {}

        /**
         * Check if the result is callable.
         * @return False.
         */
        @Override
        public boolean isCallable() {
            return false;
        }
    }
}



