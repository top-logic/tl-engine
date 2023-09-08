/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580._20653;

import com.google.inject.Inject;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.util.model.ModelService;

/**
 * Migration rewriter for Ticket #20653.
 * 
 * <p>
 * This {@link RewritingEventVisitor} sets the correct {@link TLStructuredTypePart} into
 * {@link StructuredElementWrapper#CHILD_ASSOCIATION}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HasStructureChildMASetter extends RewritingEventVisitor {

	private static final String CHILDREN_ATTR = "children";

	private static final String STRUCTURED_ELEMENT_CONTAINER_CLASS_NAME = "StructuredElementContainer";

	private static final String TL_ELEMENT_MODULE_NAME = "tl.element";

	private ObjectKey _childrenAttrKey;

	/**
	 * Initialises this {@link HasStructureChildMASetter} with the given {@link ModelService}.
	 */
	@Inject
	public void initModelService(ModelService modelService) {
		TLModel applicationModel = modelService.getModel();
		TLModule module = applicationModel.getModule(TL_ELEMENT_MODULE_NAME);
		if (module == null) {
			throw new RuntimeException(
				"No module " + TL_ELEMENT_MODULE_NAME + " to find type with " + CHILDREN_ATTR + " attribute.");
		}
		TLClass tlClass = (TLClass) module.getType(STRUCTURED_ELEMENT_CONTAINER_CLASS_NAME);
		if (tlClass == null) {
			throw new RuntimeException(
				"No type " + STRUCTURED_ELEMENT_CONTAINER_CLASS_NAME + " found in module " + module
					+ " to get attribute " + CHILDREN_ATTR + ".");
		}

		TLStructuredTypePart childrenAttr = tlClass.getPart(CHILDREN_ATTR);
		if (childrenAttr == null) {
			throw new RuntimeException(
				"No attribute  " + CHILDREN_ATTR + " found in type " + tlClass + ".");
		}

		_childrenAttrKey = childrenAttr.tHandle().tId();
	}

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		if (StructuredElementWrapper.CHILD_ASSOCIATION.equals(event.getObjectType().getName())) {
			event.setValue(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR, null, _childrenAttrKey);
		}
		return super.visitCreateObject(event, arg);
	}

}

