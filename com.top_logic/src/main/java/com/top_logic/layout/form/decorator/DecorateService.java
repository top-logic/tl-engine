/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelMapping;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.filter.MandatoryLabelFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.tool.export.ExcelCellRenderer;

/**
 * Service to enhance form context and fields by some decorations.
 * 
 * <p>An example for a decoration is the comparing of different revisions
 * of one object from the knowledge base.</p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class DecorateService<DI extends DecorateInfo> {

	/** Name of the column displaying the compare state of the row. */
	public static final String DECORATION_COLUMN = "_decorated_";

	/** Width declaration of {@link #DECORATION_COLUMN} */
	public static final String DECORATION_COLUMN_WIDTH = "40px";

	private static final Property<Object> DECORATED_SERVICE = TypedAnnotatable.property(Object.class, "decoratedService");


	/**
	 * Create the info object for the given field.
	 * 
	 * @param field
	 *        The original field to get the {@link DecorateInfo} from.
	 * @param labels
	 *        A label provider for getting the name (or optional tool tip).
	 * 
	 * @return The requested info object, or <code>null</code> when no decoration is given.
	 */
	protected abstract DI createDecorateInfo(FormField field, LabelProvider labels);

	/**
	 * Write the start tag for the given compare information.
	 * 
	 * @param context
	 *        Context we are currently rendering in.
	 * @param out
	 *        Writer to print the information.
	 * @param info
	 *        Info object created by {@link #createDecorateInfo(FormField, LabelProvider)}.
	 * 
	 * @see #start(DisplayContext, TagWriter, FormField, LabelProvider)
	 */
	protected abstract void start(DisplayContext context, TagWriter out, DI info) throws IOException;

	/**
	 * Write the end tag for the compare information.
	 * 
	 * @param context
	 *        Context we are currently rendering in.
	 * @param out
	 *        Writer to print the information.
	 * @param info
	 *        Info object created by {@link #createDecorateInfo(FormField, LabelProvider)}.
	 */
	protected abstract void end(DisplayContext context, TagWriter out, DI info) throws IOException;

	/**
	 * Write the start tag for the decorate information.
	 * 
	 * @param context
	 *        Context we are currently rendering in.
	 * @param out
	 *        Writer to print the information.
	 * @param field
	 *        The original field to get the {@link DecorateInfo} from.
	 * @param labels
	 *        A label provider for getting the name (or optional tool tip).
	 * 
	 * @return The created decoration object, or <code>null</code> if no decoration is written.
	 */
	protected DI doStart(DisplayContext context, TagWriter out, FormField field, LabelProvider labels)
			throws IOException {
		DI decorateInfo = createDecorateInfo(field, labels);
		if (decorateInfo == null) {
			return null;
		}

		start(context, out, decorateInfo);

		return decorateInfo;
	}

	/**
	 * Provide a table data from the given select field.
	 * 
	 * <p>
	 * Create a decorated information an enhance the returned table data by a matching column for
	 * that.
	 * </p>
	 * 
	 * @param field
	 *        The original field to get the {@link TableData} from.
	 * @return The requested table data, never <code>null</code>.
	 */
	protected abstract TableData doPrepare(SelectField field);

	/**
	 * Provide an extension to the table configuration which will define an additional column for
	 * decorated informations.
	 * 
	 * <p>
	 * The additional column has no {@link ColumnConfiguration#getExcelRenderer() excel renderer}.
	 * </p>
	 * 
	 * @see #createDecorationInfoColumn(Accessor, CellRenderer, ResKey, ExcelCellRenderer)
	 */
	protected final TableConfigurationProvider createDecorationInfoColumn(Accessor<?> infoAccessor,
			CellRenderer infoRenderer, ResKey columnLabel) {
		return createDecorationInfoColumn(infoAccessor, infoRenderer, columnLabel, null);
	}

	/**
	 * Provide an extension to the table configuration which will define an additional column for
	 * decorated informations.
	 * 
	 * @param infoAccessor
	 *        {@link Accessor} returning the {@link DecorateInfo} for the row.
	 * @param infoRenderer
	 *        {@link Renderer} rendering the {@link DecorateInfo} returned by the accessor.
	 * @param columnLabel
	 *        Label of the column.
	 * @param excelRenderer
	 *        {@link ExcelCellRenderer} for the {@link DecorateInfo}.
	 * 
	 * @return The requested configuration provider.
	 */
	protected TableConfigurationProvider createDecorationInfoColumn(final Accessor<?> infoAccessor,
			final CellRenderer infoRenderer, final ResKey columnLabel, final ExcelCellRenderer excelRenderer) {
		return new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration aTable) {
				ColumnConfiguration decorationColumn = aTable.declareColumn(DECORATION_COLUMN);

				decorationColumn.setDefaultColumnWidth(DECORATION_COLUMN_WIDTH);
				decorationColumn.setAccessor(infoAccessor);
				decorationColumn.setCellRenderer(infoRenderer);
				decorationColumn.setVisibility(DisplayMode.mandatory);
				decorationColumn.setShowHeader(false);
				decorationColumn.setSortable(false);
				decorationColumn.setFilterProvider(MandatoryLabelFilterProvider.INSTANCE);
				decorationColumn.setColumnLabelKey(columnLabel);
				decorationColumn.setSortKeyProvider(LabelMapping.INSTANCE);
				decorationColumn.setCellStyle("text-align:center;");
				decorationColumn.setExcelRenderer(excelRenderer);
			}
		};
	}

	/**
	 * Check, if the given form context contains information about decoration.
	 * 
	 * @param context
	 *        The form to get the information from, must not be <code>null</code>.
	 * @return <code>true</code> when decoration has to be displayed.
	 */
	public static boolean isDecorated(FormContext context) {
		return context.get(DecorateService.DECORATED_SERVICE) != null;
	}

	/**
	 * Provide a table data from the given select field.
	 * 
	 * <p>
	 * This method will look for an {@link DecorateService} in the given field. When there is such a
	 * service, the {@link DecorateService#doPrepare(SelectField)} will be called to enhance the
	 * table data by a decorated column.
	 * </p>
	 * 
	 * @param field
	 *        The original field to get the {@link TableData} from.
	 * @return The requested table data, never <code>null</code>.
	 */
	public static TableData prepareTableData(SelectField field) {
		DecorateService<?> decorateService = DecorateService.getService(field);

		if (decorateService != null) {
			return decorateService.doPrepare(field);
		}
		return field.getTableData();
	}

	/**
	 * Starts decoration with {@link #getDefaultLabels(FormField) default labels}.
	 * 
	 * @see #start(DisplayContext, TagWriter, FormField, LabelProvider)
	 */
	public static DecorateInfo start(DisplayContext context, TagWriter out, FormField field) throws IOException {
		return start(context, out, field, getDefaultLabels(field));
	}

	/**
	 * Write the start tag for the decorate information.
	 * 
	 * @param context
	 *        Context we are currently rendering in.
	 * @param out
	 *        Writer to print the information.
	 * @param field
	 *        The original field to add the {@link DecorateInfo} to (for calling
	 *        {@link #end(DisplayContext, TagWriter, FormField, DecorateInfo)}).
	 * @param labels
	 *        A label provider for getting the name (or optional tool tip).
	 * @return The created info object, or <code>null</code>.
	 */
	public static DecorateInfo start(DisplayContext context, TagWriter out, FormField field, LabelProvider labels)
			throws IOException {
		DecorateService<?> decorateService = DecorateService.getService(field);

		if (decorateService != null) {
			return decorateService.doStart(context, out, field, labels);
		}
		return null;
	}

	/**
	 * The default {@link LabelProvider} to create decorate info.
	 */
	public static LabelProvider getDefaultLabels(FormField field) {
		return (LabelProvider) field.visit(LabelProviderVisitor.INSTANCE, null);
	}

	/**
	 * Write the end tag for the compare information.
	 * 
	 * @param context
	 *        Context we are currently rendering in.
	 * @param out
	 *        Writer to print the information.
	 * @param info
	 *        Info object created by
	 *        {@link #start(DisplayContext, TagWriter, FormField, LabelProvider)}, or
	 *        <code>null</code>.
	 */
	public static void end(DisplayContext context, TagWriter out, FormField field, DecorateInfo info)
			throws IOException {
		if (info == null) {
			return;
		}
		DecorateService<DecorateInfo> decorateService = DecorateService.getService(field);

		if (decorateService != null) {
			decorateService.end(context, out, info);
		}
	}

	/**
	 * Handle the decoration in the form context.
	 * 
	 * <p>
	 * When the given service is <code>null</code> the decoration service will be deactivated for
	 * the given form context.
	 * </p>
	 * 
	 * @param context
	 *        The form context to be annotated, must not be <code>null</code>.
	 * @param decorateService
	 *        The service to be annotated, may be <code>null</code>.
	 */
	public static void annotate(FormContext context, DecorateService<?> decorateService) {
		if (decorateService != null) {
			context.set(DecorateService.DECORATED_SERVICE, decorateService);
		} else {
			context.reset(DecorateService.DECORATED_SERVICE);
		}
	}

	private static DecorateService<DecorateInfo> getService(FormField field) {
		FormContext context = field.getFormContext();
		if (context == null) {
			// No context, no decorator
			return null;
		}
		return getDecorator(context);
	}

	/**
	 * Returns the {@link DecorateService} formerly annotated by
	 * {@link #annotate(FormContext, DecorateService)}.
	 * 
	 * @param context
	 *        The {@link FormContext} to get decorator from.
	 * @return The formerly annotated decorator, or <code>null</code> if is not decorated.
	 * 
	 * @see #isDecorated(FormContext)
	 */
	@SuppressWarnings("unchecked")
	public static DecorateService<DecorateInfo> getDecorator(FormContext context) {
		return (DecorateService<DecorateInfo>) context.get(DECORATED_SERVICE);
	}

	public static TableData prepareTableData(TableField field) {
		DecorateService<?> decorateService = DecorateService.getService(field);

		if (decorateService != null) {
			return decorateService.doPrepare(field);
		}
		return field;
	}

	/**
	 * Prepare the values in the given table field.
	 * 
	 * @param field   Table to be prepared.
	 * @return   New created table data.
	 */
	protected abstract TableData doPrepare(TableField field);

}
