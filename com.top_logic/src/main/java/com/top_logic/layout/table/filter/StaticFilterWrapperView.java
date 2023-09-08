/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.io.IOException;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link FilterViewControl} of a {@link StaticFilterWrapper}.
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public class StaticFilterWrapperView extends FilterViewControl<StaticFilterWrapperConfiguration> {
	
	static final String ACTIVE_FIELD = "active";
	
	static final String MATCH_COUNT_FIELD = "matchCount";

	static final String FILTER_SETTINGS_FORM_GROUP = "filterList";

	static final String FILTER_GROUP = StaticFilterWrapperView.class.getSimpleName();

	static final ResPrefix RES_PREFIX = ResPrefix.GLOBAL;

	private static final String FILTER_LIST_CSS_CLASS = "fltList";

	private static final String FILTER_MENU_CSS_CLASS = "fltMenu";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe("<t:group"
			+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
			+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
			+ ">"
			+ "<div class='" + FILTER_MENU_CSS_CLASS + "'>"
			+ "<p:field name='" + FILTER_SETTINGS_FORM_GROUP + "'>"
			+ "<t:group>"
			+ "<div class='" + FILTER_LIST_CSS_CLASS + "'>"
			+ "<p:field name='" + ACTIVE_FIELD + "'/>"
			+ "<p:field name='" + ACTIVE_FIELD + "' style='label'/>"
			+ "<span style='margin-left: 5px;'>"
			+ "<p:field name='" + MATCH_COUNT_FIELD + "'/>"
			+ "</span>"
			+ "</div>"
			+ "</t:group>"
			+ "</p:field>"
			+ "</div>"
			+ "</t:group>");

	/**
	 * Create a new {@link StaticFilterWrapperView}
	 */
	public StaticFilterWrapperView(StaticFilterWrapperConfiguration filterConfiguration, FormGroup formGroup) {
		super(filterConfiguration, formGroup);
	}
	
	private Document getViewTemplate() {
		return TEMPLATE;
	}

	@Override
	protected boolean internalApplyFilterSettings() {
		boolean newActive = getActivationField().getAsBoolean();
		boolean changed = newActive != getFilterModel().isActive();
		getFilterModel().setActive(newActive);
		return changed;
	}

	@Override
	public void resetFilterSettings() {
		setFilterSettings(false);
	}

	private void setFilterSettings(boolean isActive) {
		getActivationField().setAsBoolean(isActive);
	}

	@Override
	protected void internalValueChanged() {
		setFilterSettings(getFilterModel().isActive());
		getMatchCountField()
			.setAsString(StaticFilterWrapperViewBuilder.getFormattedTextOutput(getFilterModel().getMatchCount()));
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		DefaultFormFieldControlProvider provider = new DefaultFormFieldControlProvider() {
			@Override
			public Control visitCommandField(CommandField member, Void arg) {
				return new ButtonControl(member, ButtonComponentButtonRenderer.INSTANCE);
			}
		};
		FormGroupControl filterContentControl = new FormGroupControl(getFilterGroup(),
			provider,
			DOMUtil.getFirstElementChild(getViewTemplate().getDocumentElement()),
			RES_PREFIX);
		filterContentControl.write(context, out);
	}

	private BooleanField getActivationField() {
		return (BooleanField) getFilterGroup().getFirstMemberRecursively(ACTIVE_FIELD);
	}

	private StringField getMatchCountField() {
		return (StringField) getFilterGroup().getFirstMemberRecursively(MATCH_COUNT_FIELD);
	}
}
