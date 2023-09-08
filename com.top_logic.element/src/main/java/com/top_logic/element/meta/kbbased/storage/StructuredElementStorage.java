/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ListStorage} for {@link StructuredElement}s.
 * 
 * TODO #25955: Remove class.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 * 
 * @deprecated Composition references have an implicit storage algorithm that cannot be customized.
 */
@Deprecated
public class StructuredElementStorage<C extends StructuredElementStorage.Config<?>> extends ListStorage<C> {

	/**
	 * {@link ConfigurationItem} for the {@link StructuredElementStorage}.
	 */
	@TagName("structure-storage")
	public interface Config<I extends StructuredElementStorage<?>> extends ListStorage.Config<I> {

		@StringDefault(StructuredElementWrapper.CHILD_ASSOCIATION)
		@Override
		String getTable();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link StructuredElementStorage}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public StructuredElementStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void checkBasicValue(TLObject object, TLStructuredTypePart attribute, Object value) throws IllegalArgumentException {
		/* When <code>true</code>, activate a special hack that treats objects without type as
		 * assignment compatible to all object types. This is necessary when a new StructuredElement
		 * is created, to break through a cyclic dependency that occurs when creating instances of
		 * local types (for example "DemoTypes:C"). Without this hack, initializing the
		 * <code>parent</code> reference of a new node would require the type of the node being
		 * created to be set, but resolving this type (a local type defined within its parent)
		 * requires the scope of the new node, and resolving this scope requires the parent to be
		 * initialized, which completes the cycle. */
		boolean ignoreMissingValueType = true;
		TLItemStorage.checkCorrectType(attribute, value, ignoreMissingValueType, storageMapping());
	}

}
