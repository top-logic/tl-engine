/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.values;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;

/**
 * A {@link ConfigurationItem} that serves as example value for {@link ConfigurationItem}-valued
 * properties.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface DemoConfigItem extends AbstractDemoConfigItem {

	/** @see #getPreinitializedUUID() */
	String VALUE_WITH_INITIALIZER = "preinitialized-uuid";

	/** @see #getMinUUIDSize() */
	String MIN_UUID_SIZE = "min-uuid-size";

	/**
	 * Property containing a unique identifier (UUID) which is pre-initialized with a random value.
	 */
	@ValueInitializer(UUIDInitializer.class)
	@Name(VALUE_WITH_INITIALIZER)
	String getPreinitializedUUID();

	/**
	 * Minimum length of {@link #getPreinitializedUUID()}.
	 */
	@Constraint(value = MinUUIDSizeConstraint.class, args = { @Ref(VALUE_WITH_INITIALIZER) })
	@IntDefault(30)
	@Name(MIN_UUID_SIZE)
	int getMinUUIDSize();

	/**
	 * Dependency checking that the UUID has the configured minumum size.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class MinUUIDSizeConstraint extends GenericValueDependency<Integer, String> {

		/**
		 * Creates a new {@link MinUUIDSizeConstraint}.
		 */
		public MinUUIDSizeConstraint() {
			super(Integer.class, String.class);
		}

		@Override
		protected void checkValue(PropertyModel<Integer> propertyA, PropertyModel<String> propertyB) {
			if (propertyA.getValue() == null) {
				return;
			}
			int minSize = propertyA.getValue().intValue();
			String uuid = StringServices.nonNull(propertyB.getValue());
			if (uuid.length() < minSize) {
				ResKey error = I18NConstants.ERROR_UUID_TOO_SHORT__UUID__MIN_SIZE.fill(uuid, minSize);
				propertyA.setProblemDescription(error);
				propertyB.setProblemDescription(error);
			}
		}

	}

}
