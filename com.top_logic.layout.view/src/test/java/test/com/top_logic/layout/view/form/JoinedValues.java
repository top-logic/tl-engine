/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.kbbased.storage.AbstractDerivedStorage;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.TracingScope;

/**
 * Derived attribute joining the values of other attributes of the same object, separated by a
 * blank.
 *
 * <p>
 * The values are read through {@link TLObject#tValue(TLStructuredTypePart)} like in any other Java
 * storage. In a {@link TracingScope}, these reads are reported and answered from the overlay of the
 * object.
 * </p>
 */
public class JoinedValues<C extends JoinedValues.Config<?>> extends AbstractDerivedStorage<C> {

	/**
	 * Configuration options for {@link JoinedValues}.
	 */
	public interface Config<I extends JoinedValues<?>> extends AbstractDerivedStorage.Config<I> {

		/**
		 * @see #getAttributes()
		 */
		String ATTRIBUTES = "attributes";

		/**
		 * The names of the attributes whose values are joined.
		 */
		@Name(ATTRIBUTES)
		@Format(CommaSeparatedStrings.class)
		List<String> getAttributes();

		/**
		 * @see #getAttributes()
		 */
		void setAttributes(List<String> value);

	}

	/**
	 * Creates a {@link JoinedValues} from configuration.
	 */
	public JoinedValues(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The {@link TLStorage} annotation making an attribute join the values of the given
	 * attributes.
	 */
	public static TLStorage annotation(String... attributes) {
		@SuppressWarnings("unchecked")
		Config<JoinedValues<?>> config = TypedConfiguration.newConfigItem(Config.class);
		config.setAttributes(List.of(attributes));
		TLStorage result = TypedConfiguration.newConfigItem(TLStorage.class);
		result.setImplementation(config);
		return result;
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) {
		List<String> values = new ArrayList<>();
		for (String name : getConfig().getAttributes()) {
			TLStructuredTypePart part = attribute.getOwner().getPart(name);
			values.add(String.valueOf(object.tValue(part)));
		}
		return String.join(" ", values);
	}

}
