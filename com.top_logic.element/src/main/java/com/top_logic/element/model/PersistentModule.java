/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.impl.util.TLPrimitiveColumns;
import com.top_logic.util.model.ModelService;

/**
 * Persistent variant of a {@link TLModule}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentModule extends PersistentScope implements TLModuleBase {

	/**
	 * The table to store {@link PersistentModule}s in.
	 */
	public static final String OBJECT_TYPE = ApplicationObjectUtil.MODULE_OBJECT_TYPE;

	/**
	 * The {@link #getName()} column.
	 */
	public static final String NAME_ATTR = ApplicationObjectUtil.MODULE_NAME_ATTR;

	/**
	 * The {@link #getModel()} reference.
	 */
	public static final String MODEL_ATTR = ApplicationObjectUtil.MODULE_MODEL_ATTR;

	private static final AssociationSetQuery<PersistentDatatype> DATATYPES =
		AssociationQuery.createQuery("datatypes", PersistentDatatype.class,
			TLPrimitiveColumns.OBJECT_TYPE, PersistentDatatype.MODULE_REF);

	/**
	 * Creates a {@link PersistentModule} for a given {@link KnowledgeObject}.
	 */
	public PersistentModule(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public TLPrimitive getDatatype(String name) {
		for (TLPrimitive datatype : AbstractWrapper.resolveLinks(this, DATATYPES)) {
			if (datatype.getName().equals(name)) {
				return datatype;
			}
		}
		return null;
	}

	@Override
	public TLModel getModel() {
		return tGetDataReference(TLModel.class, MODEL_ATTR);
	}

	/**
	 * Lookup or create a {@link TLModule} with the given name.
	 */
	public static PersistentModule mkModule(String moduleName) {
		PersistentModule existingModule = getModule(moduleName);
		if (existingModule != null) {
			return existingModule;
		}

		return createModule(moduleName, ModelService.getApplicationModel());
	}

	/**
	 * Create a {@link PersistentModule} with the given name.
	 */
	public static PersistentModule createModule(String moduleName, TLModel model) {
		try {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

			KeyValueBuffer<String, Object> initialValues = new NameValueBuffer()
				.put(NAME_ATTR, moduleName)
				.put(MODEL_ATTR, model.tHandle());
			PersistentModule newModule =
				(PersistentModule) kb.createKnowledgeObject(OBJECT_TYPE, initialValues).getWrapper();
			return newModule;
		} catch (DataObjectException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	/**
	 * Lookup {@link PersistentModule} with the given name.
	 * 
	 * @param moduleName
	 *        The module name to find.
	 * @return The resolved {@link PersistentModule} or <code>null</code> if no such module is
	 *         found.
	 */
	public static PersistentModule getModule(String moduleName) {
		return (PersistentModule) ModelService.getApplicationModel().getModule(moduleName);
	}

	@Override
	public String getName() {
		return tGetDataString(NAME_ATTR);
	}

	@Override
	public void setName(String value) {
		tSetDataString(NAME_ATTR, value);
	}

}
