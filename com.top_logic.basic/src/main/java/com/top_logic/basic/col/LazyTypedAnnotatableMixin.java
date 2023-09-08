/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Mix-in interface for easy implementation of {@link TypedAnnotatable}, where no
 * {@link LazyTypedAnnotatable} super-class can be used.
 * 
 * <p>
 * Implementation of the interface is reduced to the two internal methods
 * {@link #internalAccessLazyPropertiesStore()} and
 * {@link #internalUpdateLazyPropertiesStore(InlineMap)}. See {@link LazyTypedAnnotatable} for an
 * example.
 * </p>
 * 
 * @see LazyTypedAnnotatable
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LazyTypedAnnotatableMixin extends TypedAnnotatable {

	@Override
	default <T> T set(TypedAnnotatable.Property<T> property, T value) {
		T oldValue = get(property);
		internalUpdateLazyPropertiesStore(
			internalAccessLazyPropertiesStore().putValue(property, property.internalize(value)));
		return oldValue;
	}

	@Override
	default <T> T get(TypedAnnotatable.Property<T> property) {
		return property.externalize(this, internalAccessLazyPropertiesStore().getValue(property));
	}

	@Override
	default boolean isSet(TypedAnnotatable.Property<?> property) {
		return internalAccessLazyPropertiesStore().hasValue(property);
	}

	@Override
	default <T> T reset(TypedAnnotatable.Property<T> property) {
		T oldValue = get(property);
		internalUpdateLazyPropertiesStore(internalAccessLazyPropertiesStore().removeValue(property));
		return oldValue;
	}

	/**
	 * Internal method to enable mixin-implementation of {@link LazyTypedAnnotatableMixin}.
	 *
	 * <p>
	 * Note: This method <b>must not</b> be called directly. It is only public, because interfaces
	 * can only declare public methods in Java.
	 * </p>
	 * 
	 * @return The internal properties store.
	 */
	InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore();

	/**
	 * Internal method to enable mixin-implementation of {@link LazyTypedAnnotatableMixin}.
	 * 
	 * <p>
	 * Updates the internal properties store after a {@link #set(Property, Object)} or
	 * {@link #reset(Property)} operation.
	 * </p>
	 * 
	 * <p>
	 * Note: This method <b>must not</b> be called directly. It is only public, because interfaces
	 * can only declare public methods in Java.
	 * </p>
	 */
	void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties);

}
