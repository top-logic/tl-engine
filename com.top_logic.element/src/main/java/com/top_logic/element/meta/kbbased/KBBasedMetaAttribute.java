/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.attribute.AttributeClassifierManager;
import com.top_logic.element.boundsec.manager.ElementAccessHelper;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.layout.security.AccessChecker;
import com.top_logic.layout.security.LiberalAccessChecker;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.IGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContext;

/**
 * Persistent {@link TLStructuredTypePart} implementations.
 *
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public abstract class KBBasedMetaAttribute extends PersistentStructuredTypePart implements TLStructuredTypePart, AccessChecker {

	/**
	 * Configuration of application wide settings of {@link KBBasedMetaAttribute}s.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/** Name of the property {@link GlobalConfig#isUseClassification()}. */
		String USE_CLASSIFIACTION_NAME = "use-classification";

		/**
		 * Whether classification is actually used. This is a optimisation for applications without
		 * attribute security.
		 */
		@BooleanDefault(true)
		@Name(USE_CLASSIFIACTION_NAME)
		boolean isUseClassification();

	}

	/** Attribute names of attribute. */
	public static final String NAME = AttributeConfig.NAME;

	/**
	 * Reference to the {@link TLClass} owner of the reference aspect of this attribute.
	 */
	public static final String OWNER_REF = ApplicationObjectUtil.META_ELEMENT_ATTR;

	/**
	 * The attribute storing the sort order of attributes with the same {@link #OWNER_REF}
	 * reference.
	 */
	public static final String OWNER_REF_ORDER_ATTR = ApplicationObjectUtil.OWNER_REF_ORDER_ATTR;

	/**
	 * Property deciding about the concrete meta type of the attribute.
	 * 
	 * @see TLStructuredTypeColumns#CLASS_PROPERTY_IMPL
	 * @see TLStructuredTypeColumns#REFERENCE_IMPL
	 * @see TLStructuredTypeColumns#ASSOCIATION_PROPERTY_IMPL
	 * @see TLStructuredTypeColumns#ASSOCIATION_END_IMPL
	 */
	public static final String IMPLEMENTATION_NAME = ApplicationObjectUtil.IMPLEMENTATION_NAME;

	/**
	 * {@link MetaObject} name for {@link TLStructuredTypePart}s.
	 */
	public static final String OBJECT_NAME = ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE;

    public static final String DEFAULT_VALUE        ="defaultValue";
    
	public final static boolean USE_ATTRIBUTE_CLASSIFIERS;

	public final static AssociationSetQuery<KnowledgeAssociation> CLASSIFIER_QUERY;

    static {

        CLASSIFIER_QUERY = AssociationQuery.createOutgoingQuery("classifiers", AttributeClassifierManager.KA_CLASSIFIED_BY);

		boolean useClassifier;
        try {
			GlobalConfig globalConfiguration = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
			useClassifier = globalConfiguration.isUseClassification();
		} catch (Throwable ex) {
            useClassifier = true;
			Logger.error("Problem reading global configuration.", ex, KBBasedMetaAttribute.class);
        }
        USE_ATTRIBUTE_CLASSIFIERS = useClassifier;
    }

	public KBBasedMetaAttribute(KnowledgeObject ko) {
		super(ko);
	}
	
	@Override
	public AccessChecker getAccessChecker() {
		if (AttributeOperations.isClassified(this)) {
			return this;
		}
		return LiberalAccessChecker.INSTANCE;
	}
	
	@Override
	public final Set<BoundCommandGroup> getAccessRights(Object object, IGroup group) {
		if (!(object instanceof BoundObject)) {
			return LiberalAccessChecker.ALL_RIGHTS;
		}
		Collection<BoundRole> roles = getRoles((BoundObject) object);
		
		return AttributeOperations.getAccess(this, roles);
	}

	private Collection<BoundRole> getRoles(BoundObject boundObject) {
		AccessManager accessManager = AccessManager.getInstance();
		Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();
		return accessManager.getRoles(currentPerson, boundObject);
	}
	
	@Override
	public final boolean hasAccessRight(Object object, IGroup group, BoundCommandGroup accessRight) {
		if (!(object instanceof BoundObject)) {
			return true;
		}
		Collection<BoundRole> roles = getRoles((BoundObject) object);
		if (roles.isEmpty()) {
			return false;
		}
		Map<BoundedRole, Set<BoundCommandGroup>> accessMap = ElementAccessHelper.getAccessRights(this);
		for (BoundRole role: roles) {
			Set<BoundCommandGroup> rights = accessMap.get(role);
			if (rights.contains(accessRight)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the {@link AssociationQuery queries} that are used by instances of
	 * {@link PersistentClass} to cache relations to other model elements.
	 */
	@FrameworkInternal
	public static List<AbstractAssociationQuery<? extends TLObject, ?>> getAssociationQueries() {
		if (USE_ATTRIBUTE_CLASSIFIERS) {
			return Collections.<AbstractAssociationQuery<? extends TLObject, ?>> singletonList(CLASSIFIER_QUERY);
		} else {
			return Collections.emptyList();
		}
	}

}
