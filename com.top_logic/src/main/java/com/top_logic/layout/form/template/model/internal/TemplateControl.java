/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.CssTagAttributeTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.StartTagTemplate;
import com.top_logic.html.template.TagAttributeTemplate;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.html.template.VariableTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.RemovedListener;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * {@link Control} displaying a {@link FormMember} through a {@link HTMLTemplateFragment}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateControl extends AbstractFormMemberControl implements CollapsedListener, MemberChangedListener {

	/**
	 * Required attribute on the top-level template tag that embedds the control ID.
	 */
	private static final TagAttributeTemplate ID_EXPR_ATTR =
		new TagAttributeTemplate(0, 0, HTMLConstants.ID_ATTR, new VariableTemplate(Control.ID));

	private static final VariableTemplate CSS_CLASS_EXPR = new VariableTemplate(TemplateControl.CSS_CLASS);

	private static final CssTagAttributeTemplate CSS_CLASS_EXPR_ATTR =
		new CssTagAttributeTemplate(0, 0, HTMLConstants.CLASS_ATTR, CSS_CLASS_EXPR);

	private final HTMLTemplateFragment _template;

	private List<FormMember> _parts = null;

	private PartUpdater _partUpdate = null;

	private ControlProvider _controlProvider;

	static final String SCOPE = "templateScope";

	static final String MODEL = "templateModel";

	/**
	 * Creates a {@link TemplateControl}.
	 * 
	 * @param model
	 *        The top-level {@link FormMember} to render with the given template.
	 * @param controlProvider
	 *        The {@link ControlProvider} to use for {@link FormMember}s referenced in the given
	 *        template.
	 * @param template
	 *        The template to render.
	 */
	public TemplateControl(FormMember model, ControlProvider controlProvider, HTMLTemplateFragment template) {
		super(model, COMMANDS);
		_controlProvider = controlProvider;
		_template = ensureTechnicalAttributes(template);
	}

	private static HTMLTemplateFragment ensureTechnicalAttributes(HTMLTemplateFragment template) {
		if (!(template instanceof TagTemplate)) {
			return template;
		}

		TagTemplate tag = (TagTemplate) template;
		StartTagTemplate customStart = tag.getStart();
		StartTagTemplate syntheticsStart = customStart.copy();
		syntheticsStart.addAttribute(ID_EXPR_ATTR);
		boolean hasClassAttr = false;
		for (TagAttributeTemplate attr : customStart.getAttributes()) {
			switch (attr.getName()) {
				case HTMLConstants.ID_ATTR:
					// Added explicitly above.
					break;
				case HTMLConstants.CLASS_ATTR:
					hasClassAttr = true;
					syntheticsStart.addAttribute(
						new CssTagAttributeTemplate(attr.getLine(), attr.getColumn(), HTMLConstants.CLASS_ATTR,
							Templates.fragment(CSS_CLASS_EXPR, attr.getContent())));
					break;
				default:
					syntheticsStart.addAttribute(attr);
			}
		}
		if (!hasClassAttr) {
			syntheticsStart.addAttribute(CSS_CLASS_EXPR_ATTR);
		}

		return new TagTemplate(syntheticsStart, tag.getContent());
	}

	/**
	 * The {@link FormMember} to display.
	 */
	@TemplateVariable(MODEL)
	public FormMember getFormMember() {
		return super.getModel();
	}

	/**
	 * Access to the inner-most {@link TemplateControl} currently rendering a part of a form.
	 */
	@TemplateVariable(SCOPE)
	public TemplateControl getTemplateScope() {
		return this;
	}

	@Override
	protected String buildInputId() {
		return null;
	}

	/**
	 * The default {@link ControlProvider} to use, if a field has none attached.
	 */
	public ControlProvider getControlProvider() {
		return _controlProvider;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		this.writeTopLevelTag(context, out);
	}

	void writeTopLevelTag(DisplayContext context, TagWriter out) throws IOException {
		assert _parts == null || _parts.isEmpty() : "Not cleaned up when writing top-level tag.";

		_template.write(context, out, this);
	}

	void addPart(FormMember embeddedMember) {
		if (_parts == null) {
			_parts = new ArrayList<>();
			_partUpdate = new PartUpdater();
		}
		_parts.add(embeddedMember);
		_partUpdate.registerAt(embeddedMember);
	}

	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();
	
		if (_parts != null) {
			for (FormMember part : _parts) {
				_partUpdate.deregisterFrom(part);
			}
			_parts.clear();
		}
	}
	
	@Override
	protected void registerListener(FormMember member) {
		super.registerListener(member);
		member.addListener(Collapsible.COLLAPSED_PROPERTY, this);
	}

	@Override
	protected void afterRendering() {
		super.afterRendering();

		// Note: While rendering a template, inner form fields may be built by expanding embedded
		// forms. Therefore, a template control must not start listening for changes to the form
		// hierarchy before its rendering process has completed.
		FormMember member = getModel();
		member.addListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		member.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
	}

	@Override
	protected void deregisterListener(FormMember member) {
		member.removeListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
		member.removeListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		member.removeListener(Collapsible.COLLAPSED_PROPERTY, this);
		super.deregisterListener(member);
	}

	@Override
	public Bubble memberAdded(FormContainer parent, FormMember member) {
		return repaintOnEvent(parent);
	}

	@Override
	public Bubble memberRemoved(FormContainer parent, FormMember member) {
		return repaintOnEvent(parent);
	}

	@Override
	public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent(collapsible);
	}

	final class PartUpdater implements RemovedListener, VisibilityListener, MemberChangedListener, CollapsedListener {

		@Override
		public Bubble handleRemovedFromParent(FormMember sender, FormContainer formerParent) {
			return repaintOnPartChange(sender);
		}

		private Bubble repaintOnPartChange(FormMember part) {
			if (TemplateControl.this._parts.contains(part)) {
				/* The TemplateControl renders the given part. As it has changed, the control must
				 * be repainted. */
				TemplateControl.this.requestRepaint();
			}
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
			return repaintOnPartChange((FormMember) sender);
		}

		@Override
		public Bubble memberAdded(FormContainer parent, FormMember member) {
			return repaintOnPartChange(parent);
		}

		@Override
		public Bubble memberRemoved(FormContainer parent, FormMember member) {
			return repaintOnPartChange(parent);
		}

		@Override
		public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
			return repaintOnPartChange((FormMember) collapsible);
		}

		void registerAt(FormMember part) {
			part.addListener(FormMember.REMOVED_FROM_PARENT, this);
			part.addListener(FormMember.VISIBLE_PROPERTY, this);
			part.addListener(Collapsible.COLLAPSED_PROPERTY, this);
			part.addListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
			part.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
		}

		void deregisterFrom(FormMember part) {
			part.removeListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
			part.removeListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
			part.removeListener(Collapsible.COLLAPSED_PROPERTY, this);
			part.removeListener(FormMember.VISIBLE_PROPERTY, this);
			part.removeListener(FormMember.REMOVED_FROM_PARENT, this);

		}

	}

}