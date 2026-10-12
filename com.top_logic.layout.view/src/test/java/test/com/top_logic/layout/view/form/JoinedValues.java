/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.kbbased.storage.AbstractDerivedStorage;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.TracedValueComputation;
import com.top_logic.model.util.Pointer;

/**
 * Derived attribute joining the values of other attributes of the same object, separated by a
 * blank.
 *
 * <p>
 * The traced computation reports the attributes it reads directly. An attribute it reads that is
 * derived itself is read as the object delivers it, without tracing into its computation.
 * </p>
 */
public class JoinedValues<C extends JoinedValues.Config<?>> extends AbstractDerivedStorage<C>
		implements TracedValueComputation {

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
		return join(object, attribute, null);
	}

	@Override
	public Object computeTraced(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		TLObject self = object;
		if (overlays != null) {
			TLObject overlay = overlays.getExistingOverlay(object);
			if (overlay != null) {
				self = overlay;
			}
		}
		return join(self, attribute, trace);
	}

	private Object join(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace) {
		List<String> values = new ArrayList<>();
		for (String name : getConfig().getAttributes()) {
			TLStructuredTypePart part = attribute.getOwner().getPart(name);
			if (trace != null) {
				trace.add(Pointer.create(object, part));
			}
			values.add(String.valueOf(object.tValue(part)));
		}
		return String.join(" ", values);
	}

}
