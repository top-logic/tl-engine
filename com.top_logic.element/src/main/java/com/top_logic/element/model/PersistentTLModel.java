/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.util.Collection;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.MetaAttributeFactory;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLQuery;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.util.model.ModelService;

/**
 * Persistent implementation of {@link TLModel}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentTLModel extends DynamicModelPart implements TLModel {
	
	/** Name of the {@link MetaObject} holding {@link PersistentTLModel}. */
	public static final String OBJECT_TYPE = TlModelFactory.KO_NAME_TL_MODEL;
	
	private static final AssociationSetQuery<TLModule> MODULES = AssociationQuery.createQuery("modules", TLModule.class,
		ApplicationObjectUtil.MODULE_OBJECT_TYPE, ApplicationObjectUtil.MODULE_MODEL_ATTR);

	public PersistentTLModel(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public TLModel getModel() {
		return this;
	}

	@Override
	public Collection<TLModule> getModules() {
		return AbstractWrapper.resolveLinks(this, MODULES);
	}

	@Override
	public TLModule getModule(String name) {
		Collection<TLModule> allModules = getModules();
		for (TLModule module: allModules) {
			if (name.equals(module.getName())) {
				return module;
			}
		}
		return null;
	}

	@Override
	public <T extends TLQuery> T getQuery(Class<T> queryInterface) {
		return ModelService.getInstance().getQuery(queryInterface);
	}

	@Override
	public TLModule addModule(TLModel model, String moduleName) {
		TLModule module = model.getModule(moduleName);
		if (module != null) {
			throw new IllegalArgumentException("Model '" + model + "' already contains a module named '" + moduleName
				+ "'");
		}
		return PersistentModule.createModule(moduleName, model);
	}

	@Override
	public TLEnumeration addEnumeration(TLModule module, TLScope scope, String name) {
		return FastList.createEnum(tKnowledgeBase(), module, scope, name, null);
	}

	@Override
	public TLPrimitive addDatatype(TLModule module, TLScope scope, String name, Kind kind, StorageMapping<?> mapping) {
		TLType type = scope.getType(name);
		if (type != null) {
			throw failScopeContainsType(scope, type);
		}
		return PersistentDatatype.createDatatype(module, scope, name, kind, mapping);
	}

	@Override
	public TLClassifier createClassifier(String name) {
		return FastListElement.createFastListElement(null, name, null, 0);
	}

	@Override
	public TLClass addClass(TLModule module, TLScope scope, String name) {
		TLType type = scope.getType(name);
		if (type != null) {
			throw failScopeContainsType(scope, type);
		}
		TLClass newClass =
			MetaElementFactory.getInstance().createMetaElement((PersistentModule) module, (MetaElementHolder) scope,
				name, PersistencyLayer.getKnowledgeBase());
		return newClass;
	}

	@Override
	public TLAssociation addAssociation(TLModule module, TLScope scope, String name) {
		TLType type = scope.getType(name);
		if (type != null) {
			throw failScopeContainsType(scope, type);
		}
		TLClass newAssociation =
			MetaElementFactory.getInstance().createAssociationMetaElement(module, scope, name,
				PersistencyLayer.getKnowledgeBase());
		return (TLAssociation) newAssociation;
	}

	@Override
	public TLAssociationProperty addAssociationProperty(TLAssociation association, String name, TLType valueType) {
		TLStructuredTypePart part = association.getPart(name);
		if (part != null) {
			throw failTypeContainsPart(association, part);
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLAssociationProperty associationProperty = MetaAttributeFactory.getInstance().createAssocationProperty(kb);
		associationProperty.setName(name);
		associationProperty.setType(valueType);
		addStructuredTypePart(association, associationProperty);
		return associationProperty;
	}

	@Override
	public TLClassProperty addClassProperty(TLClass tlClass, String name, TLType valueType) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLClassProperty classProperty = MetaAttributeFactory.getInstance().createClassProperty(kb);
		classProperty.setName(name);
		classProperty.setType(valueType);
		addStructuredTypePart(tlClass, classProperty);
		return classProperty;
	}

	@Override
	public TLReference addReference(TLClass tlClass, String name, TLAssociationEnd end) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLReference reference = MetaAttributeFactory.getInstance().createTLReference(kb, end);
		reference.setName(name);
		addStructuredTypePart(tlClass, reference);
		return reference;
	}

	@Override
	public TLAssociationEnd addAssociationEnd(TLAssociation association, String name, TLType targetType) {
		TLStructuredTypePart part = association.getPart(name);
		if (part != null) {
			throw failTypeContainsPart(association, part);
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLAssociationEnd end = MetaAttributeFactory.getInstance().createEnd(kb);
		end.setName(name);
		end.setType(targetType);
		/* Ensure that a history type is always set, as most code expects it. */
		end.setHistoryType(HistoryType.CURRENT);
		addStructuredTypePart(association, end);
		return end;
	}

	private void addStructuredTypePart(TLStructuredType type, TLStructuredTypePart part) {
		try {
			MetaElementUtil.addMetaAttribute(((TLClass) type), part);
		} catch (DuplicateAttributeException ex) {
			throw new KnowledgeBaseRuntimeException("MA '" + part + "' exists whereas ME '" + type
				+ "' does not return part?", ex);
		} catch (AttributeException ex) {
			throw new KnowledgeBaseRuntimeException("Configuration invalid?", ex);
		}
	}
	
	/**
	 * Creates a new persistent {@link TLModel} instance.
	 */
	public static TLModel newInstance(KnowledgeBase kb)
			throws DataObjectException {
		return kb.createKnowledgeItem(OBJECT_TYPE, KnowledgeObject.class).getWrapper();
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
