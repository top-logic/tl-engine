/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * {@link RewritingEventVisitor} changing the name of an {@link MetaObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeRenaming extends ConfiguredRewritingEventVisitor<TypeRenaming.Config> {

	/**
	 * Configuration of the {@link TypeRenaming}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("type-renaming")
	public interface Config extends ConfiguredRewritingEventVisitor.Config<TypeRenaming>, TypesConfig {

		/** Configuration name of {@link #getTargetType()} */
		String TARGET_TYPE_NAME = "target-type";

		/**
		 * The names of all types to rename.
		 */
		@Override
		Set<String> getTypeNames();

		/**
		 * The type name to rename elements to.
		 */
		@Name(TARGET_TYPE_NAME)
		String getTargetType();
	}

	private MetaObject _targetType;

	/**
	 * Creates a {@link AttributeRenaming} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TypeRenaming(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Set<String> typeNames() {
		return getConfig().getTypeNames();
	}

	/**
	 * Sets the {@link MORepository} to resolve the target type.
	 */
	@Inject
	public void initMORepository(MORepository typeSystem) throws ConfigurationException {
		String targetTypeName = getConfig().getTargetType();
		try {
			_targetType = typeSystem.getMetaObject(targetTypeName);
		} catch (UnknownTypeException ex) {
			throw new ConfigurationException(ResKey.text("Non existing target type: " + targetTypeName),
				Config.TARGET_TYPE_NAME, targetTypeName);
		}
	}

	@Override
	protected Object visitItemEvent(ItemEvent event, Void arg) {
		ObjectBranchId id = event.getObjectId();
		ObjectBranchId newId = updateType(id);
		if (newId != id) {
			event.setObjectId(newId);
		}
		return super.visitItemEvent(event, arg);
	}

	private ObjectBranchId updateType(ObjectBranchId id) {
		ObjectBranchId newId;
		if (typeNames().contains(id.getObjectType().getName())) {
			newId = new ObjectBranchId(id.getBranchId(), _targetType, id.getObjectName());
		} else {
			newId = id;
		}
		return newId;
	}

	private ObjectKey updateType(ObjectKey id) {
		ObjectKey newId;
		if (typeNames().contains(id.getObjectType().getName())) {
			newId =
				new DefaultObjectKey(id.getBranchContext(), id.getHistoryContext(), _targetType, id.getObjectName());
		} else {
			newId = id;
		}
		return newId;
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		updateValues(event.getValues());
		return super.visitItemChange(event, arg);
	}

	private void updateValues(Map<String, Object> values) {
		for (Entry<String, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			Object newValue;
			if (value instanceof ObjectKey) {
				newValue = updateType((ObjectKey) value);
			} else if (value instanceof ObjectBranchId) {
				newValue = updateType((ObjectBranchId) value);
			} else {
				newValue = value;
			}
			if (newValue != value) {
				entry.setValue(newValue);
			}
		}
	}

	@Override
	public Object visitBranch(BranchEvent event, Void arg) {
		Set<String> branchedTypeNames = event.getBranchedTypeNames();
		boolean containsAny = branchedTypeNames.removeAll(typeNames());
		if (containsAny) {
			branchedTypeNames.add(_targetType.getName());
		}
		return super.visitBranch(event, arg);
	}
}

