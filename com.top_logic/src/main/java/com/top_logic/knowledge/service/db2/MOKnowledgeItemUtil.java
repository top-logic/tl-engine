/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.annotation.IndexColumnsStrategy;
import com.top_logic.dob.schema.config.annotation.IndexColumnsStrategyAnnotation;
import com.top_logic.knowledge.service.xml.annotation.DBAccessFactoryAnnotation;
import com.top_logic.knowledge.service.xml.annotation.FullLoadAnnotation;
import com.top_logic.knowledge.service.xml.annotation.KnowledgeItemFactoryAnnotation;
import com.top_logic.knowledge.service.xml.annotation.SystemAnnotation;

/**
 * Utilities for updating {@link MOKnowledgeItem}-specific annotations to {@link MOClass} instances.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MOKnowledgeItemUtil {

	private static final MOAnnotation SYSTEM = TypedConfiguration.newConfigItem(SystemAnnotation.class);

	private static final MOAnnotation FULL_LOAD;

	private static final MOAnnotation ID_LOAD;

	static {
		FullLoadAnnotation fullLoad = TypedConfiguration.newConfigItem(FullLoadAnnotation.class);
		fullLoad.update(fullLoad.descriptor().getProperty(FullLoadAnnotation.FULL_LOAD), Boolean.TRUE);
		FULL_LOAD = fullLoad;

		FullLoadAnnotation idLoad = TypedConfiguration.newConfigItem(FullLoadAnnotation.class);
		idLoad.update(idLoad.descriptor().getProperty(FullLoadAnnotation.FULL_LOAD), Boolean.FALSE);
		ID_LOAD = idLoad;
	}

	/**
	 * Updates the {@link SystemAnnotation}.
	 */
	public static void setSystem(MOClass self, boolean system) {
		if (system) {
			self.addAnnotation(SYSTEM);
		} else {
			self.removeAnnotation(SystemAnnotation.class);
		}
	}

	/**
	 * Updates the {@link FullLoadAnnotation}.
	 */
	public static void setFullLoad(MOClass self, boolean value) {
		if (value) {
			self.addAnnotation(FULL_LOAD);
		} else {
			self.addAnnotation(ID_LOAD);
		}
	}

	/**
	 * Checks for the {@link FullLoadAnnotation} being set.
	 */
	public static boolean hasFullLoadAnnotation(MOClass self) {
		return self.getAnnotation(FullLoadAnnotation.class) != null;
	}

	/**
	 * Updates the {@link KnowledgeItemFactoryAnnotation}.
	 */
	public static void setImplementationFactory(MOClass self, KnowledgeItemFactory factory) {
		if (factory == null) {
			self.removeAnnotation(KnowledgeItemFactoryAnnotation.class);
		} else {
			KnowledgeItemFactoryAnnotation annotation =
				TypedConfiguration.newConfigItem(KnowledgeItemFactoryAnnotation.class);
			annotation.setImplementationFactory(factory);
			self.addAnnotation(annotation);
		}
	}

	/**
	 * Retrieves the {@link DBAccessFactoryAnnotation}.
	 */
	public static DBAccessFactory getDBAccessFactory(MOClass self) {
		DBAccessFactoryAnnotation annotation = self.getAnnotation(DBAccessFactoryAnnotation.class);
		if (annotation == null) {
			return DefaultDBAccessFactory.INSTANCE;
		}
		return annotation.getDBAccessFactory();
	}

	/**
	 * Updates the {@link DBAccessFactoryAnnotation}.
	 */
	public static void setDBAccessFactory(MOClass self, DBAccessFactory factory) {
		if (factory == null) {
			self.removeAnnotation(DBAccessFactoryAnnotation.class);
		} else {
			DBAccessFactoryAnnotation annotation = TypedConfiguration.newConfigItem(DBAccessFactoryAnnotation.class);
			annotation.setDBAccessFactory(factory);
			self.addAnnotation(annotation);
		}
	}

	/**
	 * Updates the {@link IndexColumnsStrategyAnnotation}.
	 */
	public static void setIndexStrategy(MOClass self, IndexColumnsStrategy strategy) {
		if (strategy == null) {
			self.removeAnnotation(IndexColumnsStrategyAnnotation.class);
		} else {
			IndexColumnsStrategyAnnotation annotation =
				TypedConfiguration.newConfigItem(IndexColumnsStrategyAnnotation.class);
			annotation.setStrategy(strategy);
			self.addAnnotation(annotation);
		}
	}

}
