/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.util;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.config.annotation.TLVisibleColumns;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.model.AllVisibleColumnsProvider;
import com.top_logic.layout.table.model.IDColumnConfig;
import com.top_logic.layout.table.model.IDColumnTableConfigurationProvider;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.SortColumnsConfig;
import com.top_logic.layout.table.model.SortColumnsTableConfigurationProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.annotate.ui.TLIDColumn;
import com.top_logic.model.annotate.ui.TLSortColumns;
import com.top_logic.model.config.annotation.MainProperties;

/**
 * The {@link FormUtil} contains usefual static methods for forms.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class FormUtil {

	private FormUtil() {
		// Use the static methods
	}
	
	/**
	 * This method returns the {@link FormField} for the given attributed and
	 * meta attribute name if a {@link FormField} exists.
	 * 
	 * @param formContext
	 *            A {@link FormContext} must NOT be <code>null</code>.
	 * @param attributed
	 *            An attributed must NOT be <code>null</code>.
	 * @param metaAttributeName
	 *            A meta attribute name for the attributed must NOT be
	 *            <code>null</code>.
	 * @return Returns the {@link FormField} for the given attributed and meta
	 *         attribute name.
	 */
	public static FormField getFieldFor(FormContext formContext, Wrapper attributed, String metaAttributeName) {
		try {
			String fieldName = MetaAttributeGUIHelper.getAttributeID(metaAttributeName, attributed);
			
			return (FormField) formContext.getFirstMemberRecursively(fieldName);
		} catch (Exception e) {
			Logger.error("Could not get the form field for the meta attribute name ('" + metaAttributeName + "') for the attributed ('" + ((Wrapper) attributed).getName() + "'). Used null instead!", e, FormUtil.class);
			return null;
		}
	}
	
	/**
	 * This method returns the value of the {@link FormField} for the given
	 * attributed and meta attribute name if a {@link FormField} exists. If 
	 * no field exists <code>null</code> is returned.
	 * 
	 * @param formContext
	 *            A {@link FormContext} must NOT be <code>null</code>.
	 * @param attributed
	 *            An attributed must NOT be <code>null</code>.
	 * @param metaAttributeName
	 *            A meta attribute name for the attributed must NOT be
	 *            <code>null</code>.
	 * @return Returns the value of the {@link FormField} for the given
	 *         attributed and meta attribute name.
	 */
	public static Object getFieldValueFor(FormContext formContext, Wrapper attributed, String metaAttributeName) {
		FormField field = getFieldFor(formContext, attributed, metaAttributeName);
		
		return field != null ? field.getValue() : null;
	}
	
	/**
	 * Adds all tables providers resulting from the annotations of the given {@link EditContext} to
	 * the list of providers.
	 * 
	 * @param providers
	 *        List of providers that may be expanded.
	 * @param context
	 *        Edit operation for some object.
	 */
	public static void addProvidersFromAnnotations(List<TableConfigurationProvider> providers, EditContext context) {
		addVisibleColumnsTableProvider(providers, context);
		providers.add(new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				MainProperties mainProperties = context.getAnnotation(MainProperties.class);

				if (mainProperties != null) {
					table.setDefaultColumns(mainProperties.getProperties());
				}
			}
		});
		providers.add(GenericTableConfigurationProvider.showDefaultColumns());
		addSortColumnsTableProvider(providers, context);
		addIDColumnTableProvider(providers, context);
	}

	private static void addVisibleColumnsTableProvider(List<TableConfigurationProvider> providers, EditContext update) {
		TLVisibleColumns visibleColumns = update.getAnnotation(TLVisibleColumns.class);

		if (visibleColumns != null) {
			AllVisibleColumnsProvider.Config<?> config =
				TypedConfiguration.newConfigItem(AllVisibleColumnsProvider.Config.class);
			config.setColumns(visibleColumns.getVisibleColumns());
			providers.add(TypedConfigUtil.createInstance(config));
		}
	}

	private static void addSortColumnsTableProvider(List<TableConfigurationProvider> providers, EditContext update) {
		TLSortColumns sortColumnsAnnotation = update.getAnnotation(TLSortColumns.class);

		if (sortColumnsAnnotation != null) {
			SortColumnsTableConfigurationProvider.Config config =
				TypedConfiguration.newConfigItem(SortColumnsTableConfigurationProvider.Config.class);
			TypedConfigUtil.setProperty(config, SortColumnsConfig.ORDER, sortColumnsAnnotation.getOrder());
			providers.add(TypedConfigUtil.createInstance(config));
		}
	}

	private static void addIDColumnTableProvider(List<TableConfigurationProvider> providers, EditContext update) {
		TLIDColumn idColumnAnnotation = update.getAnnotation(TLIDColumn.class);

		if (idColumnAnnotation != null) {
			IDColumnTableConfigurationProvider.Config config =
				TypedConfiguration.newConfigItem(IDColumnTableConfigurationProvider.Config.class);
			TypedConfigUtil.setProperty(config, IDColumnConfig.VALUE, idColumnAnnotation.getValue());
			providers.add(TypedConfigUtil.createInstance(config));
		}
	}
}
