/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOException;
import java.util.Iterator;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.fragments.Fragments.Attribute;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link AbstractFormMemberControl} displaying a {@link FormContainer} by applying a template to
 * each member of the rendered container.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ForeachControl extends AbstractFormMemberControl implements MemberChangedListener {

	static final Property<Binding> CURRENT_MEMBER = TypedAnnotatable.property(Binding.class, "currentMember");

	private final Tag _content;

	/**
	 * @see Fragments#foreach(FormContainer, Tag)
	 */
	protected ForeachControl(FormContainer container, Tag content) {
		super(container, COMMANDS);
		_content = content;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		FormMember model = getModel();
		model.addListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		model.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
	}

	@Override
	protected void internalDetach() {
		FormMember model = getModel();
		model.removeListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		model.removeListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);

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

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);

		for (Attribute attribute : _content.getAttributes()) {
			String name = attribute.getName();
			if (name.equals(HTMLConstants.ID_ATTR)) {
				continue;
			}
			if (name.equals(HTMLConstants.CLASS_ATTR)) {
				continue;
			}
		}
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);

		for (Attribute attribute : _content.getAttributes()) {
			String name = attribute.getName();
			if (name.equals(HTMLConstants.CLASS_ATTR)) {
				out.append(attribute.getValue());
				break;
			}
		}
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(_content.getTagName());
		writeControlAttributes(context, out);
		out.endBeginTag();

		writeInScope(context, out, getModel(), _content.getContent());

		out.endTag(_content.getTagName());
	}

	static abstract class AbstractLoop implements HTMLFragment {

		private final String _bindingName;

		public AbstractLoop(String bindingName) {
			_bindingName = bindingName;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			Binding outer = context.get(CURRENT_MEMBER);
			if (outer == null) {
				throw new IllegalStateException("Loop template outside a foreach control.");
			}

			Binding current = bind(context, _bindingName, null);
			try {
				FormContainer container = (FormContainer) outer.getMember();
				ContainerRenderer renderer = renderer();
				for (Iterator<? extends FormMember> it = renderer.begin(context, out, container); it.hasNext();) {
					FormMember member = it.next();
					current.setMember(member);
					
					renderer.write(context, out, member);
				}
				renderer.end(context, out, container);
			} finally {
				current.unbind(context);
			}
		}

		protected abstract ContainerRenderer renderer();

	}

	static class Loop extends AbstractLoop implements ContainerRenderer {

		private final Tag _template;

		public Loop(String bindingName, Tag template) {
			super(bindingName);
			_template = template;
		}

		@Override
		protected ContainerRenderer renderer() {
			return this;
		}

		@Override
		public Iterator<? extends FormMember> begin(DisplayContext context, TagWriter out, FormContainer container) {
			return container.getMembers();
		}

		@Override
		public void write(DisplayContext context, TagWriter out, FormMember member) throws IOException {
			Fragments.conditional(member, _template).write(context, out);
		}

		@Override
		public void end(DisplayContext context, TagWriter out, FormContainer container) {
			// Ignore.
		}

	}

	static void writeInScope(DisplayContext context, TagWriter out, FormMember member, HTMLFragment... contents)
			throws IOException {
		Binding binding = bind(context, null, member);
		try {
			writeContents(context, out, contents);
		} finally {
			binding.unbind(context);
		}
	}

	static Binding bind(DisplayContext context, String name, FormMember member) {
		Binding current = new Binding(name, member);
		Binding outer = context.set(CURRENT_MEMBER, current);
		current.setOuter(outer);
		return current;
	}

	static void writeContents(DisplayContext context, TagWriter out, HTMLFragment... contents)
			throws IOException {
		for (HTMLFragment content : contents) {
			content.write(context, out);
		}
	}

	abstract static class Reference implements HTMLFragment {

		private String _varName;

		private final String _fieldName;

		public Reference(String name) {
			if (name.startsWith("$")) {
				int end = 1;
				while (true) {
					if (end >= name.length()) {
						end = -1;
						break;
					}
					char ch = name.charAt(end);
					if (ch == '.' || ch == '/') {
						break;
					}
					end++;
				}

				if (end < 0) {
					_varName = name.substring(1);
					_fieldName = ".";
				} else {
					_varName = name.substring(1, end);
					_fieldName = name.substring(end + 1);
				}
			} else {
				_varName = null;
				_fieldName = name;
			}
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			renderer().write(context, out, resolve(context));
		}

		protected abstract MemberRenderer renderer();

		private FormMember resolve(DisplayContext context) {
			Binding binding = context.get(CURRENT_MEMBER);
			if (binding == null) {
				throw new IllegalStateException("Not within an foreach control, member '" + _fieldName + "' not found.");
			}

			if (_varName != null) {
				while (!_varName.equals(binding.getName())) {
					binding = binding.getOuter();

					if (binding == null) {
						throw new IllegalStateException("No binding found for variable '" + _varName + "'.");
					}
				}
			}

			return FormGroup.getMemberByRelativeName(binding.getMember(), _fieldName);
		}

	}

	abstract static class ReferenceRenderer extends Reference implements MemberRenderer {

		public ReferenceRenderer(String fieldName) {
			super(fieldName);
		}

		@Override
		protected MemberRenderer renderer() {
			return this;
		}

	}

	static class Scope extends ReferenceRenderer {

		private final HTMLFragment[] _contents;

		public Scope(String fieldName, HTMLFragment... contents) {
			super(fieldName);
			_contents = contents;
		}

		@Override
		public void write(DisplayContext context, TagWriter out, FormMember member) throws IOException {
			writeInScope(context, out, member, _contents);
		}

	}

	static class Binding {

		private final String _name;

		private FormMember _member;

		private Binding _outer;

		public Binding(String name, FormMember member) {
			_name = name;
			_member = member;
		}

		public String getName() {
			return _name;
		}

		public Binding getOuter() {
			return _outer;
		}

		public void setOuter(Binding outer) {
			_outer = outer;
		}

		public FormMember getMember() {
			return _member;
		}

		public void setMember(FormMember member) {
			_member = member;
		}

		void unbind(DisplayContext context) {
			Binding before = context.set(ForeachControl.CURRENT_MEMBER, _outer);
			assert this == before;
		}

	}

}
