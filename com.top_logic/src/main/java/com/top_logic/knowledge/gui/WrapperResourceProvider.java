/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.TLIDColumn;

/**
 * {@link ResourceProvider} for {@link TLObject} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrapperResourceProvider extends AbstractTLItemResourceProvider {

	/**
	 * Singleton {@link WrapperResourceProvider} instance.
	 */
	@SuppressWarnings("hiding")
	public static final WrapperResourceProvider INSTANCE = new WrapperResourceProvider();

	/**
	 * Creates a new {@link WrapperResourceProvider}. Links will be created to "defaultFor"
	 * component.
	 */
    protected WrapperResourceProvider() {
        this(null);
    }

	/**
	 * Creates a new {@link WrapperResourceProvider}. Links will be created to the given component.
	 */
	public WrapperResourceProvider(LayoutComponent gotoComponent) {
		super(gotoComponent);
    }

    @Override
	public String getLabel(Object object) {
		if (object instanceof TLObject) {
			TLObject tlObject = (TLObject) object;

			// The Java implementation class is not required to implement the TLNamed interface, but
			// may provide a name attribute anyway. However, since most implementation classes
			// extend AbstractWrapper, they implement TLNamed, whether they have a name attribute or
			// not.
			TLStructuredType type = tlObject.tType();
			if (type != null) {
				TLIDColumn idColumn = type.getAnnotation(TLIDColumn.class);

				if (idColumn != null) {
					TLStructuredTypePart idColumnPart = type.getPart(idColumn.getValue());

					if (idColumnPart != null) {
						return getValue(tlObject, idColumnPart);
					}
				}
			}

			if (object instanceof TLNamed) {
				return ((TLNamed) object).getName();
			}

			if (type != null) {
				TLStructuredTypePart namePart = type.getPart(TLNamed.NAME_ATTRIBUTE);
				if (namePart != null) {
					return getValue(tlObject, namePart);
				}
			}
		}

		return getDefaultLabel(object);
    }

	private String getValue(TLObject object, TLStructuredTypePart part) {
		Object value = object.tValue(part);

		return value == null ? "" : value.toString();
	}

}
