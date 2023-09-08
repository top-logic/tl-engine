/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.search.InstancesQueryBuilder;
import com.top_logic.knowledge.service.xml.annotation.FullLoadAnnotation;
import com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation;
import com.top_logic.knowledge.service.xml.annotation.KnowledgeItemFactoryAnnotation;
import com.top_logic.knowledge.service.xml.annotation.SystemAnnotation;

/**
 * BHU: this is
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
@FrameworkInternal
public class MOKnowledgeItemImpl extends MOClassImpl implements MOKnowledgeItem {

	private static final boolean SYSTEM_DEFAULT = false;

	private static final boolean FULL_LOAD_DEFAULT = false;

	private KnowledgeItemFactory factory;

	private DBAccess dbAccess;

	private boolean system = SYSTEM_DEFAULT;

	private boolean _fullLoad = FULL_LOAD_DEFAULT;

	private InstancesQueryBuilder _queryBuilder = null;

	public MOKnowledgeItemImpl(String name) {
		super(name);
	}
	
	@Override
	public InstancesQueryBuilder getInstancesQueryBuilder() {
		return _queryBuilder;
	}

	@Override
	public KnowledgeItemFactory getImplementationFactory() {
		return factory;
	}

	public DBAccess getDBAccess() {
		assert !isAbstract() : "Abstract type " + getName() + " has no DBAccess.";

		return dbAccess;
	}

	public void setDBAccess(DBAccess newDbAccess) {
		checkUpdate();

		assert ! isAbstract() : "Abstract types have no DBAccess.";

		this.dbAccess = newDbAccess;
	}

	@Override
	public boolean isSystem() {
		return system;
	}
	
	@Override
	public boolean getFullLoad() {
		return _fullLoad;
	}

	@Override
	protected void afterFreeze() {
		super.afterFreeze();
		
		MOKnowledgeItemImpl theSuperClass = (MOKnowledgeItemImpl) getSuperclass();
		if (theSuperClass != null) {
			if (this.getImplementationFactory() == null) {
				KnowledgeItemFactory factoryOfSuperClass = theSuperClass.getImplementationFactory();
				MOKnowledgeItemUtil.setImplementationFactory(this, factoryOfSuperClass);
			}
			if (getInstancesQueryBuilder() == null) {
				_queryBuilder = theSuperClass.getInstancesQueryBuilder();
			}
		}

		computeFullLoad();
	}
	
	private void computeFullLoad() {
		List<MOAttribute> attributes = getAttributes();

		// Override setting due to current search limitation (retrieving the result always
		// distinct).
		for (MOAttribute attribute : attributes) {
			MetaObject type = attribute.getMetaObject();
			if (type == MOPrimitive.CLOB) {
				// No distinct on result supported.
				MOKnowledgeItemUtil.setFullLoad(this, false);
				return;
			}
			if (type == MOPrimitive.BLOB) {
				// No distinct on result supported.
				MOKnowledgeItemUtil.setFullLoad(this, false);
				return;
			}
		}

		if (MOKnowledgeItemUtil.hasFullLoadAnnotation(this)) {
			return;
		}

		if (this.isSystem()) {
			MOKnowledgeItemUtil.setFullLoad(this, true);
		}

		MOClass superclass = getSuperclass();
		if (superclass != null) {
			boolean fullLoadSuperClass = ((MOKnowledgeItem) superclass).getFullLoad();
			MOKnowledgeItemUtil.setFullLoad(this, fullLoadSuperClass);
		}
	}

	@Override
	public MetaObject copy() {
		MOKnowledgeItemImpl copy = new MOKnowledgeItemImpl(getName());
		copy.initFromItemType(this);
		return copy;
	}

	protected final void initFromItemType(MOKnowledgeItemImpl orig) {
		super.initFromClass(orig);

		this.system = orig.system;
		this.factory = orig.factory;
		this._fullLoad = orig._fullLoad;
		this._queryBuilder = orig._queryBuilder;
	}

	@Override
	public <T extends MOAnnotation> void addAnnotation(T annotation) {
		super.addAnnotation(annotation);

		if (annotation.getConfigurationInterface() == KnowledgeItemFactoryAnnotation.class) {
			factory = ((KnowledgeItemFactoryAnnotation) annotation).getImplementationFactory();
		}
		else if (annotation.getConfigurationInterface() == SystemAnnotation.class) {
			system = true;
		}
		else if (annotation.getConfigurationInterface() == FullLoadAnnotation.class) {
			_fullLoad = ((FullLoadAnnotation) annotation).getFullLoad();
		}
		else if (annotation.getConfigurationInterface() == InstancesQueryAnnotation.class) {
			_queryBuilder = ((InstancesQueryAnnotation) annotation).getBuilder();
		}
	}

	@Override
	public <T extends MOAnnotation> T removeAnnotation(Class<T> annotationClass) {
		T result = super.removeAnnotation(annotationClass);

		if (annotationClass == KnowledgeItemFactoryAnnotation.class) {
			factory = null;
		}
		else if (annotationClass == SystemAnnotation.class) {
			system = SYSTEM_DEFAULT;
		}
		else if (annotationClass == FullLoadAnnotation.class) {
			_fullLoad = FULL_LOAD_DEFAULT;
		}
		else if (annotationClass == InstancesQueryAnnotation.class) {
			_queryBuilder = null;
		}

		return result;
	}

}
