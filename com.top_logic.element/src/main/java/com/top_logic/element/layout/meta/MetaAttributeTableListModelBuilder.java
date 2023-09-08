/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * The {@link MetaAttributeTableListModelBuilder} can be used to build the list of a
 * {@link TableComponent} to show all {@link TLTypePart}s of a {@link TLStructuredType}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MetaAttributeTableListModelBuilder implements ListModelBuilder {

	private static final Property<Boolean> SHOW_ALL = TypedAnnotatable.property(Boolean.class, "showAll", Boolean.FALSE);

	/**
	 * Singleton {@link MetaAttributeTableListModelBuilder} instance.
	 */
	public static final MetaAttributeTableListModelBuilder INSTANCE = new MetaAttributeTableListModelBuilder();

	private MetaAttributeTableListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		TLType currentModel = (TLType) aComponent.getModel();
		TLType normativeModel = ((TLTypePart) anObject).getOwner();
		if (currentModel != null && TLModelUtil.isCompatibleType(normativeModel, currentModel)) {
			return currentModel;
		} else {
			return normativeModel;
		}
	}

    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		if (anObject instanceof TLTypePart) {
			TLTypePart attribute = ((TLTypePart) anObject);

			TLStructuredType model = (TLStructuredType) aComponent.getModel();
			if (model == null) {
				return false;
			}

			if (model.getModelKind() == ModelKind.CLASS && showAll(aComponent)) {
				return TLModelUtil.isCompatibleType(attribute.getDefinition().getOwner(), model);
			} else {
				return attribute.getOwner() == model;
			}
		} else {
			return false;
		}
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return Collections.emptyList();
		} else {
			TLStructuredType type = (TLStructuredType) businessModel;
			if (type.getModelKind() == ModelKind.CLASS && showAll(aComponent)) {
				return list(((TLClass) type).getAllClassParts());
			} else {
				return type.getLocalParts();
			}
		}
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof TLStructuredType;
	}

	static boolean showAll(LayoutComponent aComponent) {
		return aComponent.get(SHOW_ALL).booleanValue();
	}

	static Object setShowAll(LayoutComponent component, boolean newValue) {
		return component.set(SHOW_ALL, Boolean.valueOf(newValue));
	}
}
