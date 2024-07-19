/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMember.IDUsage;
import com.top_logic.layout.form.LabelChangedListener;
import com.top_logic.layout.form.MandatoryChangedListener;
import com.top_logic.layout.form.TooltipChangedListener;
import com.top_logic.layout.form.model.AbstractFormMember;
import com.top_logic.layout.form.model.OptionsListener;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.tag.LabelTag;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link Control} displaying the label of a {@link FormMember}.
 * 
 * @see FormMember#getLabel()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelControl extends AbstractFormMemberControl implements OptionsListener, LabelChangedListener,
		TooltipChangedListener, DisabledPropertyListener, MandatoryChangedListener {

	/** Colon String written after the label (if requested). */
	public static final String COLON = ":";

	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		AbstractFormMemberControl.COMMANDS,
		new ControlCommand[] { LabelInspector.INSTANCE });

	private boolean colon;
	
	private boolean hasIndex;
	
	/**
	 * @see #setIndex(int)
	 */
	private int index;
	
	/**
	 * @see #setOption(Object)
	 */
	private Object option;

	public LabelControl(FormMember model) {
		super(model, COMMANDS);
	}
	
	@Override
	protected void registerListener(FormMember member) {
		super.registerListener(member);
		member.addListener(SelectField.OPTIONS_PROPERTY, this);
		member.addListener(FormMember.LABEL_PROPERTY, this);
		member.addListener(FormMember.TOOLTIP_PROPERTY, this);
		member.addListener(FormMember.DISABLED_PROPERTY, this);
		member.addListener(FormField.MANDATORY_PROPERTY, this);
	}

	@Override
	protected void deregisterListener(FormMember member) {
		member.removeListener(FormMember.DISABLED_PROPERTY, this);
		member.removeListener(FormMember.TOOLTIP_PROPERTY, this);
		member.removeListener(FormMember.LABEL_PROPERTY, this);
		member.removeListener(SelectField.OPTIONS_PROPERTY, this);
		member.removeListener(FormField.MANDATORY_PROPERTY, this);
		super.deregisterListener(member);
	}

	/**
	 * Sets the option index of a select option label.
	 * 
	 * @see #setOption(Object) for an alternative way of specifying a select option label.
	 * 
	 * @param index
	 *        Index of the option in the {@link SelectField#getOptions()} list
	 *        that is represented by this label.
	 */
	public void setIndex(int index) {
		if (option != null) throw illegalState();
		this.index = index;
		this.hasIndex = true;
		
		requestRepaint();
	}
	
	/**
	 * Sets the option of this select option label.
	 * 
	 * @see #setIndex(int) for an alternative way of specifying a select option label.
	 * 
	 * @param option
	 *        Option from the {@link SelectField#getOptions()} list that is
	 *        represented by this label.
	 */
	public void setOption(Object option) {
		if (hasIndex) throw illegalState();
		this.option = option;
		
		requestRepaint();
	}
	
	public void setColon(boolean colon) {
		this.colon = colon;
	}
	
	private static IllegalStateException illegalState() {
		return new IllegalStateException("Only one property 'option' or 'index' may be present.");
	}

	@Override
	protected String getTypeCssClass() {
		return "cLabel";
	}

	@Override
	protected String buildInputId() {
		return LabelTag.inputIdFor(getModel(), getFrameScope(), IDUsage.LABEL, hasIndex, index, option);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		FormMember field = getModel();

		if (! field.isVisible()) {
			writeInvisibleLabel(context, out);
	        
	        return;
		}
		
		
		String label;
		if (hasIndex || option != null) {
			if (! (field instanceof SelectField)) {
				throw new IllegalArgumentException(
					"An indexed label requires a select field as model.");
			}
			SelectField selectField = (SelectField) field;
			Object theOption = (option != null) ? option : selectField.getOptions().get(index);
			label = selectField.getOptionLabel(theOption);
		} else {
			label = field.getLabel();
			if (!label.isEmpty() && this.colon) {
				label += COLON;
			}
		}

		if (label.isEmpty()) {
			writeInvisibleLabel(context, out);

			return;
		}

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			int shortcutIndex = label.indexOf("&&");
			char accessKey = 0;
			if (shortcutIndex >= 0) {
				if (label.length() >= shortcutIndex + 3) {
					accessKey = label.charAt(shortcutIndex + 2);
					if (accessKey == '&') {
						// Accept "&&&" as quote for "&&"
						accessKey = 0;
						label = label.substring(0, shortcutIndex + 2) + label.substring(shortcutIndex + 3);
					}
				}
			}
		
			out.beginBeginTag(LABEL);
			writeStyle(out);
			writeForAttr(out);

			if (field.isDisabled() && !field.isImmutable()) {
				out.writeAttribute(CLASS_ATTR, DISABLED_DISABLED_VALUE);
			}

			if (accessKey != 0) {
				out.writeAttribute(ACCESSKEY_ATTR, accessKey);
			}

			// TODO: Remove spurious cast by pulling up "tooltip" to FormMember.
			if (field instanceof AbstractFormMember) {
				String tooltip = ((AbstractFormMember) field).getTooltip();
				if (!StringServices.isEmpty(tooltip)) {
					String tooltipCaption = ((AbstractFormMember) field).getTooltipCaption();
					OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip,
						tooltipCaption);
				}
			}

			out.endBeginTag();
			if (accessKey != 0) {
				out.writeText(label.substring(0, shortcutIndex));
				
				out.beginBeginTag(SPAN);
				out.writeAttribute(CLASS_ATTR, FormConstants.ACCESSKEY_CSS_CLASS);
				out.endBeginTag();
				{
					out.writeText(Character.toString(accessKey));
				}
				out.endTag(SPAN);
				
				out.writeText(label.substring(shortcutIndex + 3, label.length()));
			} else {
				out.writeText(label);
			}
			
			out.endTag(LABEL);
		}
		out.endTag(SPAN);
	}

	private void writeInvisibleLabel(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.writeAttribute(STYLE_ATTR, "display: none;");
		out.endBeginTag();
		out.endTag(SPAN);
	}

	private void writeForAttr(TagWriter out) throws IOException {
		out.beginAttribute(FOR_ATTR);
		appendInputId(out);
		out.endAttribute();
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		
		// Optionally add mandatory class.
		FormMember targetMember = getModel();
		if (targetMember instanceof FormField) {
			final FormField targetField = (FormField) targetMember;
			if (targetField.isActive()) {
				if (targetField.isMandatory()) {
					out.append(FormConstants.MANDATORY_CSS_CLASS);
				}
				out.append(FormConstants.CAN_EDIT_CSS_CLASS);
			}
		}
	}

	@Override
	public Bubble handleCSSClassChange(Object sender, String oldValue, String newValue) {
		return repaintOnEvent(sender);
	}

	@Override
	public Bubble handleOptionsChanged(SelectField sender) {
		return repaintOnEvent(sender);
	}

	@Override
	public Bubble handleLabelChanged(Object sender, String oldLabel, String newLabel) {
		return repaintOnEvent(sender);
	}

	@Override
	public Bubble handleTooltipChanged(Object sender, String oldValue, String newValue) {
		return repaintOnEvent(sender);
	}

	@Override
	public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent(sender);
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent(sender);
	}
}
