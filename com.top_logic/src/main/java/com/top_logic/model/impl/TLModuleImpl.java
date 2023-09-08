/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.IndexedCollection;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.ObservableMapProxy;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.util.TypeFilteredCollection;

/**
 * Default implementation of {@link TLModule}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLModuleImpl extends AbstractTLModelPart implements TLModule {

	private static final Mapping<TLType, Object> TYPE_NAME_MAPPING = new Mapping<>() {
		@Override
		public Object map(TLType type) {
			String typeName = type.getName();
			if (typeName == null) {
				return new NamedConstant("<anonymous>");
			}
			return typeName;
		}
	};

	/**
	 * @see #getName()
	 */
	private String name;
	
	/**
	 * Storage implementation of all types in this module.
	 */
	private final Map<Object, TLType> typeByName = new ObservableMapProxy<>() {

		private final Map<Object, TLType> _impl = new LinkedHashMap<>();

		@Override
		protected Map<Object, TLType> impl() {
			return _impl;
		}

		@Override
		protected void beforePut(Object key, TLType newValue) {
			if (newValue == null) {
				throw new IllegalArgumentException("Null may not be added.");
			} else {
				AbstractTLType newType = (AbstractTLType) newValue;
				String newTypeName = newType.getName();
				
				if (newTypeName != null && containsKey(newTypeName)) {
					throw new IllegalArgumentException(
						"Module '" + getName() + "' already contains a type named '" + newTypeName + "'.");
				}
				
				TLScope currentScope = newType.getScope();
				if (currentScope != null) {
					if (currentScope != TLModuleImpl.this) {
						throw new IllegalArgumentException(
							"Type '" + newType + "' may not be added to more than one module: Currently part of '" + currentScope + "', attempting to add type to '" + TLModuleImpl.this + "'.");
					} else {
						throw new IllegalArgumentException(
							"Type '" + newType + "' is alread part of this module '" + currentScope + "'.");
					}
				}
			}
		}
		
		@Override
		protected void afterPut(Object key, TLType newValue) {
			if (newValue != null) {
				AbstractTLType newType = (AbstractTLType) newValue;
				
				newType.internalSetScope(TLModuleImpl.this);
				newType.setModule(TLModuleImpl.this);
			}
		}

		@Override
		protected void afterRemove(Object key, TLType oldValue) {
			if (oldValue != null) {
				AbstractTLType newType = (AbstractTLType) oldValue;
				
				newType.internalSetScope(null);
				newType.setModule(null);
			}
		}
		
	};
	
	/**
	 * Mutable view of all values of {@link #typeByName}.
	 */
	private final Collection<TLType> types = new IndexedCollection<>(typeByName, TYPE_NAME_MAPPING);
	
	/**
	 * Mutable view of all {@link TLAssociation} values of {@link #typeByName}.
	 */
	private final Collection<TLAssociation> associations = new TypeFilteredCollection<>(types, TLAssociation.class);

	/**
	 * Mutable view of all {@link TLEnumeration} values of {@link #typeByName}.
	 */
	private final Collection<TLEnumeration> enumerations = new TypeFilteredCollection<>(types, TLEnumeration.class);
	
	/**
	 * Mutable view of all {@link TLPrimitive} values of {@link #typeByName}.
	 */
	private final Collection<TLPrimitive> datatypes = new TypeFilteredCollection<>(types, TLPrimitive.class);

	/**
	 * Mutable view of all {@link TLClass} values of {@link #typeByName}.
	 */
	private final Collection<TLClass> classes = new TypeFilteredCollection<>(types, TLClass.class);

	/*package protected*/ TLModuleImpl(TLModelImpl model, String name) {
		super(model);
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String value) {
		this.name = value;
	}
	
	@Override
	public Collection<TLAssociation> getAssociations() {
		return this.associations;
	}
	
	@Override
	public Collection<TLEnumeration> getEnumerations() {
		return this.enumerations;
	}

	@Override
	public Collection<TLPrimitive> getDatatypes() {
		return this.datatypes;
	}

	@Override
	public Collection<TLClass> getClasses() {
		return this.classes;
	}

	@Override
	public Collection<TLType> getTypes() {
		return types;
	}
	
	@Override
	public TLType getType(String name) {
		return this.typeByName.get(name);
	}

}
