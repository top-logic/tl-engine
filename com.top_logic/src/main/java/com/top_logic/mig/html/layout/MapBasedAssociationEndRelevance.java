/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.EnabledConfiguration;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder.LinkRelevanceConfig;

/**
 * {@link AssociationEndRelevance} based on a map implementation.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MapBasedAssociationEndRelevance extends HashMap<MetaObject, Map<MOReference, Boolean>> implements
		AssociationEndRelevance {

	@Override
	public Boolean isRelevant(MetaObject type, MOReference reference) {
		Map<MOReference, Boolean> relevanceByAttribute = get(type);
		if (relevanceByAttribute == null) {
			return null;
		}
		return relevanceByAttribute.get(reference);
	}

	@Override
	public Map<MOReference, Boolean> relevanceByReference(MetaObject type) {
		return get(type);
	}
	
	/**
	 * Creates a new {@link AssociationEndRelevance} from the given configuration.
	 * 
	 * @param log
	 *        {@link Protocol} to write configuration errors to.
	 * @param relevanceConfigurations
	 *        Configuration to create {@link AssociationEndRelevance} from.
	 * @param typeSystem
	 *        Context to resolve types from.
	 * 
	 * @return An {@link AssociationEndRelevance} represented by the configuration.
	 */
	public static AssociationEndRelevance newAssociationEndRelevance(Protocol log,
			Map<String, LinkRelevanceConfig> relevanceConfigurations, MORepository typeSystem) {
		MapBasedAssociationEndRelevance endRelevance = new MapBasedAssociationEndRelevance();

		Map<MetaObject, Map<String, Boolean>> configuredValues =
			resolveConfiguration(log, relevanceConfigurations, typeSystem);
		Map<MOClass, Set<MOClass>> subtypesByType = subTypesByType(typeSystem);
		MOClass rootType;
		try {
			rootType = (MOClass) typeSystem.getType(BasicTypes.ITEM_TYPE_NAME);
		} catch (UnknownTypeException ex) {
			throw new IllegalArgumentException("Typesystem '" + typeSystem + "' has no root type '"
				+ BasicTypes.ITEM_TYPE_NAME + "'.");
		}
		try {
			process(rootType, subtypesByType, configuredValues, endRelevance);
		} catch (NoSuchAttributeException ex) {
			throw new IllegalArgumentException(ex);
		}
		return endRelevance;
	}

	private static void process(MOClass type, Map<MOClass, Set<MOClass>> subtypesByType,
			Map<MetaObject, Map<String, Boolean>> configuredRelevance,
			MapBasedAssociationEndRelevance associationEndRelevance) throws NoSuchAttributeException {
		addRelevanceForType(type, configuredRelevance, associationEndRelevance);

		Set<MOClass> subTypes = subtypesByType.get(type);
		if (subTypes != null) {
			for (MOClass subType: subTypes) {
				process(subType, subtypesByType, configuredRelevance, associationEndRelevance);
			}
		}
	}

	private static void addRelevanceForType(MOClass type, Map<MetaObject, Map<String, Boolean>> configuredRelevance,
			MapBasedAssociationEndRelevance associationEndRelevance) throws NoSuchAttributeException {
		Map<MOReference, Boolean> result = null;
		result = addInheritedRelevance(type, result, associationEndRelevance);
		result = addConfiguredRelevance(type, result, configuredRelevance);
		if (result != null) {
			associationEndRelevance.put(type, result);
		}
	}

	private static Map<MOReference, Boolean> addConfiguredRelevance(MOClass type, Map<MOReference, Boolean> result,
			Map<MetaObject, Map<String, Boolean>> configuredRelevance) throws NoSuchAttributeException {
		Map<String, Boolean> map = configuredRelevance.get(type);
		if (map != null) {
			if (result == null) {
				result = new HashMap<>();
			}
			for (Map.Entry<String, Boolean> entry : map.entrySet()) {
				result.put(MetaObjectUtils.getReference(type, entry.getKey()), entry.getValue());
			}
		}
		return result;
	}

	private static Map<MOReference, Boolean> addInheritedRelevance(MOClass type, Map<MOReference, Boolean> result,
			MapBasedAssociationEndRelevance associationEndRelevance) throws NoSuchAttributeException {
		MOClass parent = type.getSuperclass();
		if (parent != null) {
			Map<MOReference, Boolean> inherited = associationEndRelevance.get(parent);
			if (inherited != null) {
				if (result == null) {
					result = new HashMap<>();
				}
				for (Map.Entry<MOReference, Boolean> entry : inherited.entrySet()) {
					result.put(MetaObjectUtils.getReference(type, entry.getKey().getName()), entry.getValue());
				}
			}
		}
		return result;
	}

	private static Map<MOClass, Set<MOClass>> subTypesByType(MORepository typeSystem) {
		Map<MOClass, Set<MOClass>> result = new HashMap<>();
		for (MetaObject mo : typeSystem.getMetaObjects()) {
			if (!(mo instanceof MOClass)) {
				continue;
			}
			MOClass type = (MOClass) mo;
			while (true) {
				MOClass parent = type.getSuperclass();
				if (parent == null) {
					break;
				}
				boolean added = MultiMaps.add(result, parent, type);
				if (!added) {
					// value already added. Therefore whole parant hierarchy
					break;
				}
				type = parent;
			}

		}
		return result;
	}

	private static Map<MetaObject, Map<String, Boolean>> resolveConfiguration(
			Protocol log, Map<String, LinkRelevanceConfig> relevanceConfigurations, MORepository typeSystem) {
		HashMap<MetaObject, Map<String, Boolean>> result = new HashMap<>();
		for (java.util.Map.Entry<String, LinkRelevanceConfig> configurationEntry : relevanceConfigurations.entrySet()) {
			MetaObject type;
			try {
				type = typeSystem.getMetaObject(configurationEntry.getKey());
			} catch (UnknownTypeException ex) {
				log.error("No such type: '" + configurationEntry.getKey() + "'", ex);
				continue;
			}
			Map<String, EnabledConfiguration> enabledConfigByAttr = configurationEntry.getValue().getAttributes();
			if (enabledConfigByAttr.isEmpty()) {
				continue;
			}
			Map<String, Boolean> enabledByAttr = new HashMap<>(enabledConfigByAttr.size());
			for (java.util.Map.Entry<String, EnabledConfiguration> enabledEntry : enabledConfigByAttr.entrySet()) {
				String attrName = enabledEntry.getKey();
				MOAttribute attribute;
				try {
					attribute = MetaObjectUtils.getAttribute(type, attrName);
				} catch (NoSuchAttributeException ex) {
					log.error("Type '" + type + "' has no attribute '" + attrName + "'.");
					continue;
				}
				if (!(attribute instanceof MOReference)) {
					log.error("Attribute '" + attribute + "' of type '" + type + "' is not a reference.");
					continue;
				}
				enabledByAttr.put(attrName, enabledEntry.getValue().isEnabled());
			}
			result.put(type, enabledByAttr);
		}
		return result;
	}

}

