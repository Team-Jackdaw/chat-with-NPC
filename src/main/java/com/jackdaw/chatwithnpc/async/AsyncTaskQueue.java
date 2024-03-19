package com.jackdaw.chatwithnpc.async;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncTaskQueue {
    private final BlockingQueue<Task> tasks = new LinkedBlockingQueue<>();
    private final Thread workerThread;

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

    public boolean addTask(Task task) {
        return tasks.offer(task);
    }

    public void clear() {
        tasks.clear();
    }

    public void shutdown() {
        workerThread.interrupt();
    }
}


