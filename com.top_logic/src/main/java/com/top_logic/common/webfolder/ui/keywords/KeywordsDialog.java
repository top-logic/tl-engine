/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.keywords;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ArrayTableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.Enabled;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.table.renderer.TableRendererProxy;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The {@link KeywordsDialog} shows some keywords
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KeywordsDialog extends AbstractFormPageDialog {

	/** Name of the table field in form context */
	private static final String TABLE_NAME = "table";

	/**
	 * The keywords to show
	 */
	private final Collection<String> _contents;

	/**
	 * Creates a new {@link KeywordsDialog}.
	 * 
	 * @param contents
	 *        the keywords to display. must not be <code>null</code>
	 */
	public KeywordsDialog(Collection<String> contents, DisplayDimension width, DisplayDimension height) {
		super(I18NConstants.DIALOG, width, height);
		_contents = contents;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		addClose(buttons, ButtonType.CLOSE);
	}

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.KEYWORDS_60);
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return input(TABLE_NAME);
	}

	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.DIALOG;
	}

	@Override
	protected void fillFormContext(FormContext context) {

		/* Displays the list in two (logical) columns */
		final int size = _contents.size();
		int middle;
		if (size % 2 == 0) {
			middle = (size / 2);
		} else {
			middle = (size / 2) + 1;
		}
		
		/* the columns 1 and 3 show the position in the keyword list, the columns 2 and 4 the actual
		 * keyword */
		List<String[]> tableRows = new ArrayList<>(middle);
		int pos = 0;
		for (String keyword : _contents) {
			String[] rowObj;
			if (pos < middle) {
				rowObj = new String[4];
				tableRows.add(rowObj);
				rowObj[0] = Integer.toString(pos + 1);
				rowObj[1] = keyword;
			} else {
				rowObj = tableRows.get(pos - middle);
				rowObj[2] = Integer.toString(pos + 1);
				rowObj[3] = keyword;
			}
			pos++;
		}
		
		String[] columns = new String[] { "1", "2", "3", "4" };
		TableConfiguration tableConfiguration = createTableConfiguration();
		final TableModel model = new ArrayTableModel<>(columns, tableRows, tableConfiguration);

		final TableField tableField = FormFactory.newTableField(TABLE_NAME, model);
		tableField.setControlProvider(new DefaultFormFieldControlProvider() {

			@Override
			public Control visitTableField(TableField aMember, Void arg) {
				final TableControl result = (TableControl) super.visitTableField(aMember, arg);

				ITableRenderer renderer = result.getRenderer();
				/* Search actual implementation */
				while (renderer instanceof TableRendererProxy) {
					renderer = ((TableRendererProxy<?>) renderer).getImplementation();
				}
				if (renderer instanceof DefaultTableRenderer) {
					((DefaultTableRenderer) renderer).setFooterText(HTMLConstants.SUM + " " + size);
				}
				return result;
			}
		});
		context.addMember(tableField);
	}

	private TableConfiguration createTableConfiguration() {
		TableConfiguration tableConfiguration = TableConfiguration.table();
		// disable sorting as there are 4 technical but only 2 logic columns
		tableConfiguration.setMultiSort(Enabled.never);
		tableConfiguration.setColumnCustomization(ColumnCustomization.NONE);
		tableConfiguration.setResPrefix(I18NConstants.DIALOG.append(TABLE_NAME));
		ColumnConfiguration defaultColumn = tableConfiguration.getDefaultColumn();
		defaultColumn.setSortable(false);

		return tableConfiguration;
	}

}
