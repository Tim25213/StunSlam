package com.tim25213.shieldbreaker.client.hud;

import java.util.ArrayDeque;
import java.util.Deque;

public class ClickTracker {
	private static final long WINDOW_NANOS = 1_000_000_000L;

	private final Deque<Long> left = new ArrayDeque<>();
	private final Deque<Long> right = new ArrayDeque<>();

	public void record(boolean leftButton) {
		(leftButton ? left : right).addLast(System.nanoTime());
	}

	public int cps(boolean leftButton) {
		Deque<Long> window = leftButton ? left : right;
		long cutoff = System.nanoTime() - WINDOW_NANOS;
		while (!window.isEmpty() && window.peekFirst() < cutoff) {
			window.removeFirst();
		}
		return window.size();
	}
}
