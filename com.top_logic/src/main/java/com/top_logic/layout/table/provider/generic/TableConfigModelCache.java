/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.KBCache;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.cache.AbstractTLModelCache;

/**
 * The actual {@link KBCache} of the {@link TableConfigModelService}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableConfigModelCache
		extends AbstractTLModelCache<TableConfigModelCacheEntry, TableConfigModelCache.Config<?>>
		implements TableConfigModelInfoProvider {

	/**
	 * {@link PolymorphicConfiguration} of the {@link TableConfigModelCache}.
	 */
	public interface Config<T extends TableConfigModelCache> extends AbstractTLModelCache.Config<T> {

		/** The name of the {@link #getModelInfoFactory()} property. */
		String MODEL_INFO_FACTORY = "model-info-factory";

		/**
		 * The {@link TableConfigModelInfoFactory} that should be used.
		 */
		@Name(MODEL_INFO_FACTORY)
		@NonNullable
		@InstanceFormat
		@InstanceDefault(TableConfigModelInfoFactoryImpl.class)
		TableConfigModelInfoFactory getModelInfoFactory();

		@Override
		@ClassDefault(TableConfigModelCache.class)
		Class<? extends T> getImplementationClass();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link TableConfigModelCache}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public TableConfigModelCache(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected boolean isRelevantChange(KnowledgeItem item) {
		return super.isRelevantChange(item) || item.tTable().getName().equals(ApplicationObjectUtil.KA_CLASSIFIED_BY);
	}

	@Override
	protected TableConfigModelCacheEntry newLocalCacheValue() {
		return new TableConfigModelCacheEntry(getConfig().getModelInfoFactory());
	}

	@Override
	public TableConfigModelInfo getModelInfo(Set<? extends TLClass> classes) {
		return getValue().getModelInfo(classes);
	}

}
