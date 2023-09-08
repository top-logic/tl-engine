/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.binding;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.TLObject;

/**
 * Binding for a certain {@link MetaObject static type}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractImplementationBinding implements ImplementationBinding {

	/**
	 * Legacy wrapper constructor signature.
	 * 
	 * @see #wrapWith(Class, KnowledgeItem)
	 */
	private static final Class<?>[] CONSTRUCTOR_PARAMS = new Class[] { KnowledgeObject.class };

	private MOClass _staticType;

	@Override
	public void initTable(MOClass staticType) {
		_staticType = staticType;
	}

	/**
	 * The table type, this {@link ImplementationBinding} is used for.
	 */
	public MOClass staticType() {
		return _staticType;
	}

	/**
	 * Name of {@link #staticType()}.
	 */
	public String staticTypeName() {
		return _staticType.getName();
	}

	/**
	 * Creates a legacy wrapper for the given {@link KnowledgeItem} by invoking its constructor with
	 * the signature {@link #CONSTRUCTOR_PARAMS}.
	 */
	protected final TLObject wrapWith(Class<? extends TLObject> wrapperClass, KnowledgeItem item) {
		Class<? extends TLObject> implClass = wrapperClass;
		try {
			return ConfigUtil.newInstance(implClass, CONSTRUCTOR_PARAMS, item);
		} catch (ConfigurationException ex) {
			throw new WrapperRuntimeException("Cannot instantiate wrapper class '" + implClass.getName() + "'.", ex);
		}
	}

}