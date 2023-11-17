/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link PartAnnotationOptions} for cases where only the concrete {@link TLType} is known.
 */
public class SimplePartAnnotationOptions extends Function1<OptionModel<Class<?>>, TLType> {

	/**
	 * Delegate for {@link SimplePartAnnotationOptions} with a {@link TLModelPartRef} argument to
	 * resolve first.
	 */
	public static class ForTypeRef extends Function1<OptionModel<Class<?>>, TLModelPartRef> {

		private final SimplePartAnnotationOptions _delegate;

		/**
		 * Creates a {@link SimplePartAnnotationOptions.ForTypeRef}.
		 */
		public ForTypeRef(DeclarativeFormOptions options) {
			_delegate = new SimplePartAnnotationOptions(options);
		}

		@Override
		public OptionModel<Class<?>> apply(TLModelPartRef arg) {
			if (arg == null) {
				return new DefaultListOptionModel<>(Collections.emptyList());
			}
			TLType type = arg.resolveType();
			return _delegate.apply(type);
		}

	}

	private PartAnnotationOptions _delegate;

	/**
	 * Creates a {@link PartAnnotationOptions}.
	 */
	@CalledByReflection
	public SimplePartAnnotationOptions(DeclarativeFormOptions options) {
		_delegate = new PartAnnotationOptions(options);
	}

	@Override
	public OptionModel<Class<?>> apply(TLType type) {
		TLTypeKind kind = type == null ? null : TLTypeKind.getTLTypeKind(type);
		return _delegate.apply(type, kind, Boolean.FALSE);
	}

}
