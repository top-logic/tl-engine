/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.model.ColumnBaseConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;
import com.top_logic.layout.table.model.TableConfigUtil;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.annotate.ui.TLColumnInfo;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link ColumnInfoProvider} that is directly derived from a {@link TLColumnInfo} annotation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredColumnInfoProvider implements ColumnInfoProvider {

	private final ColumnConfigurator _configurator;

	/**
	 * Creates a {@link ConfiguredColumnInfoProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredColumnInfoProvider(InstantiationContext context, ColumnBaseConfig config) {
		_configurator = configurators(context, config);
	}

	private ColumnConfigurator configurators(InstantiationContext context, ColumnBaseConfig config) {
		return TableConfigUtil.plainColumnConfigurator(context, config);
	}

	@Override
	public ColumnInfo createColumnInfo(TLTypeContext contentType, ResKey headerI18NKey) {
		return new ConfiguredColumnInfo(contentType, headerI18NKey,
			GenericTableConfigurationProvider.getDefaultAccessor(), _configurator);
	}

	/**
	 * {@link ColumnInfo} that is directly derived from a configuration.
	 */
	private static final class ConfiguredColumnInfo extends ColumnInfo {
		private final ColumnConfigurator _configurator;
	
		/** 
		 * Creates a {@link ConfiguredColumnInfo}.
		 */
		ConfiguredColumnInfo(TLTypeContext contentType, ResKey headerI18NKey, Accessor accessor,
				ColumnConfigurator configurator) {
			super(contentType, headerI18NKey, null, accessor);
			_configurator = configurator;
		}
	
		@Override
		protected void internalAdapt(ColumnConfiguration column) {
			super.internalAdapt(column);

			_configurator.adapt(column);
		}
	
		@Override
		protected void setComparators(ColumnConfiguration column) {
			// Legacy.
		}
	
		@Override
		protected void setFilterProvider(ColumnConfiguration column) {
			// Legacy.
		}
	
		@Override
		protected void setCellExistenceTester(ColumnConfiguration column) {
			// Legacy.
		}
	
		@Override
		protected void setExcelRenderer(ColumnConfiguration column) {
			// Legacy.
		}
	
		@Override
		protected void setRenderer(ColumnConfiguration column) {
			// Legacy.
		}

		@Override
		protected void setPDFRenderer(ColumnConfiguration column) {
			// Legacy.
		}
	
		@Override
		protected ControlProvider getControlProvider() {
			// Legacy.
			return null;
		}
	
		@Override
		protected ColumnInfo internalJoin(ColumnInfo other) {
			// Cannot join, must be resolved by the user by given an explicit column configuration.
			return this;
		}
	}

}
