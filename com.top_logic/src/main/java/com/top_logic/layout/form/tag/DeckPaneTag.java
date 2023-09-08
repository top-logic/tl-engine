/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DeckPaneControl;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.mig.html.layout.Card;

/**
 * The class {@link DeckPaneTag} is used to render the currently selected field of an
 * {@link DeckField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeckPaneTag extends AbstractFormMemberControlTag implements FormContainerTag {

	private Map<FormMember, Control> controlsByMember = new HashMap<>();

	private ControlProvider controlProvider = DefaultFormFieldControlProvider.INSTANCE;

	public void setControlProvider(ControlProvider aProvider) {
		this.controlProvider = aProvider;
	}

	public ControlProvider getControlProvider() {
		return this.controlProvider;
	}

	/**
	 * This method adds a control for the {@link FormMember} for using to render if the
	 * corresponding {@link FormMember} is selected.
	 */
	void setControl(FormMember aFormMember, Control aControl) {
		controlsByMember.put(aFormMember, aControl);
	}

	/**
	 * This method returns the {@link Control} for a {@link FormMember} which was formerly added
	 * 
	 * @see #setControl(FormMember, Control)
	 */
	Control getControlForMember(FormMember aFormMember) {
		return controlsByMember.get(aFormMember);
	}

	/**
	 * creates an {@link DeckPaneControl} with the given {@link ControlProvider} as control provider
	 * for all {@link FormMember} except the member for which controls were added via
	 * {@link #setControl(FormMember, Control)}.
	 * 
	 * @see AbstractFormMemberControlTag#createControl(com.top_logic.layout.form.FormMember, String)
	 */
	@Override
	public Control createControl(FormMember member, String displayStyle) {
		ControlProvider cp = new ControlProvider() {
			@Override
			public Control createControl(Object aModel, String aStyle) {
				Card card = (Card) aModel;
				FormMember tabGroup = (FormMember) card.getContent();

				Control existingControl = getControlForMember(tabGroup);
				if (existingControl != null) {
					return existingControl;
				} else {
					ControlProvider customCP = tabGroup.getControlProvider();
					if (customCP != null) {
						return customCP.createControl(tabGroup, aStyle);
					} else {
						return getControlProvider().createControl(tabGroup, aStyle);
					}
				}
			}
		};
		return new DeckPaneControl(getDeckField().getModel(), cp);
	}

	public DeckField getDeckField() {
		return (DeckField) getMember();
	}

	@Override
	protected int endFormMember() throws IOException, JspException {
		ControlTagUtil.writeControl(this, pageContext, getControl());
		return EVAL_PAGE;
	}

	@Override
	protected int startFormMember() throws IOException, JspException {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public FormContainer getFormContainer() {
		return (FormContainer) getMember();
	}

}
