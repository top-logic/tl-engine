/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.resolver;

import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.importer.XMLInstanceImporter;

/**
 * {@link InstanceResolver} that can resolve any persistent object based on its {@link ObjectKey}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentObjectResolver implements InstanceResolver {

	/**
	 * Resolver kind to register {@link PersistentObjectResolver}s for.
	 * 
	 * @see XMLInstanceImporter#addResolver(String, InstanceResolver)
	 */
	public static final String KIND = "key";

	private KnowledgeBase _kb;

	/**
	 * Creates a {@link PersistentObjectResolver}.
	 *
	 * @param kb
	 *        The {@link KnowledgeBase} to resolve instances in.
	 */
	public PersistentObjectResolver(KnowledgeBase kb) {
		_kb = kb;
	}

	@Override
	public TLObject resolve(String kind, String id) {
		ObjectKey key;
		try {
			key = ObjectKey.fromStringObjectKey(_kb.getMORepository(), id);
		} catch (UnknownTypeException ex) {
			throw new IllegalArgumentException("Invalid persistent object ID: " + id, ex);
		}
		KnowledgeItem item = _kb.resolveObjectKey(key);
		if (item == null) {
			throw new IllegalArgumentException("Cannot resolve persistent object with ID '" + id + "'.");
		}
		return item.getWrapper();
	}

	@Override
	public String buildId(TLObject obj) {
		return obj.tHandle().tId().toString();
	}

}
