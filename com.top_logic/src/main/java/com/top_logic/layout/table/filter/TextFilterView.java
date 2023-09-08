/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;


import static com.top_logic.layout.table.filter.SelectionFilterViewBuilder.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.layout.table.filter.SelectionFilterView.CheckboxMultiSelectControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Utils;

/**
 * {@link FilterViewControl} of a {@link TextFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public class TextFilterView extends FilterViewControl<TextFilterConfiguration> {

	static final String REGEXP_FIELD = "regexp";

	static final String SELECTABLE_VALUES_FIELD = "selectableValuesField";

	static final String TEXT_FIELD = "text";

	static final String CASE_SENSITIVE_FIELD = "caseSensitive";

	static final String WHOLE_FIELD_FIELD = "wholeField";

	static final String FILTER_SETTINGS_FORM_GROUP = "filterSettings";

	static final String FILTER_FIELD_FORM_GROUP = "filterField";

	static final String FILTER_CONTEXT = "filterDialogContent";
	
	private static final ResPrefix RES_PREFIX = ResPrefix.legacyClassLocal(TextFilterView.class);
	
	static final ResKey WHOLE_FIELD_CHECKBOX = RES_PREFIX.key(WHOLE_FIELD_FIELD);

	static final ResKey CASE_SENSITIVE_CHECKBOX = RES_PREFIX.key(CASE_SENSITIVE_FIELD);

	static final ResKey REG_EXP_CHECKBOX = RES_PREFIX.key("regExp");
	
	static final ResKey INVALID_REG_EXP_EXPRESSION = RES_PREFIX.key("regExp.invalid");

	private static final String FILTER_LIST_CSS_CLASS = "fltList";

	private static final String FILTER_MENU_CSS_CLASS = "fltMenu";

	private static final String DEFAULT_FILTER_CONTAINER_CLASS = "defaultFilterContainer";

	private static final Document DIALOG_TEMPLATE = DOMUtil.parseThreadSafe("<t:group"
		+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
		+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
		+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
		+ ">"
		+ "<form action=''>"
		+ "<p:field name='" + FILTER_FIELD_FORM_GROUP + "'>"
		+ "<t:list>"
		+ "<div class='" + FILTER_LIST_CSS_CLASS + "'>"
		+ "<t:items>"
		+ "<div><p:self /><p:self style='error'/></div>"
		+ "</t:items>"
		+ "</div>"
		+ "</t:list>"
		+ "</p:field>"
		+ "<p:field name='" + FILTER_SETTINGS_FORM_GROUP + "'>"
		+ "<t:list>"
		+ "<div class='" + FILTER_LIST_CSS_CLASS + "'>"
		+ "<t:items>"
		+ "<div><p:self /><p:self style='label'/></div>"
		+ "</t:items>"
		+ "</div>"
		+ "</t:list>"
		+ "</p:field>"
		+ "</form>"
		+ "</t:group>");
	
	private int _separateOptionDisplayThreshold;

	/**
	 * Create a new {@link TextFilterView}
	 */
	public TextFilterView(TextFilterConfiguration filterConfiguration, FormGroup formGroup, int separateOptionDisplayThreshold) {
		super(filterConfiguration, formGroup);
		_separateOptionDisplayThreshold = separateOptionDisplayThreshold;
	}
	
	/**
	 * Creates the document pattern for the filter dialog
	 * 
	 * @return document pattern
	 */
	private Document getDialogTemplate() {
		return DIALOG_TEMPLATE;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected boolean internalApplyFilterSettings() {
		boolean caseSensitiveValue;
		boolean wholeFieldValue;
		boolean regExpValue;
		String text = getText();
		if (!StringServices.isEmpty(text)) {
			caseSensitiveValue = getCaseSensitiveValue();
			wholeFieldValue = getWholeFieldValue();
			if (hasRegExpField()) {
				regExpValue = getRegExpValue();
			} else {
				regExpValue = false;
			}
		} else {
			caseSensitiveValue = wholeFieldValue = regExpValue = false;
		}
		List newSelection = getSelectionPatternField().getSelection();
		boolean changed = !Utils.equals(text, getFilterModel().getTextPattern());
		changed |= !Utils.equals(caseSensitiveValue, getFilterModel().isCaseSensitive());
		changed |= !Utils.equals(wholeFieldValue, getFilterModel().isWholeField());
		changed |= !Utils.equals(regExpValue, getFilterModel().isRegExp());
		changed |= !CollectionUtil.containsSame(newSelection, getFilterModel().getFilterPattern());
		getFilterModel().setFilterPattern(newSelection);
		getFilterModel().setTextPattern(text);
		getFilterModel().setCaseSensitive(caseSensitiveValue);
		getFilterModel().setWholeField(wholeFieldValue);
		getFilterModel().setRegExp(regExpValue);

		return changed;
	}

	@Override
	public void resetFilterSettings() {
		setSelectionFilterSettings(getOptions(getFilterModel()), Collections.emptyList());
		setRuleBasedFilterSettings("", false, false, false);
	}

	private void setSelectionFilterSettings(List<?> options, List<?> selection) {
		getSelectionPatternField().setOptions(options);
		getSelectionPatternField().setAsSelection(selection);
	}

	private void setRuleBasedFilterSettings(String filterPattern, boolean isCaseSensitive, boolean isWholeField,
			boolean isRegExp) {
		getTextPatternField().setAsString(filterPattern);
		getCaseSensitiveField().setAsBoolean(isCaseSensitive);
		getWholeFieldField().setAsBoolean(isWholeField);
		if (hasRegExpField()) {
			getRegExpField().setAsBoolean(isRegExp);
		}
	}

	@Override
	protected void internalValueChanged() {
		setSelectionFilterSettings(getOptions(getFilterModel()), getFilterModel().getFilterPattern());
		setRuleBasedFilterSettings(getFilterModel().getTextPattern(), getFilterModel().isCaseSensitive(),
			getFilterModel().isWholeField(), getFilterModel().isRegExp());
		requestRepaint();
	}

	private boolean hasRegExpField() {
		return getFilterGroup().getFirstMemberRecursively(REGEXP_FIELD) != null;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeFilterContent(context, out);
		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, FILTER_MENU_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, DEFAULT_FILTER_CONTAINER_CLASS);
	}

	private void writeFilterContent(DisplayContext context, TagWriter out) throws IOException {
		if (shallDisplayOptionsSeparately()) {
			if (ruleBasedFilterActive()) {
				writeRuleBasedFilter(context, out);
				if (selectionBasedFilterActive()) {
					writeSelectionBasedFilter(context, out, true);
				}
			} else {
				writeSelectionBasedFilter(context, out, false);
			}
		} else {
			writeRuleBasedFilter(context, out);
			if (selectionBasedFilterActive()) {
				writeSelectionBasedFilter(context, out, true);
			}
		}
	}

	private void writeSelectionBasedFilter(DisplayContext context, TagWriter out, boolean showSelectionOnly)
			throws IOException {
		CheckboxMultiSelectControl checkboxMultiSelectControl =
			new CheckboxMultiSelectControl(getSelectionPatternField(), getFilterModel());
		checkboxMultiSelectControl.setShowSelectionOnly(showSelectionOnly);
		checkboxMultiSelectControl.write(context, out);
	}

	private void writeRuleBasedFilter(DisplayContext context, TagWriter out) throws IOException {
		DefaultFormFieldControlProvider provider = new DefaultFormFieldControlProvider() {
			@Override
			public Control visitCommandField(CommandField member, Void arg) {
				return new ButtonControl(member, ButtonComponentButtonRenderer.INSTANCE);
			}
		};
		FormGroupControl filterContentControl = new FormGroupControl(getFilterGroup(),
			provider,
			DOMUtil.getFirstElementChild(getDialogTemplate().getDocumentElement()),
			RES_PREFIX);

		filterContentControl.write(context, out);
	}

	private boolean ruleBasedFilterActive() {
		return !getFilterModel().getTextPattern().equals("");
	}

	private boolean selectionBasedFilterActive() {
		return !getFilterModel().getFilterPattern().isEmpty();
	}

	private boolean shallDisplayOptionsSeparately() {
		return getFilterModel().showOptionEntries()
			&& _separateOptionDisplayThreshold > getFilterModel().getOptions().size();
	}

	private boolean getCaseSensitiveValue() {
		return getCaseSensitiveField().getAsBoolean();
	}

	private BooleanField getCaseSensitiveField() {
		return (BooleanField) getFilterGroup().getFirstMemberRecursively(CASE_SENSITIVE_FIELD);
	}

	private String getText() {
		return getTextPatternField().getAsString();
	}

	private SelectField getSelectionPatternField() {
		return (SelectField) getFilterGroup().getFirstMemberRecursively(SELECTABLE_VALUES_FIELD);
	}

	private StringField getTextPatternField() {
		return (StringField) getFilterGroup().getFirstMemberRecursively(TEXT_FIELD);
	}

	private boolean getWholeFieldValue() {
		return getWholeFieldField().getAsBoolean();
	}

	private BooleanField getWholeFieldField() {
		return (BooleanField) getFilterGroup().getFirstMemberRecursively(WHOLE_FIELD_FIELD);
	}

	private boolean getRegExpValue() {
		return getRegExpField().getAsBoolean();
	}

	private BooleanField getRegExpField() {
		return (BooleanField) getFilterGroup().getFirstMemberRecursively(REGEXP_FIELD);
	}
}