/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Mutable variant of {@link Integer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MutableInteger extends Number {

	private int value;

	/**
	 * Creates a {@link MutableInteger} initialized to zero.
	 */
	public MutableInteger() {
		this(0);
	}

	/**
	 * Creates a {@link MutableInteger}.
	 *
	 * @param value See {@link #intValue()}
	 */
	public MutableInteger(int value) {
		this.value = value;
	}
	
	/**
	 * Setter for the {@link #intValue()} property.
	 */
	public void setValue(int value) {
		this.value = value;
	}
	
	/**
	 * Increments {@link #intValue()} by one.
	 * 
	 * @return The incremented value.
	 */
	public int inc() {
		this.value++;
		return this.value;
	}

	/**
	 * Increments {@link #intValue()} by the given amount.
	 * 
	 * @return The incremented value.
	 */
	public int inc(int increment) {
		this.value += increment;
		return this.value;
	}
	
	/**
	 * Decrements {@link #intValue()} by one.
	 * 
	 * @return The decremented value.
	 */
	public int dec() {
		this.value--;
		return this.value;
	}
	
	/**
	 * Decrements {@link #intValue()} by the given amount.
	 * 
	 * @return The decremented value.
	 */
	public int dec(int decrement) {
		this.value -= decrement;
		return this.value;
	}
	
	@Override
	public int intValue() {
		return this.value;
	}

	@Override
	public double doubleValue() {
		return this.value;
	}

	@Override
	public float floatValue() {
		return this.value;
	}

	@Override
	public long longValue() {
		return this.value;
	}

}
