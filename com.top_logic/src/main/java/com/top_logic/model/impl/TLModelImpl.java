/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Named;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.IndexedCollection;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLQuery;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.impl.util.TLCharacteristicsCopier;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.util.TLTypeUsage;
import com.top_logic.model.visit.DefaultDescendingTLModelVisitor;
import com.top_logic.util.error.TopLogicException;

/**
 * Default implementation of {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLModelImpl extends AbstractTLModelPart implements TLModel {

	private final Map<String, TLModule> modulesByName = new HashMap<>();

	private final Collection<TLModule> modules = new IndexedCollection<>(modulesByName, Named.NameMapping.INSTANCE);

	private Map<Class<? extends TLQuery>, TLQuery> _queries = new HashMap<>();

	/**
	 * Creates a {@link TLModelImpl}.
	 */
	public TLModelImpl() {
		super(null);
		_queries.put(TLModuleSingletons.class, new TLModuleSingletonsImpl());
		_queries.put(TLTypeUsage.class, new TLTypeUsageImpl());

	}

	/**
	 * Adds the built in primitives.
	 */
	public void addCoreModule() {
		TLModule core = TLModelUtil.addModule(this, TLCore.TL_CORE);
		for (Kind kind : Kind.values()) {
			if (kind != Kind.CUSTOM) {
				TLModelUtil.addDatatype(core, core, kind.getExternalName(), kind);
			}
		}
	}
	
	/**
	 * Resolves complex derived attributes of model parts.
	 * 
	 * @param log
	 *        The {@link Protocol} to report errors to.
	 */
	public void resolve(Protocol log) {
		visit(new DefaultDescendingTLModelVisitor<Protocol>() {
			@Override
			protected Void visitModelPart(TLModelPart model, Protocol arg) {
				((AbstractTLModelPart) model).localResolve(arg);
				return null;
			}
		}, log);
	}
	
	@Override
	public Collection<TLModule> getModules() {
		return this.modules;
	}
	
	@Override
	public TLModule getModule(String name) {
		return this.modulesByName.get(name);
	}
	
	@Override
	public <T extends TLQuery> T getQuery(Class<T> queryInterface) {
		@SuppressWarnings("unchecked")
		T result = (T) _queries.get(queryInterface);
		if (result == null) {
			throw new UnsupportedOperationException("Query interface '" + queryInterface.getName() + "' not supported.");
		}
		return result;
	}

	@Override
	public TLModel getModel() {
		return this;
	}

	@Override
	public TLAssociation addAssociation(TLModule module, TLScope scope, String name) {
		TLType type = scope.getType(name);
		if (type != null) {
			throw failScopeContainsType(scope, type);
		}
		TLAssociation newAssociation = new TLAssociationImpl((TLModelImpl) module.getModel(), name);
		scope.getAssociations().add(newAssociation);
		return newAssociation;
	}

	@Override
	public TLClass addClass(TLModule module, TLScope scope, String name) {
		TLType type = scope.getType(name);
		if (type != null) {
			throw failScopeContainsType(scope, type);
		}
		TLClass newClass = new TLClassImpl(module.getModel(), name);
		scope.getClasses().add(newClass);
		return newClass;
	}

	@Override
	public TLModule addModule(TLModel model, String moduleName) {
		if (model.getModule(moduleName) != null) {
			throw new IllegalArgumentException("Model '" + moduleName + "' already has a module named '" + moduleName
				+ "'.");
		}
		TLModule newModule = new TLModuleImpl((TLModelImpl) model, moduleName);
		model.getModules().add(newModule);
		return newModule;
	}

	@Override
	public TLAssociationProperty addAssociationProperty(TLAssociation association, String name, TLType valueType) {
		TLStructuredTypePart part = association.getPart(name);
		if (part != null) {
			throw failTypeContainsPart(association, part);
		}
		TLAssociationProperty newProperty = new TLAssociationPropertyImpl((TLModelImpl) association.getModel(), name);
		newProperty.setType(valueType);
		association.getAssociationParts().add(newProperty);
		return newProperty;
	}

	@Override
	public TLClassProperty addClassProperty(TLClass tlClass, String name, TLType valueType) {
		return TransientModelFactory.addClassProperty(tlClass, name, valueType);
	}

	@Override
	public TLReference addReference(TLClass tlClass, String name, TLAssociationEnd end) {
		TLClassPart part = (TLClassPart) tlClass.getPart(name);
		if (end != null) {
			/* TLModelImpl hierarchy is used in ModelReader which analyses a stream. Therefore it
			 * may be that the end is not present at reference creation time. The reference must
			 * later be filled with a correct end. */
			TLReference reference = end.getReference();
			if (reference != null) {
				throw new IllegalArgumentException("Association end '" + end + "' already implemented by '"
					+ reference.getOwner() + "' in attribute '" + reference.getName() + "'.");
			}
		}
		TLReference result = new TLReferenceImpl((TLModelImpl) tlClass.getModel(), name);
		result.setEnd(end);
		if (part != null) {
			if (part.getOwner() == tlClass) {
				throw new TopLogicException(
					com.top_logic.model.I18NConstants.DUPLICATE_ATTRIBUTE__NAME_CLASS.fill(name, tlClass));
			}
			TLCharacteristicsCopier.copyOverrideCharacteristics(part, result);
		}
		tlClass.getLocalClassParts().add(result);
		result.updateDefinition();
		return result;
	}

	@Override
	public TLAssociationEnd addAssociationEnd(TLAssociation association, String name, TLType targetType) {
		TLStructuredTypePart part = association.getPart(name);
		if (part != null) {
			throw failTypeContainsPart(association, part);
		}
		TLAssociationEnd end = new TLAssociationEndImpl((TLModelImpl) association.getModel(), name);
		end.setType(targetType);
		association.getAssociationParts().add(end);
		return end;
	}

	@Override
	public TLEnumeration addEnumeration(TLModule module, TLScope scope, String name) {
		TLType type = scope.getType(name);
		if (type != null) {
			throw failScopeContainsType(scope, type);
		}
		TLEnumerationImpl newEnum = new TLEnumerationImpl(module.getModel(), name);
		scope.getEnumerations().add(newEnum);
		return newEnum;
	}
	
	@Override
	public TLPrimitive addDatatype(TLModule module, TLScope scope, String name, Kind kind, StorageMapping<?> mapping) {
		TLType type = scope.getType(name);
		if (type != null) {
			throw failScopeContainsType(scope, type);
		}
		TLPrimitive result = new TLPrimitiveImpl((TLModelImpl) module.getModel(), name, kind, mapping);
		scope.getDatatypes().add(result);
		return result;
	}

	@Override
	public TLClassifier createClassifier(String name) {
		return new TLClassifierImpl(this, name);
	}

	private static IllegalArgumentException failScopeContainsType(TLScope scope, TLType type) {
		StringBuilder error = new StringBuilder();
		error.append("Scope '");
		error.append(scope);
		error.append("' already contains a type with name '");
		error.append(type.getName());
		error.append("': ");
		error.append(type);
		throw new IllegalArgumentException(error.toString());
	}

	private static IllegalArgumentException failTypeContainsPart(TLType type, TLTypePart part) {
		StringBuilder error = new StringBuilder();
		error.append("Type '");
		error.append(type);
		error.append("' already contains a part with name '");
		error.append(part.getName());
		error.append("': ");
		error.append(part);
		throw new IllegalArgumentException(error.toString());
	}

}
