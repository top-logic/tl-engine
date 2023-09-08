/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.element.util.dbadmin.TableUsageListModelBuilder.Usage;
import com.top_logic.knowledge.service.xml.annotation.SystemAnnotation;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.configEdit.RemoveConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} that removes a table type, if it is not used.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteTable extends RemoveConfiguration {

	/**
	 * Creates a {@link DeleteTable}.
	 */
	public DeleteTable(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {

		MetaObjectName table = (MetaObjectName) model;
		if (table == null) {
			throw new TopLogicException(com.top_logic.tool.execution.I18NConstants.ERROR_NO_MODEL);
		}

		SchemaConfiguration schema = (SchemaConfiguration) component.getModel();

		String tableName = table.getObjectName();

		if (TLAnnotations.GENERIC_TABLE_NAME.equals(tableName)) {
			throw new TopLogicException(I18NConstants.ERROR_SYSTEM_TABLE_CANNOT_BE_DELETED);
		}

		if (table instanceof MetaObjectConfig) {
			if (((MetaObjectConfig) table).getAnnotation(SystemAnnotation.class) != null) {
				throw new TopLogicException(I18NConstants.ERROR_SYSTEM_TABLE_CANNOT_BE_DELETED);
			}
		}

		List<Usage> usages = TableUsageListModelBuilder.usageStream(schema, tableName)
			.filter(u -> u.getReference() == null || !tableName.equals(u.getType().getObjectName()))
			.collect(Collectors.toList());
		if (!usages.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_CANNOT_DELETE_TYPE_IS_IN_USE)
				.initSeverity(ErrorSeverity.WARNING)
				.initDetails(I18NConstants.TYPE_USAGE__LOCATIONS.fill(usages));
		}

		return super.handleCommand(aContext, component, model, someArguments);
	}

}
