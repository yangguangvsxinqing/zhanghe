package com.fineos.theme.webservice;

import java.util.LinkedList;

public class RequestQueue {

	private LinkedList<Object> queue;

	public RequestQueue() {
		queue = new LinkedList<Object>();
	}

	public synchronized void clearPendingRequest() {
		try {
			queue.clear();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized int getPendingCount() {
		int count = 0;
		try {
			count = queue.size();
		} catch (Exception e) {
			e.printStackTrace();
			count = 0;
		}
		return count;
	}

	public synchronized boolean isEmpty() {
		boolean bIsEmpty = true;
		try {
			if (queue.size() > 0) {
				bIsEmpty = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bIsEmpty;
	}

	public synchronized void pushRequest(Object request) {
		queue.addLast(request);
		notify();
		return;
	}

	public synchronized Object popRequest() throws InterruptedException {
		while (true) {
			if (queue.size() > 0) {
				return queue.removeFirst();
			}
			try {
				wait();
			} catch (InterruptedException e) {
				throw e;
			}
		}
	}
}