/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Date;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link RevisionSelectDialog} for a current model. The user must select a date to determine
 * {@link Revision} from.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CurrentRevisionSelectDialog extends RevisionSelectDialog {

	private static class CloseCommand extends AbstractCloseCommand {

		public CloseCommand(RevisionSelectDialog dialog) {
			super(dialog);
		}

		@Override
		protected Revision getRevision() {
			FormContext formContext = _dialog.getFormContext();
			FormField field = formContext.getField(DATE_FIELD_NAME);
			Date date = (Date) field.getValue();
			return HistoryUtils.getRevisionAt(date.getTime());
		}

	}

	private static final Document TEMPLATE;
	static {
		StringBuilder template = new StringBuilder();
		template.append("<t:group");
		template.append(" xmlns='" + HTMLConstants.XHTML_NS + "'");
		template.append(" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'");
		template.append(" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'");
		template.append(">");
		template.append("<div style='margin: 5px; padding-left: 10px;'>");
		appendDateField(template);
		template.append("</div>");
		template.append("</t:group>");
		TEMPLATE = DOMUtil.parseThreadSafe(template.toString());
	}

	protected CurrentRevisionSelectDialog(RevisionCallback revCallback, DisplayDimension width, DisplayDimension height,
			boolean suppressRecording) {
		super(revCallback, width, height, suppressRecording);
	}

	@Override
	protected AbstractCloseCommand createCloseCommand() {
		return new CloseCommand(this);
	}

	@Override
	protected void fillFormContext(FormContext context) {
		// No additional field.
		super.fillFormContext(context);
	}

	@Override
	protected Document getTemplate() {
		return TEMPLATE;
	}

}

