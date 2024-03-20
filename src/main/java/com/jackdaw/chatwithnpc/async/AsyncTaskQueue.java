package com.jackdaw.chatwithnpc.async;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A queue for asynchronous tasks.
 */
public class AsyncTaskQueue {
    private final BlockingQueue<Task> tasks = new LinkedBlockingQueue<>();
    private final Thread workerThread;

    /**
     * Create a new task queue.
     */
    public AsyncTaskQueue() {
        workerThread = new Thread(this::processTasks);
        workerThread.start();
    }

    private void processTasks() {
        while (true) {
            try {
                Task task = tasks.take();
                task.execute();
            } catch (InterruptedException e) {
                ChatWithNPCMod.LOGGER.error(e.getMessage());
                break;
            }
        }
    }

    /**
     * Add a task to the queue.
     * @param task The task to add.
     * @return True if the task was added, false if the queue is full.
     */
    public boolean addTask(Task task) {
        return tasks.offer(task);
    }

    /**
     * Clear all tasks from the queue.
     */
    public void clear() {
        tasks.clear();
    }

    /**
     * Shut down the task queue.
     */
    public void shutdown() {
        workerThread.interrupt();
    }
}


