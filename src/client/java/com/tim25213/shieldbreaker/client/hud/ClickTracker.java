package com.tim25213.shieldbreaker.client.hud;

import java.util.ArrayDeque;
import java.util.Deque;

public class ClickTracker {
	private static final long WINDOW_NANOS = 1_000_000_000L;

	private final Deque<Long> manualLeft = new ArrayDeque<>();
	private final Deque<Long> manualRight = new ArrayDeque<>();
	private final Deque<Long> automatedLeft = new ArrayDeque<>();
	private final Deque<Long> automatedRight = new ArrayDeque<>();

	public void record(boolean automated, boolean leftButton) {
		window(automated, leftButton).addLast(System.nanoTime());
	}

	public double cps(boolean automated, boolean leftButton) {
		Deque<Long> window = window(automated, leftButton);
		trim(window);
		return window.size();
	}

	private Deque<Long> window(boolean automated, boolean leftButton) {
		if (automated) {
			return leftButton ? automatedLeft : automatedRight;
		}
		return leftButton ? manualLeft : manualRight;
	}

	private static void trim(Deque<Long> window) {
		long cutoff = System.nanoTime() - WINDOW_NANOS;
		while (!window.isEmpty() && window.peekFirst() < cutoff) {
			window.removeFirst();
		}
	}
}
