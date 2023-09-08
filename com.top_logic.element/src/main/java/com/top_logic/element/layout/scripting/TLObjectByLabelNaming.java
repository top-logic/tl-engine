/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import static com.top_logic.basic.CollectionUtil.findSingleton;
import static com.top_logic.basic.shared.string.StringServicesShared.*;
import static com.top_logic.model.util.TLModelUtil.*;

import java.util.List;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;

/**
 * A {@link ModelNamingScheme} that identifies {@link TLObject}s by their type and label.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLObjectByLabelNaming
		extends AbstractModelNamingScheme<TLObject, TLObjectByLabelNaming.TLObjectByLabelName> {

	/** {@link ModelName} for the {@link TLObjectByLabelNaming}. */
	public interface TLObjectByLabelName extends ModelName {

		/** Property name of {@link #getObjectLabel()}. */
		String OBJECT_LABEL = "object-label";

		/** Property name of {@link #getClassName()}. */
		String CLASS_NAME = "class-name";

		/** The label of the object. */
		@Mandatory
		@NonNullable
		@Name(OBJECT_LABEL)
		String getObjectLabel();

		/** @see #getObjectLabel() */
		void setObjectLabel(String label);

		/**
		 * The qualified name of the type.
		 */
		@Mandatory
		@NonNullable
		@Name(CLASS_NAME)
		String getClassName();

		/** @see #getClassName() */
		void setClassName(String name);

	}

	/** Creates a {@link TLObjectByLabelNaming}. */
	public TLObjectByLabelNaming() {
		super(TLObject.class, TLObjectByLabelName.class);
	}

	@Override
	protected void initName(TLObjectByLabelName name, TLObject model) {
		name.setObjectLabel(getLabel(model));
		name.setClassName(qualifiedName(model.tType()));
	}

	@Override
	protected boolean isCompatibleModel(TLObject model) {
		if (model == null) {
			return false;
		}
		if (isEmpty(getLabel(model))) {
			return false;
		}
		TLStructuredType type = model.tType();
		if (!(type instanceof TLClass)) {
			return false;
		}
		if (type instanceof TLAssociation) {
			/* TLAssociations are instances of TLClass, too, for technical reasons. Therefore, they
			 * have to be filtered explicitly. */
			return false;
		}
		return isGlobal(type);
	}

	@Override
	public TLObject locateModel(ActionContext context, TLObjectByLabelName name) {
		TLClass tlClass = getClassByName(name.getClassName());
		return getInstanceByLabel(tlClass, name.getObjectLabel());
	}

	private TLClass getClassByName(String name) {
		return (TLClass) TLModelUtil.resolveQualifiedName(name);
	}

	private TLObject getInstanceByLabel(TLClass tlClass, String label) {
		return findSingleton(getAllInstances(tlClass), instance -> hasLabel(instance, label));
	}

	private List<TLObject> getAllInstances(TLClass tlClass) {
		/* As the object is an instance of exactly the given class, not one of its subclasses, the
		 * search can be restricted to the direct instances. */
		return MetaElementUtil.getAllDirectInstancesOf(tlClass, TLObject.class);
	}

	private boolean hasLabel(TLObject object, String label) {
		return label.equals(getLabel(object));
	}

	private String getLabel(TLObject model) {
		return MetaLabelProvider.INSTANCE.getLabel(model);
	}

}
