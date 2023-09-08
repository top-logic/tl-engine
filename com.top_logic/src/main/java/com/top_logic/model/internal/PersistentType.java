/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.internal;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;

/**
 * Base class for persistent {@link TLType} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PersistentType extends PersistentModelPart implements TLType {

	/**
	 * Direct reference to the defining module.
	 */
	public static final String MODULE_REF = ApplicationObjectUtil.META_ELEMENT_MODULE_REF;

	/**
	 * Direct reference to the surrounding scope.
	 */
	public static final String SCOPE_REF = ApplicationObjectUtil.META_ELEMENT_SCOPE_REF;

	/**
	 * Name of the abstract table type, all persistent {@link TLType} instances are stored in.
	 */
	public static final String TL_TYPE_KO = "TLType";

	/**
	 * Creates a {@link PersistentType}.
	 */
	@CalledByReflection
	public PersistentType(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public final String getName() {
		return tGetDataString(NAME_ATTR);
	}

	/**
	 * @see #getName()
	 */
	@Override
	public final void setName(String value) {
		tSetDataString(NAME_ATTR, value);
	}

	@Override
	public final TLModule getModule() {
		return tGetDataReference(TLModule.class, MODULE_REF);
	}

	@Override
	public final void setModule(TLModule module) {
		tSetDataReference(MODULE_REF, module);
	}

	@Override
	public TLScope getScope() {
		return tGetDataReference(TLScope.class, SCOPE_REF);
	}

	@Override
	public TLModel getModel() {
		return getModule().getModel();
	}

}
