/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.ObservableList;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;

/**
 * Default base implementation of {@link TLStructuredType}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTLStructuredType<P extends TLStructuredTypePart> extends AbstractTLType implements TLStructuredType {

	/* package protected */final Map<String, TLStructuredTypePart> localPartsByName =
		new LinkedHashMap<>();
	
	private final List<P> localParts = createPartList();

	AbstractTLStructuredType(TLModel model, String name) {
		super(model, name);
	}

	@Override
	public TLStructuredTypePart getPart(String name) {
		return this.localPartsByName.get(name);
	}
	
	@Override
	public final List<P> getLocalParts() {
		return localParts;
	}
	
	protected <T extends TLStructuredTypePart> List<T> createPartList() {
		return new PartList<>();
	}
	
	protected class PartList<T extends TLStructuredTypePart> extends ObservableList<T> {
		
		public PartList() {
			super();
		}
		
		@Override
		protected void beforeAdd(T element) {
			String elementName = element.getName();
			if (localPartsByName.containsKey(elementName)) {
				throw new IllegalArgumentException("Type '" + AbstractTLStructuredType.this + "' already contains a part with name '" + elementName + "'");
			}
			localPartsByName.put(elementName, element);
			
			initOwner(element);
		}

		@Override
		protected void afterRemove(T element) {
			TLStructuredTypePart removedPart = localPartsByName.remove(element.getName());
			assert removedPart != null : "Inconsistent part map in type '" + AbstractTLStructuredType.this + "': part '" + element.getName() + "' not found.";

			resetOwner(removedPart);
			assert removedPart == element : "Inconsistent part map in type '" + AbstractTLStructuredType.this + "': removed '" + removedPart + "', expected '" + element + "'.";
		}

		private void initOwner(TLTypePart element) {
			((AbstractTLTypePart) element).internalSetOwner(AbstractTLStructuredType.this);
		}
		
		private void resetOwner(TLTypePart element) {
			((AbstractTLTypePart) element).internalSetOwner(null);
		}

	}
	
}
