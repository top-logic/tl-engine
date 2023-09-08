/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.kbbased.AbstractElementFactory;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.service.db2.migration.rewriters.Rewriter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.internal.PersistentType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Rewriter to resolve the concrete {@link TLObject#tType() object type} for a
 * {@link StructuredElement} from the 5.7. attribute values.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConcreteTypeComputation extends Rewriter {

	/**
	 * Configuration of a {@link Rewriter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends Rewriter.Config {

		/** Name of {@link #getTypeAttribute()}. */
		String TYPE_ATTRIBUTE = "type-attribute";

		/**
		 * Name of the attribute holding the type of an {@link TLObject}.
		 */
		@Name(TYPE_ATTRIBUTE)
		String getTypeAttribute();

	}

	/* Taken from com.top_logic.element.core.wrap.WrapperTLElement@237318 */
	/** Constant for the type of the structure the object lives in. */
	public static final String STRUCTURE_TYPE = "structureName";

	/** Constant for the name of the type the object lives in. */
	@SuppressWarnings("deprecation")
	public static final String ELEMENT_NAME_ATTRIBUTE = AbstractElementFactory.ELEMENT_NAME_ATTRIBUTE;

	private final Map<Key, ObjectKey> _concreteTypes = new HashMap<>();

	private final Map<String, Set<String>> _modulesByTable = new HashMap<>();

	private final Set<ObjectKey> _globalTypes = new HashSet<>();

	private final String _typeAttribute;

	/**
	 * Creates a new {@link ConcreteTypeComputation}.
	 */
	public ConcreteTypeComputation(InstantiationContext context, Config config) {
		super(context, config);

		_typeAttribute = config.getTypeAttribute();
	}

	/**
	 * Initialises this {@link ConcreteTypeComputation} with the given {@link ModelService}.
	 */
	@Inject
	public void initModelService(ModelService modelService) {
		TLModel applicationModel = modelService.getModel();
		Collection<TLClass> globalClasses = TLModelUtil.getAllGlobalClasses(applicationModel);
		for (TLClass type : globalClasses) {
			if (!TLModelUtil.IS_CONCRETE.accept(type)) {
				continue;
			}
			storeModuleByTable(type);
			_concreteTypes.put(new Key(type.getModule().getName(), type.getName()), type.tId());
		}
	}

	private void storeModuleByTable(TLClass type) {
		String table = TLAnnotations.getTable(type);
		Set<String> moduleNames = _modulesByTable.get(table);
		if (moduleNames == null) {
			moduleNames = new HashSet<>();
			_modulesByTable.put(table, moduleNames);
		}
		moduleNames.add(type.getModule().getName());
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		map(event.getValues(), event);
		return super.visitItemChange(event, arg);
	}

	@Override
	protected Object processUpdate(ItemUpdate event) {
		if (event.getOldValues() != null) {
			map(event.getOldValues(), event);
		}
		return super.processUpdate(event);
	}

	private void map(Map<String, Object> values, ItemEvent event) {
		if (!values.containsKey(_typeAttribute)) {
			return;
		}
		ObjectKey abstractType = (ObjectKey) values.get(_typeAttribute);
		if (abstractType == null) {
			// type was null
			return;
		}
		if (!isGlobal(abstractType, event)) {
			// Object has a local type
			return;
		}
		String moduleName = findModuleName(event, values);
		if (moduleName == null) {
			return;
		}
		String typeName = (String) values.remove(ELEMENT_NAME_ATTRIBUTE);
		ObjectKey correspondingConcreteType = _concreteTypes.get(new Key(moduleName, typeName));
		if (correspondingConcreteType != null) {
			values.put(_typeAttribute, correspondingConcreteType);
		} else {
			migration().error(
				"No concrete type found for module '" + moduleName + "' and type '" + typeName + "' in event " + event);
		}
	}

	private boolean isGlobal(ObjectKey type, ItemEvent event) {
		if (_globalTypes.contains(type)) {
			return true;
		}
		if (event instanceof ItemCreation) {
			Object scope = ((ItemCreation) event).getValues().get(PersistentType.SCOPE_REF);
			if (scope == null) {
				_globalTypes.add(event.getObjectId().toCurrentObjectKey());
				return true;
			}
		}
		return false;
	}

	private String findModuleName(ItemEvent event, Map<String, Object> values) {
		String moduleName = (String) values.get(STRUCTURE_TYPE);
		if (moduleName != null) {
			values.remove(STRUCTURE_TYPE);
			return moduleName;
		}
		if (values.containsKey(STRUCTURE_TYPE)) {
			migration().error("No module (attribute '" + STRUCTURE_TYPE + "') in event " + event);
			return null;
		}
		String tableName = event.getObjectType().getName();
		Set<String> modulesForTable = _modulesByTable.get(tableName);
		if (modulesForTable == null) {
			migration().error("No modules found for table '" + tableName + "' in event " + event);
			return null;
		}
		switch (modulesForTable.size()) {
			case 0:
				migration().error("No modules found for table '" + tableName + "' in event " + event);
				return null;
			case 1:
				return modulesForTable.iterator().next();
			default:
				migration().error(
					"Multiple modules found for table '" + tableName + "' in event " + event + ": " + modulesForTable);
				return null;
		}
	}

	private static class Key {

		final String _moduleName, _typeName;

		private final int _hash;

		public Key(String moduleName, String typeName) {
			super();
			_moduleName = moduleName;
			_typeName = typeName;
			_hash = hash();

		}

		@Override
		public int hashCode() {
			return _hash;
		}

		private int hash() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_moduleName == null) ? 0 : _moduleName.hashCode());
			result = prime * result + ((_typeName == null) ? 0 : _typeName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (_moduleName == null) {
				if (other._moduleName != null)
					return false;
			} else if (!_moduleName.equals(other._moduleName))
				return false;
			if (_typeName == null) {
				if (other._typeName != null)
					return false;
			} else if (!_typeName.equals(other._typeName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[" + _moduleName + "," + _typeName + "]";
		}

	}

}

