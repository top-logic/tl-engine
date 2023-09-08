/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import com.top_logic.basic.config.annotation.Inspectable;

/**
 * {@link TypedAnnotatable} that works with lazy storage.
 * 
 * <p>
 * Note: This implementation must not be used multi-threaded, consider using
 * {@link ConcurrentTypedAnnotationContainer} in such situations.
 * </p>
 * 
 * <p>
 * Note: This class is not intended to be used stand-alone (e.g. as class member to implement
 * {@link TypedAnnotatable} through delegation). Either use {@link LazyTypedAnnotatable} as super
 * class, or implement {@link LazyTypedAnnotatableMixin} and implement internal storage methods.
 * </p>
 * 
 * @see TypedAnnotationContainer
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LazyTypedAnnotatable implements LazyTypedAnnotatableMixin {

	/**
	 * Creates a {@link LazyTypedAnnotatable}.
	 * 
	 * <p>
	 * Note: This constructor should only be called from super-classes. It is only public for
	 * compatibility reasons. This class is not intended to be used stand-alone (e.g. as class
	 * member to implement {@link TypedAnnotatable} through delegation). Either use
	 * {@link LazyTypedAnnotatable} as super class, or implement {@link LazyTypedAnnotatableMixin} and
	 * implement internal storage methods.
	 * </p>
	 */
	public LazyTypedAnnotatable() {
		super();
	}

	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();

	@Override
	public InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore() {
		return _properties;
	}

	@Override
	public void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties) {
		_properties = newProperties;
	}

	// Note This dummy-override is necessary to allow extending classes to override this method.
	// Without this dummy override, at least some Eclipse compiler versions complain that this super
	// implementation cannot be called directly.
	@Override
	public <T> T set(Property<T> property, T value) {
		return LazyTypedAnnotatableMixin.super.set(property, value);
	}

	// Note This dummy-override is necessary to allow extending classes to override this method.
	// Without this dummy override, at least some Eclipse compiler versions complain that this super
	// implementation cannot be called directly.
	@Override
	public <T> T reset(Property<T> property) {
		return LazyTypedAnnotatableMixin.super.reset(property);
	}

}

