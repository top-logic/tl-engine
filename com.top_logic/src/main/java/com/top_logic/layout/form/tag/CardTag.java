/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.BlockControl;

/**
 * The class {@link CardTag} is a teg which can be used as child of an {@link DeckPaneTag} to
 * describe how the given member should be rendered.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CardTag extends CompositeControlTag implements FormContainerTag {

	private static final String CARD = "_card";

	private FormContainer member;

	public void setName(String name) {
		if (!getDeckPaneTag().getDeckField().hasMember(name)) {
			throw new IllegalArgumentException(" There is no member with name '" + name + "' in the DeckField");
		}
		FormMember memberByName = getDeckPaneTag().getDeckField().getMember(name);
		assert memberByName instanceof FormContainer : "Failure in DeckField: DeckField should only contain FormContainer";
		this.member = (FormContainer) memberByName;
	}

	private DeckPaneTag getDeckPaneTag() {
		return (DeckPaneTag) getParent();
	}

	@Override
	protected Control createControl() {
		return new BlockControl();
	}

	@Override
	protected int endElement() throws IOException, JspException {
		/* sets the Control to the DeckPaneTag for can be used during rendering the DeckField */
		getDeckPaneTag().setControl(member, getControl());
		return super.endElement();
	}

	@Override
	protected void renderContents() throws IOException {
		/*
		 * nothing to render here. The BlockControl which is build here is added to the surrounding
		 * DeckPaneTag to be used during rendering the DeckField.
		 */
	}

	@Override
	protected void setup() throws JspException {
		super.setup();
		if (getId() == null) {
			setId(member.getQualifiedName() + CARD);
		}
	}

	@Override
	protected void teardown() {
		super.teardown();
		setId(null);
	}

	@Override
	public FormContainer getFormContainer() {
		return this.member;
	}

	@Override
	public FormMember getMember() {
		return this.member;
	}

	@Override
	public FormTag getFormTag() {
		return getDeckPaneTag().getFormTag();
	}
}
