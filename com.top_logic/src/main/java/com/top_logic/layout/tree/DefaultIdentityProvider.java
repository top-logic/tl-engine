/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.layout.IdentifierSource;
import com.top_logic.layout.IdentityProvider;
import com.top_logic.layout.basic.AbstractAttachable;

/**
 * The class {@link DefaultIdentityProvider} is an {@link IdentityProvider}
 * which can handle invalid ids and which is attachable
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultIdentityProvider extends AbstractAttachable implements IdentityProvider {

	private final BidiMap nodesById = new BidiHashMap();
	private final IdentifierSource idSource;
	private final Set<String> invalidIds = new HashSet<>();

	public DefaultIdentityProvider(IdentifierSource idSource) {
		this.idSource = idSource;
	}

	@Override
	protected void internalAttach() {
	}

	@Override
	protected void internalDetach() {
		invalidIds.clear();
		nodesById.clear();
	}

	@Override
	public String getObjectId(Object node) {
		checkAttached();
		String result = (String) nodesById.getKey(node);
		if (result == null) {
			result = idSource.createNewID();
			nodesById.put(result, node);
		}
		return result;
	}

	/**
	 * Invalidates the ID of the given node.
	 * 
	 * <p>
	 * The ID is valid until {@link #flush()} was called or the provider was {@link #detach()
	 * detached}.
	 * </p>
	 * 
	 * @return Whether the given node was already identified. <code>false</code>, if the given node
	 *         did not require invalidation, because no ID was assigned to it.
	 */
	public boolean invalidateId(Object node) {
		String id = (String) nodesById.getKey(node);
		if (id == null) {
			return false;
		}

		invalidIds.add(id);
		return true;
	}

	@Override
	public Object getObjectById(String id) {
		checkAttached();
		return nodesById.get(id);
	}

	public void flush() {
		checkAttached();
		nodesById.keySet().removeAll(invalidIds);
		invalidIds.clear();
	}

}
