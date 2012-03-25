package org.wrowclif.recipebox.util;

import java.util.concurrent.atomic.AtomicInteger;

public class ConstantInitializer {

	private static final AtomicInteger count;

	static {
		count = new AtomicInteger();
	}

	public static int assignId() {
		return count.getAndIncrement();
	}
}
