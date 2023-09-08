/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.PopupFilterDialogBuilder.*;

import java.io.IOException;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link FilterViewControl} of the {@link TableFilterModel}s inversion option.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class InversionOptionViewControl extends FilterViewControl<TableFilterModel> {

	private static final String FILTER_LIST_CSS_CLASS = "fltList";

	private static final String FILTER_MENU_CSS_CLASS = "fltMenu";

	private static final Document INVERSION_OPTION_TEMPLATE = DOMUtil.parseThreadSafe("<t:group"
		+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
		+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
		+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
		+ ">"
		+ "<div class='" + FILTER_MENU_CSS_CLASS + " " + FILTER_CONTENT_AREA_CSS_CLASS + "'>"
		+ "<hr/>"
		+ "<p:field name='" + FILTER_SETTINGS_FORM_GROUP + "'>"
		+ "<t:list>"
		+ "<div class='" + FILTER_LIST_CSS_CLASS + "'>"
		+ "<t:items>"
		+ "<div><p:self /><p:self style='label'/></div>"
		+ "</t:items>"
		+ "</div>"
		+ "</t:list>"
		+ "</p:field>"
		+ "</div>"
		+ "</t:group>");

	/**
	 * Create a new {@link InversionOptionViewControl}.
	 */
	public InversionOptionViewControl(TableFilterModel filterModel, FormGroup formGroup) {
		super(filterModel, formGroup);
	}

	@Override
	protected boolean internalApplyFilterSettings() {
		boolean filtersActive = false;
		for (ConfiguredFilter configuredFilter : getFilterModel().getSubFilters()) {
			filtersActive |= configuredFilter.isActive();
		}
		boolean newInversionState;
		if (filtersActive) {
			newInversionState = getInversionOptionField().getAsBoolean();
		} else {
			newInversionState = false;
		}

		boolean changed = newInversionState != getFilterModel().isInversionStateActive();
		getFilterModel().setInversionStateIsActive(newInversionState);
		return changed;
	}

	@Override
	public void resetFilterSettings() {
		setFilterSettings(false);
	}

	private void setFilterSettings(boolean isActive) {
		getInversionOptionField().setAsBoolean(isActive);
	}

	@Override
	protected void internalValueChanged() {
		setFilterSettings(getFilterModel().isInversionStateActive());
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		FormGroupControl formGroupControl = new FormGroupControl(getFilterGroup(),
			DefaultFormFieldControlProvider.INSTANCE,
			DOMUtil.getFirstElementChild(INVERSION_OPTION_TEMPLATE.getDocumentElement()),
			RES_PREFIX);
		formGroupControl.write(context, out);
	}

	private BooleanField getInversionOptionField() {
		return (BooleanField) getFilterGroup().getFirstMemberRecursively(INVERT_FIELD);
	}
}
