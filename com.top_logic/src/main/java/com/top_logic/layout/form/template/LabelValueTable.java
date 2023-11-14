/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import static com.top_logic.layout.form.template.FormTemplateConstants.*;

import java.io.IOException;
import java.util.Iterator;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.MemberChangedListener;

/**
 * {@link ControlProvider} that renders elements of a {@link FormContainer} as two-column table with
 * labels in the first column and input elements in the second column.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelValueTable extends FormControlProviderAdapter {

	private static class View extends ConstantControl<FormContainer> implements MemberChangedListener {

		private final ControlProvider _cp;

		View(ControlProvider cp, FormContainer model) {
			super(model);

			_cp = cp;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(TABLE);
			writeControlAttributes(context, out);
			out.endBeginTag();
			{
				out.beginBeginTag(TBODY);
				out.endBeginTag();
				{
					out.beginBeginTag(TR);
					out.endBeginTag();
					{
						for (Iterator<? extends FormMember> it = getModel().getMembers(); it.hasNext();) {
							FormMember member = it.next();

							out.beginBeginTag(TD);
							out.writeAttribute(CLASS_ATTR, "label");
							out.endBeginTag();
							{
								_cp.createFragment(member, STYLE_LABEL_VALUE).write(context, out);
							}
							out.endTag(TD);

							out.beginBeginTag(TD);
							out.writeAttribute(CLASS_ATTR, "value");
							out.endBeginTag();
							{
								_cp.createFragment(member).write(context, out);
								out.writeText(NBSP);
								_cp.createFragment(member, STYLE_ERROR_VALUE).write(context, out);
							}
						}
						out.endTag(TD);
					}
					out.endTag(TR);
				}
				out.endTag(TBODY);
			}
			out.endTag(TABLE);
		}

		@Override
		protected void internalAttach() {
			super.internalAttach();
			getModel().addListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
			getModel().addListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		}

		@Override
		protected void internalDetach() {
			getModel().removeListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
			getModel().removeListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
			super.internalDetach();
		}

		@Override
		public Bubble memberAdded(FormContainer parent, FormMember member) {
			requestRepaint();
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble memberRemoved(FormContainer parent, FormMember member) {
			requestRepaint();
			return Bubble.BUBBLE;
		}
	}

	/**
	 * Singleton {@link LabelValueTable} instance.
	 */
	public static final LabelValueTable DEFAULT_INSTANCE =
		new LabelValueTable(DefaultFormFieldControlProvider.INSTANCE);

	/**
	 * Create a {@link LabelValueTable}.
	 * 
	 * @param inner
	 *        The {@link ControlProvider} to use for inner field elements.
	 */
	public LabelValueTable(ControlProvider inner) {
		super(inner);
	}

	@Override
	public Control visitFormContainer(FormContainer member, String arg) {
		return new View(getFallback(), member);
	}

}
