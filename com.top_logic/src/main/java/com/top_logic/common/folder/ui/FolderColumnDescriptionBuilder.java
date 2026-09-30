/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.ui;

import java.text.DecimalFormat;
import java.util.Comparator;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.common.folder.ContentComparator;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.control.FirstLineLabelProvider;
import com.top_logic.layout.form.control.FirstLineRenderer;
import com.top_logic.layout.form.control.TextPopupControl;
import com.top_logic.layout.form.control.TextPopupControl.CP;
import com.top_logic.layout.form.control.TextPopupControl.CP.Config;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.util.FormFieldValueMapping;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.provider.FormatLabelProvider;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.filter.SimpleComparableFilterProvider;
import com.top_logic.layout.table.filter.SimpleDateFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.provider.DescriptionColumnFieldProvider;
import com.top_logic.layout.table.renderer.DateTimeFormat;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class FolderColumnDescriptionBuilder {

	private final ExecutableState allowClipboard;

	private final ExecutableState allowWrite;

	private final ExecutableState allowDelete;

	private static final String DESCRIPTION_SIZE_MAX_CSS_CLASS = "descriptionSizeMax";


	/**
	 * Creates a {@link FolderColumnDescriptionBuilder}.
	 * 
	 * @param canUpdate
	 *        Whether update is allowed.
	 * @param canDelete
	 *        Whether content deletion is allowed.
	 */
	public FolderColumnDescriptionBuilder(ExecutableState canAddToClipboard, ExecutableState canUpdate,
			ExecutableState canDelete) {
		this.allowClipboard = canAddToClipboard;
		this.allowWrite = canUpdate;
		this.allowDelete = canDelete;
	}

	public ColumnConfiguration createNameColumn(ColumnConfiguration column) {
		column.setComparator(getComparator());
		column.setRenderer(getRenderer());
		column.setFilterProvider(LabelFilterProvider.INSTANCE);
		column.setDefaultColumnWidth("100%");
		column.setSortable(true);

		return column;
	}

	protected Comparator getComparator() {
		return ContentComparator.INSTANCE;
	}

	protected Renderer<Object> getRenderer() {
		return ResourceRenderer.INSTANCE;
	}

	/**
	 * Creates a column for the description of a {@link DocumentVersion} or {@link WebFolder}.
	 * 
	 * <p>
	 * Contains a {@link StringField} with a {@link TextPopupControl} to enable editing of the
	 * description. Too long texts will be trimmed.
	 * </p>
	 */
	public ColumnConfiguration createDescriptionColumn(ColumnConfiguration column) {
		column.setFieldProvider(DescriptionColumnFieldProvider.INSTANCE);
		column.setControlProvider(cp());
		column.setSortKeyProvider(FormFieldValueMapping.INSTANCE);
		column.setSortable(true);
		return column;
	}

	private CP cp() {
		Config textConfig = TypedConfiguration.newConfigItem(TextPopupControl.CP.Config.class);
		FirstLineLabelProvider.Config<?> labelProvider =
			TypedConfiguration.newConfigItem(FirstLineLabelProvider.Config.class);
		labelProvider.setMaxChars(150);
		textConfig.setLabelProvider(labelProvider);
		CP instance = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(textConfig);
		return instance;
	}

	/**
	 * Creates a column for the description of a {@link DocumentVersion} without a
	 * {@link TextPopupControl}.
	 * 
	 * <p>
	 * Too long texts will be trimmed.
	 * </p>
	 */
	public ColumnConfiguration createImmutableDescriptionColumn(ColumnConfiguration column) {
		column.setSortable(true);
		column.setRenderer(FirstLineRenderer.DEFAULT_INSTANCE);
		column.addCssClass(DESCRIPTION_SIZE_MAX_CSS_CLASS);

		return column;
	}

	public ColumnConfiguration createDownloadColumn(ColumnConfiguration column) {
		return this.createButtonColumn(column);
	}

	public ColumnConfiguration createDateColumn(ColumnConfiguration column) {
		column.setSelectable(false);
		column.setSortable(true);
		column.setComparator(ComparableComparator.INSTANCE);
		column.setFilterProvider(SimpleDateFilterProvider.INSTANCE);
		column.setLabelProvider(DateTimeFormat.INSTANCE);
		column.setCellStyle(HTMLConstants.TEXT_ALIGN_CENTER);

		return column;
	}

	public ColumnConfiguration createTypeColumn(ColumnConfiguration column) {
		column.setSelectable(false);
		column.setSortable(true);
		column.setComparator(ComparableComparator.INSTANCE);
		column.setLabelProvider(DefaultLabelProvider.INSTANCE);

		return column;
	}

	public ColumnConfiguration createSizeColumn(ColumnConfiguration column) {
		column.setSelectable(false);
		column.setSortable(true);
		column.setComparator(ComparableComparator.INSTANCE);
		column.setFilterProvider(SimpleComparableFilterProvider.INSTANCE);
		column.setLabelProvider(new FormatLabelProvider(new DecimalFormat("#,##0 Byte")));
		column.addCssClass("tblRight");

		return column;
	}

	public ColumnConfiguration createClipboardColumn(ColumnConfiguration column) {
		ColumnConfiguration result = this.createButtonColumn(column);
		result.setVisible(allowClipboard.isVisible());
		return result;
	}

	public ColumnConfiguration createLockColumn(ColumnConfiguration column) {
		ColumnConfiguration result = this.createButtonColumn(column);
		result.setVisible(this.allowWrite.isVisible());
		return result;
	}

	public ColumnConfiguration createVersionColumn(ColumnConfiguration column) {
		return this.createButtonColumn(column);
	}

	public ColumnConfiguration createMailColumn(ColumnConfiguration column) {
		return this.createButtonColumn(column);
	}

	public ColumnConfiguration createDeleteColumn(ColumnConfiguration column) {
		ColumnConfiguration result = this.createButtonColumn(column);
		result.setVisible(this.allowDelete.isVisible());
		return result;
	}

	public ColumnConfiguration createSimilarDocsColumn(ColumnConfiguration column) {
		ColumnConfiguration result = this.createButtonColumn(column);
		return result;
	}

	public ColumnConfiguration createKeywordsColumn(ColumnConfiguration column) {
		ColumnConfiguration result = this.createButtonColumn(column);
		return result;
	}

	public ColumnConfiguration createButtonColumn(ColumnConfiguration column) {
		column.setSelectable(false);
		column.setFieldProvider(getFieldProvider());
		column.setFilterProvider(null);
		column.setControlProvider(getControlProvider());
		column.setShowHeader(false);
		return column;
	}

	protected FieldProvider getFieldProvider() {
		return createFolderFieldProvider(allowClipboard, allowWrite, allowDelete);
	}


	protected ControlProvider getControlProvider() {
		return DefaultFormFieldControlProvider.INSTANCE;
	}

	/**
	 * Creates and returns a FieldProvider for the given states.
	 */
	protected abstract FieldProvider createFolderFieldProvider(ExecutableState allowClipboard,
			ExecutableState allowWrite,
			ExecutableState allowDelete);



}
