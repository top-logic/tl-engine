/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import junit.framework.TestCase;

import com.top_logic.basic.util.ExponentialBackoff;


/**
 * {@link TestCase}s for: {@link ExponentialBackoff}
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestExponentialBackoff extends TestCase {
	
	private static final double DELTA = 0.01;

	public void testStartA() {
		double start = 1;
		double backoffFactor = 2;
		double max = Double.POSITIVE_INFINITY;
		assertEquals(start, new ExponentialBackoff(start, backoffFactor, max).next(), DELTA);
	}
	
	public void testStartB() {
		double start = 0.3;
		double backoffFactor = 2;
		double max = Double.POSITIVE_INFINITY;
		assertEquals(start, new ExponentialBackoff(start, backoffFactor, max).next(), DELTA);
	}
	
	public void testFactorA() {
		double start = 1;
		double backoffFactor = 2;
		double max = Double.POSITIVE_INFINITY;
		ExponentialBackoff exponentialBackoff = new ExponentialBackoff(start, backoffFactor, max);
		assertEquals(start, exponentialBackoff.next(), DELTA);
		assertEquals(start * backoffFactor, exponentialBackoff.next(), DELTA);
		assertEquals(start * backoffFactor * backoffFactor, exponentialBackoff.next(), DELTA);
	}
	
	public void testFactorB() {
		double start = 0.3;
		double backoffFactor = 1.5;
		double max = Double.POSITIVE_INFINITY;
		ExponentialBackoff exponentialBackoff = new ExponentialBackoff(start, backoffFactor, max);
		assertEquals(start, exponentialBackoff.next(), DELTA);
		assertEquals(start * backoffFactor, exponentialBackoff.next(), DELTA);
		assertEquals(start * backoffFactor * backoffFactor, exponentialBackoff.next(), DELTA);
	}
	
	public void testMaxA() {
		double start = 1;
		double backoffFactor = 2;
		double max = 5;
		ExponentialBackoff exponentialBackoff = new ExponentialBackoff(start, backoffFactor, max);
		assertEquals(1, exponentialBackoff.next(), DELTA);
		assertEquals(2, exponentialBackoff.next(), DELTA);
		assertEquals(4, exponentialBackoff.next(), DELTA);
		assertEquals(5, exponentialBackoff.next(), DELTA);
		assertEquals(5, exponentialBackoff.next(), DELTA);
		assertEquals(5, exponentialBackoff.next(), DELTA);
	}
	
	public void testHasNext() {
		double start = 1;
		double backoffFactor = 2;
		double max = Double.POSITIVE_INFINITY;
		ExponentialBackoff exponentialBackoff = new ExponentialBackoff(start, backoffFactor, max);
		assertTrue(exponentialBackoff.hasNext());
		exponentialBackoff.next();
		assertTrue(exponentialBackoff.hasNext());
		exponentialBackoff.next();
		assertTrue(exponentialBackoff.hasNext());
	}
	
}
