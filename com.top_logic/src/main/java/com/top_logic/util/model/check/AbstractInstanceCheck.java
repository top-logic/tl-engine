/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model.check;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.resources.TLTypePartResourceProvider;

/**
 * Base class for {@link InstanceCheck} testing a single attribute.
 * 
 * @see #getAttribute()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractInstanceCheck implements InstanceCheck {

	private final TLStructuredTypePart _attribute;

	/**
	 * Creates a {@link AbstractInstanceCheck}.
	 *
	 * @param attribute
	 *        See {@link #getAttribute()}.
	 */
	public AbstractInstanceCheck(TLStructuredTypePart attribute) {
		_attribute = attribute;
	}

	/**
	 * The attribute that has the constraint to be checked.
	 */
	public final TLStructuredTypePart getAttribute() {
		return _attribute;
	}

	@Override
	public final void check(Sink<ResKey> problems, TLObject object) {
		if (!getAttribute().tValid()) {
			return;
		}

		internalCheck(problems, object);
	}

	/**
	 * Implementation of {@link #check(Sink, TLObject)}.
	 */
	protected abstract void internalCheck(Sink<ResKey> problems, TLObject object);

	/**
	 * The value of the checked attribute of the given object.
	 */
	protected final Object getValue(TLObject object) {
		return object.tValue(getAttribute());
	}

	/**
	 * Wraps the given problem with location information where the problematic value is located.
	 */
	protected final ResKey withLocation(TLObject object, ResKey problem) {
		return I18NConstants.ERROR_CONSTRAINT_VIOLATED__OBJECT_ATTRIBUTE_MESSAGE.fill(label(object), label(_attribute),
			problem);
	}

	/**
	 * A label for the given attribute.
	 */
	protected Object label(TLStructuredTypePart attribute) {
		return TLTypePartResourceProvider.labelKey(attribute);
	}

	/**
	 * A label for the given object.
	 */
	protected Object label(TLObject object) {
		// The LabelProviderService is not started when the system boots and the first checks
		// happen. Since it depends on the ModelService, it is not possible to declare a dependency
		// on the LabelProviderService.
		if (LabelProviderService.Module.INSTANCE.isActive()) {
			String label = MetaLabelProvider.INSTANCE.getLabel(object);
			if (label == null || label.isEmpty()) {
				return I18NConstants.OBJECT_WITHOUT_NAME;
			}
			return label;
		} else {
			if (object == null) {
				return "null";
			}
			return object.toString();
		}
	}

}
