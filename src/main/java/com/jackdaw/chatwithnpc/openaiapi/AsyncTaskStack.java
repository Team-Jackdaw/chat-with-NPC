package com.jackdaw.chatwithnpc.openaiapi;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;

import java.util.Stack;

public class AsyncTaskStack {
    private final Stack<Task> tasks = new Stack<>();
    private final Thread workerThread;

    public AsyncTaskStack() {
        workerThread = new Thread(this::processTasks);
        workerThread.start();
    }

    private void processTasks() {
        while (true) {
            Task task;
            synchronized (tasks) {
                while (tasks.isEmpty()) {
                    try {
                        tasks.wait();
                    } catch (InterruptedException e) {
                        ChatWithNPCMod.LOGGER.error(e.getMessage());
                    }
                }
                task = tasks.pop();
            }
            task.execute();
        }
    }

    public void addTask(Task task) {
        synchronized (tasks) {
            tasks.push(task);
            tasks.notify();
        }
    }

    public void clear() {
        synchronized (tasks) {
            tasks.clear();
        }
    }

    public void shutdown() {
        workerThread.interrupt();
    }
}

