/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.boundsec.manager.ElementAccessHelper;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.model.ModelService;

/**
 * Provides the available classifiers for meta attributes
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@ServiceDependencies(ModelService.Module.class)
public class AttributeClassifierManager extends ManagedClass {
    
	public static final String KA_CLASSIFIED_BY = ApplicationObjectUtil.KA_CLASSIFIED_BY;
    
	/**
	 * Configuration of the {@link AttributeClassifierManager}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<AttributeClassifierManager> {

		/** Configuration name for the value of {@link #getGlobalClassifications()}. */
		String GLOBAL_CLASSIFICATIONS = "global-classifications";

		/** Configuration name for the value of {@link #getTypeClassifications()}. */
		String TYPE_CLASSIFICATIONS = "type-classifications";

		/**
		 * Configuration of the global classifications.
		 */
		@Name(GLOBAL_CLASSIFICATIONS)
		@Format(CommaSeparatedStrings.class)
		List<String> getGlobalClassifications();

		/**
		 * Setter for {@link #getGlobalClassifications()}.
		 */
		void setGlobalClassifications(List<String> classifications);

		/**
		 * Type local classifications.
		 */
		@Name(TYPE_CLASSIFICATIONS)
		@Key(TypeClassifications.TYPE)
		Map<String, TypeClassifications> getTypeClassifications();
	}

	/**
	 * Configuration of the type local classifications.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface TypeClassifications extends ConfigurationItem {

		/** Configuration name for the value of {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for the value of {@link #getClassifications()}. */
		String CLASSIFICATIONS = "classifications";

		/**
		 * Qualified name of the type.
		 */
		@Name(TYPE)
		String getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(String type);

		/**
		 * Configuration of the classifications.
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(CLASSIFICATIONS)
		List<String> getClassifications();

		/**
		 * Setter for {@link #getClassifications()}.
		 */
		void setClassifications(List<String> classifications);
	}

	private final Map<ObjectKey, List<FastList>> _classifiers = new HashMap<>();

	private final List<FastList> _globalClassifiers = new ArrayList<>();
    
	/**
	 * Creates a new {@link AttributeClassifierManager} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AttributeClassifierManager}.
	 */
	public AttributeClassifierManager(InstantiationContext context, Config config) {
		Map<String, TLClass> uniqueMetaElements = ElementAccessHelper.getUniqueMetaElements();
		addClassifications(context, _globalClassifiers, config.getGlobalClassifications());
		for (TypeClassifications typeConfiguration : config.getTypeClassifications().values()) {
			String typeName = typeConfiguration.getType();
			TLClass type = uniqueMetaElements.get(typeName);
			if(type == null) {
				context.error("Unsupported type definition: " + typeName);
				continue;
			}
			ObjectKey typeKey = type.tId();
			List<String> classificationNames = typeConfiguration.getClassifications();
			List<FastList> classifications = _classifiers.get(typeKey);
			if (classifications == null) {
				classifications = new ArrayList<>(classificationNames.size());
				_classifiers.put(typeKey, classifications);
			}
			addClassifications(context, classifications, classificationNames);
		}
	}

	private void addClassifications(InstantiationContext context, List<FastList> classifications,
			List<String> classificationNames) {
		for (String classificationName : classificationNames) {
			addClassification(context, classifications, classificationName);
		}
	}

	private void addClassification(InstantiationContext context, List<FastList> classifications,
			String classificationName) {
		FastList classifier = FastList.getFastList(classificationName);
		if (classifier == null) {
			context.error("Configured attribute classification list '" + classificationName + "' not found.");
			return;
		}
		classifications.add(classifier);
	}

	/**
	 * Singleton instance of {@link AttributeClassifierManager}.
	 */
    public static AttributeClassifierManager getInstance() {
		return Module.INSTANCE.getImplementationInstance();
    }
    
    /**
	 * A list of classification options. Each list element contains a list of classifications. The
	 * available classifications for a meta attribute depend on the meta element the attribute is
	 * defined in. The Interpretaion is as follows: - The Top lovel lists are non exclusive
	 * (multiple of these lists can provide classifications) - The elements of the sub lists are
	 * exclusive (at most one such element is to be selected)
	 * 
	 * @param type
	 *        the meta element holding the meta attribute to be classified
	 */
	public List<FastList> getAvailableClassifiers(TLClass type) {
		List<FastList> localClassifications = findLocalClassifications(type);
		if (localClassifications.isEmpty()) {
			return _globalClassifiers;
        } else {
			List<FastList> result = new ArrayList<>();
			result.addAll(_globalClassifiers);
			result.addAll(localClassifications);
			return result;
        }
    }
    
    /**
	 * Get the classifiers declared for the given object
	 * 
	 * @param type
	 *        the meta element the classifiers are declared for
	 */
	public List<FastList> getDeclaredClassifiers(TLType type) {
		if (type == null) {
			return _globalClassifiers;
        } else {
			return findLocalClassifications(type);
        }
    }

	private List<FastList> findLocalClassifications(TLType type) {
		return CollectionUtil.nonNull(_classifiers.get(type.tId()));
	}
    
    /**
     * Get all classifiers declared on any meta element or global
     */
	public Set<FastList> getAllClassifiers() {
		HashSet<FastList> result = new HashSet<>();
		result.addAll(_globalClassifiers);
		for (List<FastList> classifications : _classifiers.values()) {
			result.addAll(classifications);
		}
		return result;
    }
    
    /**
     * This method recalculates the needs role properties of all meta attributes
     * based on the needs role properties of the classifications set at the meta attributes.
     */
	public void resetMetaAttributeClassificationAccess() {
		Collection<KnowledgeObject> attributeKOs =
			PersistencyLayer.getKnowledgeBase().getAllKnowledgeObjects(KBBasedMetaAttribute.OBJECT_NAME);
		for (KnowledgeObject ko : attributeKOs) {
			KBBasedMetaAttribute attribute = (KBBasedMetaAttribute) ko.getWrapper();
			ElementAccessHelper.clearAccessRights(attribute);
			Map<String, Set<BoundedRole>> attributeRolesMap = Collections.emptyMap();
            boolean isFirst = true;
			for (Object classifier : AttributeOperations.getClassifiers(attribute)) {
				Map<String, Set<BoundedRole>> rolesMap =
					ElementAccessHelper.getRolesMap((FastListElement) classifier);
                if (isFirst) {
					attributeRolesMap = rolesMap;
                    isFirst = false;
                } else {
					mergeRolesMap(attributeRolesMap, rolesMap);
                }
            }
			for (Entry<String, Set<BoundedRole>> attributeRoles : attributeRolesMap.entrySet()) {
				String right = attributeRoles.getKey();
				Collection<BoundedRole> roles = attributeRoles.getValue();
				for (BoundedRole role : roles) {
					ElementAccessHelper.addAccessRight(attribute, right, role);
                }
            }
        }
    }
    
	private <K, V extends Collection<?>> void mergeRolesMap(Map<K, V> aMap, Map<K, V> someStuffToMerge) {
		for (Iterator<Entry<K, V>> iterator = aMap.entrySet().iterator(); iterator.hasNext();) {
			Entry<K, V> entry = iterator.next();
			K key = entry.getKey();
			V value = entry.getValue();
            
			if (someStuffToMerge.containsKey(key)) {
				value.retainAll(someStuffToMerge.get(key));
            } else {
				iterator.remove();
            }
		}
    }

	/**
	 * Module for the {@link AttributeClassifierManager} service.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<AttributeClassifierManager> {

		/** Singleton {@link AttributeClassifierManager.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<AttributeClassifierManager> getImplementation() {
			return AttributeClassifierManager.class;
		}

	}

}
