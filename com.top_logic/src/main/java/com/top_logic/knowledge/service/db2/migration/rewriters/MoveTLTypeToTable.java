/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.AbstractItemEventVisitor;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEventVisitor;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.ConfiguredRewritingEventVisitor;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.model.TLClass;

/**
 * {@link RewritingEventVisitor} moving instances of several {@link TLClass}es to a new table.
 */
public class MoveTLTypeToTable extends ConfiguredRewritingEventVisitor<MoveTLTypeToTable.Config> {

	/**
	 * Configuration of the {@link MoveTLTypeToTable} rewriter.
	 */
	@TagName("move-to-table")
	public interface Config extends ConfiguredRewritingEventVisitor.Config<MoveTLTypeToTable> {

		/** @see #getTargetTable() */
		String TARGET_TABLE = "target-table";

		/** @see #getValueRewriter() */
		String VALUE_REWRITER = "value-rewriter";

		/**
		 * Fully qualified names of all types whose instances should move to
		 * {@link #getTargetTable()}.
		 * 
		 * <p>
		 * Note: All types and their sub-types that should be moved must be enumerated here.
		 * Sub-types are not moved automatically.
		 * </p>
		 */
		@ListBinding(attribute = "name")
		List<String> getTypes();

		/**
		 * The name of the table to move instances to.
		 */
		@Name(TARGET_TABLE)
		String getTargetTable();

		/**
		 * Delegate visitor that receives all events of moved objects.
		 */
		@Name(VALUE_REWRITER)
		PolymorphicConfiguration<ItemEventVisitor<?, Void>> getValueRewriter();
	}

	private static final ItemEventVisitor<?, Void> NOOP = new AbstractItemEventVisitor<>() {
		/* Ignore */
	};

	private MetaObject _targetTable;

	private final Map<ObjectBranchId, ObjectBranchId> _idMapping = new HashMap<>();

	private Set<String> _typeNames;

	private ItemEventVisitor<?, Void> _valueRewriter;

	private MigrationModelIndex _modelIndex;

	/**
	 * Creates a {@link MoveTLTypeToTable} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MoveTLTypeToTable(InstantiationContext context, Config config) {
		super(context, config);

		_valueRewriter = context.getInstance(config.getValueRewriter());
		if (_valueRewriter == null) {
			_valueRewriter = NOOP;
		}
		_typeNames = new HashSet<>(config.getTypes());

		context.resolveReference(InstantiationContext.OUTER, MigrationModelIndex.class, x -> _modelIndex = x);
	}

	/**
	 * Sets the {@link MORepository} to resolve tables.
	 */
	@Inject
	public void initMORepository(MORepository typeSystem) {
		_targetTable = typeSystem.getMetaObject(getConfig().getTargetTable());
	}

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		String typeName = _modelIndex.getTypeOf(event.getObjectId());
		if (_typeNames.contains(typeName)) {
			ObjectBranchId renamtedId = event.getObjectId();
			ObjectBranchId newId =
				new ObjectBranchId(renamtedId.getBranchId(), _targetTable, renamtedId.getObjectName());
			event.setObjectId(newId);
			_idMapping.put(renamtedId, newId);
			_valueRewriter.visitCreateObject(event, arg);
		}

		rewriteIdentifiers(event.getValues());

		return super.visitCreateObject(event, arg);
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		ObjectBranchId id = event.getObjectId();
		ObjectBranchId newId = _idMapping.get(id);
		if (newId != null) {
			event.setObjectId(newId);
			_valueRewriter.visitUpdate(event, arg);
		}
		rewriteIdentifiers(event.getValues());
		rewriteIdentifiersIfPresent(event.getOldValues());

		return super.visitUpdate(event, arg);
	}

	@Override
	public Object visitDelete(ItemDeletion event, Void arg) {
		ObjectBranchId id = event.getObjectId();
		ObjectBranchId newId = _idMapping.get(id);
		if (newId != null) {
			event.setObjectId(newId);
			_valueRewriter.visitDelete(event, arg);
		}
		rewriteIdentifiers(event.getValues());

		_idMapping.remove(id);
		return super.visitDelete(event, arg);
	}

	private void rewriteIdentifiersIfPresent(Map<String, Object> values) {
		if (values == null) {
			return;
		}
		rewriteIdentifiers(values);
	}

	private void rewriteIdentifiers(Map<String, Object> values) {
		for (Entry<String, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			ObjectBranchId newValue = _idMapping.get(value);
			if (newValue != null) {
				entry.setValue(newValue);
			} else if (value instanceof ObjectKey) {
				ObjectKey key = (ObjectKey) value;
				ObjectBranchId id = ObjectBranchId.toObjectBranchId(key);
				ObjectBranchId newId = _idMapping.get(id);
				if (newId != null) {
					entry.setValue(updateId(key, newId));
				}
			}
		}
	}

	private DefaultObjectKey updateId(ObjectKey key, ObjectBranchId newId) {
		return new DefaultObjectKey(
			newId.getBranchId(),
			key.getHistoryContext(),
			newId.getObjectType(),
			newId.getObjectName());
	}
}

