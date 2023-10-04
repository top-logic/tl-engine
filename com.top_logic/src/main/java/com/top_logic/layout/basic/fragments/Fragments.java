/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

/**
 * Factory for common {@link HTMLFragment} types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Fragments {

	/**
	 * The name of the currently rendered {@link FormMember} in the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 */
	public static final String CURRENT_MEMBER = ".";

	private static HTMLFragment NBSP = text(HTMLConstants.NBSP);

	/**
	 * The empty fragment.
	 */
	public static HTMLFragment empty() {
		return EmptyFragment.getInstance();
	}

	/**
	 * A text fragment.
	 * 
	 * @param text
	 *        The text to render, <code>null</code> means {@link #empty()}.
	 */
	public static HTMLFragment text(String text) {
		if (text == null || text.isEmpty()) {
			return empty();
		}
		return TextFragment.createTextFragment(text);
	}

	/**
	 * A non-breaking space.
	 */
	public static HTMLFragment nbsp() {
		return NBSP;
	}

	/**
	 * A resource message text fragment.
	 * 
	 * @param messageKey
	 *        The text to render, <code>null</code> means {@link #empty()}.
	 * 
	 * @see Resources#getString(ResKey)
	 */
	public static HTMLFragment message(ResKey messageKey) {
		if (messageKey == null) {
			return empty();
		}
		return MessageFragment.createMessageFragment(messageKey);
	}

	/**
	 * A fragment with a list of message texts.
	 * 
	 * @param messages
	 *        The texts to render, <code>null</code> or empty means {@link #empty()}.
	 * @return {@link HTMLFragment} containing the {@link List}
	 */
	public static HTMLFragment messageList(List<ResKey> messages) {
		if (messages == null || messages.isEmpty()) {
			return empty();
		}
		Tag[] liElements = new Tag[messages.size()];
		for (int i = 0; i < messages.size(); i++) {
			liElements[i] = li(message(messages.get(i)));
		}
		HTMLFragment list = ul(liElements);
		return list;
	}

	/**
	 * A fragment with list of message texts.
	 * 
	 * @param messages
	 *        The texts to render, <code>null</code> means {@link #empty()}.
	 * @return {@link HTMLFragment} containing the {@link List}
	 */
	public static HTMLFragment messageList(ResKey... messages) {
		return messageList(Arrays.asList(messages));
	}

	/**
	 * Returns a concatenated fragment that first writes the {@code before} fragment, and then
	 * writes the {@code after} fragment. If evaluation of either fragment throws an exception, it
	 * is relayed to the caller of the concatenated fragment.
	 *
	 * @param before
	 *        The fragment to write first.
	 * @param after
	 *        The fragment to write second.
	 * @return A concatenated fragment that first writes the {@code before} fragment and then writes
	 *         {@code after} fragment.
	 */
	public static HTMLFragment concat(HTMLFragment before, HTMLFragment after) {
		return (context, out) -> {
			before.write(context, out);
			after.write(context, out);
		};
	}

	/**
	 * Concatenation of the given fragments.
	 * 
	 * @param fragments
	 *        The other fragments to concatenate, <code>null</code> means {@link #empty()}.
	 */
	public static HTMLFragment concat(HTMLFragment... fragments) {
		if (fragments == null || fragments.length == 0) {
			return empty();
		}
		if (fragments.length == 1) {
			return fragments[0];
		}
		return new ConcatenatedFragment(fragments);
	}


	/**
	 * Wraps the given {@link XMLTag} around the given {@link HTMLFragment}.
	 * 
	 * @param tag
	 *        The outer tag, or <code>null</code> for not wrapping.
	 * @param content
	 *        The contents, or <code>null</code> for no content.
	 * @return The new {@link HTMLFragment}.
	 */
	public static HTMLFragment wrap(final XMLTag tag, final HTMLFragment content) {
		if (content == null) {
			return toFragment(tag);
		} else if (tag == null) {
			return content;
		} else {
			return new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					tag.beginBeginTag(context, out);
					tag.endBeginTag(context, out);
					content.write(context, out);
					tag.endBeginTag(context, out);
				}
			};
		}
	}

	/**
	 * Converts the given {@link XMLTag} to an {@link HTMLFragment} with empty content.
	 * 
	 * @param tag
	 *        The tag to convert.
	 * @return The new {@link HTMLFragment}.
	 */
	public static HTMLFragment toFragment(final XMLTag tag) {
		if (tag == null) {
			return empty();
		} else {
			return new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					tag.beginBeginTag(context, out);
					tag.endEmptyTag(context, out);
				}
			};
		}
	}

	/**
	 * A conditional display.
	 * 
	 * <p>
	 * The given display tag is only rendered, if the {@link VisibilityModel} is visible. In
	 * invisible state, only the top-level tag without any attributes is rendered as handle for
	 * content replacement, if the model becomes visible later.
	 * </p>
	 * 
	 * @param model
	 *        The {@link VisibilityModel} that controls the display state.
	 * @param display
	 *        The content that is conditionally displayed.
	 * @return A display that dynamically changes its visibility.
	 */
	public static Control conditional(VisibilityModel model, Tag display) {
		return new ConditionalDisplayControl(model, display);
	}

	/**
	 * Display the label of the given field.
	 * 
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #labelWithColon(ControlProvider, FormMember)
	 */
	public static Control label(FormMember field) {
		return label(DefaultFormFieldControlProvider.INSTANCE, field);
	}

	/**
	 * Display the label of the given field.
	 * 
	 * @param cp
	 *        An externally specified {@link ControlProvider}.
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #labelWithColon(ControlProvider, FormMember)
	 */
	public static Control label(ControlProvider cp, FormMember field) {
		return view(cp, field, FormTemplateConstants.STYLE_LABEL_VALUE);
	}

	/**
	 * Display the label of the given field appending a colon.
	 * 
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #labelWithColon(ControlProvider, FormMember)
	 */
	public static Control labelWithColon(FormMember field) {
		return labelWithColon(DefaultFormFieldControlProvider.INSTANCE, field);
	}

	/**
	 * Display the label of the given field appending a colon.
	 * 
	 * @param cp
	 *        An externally specified {@link ControlProvider}.
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #labelWithColon(ControlProvider, FormMember)
	 */
	public static Control labelWithColon(ControlProvider cp, FormMember field) {
		return cp.createControl(field, FormTemplateConstants.STYLE_LABEL_WITH_COLON_VALUE);
	}

	/**
	 * Display the input aspect of the given field.
	 * 
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #valueWithError(ControlProvider, FormMember)
	 */
	public static Control value(FormMember field) {
		return value(DefaultFormFieldControlProvider.INSTANCE, field);
	}

	/**
	 * Display the input aspect of the given field.
	 * 
	 * @param cp
	 *        An externally specified {@link ControlProvider}.
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #valueWithError(ControlProvider, FormMember)
	 */
	public static Control value(ControlProvider cp, FormMember field) {
		return view(cp, field, null);
	}

	/**
	 * Display the error aspect of the given field.
	 * 
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #value(ControlProvider, FormMember)
	 */
	public static Control error(FormMember field) {
		return error(DefaultFormFieldControlProvider.INSTANCE, field);
	}

	/**
	 * Display the error aspect of the given field.
	 * 
	 * @param cp
	 *        An externally specified {@link ControlProvider}.
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #value(ControlProvider, FormMember)
	 */
	public static Control error(ControlProvider cp, FormMember field) {
		return view(cp, field, FormTemplateConstants.STYLE_ERROR_VALUE);
	}

	/**
	 * Displays the given aspect of the given member.
	 * 
	 * @param member
	 *        The {@link FormMember} whose label should be displayed.
	 * @param style
	 *        The aspect to display, see {@link FormTemplateConstants#STYLE_LABEL_VALUE}.
	 * @return A {@link Control} displaying the requested aspect.
	 * 
	 * @see #view(ControlProvider, FormMember, String)
	 */
	public static Control view(FormMember member, String style) {
		return view(DefaultFormFieldControlProvider.INSTANCE, member, style);
	}

	/**
	 * Displays the given aspect of the given member using the given {@link ControlProvider}.
	 * 
	 * @param cp
	 *        The {@link ControlProvider} to use.
	 * @param member
	 *        The {@link FormMember} whose label should be displayed.
	 * @param style
	 *        The aspect to display, see {@link FormTemplateConstants#STYLE_LABEL_VALUE}.
	 * @return A {@link Control} displaying the requested aspect.
	 * 
	 * @see #view(ControlProvider, FormMember, String)
	 */
	public static Control view(ControlProvider cp, FormMember member, String style) {
		return cp.createControl(member, style);
	}

	/**
	 * Display the value of the given field with the error display appended.
	 * 
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #value(ControlProvider, FormMember)
	 */
	public static Control valueWithError(FormMember field) {
		return valueWithError(DefaultFormFieldControlProvider.INSTANCE, field);
	}

	/**
	 * Display the value of the given field with the error display appended.
	 * 
	 * @param cp
	 *        An externally specified {@link ControlProvider}.
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #value(ControlProvider, FormMember)
	 */
	public static Control valueWithError(ControlProvider cp, FormMember field) {
		return conditional(field, span(value(cp, field), error(cp, field)));
	}

	/**
	 * Display the value of the given field with the error display prepended.
	 * 
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #valueWithError(ControlProvider, FormMember)
	 */
	public static Control errorWithValue(FormMember field) {
		return errorWithValue(DefaultFormFieldControlProvider.INSTANCE, field);
	}

	/**
	 * Display the value of the given field with the error display prepended.
	 * 
	 * @param cp
	 *        An externally specified {@link ControlProvider}.
	 * @param field
	 *        The {@link FormMember} whose label should be displayed.
	 * @return A {@link Control} displaying the label.
	 * 
	 * @see #valueWithError(ControlProvider, FormMember)
	 */
	public static Control errorWithValue(ControlProvider cp, FormMember field) {
		return conditional(field, span(error(cp, field), value(cp, field)));
	}

	/**
	 * Creates a {@link ForeachControl}.
	 * 
	 * @param container
	 *        The {@link FormContainer} to display.
	 * @param content
	 *        The root tag rendered for displaying the whole container.
	 */
	public static Control foreach(FormContainer container, Tag content) {
		return new ForeachControl(container, content);
	}

	/**
	 * Expands the given {@link Tag} for each member of the {@link FormContainer} currently iterated
	 * through in an enclosing {@link #foreach(FormContainer, Tag)}.
	 * 
	 * @param content
	 *        The {@link Tag} to render for each iterated {@link FormMember}.
	 */
	public static HTMLFragment loop(Tag content) {
		return loop(null, content);
	}

	/**
	 * Expands the given {@link Tag} for each member of the {@link FormContainer} currently iterated
	 * through in an enclosing {@link #foreach(FormContainer, Tag)}.
	 * 
	 * @param bindingName
	 *        The name to bind each member to. If the member is bound to the name <code>x</code>,
	 *        the reference <code>$x</code> can be used as first part of the relative name given in
	 *        an enclosing {@link #refValue(String)} fragment. This results in resolving this
	 *        reference relatively to the iterated member.
	 * @param content
	 *        The {@link Tag} to render for each iterated {@link FormMember}.
	 */
	public static HTMLFragment loop(String bindingName, Tag content) {
		return new ForeachControl.Loop(bindingName, content);
	}

	/**
	 * Renders each member of the {@link FormContainer} currently iterated through in an enclosing
	 * {@link #foreach(FormContainer, Tag)}.
	 * 
	 * @param renderer
	 *        The {@link ContainerRenderer} to use for implementing the iteration.
	 */
	public static HTMLFragment loop(ContainerRenderer renderer) {
		return loop(null, renderer);
	}

	/**
	 * Renders each member of the {@link FormContainer} currently iterated through in an enclosing
	 * {@link #foreach(FormContainer, Tag)}.
	 * 
	 * @param bindingName
	 *        The name to bind each member to. If the member is bound to the name <code>x</code>,
	 *        the reference <code>$x</code> can be used as first part of the relative name given in
	 *        an enclosing {@link #refValue(String)} fragment. This results in resolving this
	 *        reference relatively to the iterated member.
	 * @param renderer
	 *        The {@link ContainerRenderer} to use for implementing the iteration.
	 */
	public static HTMLFragment loop(String bindingName, final ContainerRenderer renderer) {
		return new ForeachControl.AbstractLoop(bindingName) {
			@Override
			protected ContainerRenderer renderer() {
				return renderer;
			}
		};
	}

	/**
	 * Changes the scope (the current member) to the member with the given name.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        Relative name of the {@link FormMember} to be used as {@link #CURRENT_MEMBER current
	 *        member} in the contents of this scope fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @param content
	 *        The content to be rendered in the changed scope.
	 * @return A member template fragment.
	 */
	public static HTMLFragment refScope(String fieldName, HTMLFragment... content) {
		return new ForeachControl.Scope(fieldName, content);
	}

	/**
	 * Renders the label of the {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @return A member template fragment.
	 */
	public static HTMLFragment refLabel() {
		return refLabel(CURRENT_MEMBER);
	}

	/**
	 * Renders the label of the member referenced by the given name relative to the
	 * {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        The name relative to the {@link #CURRENT_MEMBER current member} in a
	 *        {@link #foreach(FormContainer, Tag) foreach} fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @return A member template fragment.
	 */
	public static HTMLFragment refLabel(String fieldName) {
		return refView(fieldName, FormTemplateConstants.STYLE_LABEL_VALUE);
	}

	/**
	 * Renders the value of the {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @return A member template fragment.
	 */
	public static HTMLFragment refValue() {
		return refValue(CURRENT_MEMBER);
	}

	/**
	 * Renders the value of the {@link #CURRENT_MEMBER current member} using the given
	 * {@link ControlProvider}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param cp
	 *        The {@link ControlProvider} to use for creating the view.
	 * @return A member template fragment.
	 */
	public static HTMLFragment refValue(ControlProvider cp) {
		return refValue(CURRENT_MEMBER, cp);
	}

	/**
	 * Renders the value of the member referenced by the given name relative to the
	 * {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        The name relative to the {@link #CURRENT_MEMBER current member} in a
	 *        {@link #foreach(FormContainer, Tag) foreach} fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @return A member template fragment.
	 */
	public static HTMLFragment refValue(String fieldName) {
		return refView(fieldName, null);
	}

	/**
	 * Renders the value of the member referenced by the given name relative to the
	 * {@link #CURRENT_MEMBER current member} using the given {@link ControlProvider}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        The name relative to the {@link #CURRENT_MEMBER current member} in a
	 *        {@link #foreach(FormContainer, Tag) foreach} fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @param cp
	 *        The {@link ControlProvider} to use for creating the view.
	 * @return A member template fragment.
	 */
	public static HTMLFragment refValue(String fieldName, ControlProvider cp) {
		return refView(fieldName, cp, null);
	}

	/**
	 * Renders the error of the {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @return A member template fragment.
	 */
	public static HTMLFragment refError() {
		return refError(CURRENT_MEMBER);
	}

	/**
	 * Renders the error of the member referenced by the given name relative to the
	 * {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        The name relative to the {@link #CURRENT_MEMBER current member} in a
	 *        {@link #foreach(FormContainer, Tag) foreach} fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @return A member template fragment.
	 */
	public static HTMLFragment refError(String fieldName) {
		return refView(fieldName, FormTemplateConstants.STYLE_ERROR_VALUE);
	}

	/**
	 * Renders the given aspect of the member referenced by the given name relative to the
	 * {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        The name relative to the {@link #CURRENT_MEMBER current member} in a
	 *        {@link #foreach(FormContainer, Tag) foreach} fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @param style
	 *        The aspect to render, see {@link FormTemplateConstants#STYLE_LABEL_VALUE}.
	 * @return A member template fragment.
	 */
	public static HTMLFragment refView(String fieldName, String style) {
		return refView(fieldName, DefaultFormFieldControlProvider.INSTANCE, style);
	}

	/**
	 * Renders the given aspect of the member referenced by the given name relative to the
	 * {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        The name relative to the {@link #CURRENT_MEMBER current member} in a
	 *        {@link #foreach(FormContainer, Tag) foreach} fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @param cp
	 *        The {@link ControlProvider} to use.
	 * @param style
	 *        The aspect to render, see {@link FormTemplateConstants#STYLE_LABEL_VALUE}.
	 * @return A member template fragment.
	 */
	public static HTMLFragment refView(String fieldName, final ControlProvider cp, final String style) {
		return new ForeachControl.ReferenceRenderer(fieldName) {
			@Override
			public void write(DisplayContext context, TagWriter out, FormMember member) throws IOException {
				view(cp, member, style).write(context, out);
			}
		};
	}

	/**
	 * Expands a nested foreach template for the {@link #CURRENT_MEMBER current member}.
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        The name relative to the {@link #CURRENT_MEMBER current member} in a
	 *        {@link #foreach(FormContainer, Tag) foreach} fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @param content
	 *        See {@link #foreach(FormContainer, Tag)}
	 * @return A member template fragment.
	 */
	public static HTMLFragment refForeach(String fieldName, final Tag content) {
		return new ForeachControl.ReferenceRenderer(fieldName) {
			@Override
			public void write(DisplayContext context, TagWriter out, FormMember member) throws IOException {
				foreach((FormContainer) member, content).write(context, out);
			}
		};
	}

	/**
	 * Uses a custom {@link MemberRenderer} for rendering the {@link #CURRENT_MEMBER current member}
	 * .
	 * 
	 * <p>
	 * This fragment is to be used within the member template of a
	 * {@link #foreach(FormContainer, Tag)} fragment.
	 * </p>
	 * 
	 * @param fieldName
	 *        The name relative to the {@link #CURRENT_MEMBER current member} in a
	 *        {@link #foreach(FormContainer, Tag) foreach} fragment. See
	 *        {@link FormGroup#getMemberByRelativeName(FormMember, String)}.
	 * @param renderer
	 *        The {@link MemberRenderer} to apply the the referenced member.
	 * @return A member template fragment.
	 */
	public static HTMLFragment ref(String fieldName, final MemberRenderer renderer) {
		return new ForeachControl.ReferenceRenderer(fieldName) {
			@Override
			public void write(DisplayContext context, TagWriter out, FormMember member) throws IOException {
				renderer.write(context, out, member);
			}
		};
	}

	/**
	 * A rendered model.
	 * 
	 * @param renderer
	 *        The {@link Renderer} to use.
	 * @param model
	 *        The model to display.
	 * @return A {@link HTMLFragment} rendering the given model.
	 */
	public static <T> HTMLFragment rendered(final Renderer<? super T> renderer, final T model) {
		return new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				renderer.write(context, out, model);
			}
		};
	}

	/**
	 * A fragment of pre-rendered HTML.
	 * 
	 * <p style="color: red;">
	 * <b>Use only in exceptional cases. Be extremely careful not to introduce XSS weaknesses. The
	 * given HTML source code should be generated with {@link TagWriter}. If this is not possible
	 * think about usage of {@link SafeHTML}.</b>
	 * </p>
	 * 
	 * @see SafeHTML
	 * 
	 * @param html
	 *        The HTML source code, <code>null</code> means {@link #empty()}.
	 * @return The new fragment.
	 */
	public static HTMLFragment htmlSource(String html) {
		if (html == null || html.isEmpty()) {
			return empty();
		}
		return RenderedFragment.createRenderedFragment(html);
	}

	/** Renders a {@link HTMLConstants#STYLE_ELEMENT} tag. */
	public static Tag style(HTMLFragment... content) {
		return plainTag(HTMLConstants.STYLE_ELEMENT, content);
	}

	/** Renders a {@link HTMLConstants#STYLE_ELEMENT} tag using a custom CSS class. */
	public static Tag style(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.STYLE_ELEMENT, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#STYLE_ELEMENT} tag using a custom arguments. */
	public static Tag style(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.STYLE_ELEMENT, attributes, content);
	}

	/** Renders a {@link HTMLConstants#HTML} tag. */
	public static Tag html(HTMLFragment... content) {
		return plainTag(HTMLConstants.HTML, content);
	}

	/** Renders a {@link HTMLConstants#HTML} tag using a custom CSS class. */
	public static Tag html(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.HTML, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#HTML} tag using a custom attributes. */
	public static Tag html(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.HTML, attributes, content);
	}

	/** Renders a {@link HTMLConstants#HEAD} tag. */
	public static Tag head(HTMLFragment... content) {
		return plainTag(HTMLConstants.HEAD, content);
	}

	/** Renders a {@link HTMLConstants#HEAD} tag using a custom CSS class. */
	public static Tag head(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.HEAD, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#HEAD} tag using a custom attributes. */
	public static Tag head(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.HEAD, attributes, content);
	}

	/** Renders a {@link HTMLConstants#TITLE} tag. */
	public static Tag title(HTMLFragment... content) {
		return plainTag(HTMLConstants.TITLE, content);
	}

	/** Renders a {@link HTMLConstants#TITLE} tag using a custom CSS class. */
	public static Tag title(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.TITLE, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#TITLE} tag using a custom attributes. */
	public static Tag title(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.TITLE, attributes, content);
	}

	/** Renders a {@link HTMLConstants#BODY} tag. */
	public static Tag body(HTMLFragment... content) {
		return plainTag(HTMLConstants.BODY, content);
	}

	/** Renders a {@link HTMLConstants#BODY} tag using a custom CSS class. */
	public static Tag body(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.BODY, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#BODY} tag using a custom attributes. */
	public static Tag body(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.BODY, attributes, content);
	}

	/** Renders a {@link HTMLConstants#FRAME} tag. */
	public static Tag frame(HTMLFragment... content) {
		return plainTag(HTMLConstants.FRAME, content);
	}

	/** Renders a {@link HTMLConstants#FRAME} tag using a custom CSS class. */
	public static Tag frame(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.FRAME, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#FRAME} tag using a custom attributes. */
	public static Tag frame(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.FRAME, attributes, content);
	}

	/** Renders a {@link HTMLConstants#FRAMESET} tag. */
	public static Tag frameset(HTMLFragment... content) {
		return plainTag(HTMLConstants.FRAMESET, content);
	}

	/** Renders a {@link HTMLConstants#FRAMESET} tag using a custom CSS class. */
	public static Tag frameset(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.FRAMESET, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#FRAMESET} tag using a custom attributes. */
	public static Tag frameset(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.FRAMESET, attributes, content);
	}

	/** Renders a {@link HTMLConstants#IFRAME} tag. */
	public static Tag iframe(HTMLFragment... content) {
		return plainTag(HTMLConstants.IFRAME, content);
	}

	/** Renders a {@link HTMLConstants#IFRAME} tag using a custom CSS class. */
	public static Tag iframe(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.IFRAME, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#IFRAME} tag using a custom attributes. */
	public static Tag iframe(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.IFRAME, attributes, content);
	}

	/** Renders a {@link HTMLConstants#H1} tag. */
	public static Tag h1(HTMLFragment... content) {
		return plainTag(HTMLConstants.H1, content);
	}

	/** Renders a {@link HTMLConstants#H1} tag using a custom CSS class. */
	public static Tag h1(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.H1, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#H1} tag using a custom CSS class. */
	public static Tag h1(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.H1, attributes, content);
	}

	/** Renders a {@link HTMLConstants#H2} tag. */
	public static Tag h2(HTMLFragment... content) {
		return plainTag(HTMLConstants.H2, content);
	}

	/** Renders a {@link HTMLConstants#H2} tag using a custom CSS class. */
	public static Tag h2(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.H2, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#H2} tag using a custom attributes. */
	public static Tag h2(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.H2, attributes, content);
	}

	/** Renders a {@link HTMLConstants#H3} tag. */
	public static Tag h3(HTMLFragment... content) {
		return plainTag(HTMLConstants.H3, content);
	}

	/** Renders a {@link HTMLConstants#H3} tag using a custom CSS class. */
	public static Tag h3(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.H3, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#H3} tag using a custom attributes. */
	public static Tag h3(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.H3, attributes, content);
	}

	/** Renders a {@link HTMLConstants#H4} tag. */
	public static Tag h4(HTMLFragment... content) {
		return plainTag(HTMLConstants.H4, content);
	}

	/** Renders a {@link HTMLConstants#H4} tag using a custom CSS class. */
	public static Tag h4(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.H4, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#H4} tag using a custom attributes. */
	public static Tag h4(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.H4, attributes, content);
	}

	/** Renders a {@link HTMLConstants#H5} tag. */
	public static Tag h5(HTMLFragment... content) {
		return plainTag(HTMLConstants.H5, content);
	}

	/** Renders a {@link HTMLConstants#H5} tag using a custom CSS class. */
	public static Tag h5(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.H5, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#H5} tag using a custom attributes. */
	public static Tag h5(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.H5, attributes, content);
	}

	/** Renders a {@link HTMLConstants#H6} tag. */
	public static Tag h6(HTMLFragment... content) {
		return plainTag(HTMLConstants.H6, content);
	}

	/** Renders a {@link HTMLConstants#H6} tag using a custom CSS class. */
	public static Tag h6(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.H6, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#H6} tag using a custom attributes. */
	public static Tag h6(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.H6, attributes, content);
	}

	/** Renders a {@link HTMLConstants#PARAGRAPH} tag. */
	public static Tag p(HTMLFragment... content) {
		return plainTag(HTMLConstants.PARAGRAPH, content);
	}

	/** Renders a {@link HTMLConstants#PARAGRAPH} tag using a custom CSS class. */
	public static Tag p(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.PARAGRAPH, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#PARAGRAPH} tag using a custom attributes. */
	public static Tag p(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.PARAGRAPH, attributes, content);
	}

	/** Renders a {@link HTMLConstants#CAPTION} tag. */
	public static Tag caption(HTMLFragment... content) {
		return plainTag(HTMLConstants.CAPTION, content);
	}

	/** Renders a {@link HTMLConstants#CAPTION} tag using a custom CSS class. */
	public static Tag caption(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.CAPTION, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#CAPTION} tag using a custom attributes. */
	public static Tag caption(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.CAPTION, attributes, content);
	}

	/** Renders a {@link HTMLConstants#ADDRESS} tag. */
	public static Tag address(HTMLFragment... content) {
		return plainTag(HTMLConstants.ADDRESS, content);
	}

	/** Renders a {@link HTMLConstants#ADDRESS} tag using a custom CSS class. */
	public static Tag address(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.ADDRESS, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#ADDRESS} tag using a custom attributes. */
	public static Tag address(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.ADDRESS, attributes, content);
	}

	/** Renders a {@link HTMLConstants#BLOCKQUOTE} tag. */
	public static Tag blockquote(HTMLFragment... content) {
		return plainTag(HTMLConstants.BLOCKQUOTE, content);
	}

	/** Renders a {@link HTMLConstants#BLOCKQUOTE} tag using a custom CSS class. */
	public static Tag blockquote(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.BLOCKQUOTE, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#BLOCKQUOTE} tag using a custom attributes. */
	public static Tag blockquote(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.BLOCKQUOTE, attributes, content);
	}

	/** Renders a {@link HTMLConstants#STRONG} tag. */
	public static Tag strong(HTMLFragment... content) {
		return plainTag(HTMLConstants.STRONG, content);
	}

	/** Renders a {@link HTMLConstants#STRONG} tag using a custom CSS class. */
	public static Tag strong(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.STRONG, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#STRONG} tag using a custom attributes. */
	public static Tag strong(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.STRONG, attributes, content);
	}

	/** Renders a {@link HTMLConstants#EM} tag. */
	public static Tag em(HTMLFragment... content) {
		return plainTag(HTMLConstants.EM, content);
	}

	/** Renders a {@link HTMLConstants#EM} tag using a custom CSS class. */
	public static Tag em(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.EM, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#EM} tag using a custom attributes. */
	public static Tag em(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.EM, attributes, content);
	}

	/** Renders a {@link HTMLConstants#ITALICS} tag. */
	public static Tag i(HTMLFragment... content) {
		return plainTag(HTMLConstants.ITALICS, content);
	}

	/** Renders a {@link HTMLConstants#ITALICS} tag using a custom CSS class. */
	public static Tag i(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.ITALICS, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#ITALICS} tag using a custom attributes. */
	public static Tag i(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.ITALICS, attributes, content);
	}

	/** Renders a {@link HTMLConstants#S} tag. */
	public static Tag s(HTMLFragment... content) {
		return plainTag(HTMLConstants.S, content);
	}

	/** Renders a {@link HTMLConstants#S} tag using a custom CSS class. */
	public static Tag s(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.S, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#S} tag using a custom attributes. */
	public static Tag s(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.S, attributes, content);
	}

	/** Renders a {@link HTMLConstants#U} tag. */
	public static Tag u(HTMLFragment... content) {
		return plainTag(HTMLConstants.U, content);
	}

	/** Renders a {@link HTMLConstants#U} tag using a custom CSS class. */
	public static Tag u(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.U, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#U} tag using a custom attributes. */
	public static Tag u(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.U, attributes, content);
	}

	/** Renders a {@link HTMLConstants#SUB} tag. */
	public static Tag sub(HTMLFragment... content) {
		return plainTag(HTMLConstants.SUB, content);
	}

	/** Renders a {@link HTMLConstants#SUB} tag using a custom CSS class. */
	public static Tag sub(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.SUB, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#SUB} tag using a custom attributes. */
	public static Tag sub(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.SUB, attributes, content);
	}

	/** Renders a {@link HTMLConstants#SUP} tag. */
	public static Tag sup(HTMLFragment... content) {
		return plainTag(HTMLConstants.SUP, content);
	}

	/** Renders a {@link HTMLConstants#SUP} tag using a custom CSS class. */
	public static Tag sup(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.SUP, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#SUP} tag using a custom attributes. */
	public static Tag sup(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.SUP, attributes, content);
	}

	/** Renders a {@link HTMLConstants#DIV} tag. */
	public static Tag div(HTMLFragment... content) {
		return plainTag(HTMLConstants.DIV, content);
	}

	/** Renders a {@link HTMLConstants#DIV} tag using a custom CSS class. */
	public static Tag div(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.DIV, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#DIV} tag using a custom attributes. */
	public static Tag div(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.DIV, attributes, content);
	}

	/** Renders a {@link HTMLConstants#PROGRESS} tag. */
	public static Tag progress(HTMLFragment... content) {
		return plainTag(HTMLConstants.PROGRESS, content);
	}

	/** Renders a {@link HTMLConstants#PROGRESS} tag using a custom CSS class. */
	public static Tag progress(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.PROGRESS, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#PROGRESS} tag using a custom attributes. */
	public static Tag progress(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.PROGRESS, attributes, content);
	}

	/** Renders a {@link HTMLConstants#NAV} tag. */
	public static Tag nav(HTMLFragment... content) {
		return plainTag(HTMLConstants.NAV, content);
	}

	/** Renders a {@link HTMLConstants#NAV} tag using a custom CSS class. */
	public static Tag nav(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.NAV, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#NAV} tag using a custom attributes. */
	public static Tag nav(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.NAV, attributes, content);
	}

	/** Renders a {@link HTMLConstants#BASE} tag. */
	public static Tag base() {
		return tagEmpty(HTMLConstants.BASE);
	}

	/** Renders a {@link HTMLConstants#BASE} tag using a custom CSS class. */
	public static Tag base(String cssClass) {
		return tagEmpty(HTMLConstants.BASE, cssClass);
	}

	/** Renders a {@link HTMLConstants#BASE} tag using a custom attributes. */
	public static Tag base(Attribute... attributes) {
		return tagEmpty(HTMLConstants.BASE, attributes);
	}

	/** Renders a {@link HTMLConstants#COMMAND} tag. */
	public static Tag command() {
		return tagEmpty(HTMLConstants.COMMAND);
	}

	/** Renders a {@link HTMLConstants#COMMAND} tag using a custom CSS class. */
	public static Tag command(String cssClass) {
		return tagEmpty(HTMLConstants.COMMAND, cssClass);
	}

	/** Renders a {@link HTMLConstants#COMMAND} tag using a custom attributes. */
	public static Tag command(Attribute... attributes) {
		return tagEmpty(HTMLConstants.COMMAND, attributes);
	}

	/** Renders a {@link HTMLConstants#EMBED} tag. */
	public static Tag embed() {
		return tagEmpty(HTMLConstants.EMBED);
	}

	/** Renders a {@link HTMLConstants#EMBED} tag using a custom CSS class. */
	public static Tag embed(String cssClass) {
		return tagEmpty(HTMLConstants.EMBED, cssClass);
	}

	/** Renders a {@link HTMLConstants#EMBED} tag using a custom attributes. */
	public static Tag embed(Attribute... attributes) {
		return tagEmpty(HTMLConstants.EMBED, attributes);
	}

	/** Renders a {@link HTMLConstants#KEYGEN} tag. */
	public static Tag keygen() {
		return tagEmpty(HTMLConstants.KEYGEN);
	}

	/** Renders a {@link HTMLConstants#KEYGEN} tag using a custom CSS class. */
	public static Tag keygen(String cssClass) {
		return tagEmpty(HTMLConstants.KEYGEN, cssClass);
	}

	/** Renders a {@link HTMLConstants#KEYGEN} tag using a custom attributes. */
	public static Tag keygen(Attribute... attributes) {
		return tagEmpty(HTMLConstants.KEYGEN, attributes);
	}

	/** Renders a {@link HTMLConstants#PARAM} tag. */
	public static Tag param() {
		return tagEmpty(HTMLConstants.PARAM);
	}

	/** Renders a {@link HTMLConstants#PARAM} tag using a custom CSS class. */
	public static Tag param(String cssClass) {
		return tagEmpty(HTMLConstants.PARAM, cssClass);
	}

	/** Renders a {@link HTMLConstants#PARAM} tag using a custom attributes. */
	public static Tag param(Attribute... attributes) {
		return tagEmpty(HTMLConstants.PARAM, attributes);
	}

	/** Renders a {@link HTMLConstants#SOURCE} tag. */
	public static Tag source() {
		return tagEmpty(HTMLConstants.SOURCE);
	}

	/** Renders a {@link HTMLConstants#SOURCE} tag using a custom CSS class. */
	public static Tag source(String cssClass) {
		return tagEmpty(HTMLConstants.SOURCE, cssClass);
	}

	/** Renders a {@link HTMLConstants#SOURCE} tag using a custom attributes. */
	public static Tag source(Attribute... attributes) {
		return tagEmpty(HTMLConstants.SOURCE, attributes);
	}

	/** Renders a {@link HTMLConstants#TRACK} tag. */
	public static Tag track() {
		return tagEmpty(HTMLConstants.TRACK);
	}

	/** Renders a {@link HTMLConstants#TRACK} tag using a custom CSS class. */
	public static Tag track(String cssClass) {
		return tagEmpty(HTMLConstants.TRACK, cssClass);
	}

	/** Renders a {@link HTMLConstants#TRACK} tag using a custom attributes. */
	public static Tag track(Attribute... attributes) {
		return tagEmpty(HTMLConstants.TRACK, attributes);
	}

	/** Renders a {@link HTMLConstants#WBR} tag. */
	public static Tag wbr() {
		return tagEmpty(HTMLConstants.WBR);
	}

	/** Renders a {@link HTMLConstants#WBR} tag using a custom CSS class. */
	public static Tag wbr(String cssClass) {
		return tagEmpty(HTMLConstants.WBR, cssClass);
	}

	/** Renders a {@link HTMLConstants#WBR} tag using a custom attributes. */
	public static Tag wbr(Attribute... attributes) {
		return tagEmpty(HTMLConstants.WBR, attributes);
	}

	/** Renders a {@link HTMLConstants#PRE} tag. */
	public static Tag pre(HTMLFragment... content) {
		return plainTag(HTMLConstants.PRE, content);
	}

	/** Renders a {@link HTMLConstants#PRE} tag using a custom CSS class. */
	public static Tag pre(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.PRE, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#PRE} tag using a custom attributes. */
	public static Tag pre(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.PRE, attributes, content);
	}

	/** Renders a {@link HTMLConstants#CODE} tag. */
	public static Tag code(HTMLFragment... content) {
		return plainTag(HTMLConstants.CODE, content);
	}

	/** Renders a {@link HTMLConstants#CODE} tag using a custom CSS class. */
	public static Tag code(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.CODE, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#CODE} tag using a custom attributes. */
	public static Tag code(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.CODE, attributes, content);
	}

	/** Renders a {@link HTMLConstants#SPAN} tag. */
	public static Tag span(HTMLFragment... content) {
		return plainTag(HTMLConstants.SPAN, content);
	}

	/** Renders a {@link HTMLConstants#SPAN} tag using a custom CSS class. */
	public static Tag span(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.SPAN, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#SPAN} tag using a custom attributes. */
	public static Tag span(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.SPAN, attributes, content);
	}

	/** Renders a {@link HTMLConstants#BR} tag. */
	public static Tag br() {
		return tagEmpty(HTMLConstants.BR);
	}

	/** Renders a {@link HTMLConstants#BR} tag using a custom CSS class. */
	public static Tag br(String cssClass) {
		return tagEmpty(HTMLConstants.BR, cssClass);
	}

	/** Renders a {@link HTMLConstants#BR} tag using a custom attributes. */
	public static Tag br(Attribute... attributes) {
		return tagEmpty(HTMLConstants.BR, attributes);
	}

	/** Renders a {@link HTMLConstants#HR} tag. */
	public static Tag hr() {
		return tagEmpty(HTMLConstants.HR);
	}

	/** Renders a {@link HTMLConstants#HR} tag using a custom CSS class. */
	public static Tag hr(String cssClass) {
		return tagEmpty(HTMLConstants.HR, cssClass);
	}

	/** Renders a {@link HTMLConstants#HR} tag using a custom attributes. */
	public static Tag hr(Attribute... attributes) {
		return tagEmpty(HTMLConstants.HR, attributes);
	}

	/** Renders a {@link HTMLConstants#OL} tag. */
	public static Tag ol(HTMLFragment... content) {
		return plainTag(HTMLConstants.OL, content);
	}

	/** Renders a {@link HTMLConstants#OL} tag using a custom CSS class. */
	public static Tag ol(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.OL, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#OL} tag using a custom attributes. */
	public static Tag ol(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.OL, attributes, content);
	}

	/** Renders a {@link HTMLConstants#UL} tag. */
	public static Tag ul(HTMLFragment... content) {
		return plainTag(HTMLConstants.UL, content);
	}

	/** Renders a {@link HTMLConstants#UL} tag using a custom CSS class. */
	public static Tag ul(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.UL, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#UL} tag using a custom attributes. */
	public static Tag ul(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.UL, attributes, content);
	}

	/** Renders a {@link HTMLConstants#LI} tag. */
	public static Tag li(HTMLFragment... content) {
		return plainTag(HTMLConstants.LI, content);
	}

	/** Renders a {@link HTMLConstants#LI} tag using a custom CSS class. */
	public static Tag li(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.LI, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#LI} tag using a custom attributes. */
	public static Tag li(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.LI, attributes, content);
	}

	/** Renders a {@link HTMLConstants#DL} tag. */
	public static Tag dl(HTMLFragment... content) {
		return plainTag(HTMLConstants.DL, content);
	}

	/** Renders a {@link HTMLConstants#DL} tag using a custom CSS class. */
	public static Tag dl(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.DL, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#DL} tag using a custom CSS class. */
	public static Tag dl(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.DL, attributes, content);
	}

	/** Renders a {@link HTMLConstants#DT} tag. */
	public static Tag dt(HTMLFragment... content) {
		return plainTag(HTMLConstants.DT, content);
	}

	/** Renders a {@link HTMLConstants#DT} tag using a custom CSS class. */
	public static Tag dt(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.DT, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#DT} tag using a custom attributes. */
	public static Tag dt(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.DT, attributes, content);
	}

	/** Renders a {@link HTMLConstants#DD} tag. */
	public static Tag dd(HTMLFragment... content) {
		return plainTag(HTMLConstants.DD, content);
	}

	/** Renders a {@link HTMLConstants#DD} tag using a custom CSS class. */
	public static Tag dd(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.DD, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#DD} tag using a custom attributes. */
	public static Tag dd(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.DD, attributes, content);
	}

	/** Renders a {@link HTMLConstants#FORM} tag. */
	public static Tag form(HTMLFragment... content) {
		return plainTag(HTMLConstants.FORM, content);
	}

	/** Renders a {@link HTMLConstants#FORM} tag using a custom CSS class. */
	public static Tag form(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.FORM, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#FORM} tag using a custom attributes. */
	public static Tag form(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.FORM, attributes, content);
	}

	/** Renders a {@link HTMLConstants#FIELDSET} tag. */
	public static Tag fieldset(HTMLFragment... content) {
		return plainTag(HTMLConstants.FIELDSET, content);
	}

	/** Renders a {@link HTMLConstants#FIELDSET} tag using a custom CSS class. */
	public static Tag fieldset(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.FIELDSET, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#FIELDSET} tag using a custom attributes. */
	public static Tag fieldset(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.FIELDSET, attributes, content);
	}

	/** Renders a {@link HTMLConstants#LEGEND} tag. */
	public static Tag legend(HTMLFragment... content) {
		return plainTag(HTMLConstants.LEGEND, content);
	}

	/** Renders a {@link HTMLConstants#LEGEND} tag using a custom CSS class. */
	public static Tag legend(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.LEGEND, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#LEGEND} tag using a custom attributes. */
	public static Tag legend(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.LEGEND, attributes, content);
	}

	/** Renders a {@link HTMLConstants#SCRIPT_REF} tag. */
	public static Tag script(HTMLFragment... content) {
		return plainTag(HTMLConstants.SCRIPT_REF, content);
	}

	/** Renders a {@link HTMLConstants#SCRIPT_REF} tag using a custom CSS class. */
	public static Tag script(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.SCRIPT_REF, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#SCRIPT_REF} tag using a custom attributes. */
	public static Tag script(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.SCRIPT_REF, attributes, content);
	}

	/**
	 * Renders a {@link HTMLConstants#IMG} tag.
	 * 
	 * <p>
	 * Consider using {@link ThemeImage}s and
	 * {@link ThemeImage#write(com.top_logic.layout.DisplayContext, TagWriter)} instead.
	 * </p>
	 */
	public static Tag img() {
		return tagEmpty(HTMLConstants.IMG);
	}

	/**
	 * Renders a {@link HTMLConstants#IMG} tag using a custom CSS class.
	 * 
	 * <p>
	 * Consider using {@link ThemeImage}s and
	 * {@link ThemeImage#write(com.top_logic.layout.DisplayContext, TagWriter)} instead.
	 * </p>
	 */
	public static Tag img(String cssClass) {
		return tagEmpty(HTMLConstants.IMG, cssClass);
	}

	/**
	 * Renders a {@link HTMLConstants#IMG} tag using a custom attributes.
	 * 
	 * <p>
	 * Consider using {@link ThemeImage}s and
	 * {@link ThemeImage#write(com.top_logic.layout.DisplayContext, TagWriter)} instead.
	 * </p>
	 */
	public static Tag img(Attribute... attributes) {
		return tagEmpty(HTMLConstants.IMG, attributes);
	}

	/** Renders a {@link HTMLConstants#MAP} tag. */
	public static Tag map(HTMLFragment... content) {
		return plainTag(HTMLConstants.MAP, content);
	}

	/** Renders a {@link HTMLConstants#MAP} tag using a custom CSS class. */
	public static Tag map(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.MAP, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#MAP} tag using a custom attributes. */
	public static Tag map(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.MAP, attributes, content);
	}

	/** Renders a {@link HTMLConstants#AREA} tag. */
	public static Tag area() {
		return tagEmpty(HTMLConstants.AREA);
	}

	/** Renders a {@link HTMLConstants#AREA} tag using a custom CSS class. */
	public static Tag area(String cssClass) {
		return tagEmpty(HTMLConstants.AREA, cssClass);
	}

	/** Renders a {@link HTMLConstants#AREA} tag using a custom attributes. */
	public static Tag area(Attribute... attributes) {
		return tagEmpty(HTMLConstants.AREA, attributes);
	}

	/** Renders a {@link HTMLConstants#ANCHOR} tag. */
	public static Tag a(HTMLFragment... content) {
		return plainTag(HTMLConstants.ANCHOR, content);
	}

	/** Renders a {@link HTMLConstants#ANCHOR} tag using a custom CSS class. */
	public static Tag a(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.ANCHOR, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#ANCHOR} tag using a custom attributes. */
	public static Tag a(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.ANCHOR, attributes, content);
	}

	/** Renders a {@link HTMLConstants#TABLE} tag. */
	public static Tag table(HTMLFragment... content) {
		return plainTag(HTMLConstants.TABLE, content);
	}

	/** Renders a {@link HTMLConstants#TABLE} tag using a custom CSS class. */
	public static Tag table(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.TABLE, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#TABLE} tag using a custom CSS class. */
	public static Tag table(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.TABLE, attributes, content);
	}

	/** Renders a {@link HTMLConstants#COLGROUP} tag. */
	public static Tag colgroup(HTMLFragment... content) {
		return plainTag(HTMLConstants.COLGROUP, content);
	}

	/** Renders a {@link HTMLConstants#COLGROUP} tag using a custom CSS class. */
	public static Tag colgroup(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.COLGROUP, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#COLGROUP} tag using a custom attributes. */
	public static Tag colgroup(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.COLGROUP, attributes, content);
	}

	/** Renders a {@link HTMLConstants#COL} tag. */
	public static Tag col() {
		return tagEmpty(HTMLConstants.COL);
	}

	/** Renders a {@link HTMLConstants#COL} tag using a custom CSS class. */
	public static Tag col(String cssClass) {
		return tagEmpty(HTMLConstants.COL, cssClass);
	}

	/** Renders a {@link HTMLConstants#COL} tag using a custom attributes. */
	public static Tag col(Attribute... attributes) {
		return tagEmpty(HTMLConstants.COL, attributes);
	}

	/** Renders a {@link HTMLConstants#THEAD} tag. */
	public static Tag thead(HTMLFragment... content) {
		return plainTag(HTMLConstants.THEAD, content);
	}

	/** Renders a {@link HTMLConstants#THEAD} tag using a custom CSS class. */
	public static Tag thead(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.THEAD, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#THEAD} tag using a custom attributes. */
	public static Tag thead(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.THEAD, attributes, content);
	}

	/** Renders a {@link HTMLConstants#TBODY} tag. */
	public static Tag tbody(HTMLFragment... content) {
		return plainTag(HTMLConstants.TBODY, content);
	}

	/** Renders a {@link HTMLConstants#TBODY} tag using a custom CSS class. */
	public static Tag tbody(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.TBODY, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#TBODY} tag using a custom attributes. */
	public static Tag tbody(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.TBODY, attributes, content);
	}

	/** Renders a {@link HTMLConstants#TFOOT} tag. */
	public static Tag tfoot(HTMLFragment... content) {
		return plainTag(HTMLConstants.TFOOT, content);
	}

	/** Renders a {@link HTMLConstants#TFOOT} tag using a custom CSS class. */
	public static Tag tfoot(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.TFOOT, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#TFOOT} tag using a custom attributes. */
	public static Tag tfoot(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.TFOOT, attributes, content);
	}

	/** Renders a {@link HTMLConstants#TD} tag. */
	public static Tag td(HTMLFragment... content) {
		return plainTag(HTMLConstants.TD, content);
	}

	/** Renders a {@link HTMLConstants#TD} tag using a custom CSS class. */
	public static Tag td(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.TD, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#TD} tag using a custom attributes. */
	public static Tag td(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.TD, attributes, content);
	}

	/** Renders a {@link HTMLConstants#TR} tag. */
	public static Tag tr(HTMLFragment... content) {
		return plainTag(HTMLConstants.TR, content);
	}

	/** Renders a {@link HTMLConstants#TR} tag using a custom CSS class. */
	public static Tag tr(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.TR, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#TR} tag using a custom attributes. */
	public static Tag tr(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.TR, attributes, content);
	}

	/** Renders a {@link HTMLConstants#TH} tag. */
	public static Tag th(HTMLFragment... content) {
		return plainTag(HTMLConstants.TH, content);
	}

	/** Renders a {@link HTMLConstants#TH} tag using a custom CSS class. */
	public static Tag th(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.TH, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#TH} tag using a custom attributes. */
	public static Tag th(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.TH, attributes, content);
	}

	/** Renders a {@link HTMLConstants#SELECT} tag. */
	public static Tag select(HTMLFragment... content) {
		return plainTag(HTMLConstants.SELECT, content);
	}

	/** Renders a {@link HTMLConstants#SELECT} tag using a custom CSS class. */
	public static Tag select(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.SELECT, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#SELECT} tag using a custom attributes. */
	public static Tag select(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.SELECT, attributes, content);
	}

	/** Renders a {@link HTMLConstants#OPTGROUP} tag. */
	public static Tag optgroup(HTMLFragment... content) {
		return plainTag(HTMLConstants.OPTGROUP, content);
	}

	/** Renders a {@link HTMLConstants#OPTGROUP} tag using a custom CSS class. */
	public static Tag optgroup(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.OPTGROUP, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#OPTGROUP} tag using a custom attributes. */
	public static Tag optgroup(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.OPTGROUP, attributes, content);
	}

	/** Renders a {@link HTMLConstants#OPTION} tag. */
	public static Tag option(HTMLFragment... content) {
		return plainTag(HTMLConstants.OPTION, content);
	}

	/** Renders a {@link HTMLConstants#OPTION} tag using a custom CSS class. */
	public static Tag option(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.OPTION, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#OPTION} tag using a custom attributes. */
	public static Tag option(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.OPTION, attributes, content);
	}

	/** Renders a {@link HTMLConstants#INPUT} tag. */
	public static Tag input() {
		return tagEmpty(HTMLConstants.INPUT);
	}

	/** Renders a {@link HTMLConstants#INPUT} tag using a custom CSS class. */
	public static Tag input(String cssClass) {
		return tagEmpty(HTMLConstants.INPUT, cssClass);
	}

	/** Renders a {@link HTMLConstants#INPUT} tag using a custom CSS class. */
	public static Tag input(Attribute... attributes) {
		return tagEmpty(HTMLConstants.INPUT, attributes);
	}

	/** Renders a {@link HTMLConstants#BUTTON} tag. */
	public static Tag button(HTMLFragment... content) {
		return plainTag(HTMLConstants.BUTTON, content);
	}

	/** Renders a {@link HTMLConstants#BUTTON} tag using a custom CSS class. */
	public static Tag button(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.BUTTON, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#BUTTON} tag using a custom attributes. */
	public static Tag button(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.BUTTON, attributes, content);
	}

	/** Renders a {@link HTMLConstants#LABEL} tag. */
	public static Tag label(HTMLFragment... content) {
		return plainTag(HTMLConstants.LABEL, content);
	}

	/** Renders a {@link HTMLConstants#LABEL} tag using a custom CSS class. */
	public static Tag label(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.LABEL, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#LABEL} tag using a custom attributes. */
	public static Tag label(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.LABEL, attributes, content);
	}

	/** Renders a {@link HTMLConstants#TEXTAREA} tag. */
	public static Tag textarea(HTMLFragment... content) {
		return plainTag(HTMLConstants.TEXTAREA, content);
	}

	/** Renders a {@link HTMLConstants#TEXTAREA} tag using a custom CSS class. */
	public static Tag textarea(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.TEXTAREA, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#TEXTAREA} tag using a custom attributes. */
	public static Tag textarea(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.TEXTAREA, attributes, content);
	}

	/** Renders a {@link HTMLConstants#BOLD} tag. */
	public static Tag b(HTMLFragment... content) {
		return plainTag(HTMLConstants.BOLD, content);
	}

	/** Renders a {@link HTMLConstants#BOLD} tag using a custom CSS class. */
	public static Tag b(String cssClass, HTMLFragment... content) {
		return cssTag(HTMLConstants.BOLD, cssClass, content);
	}

	/** Renders a {@link HTMLConstants#BOLD} tag using a custom attributes. */
	public static Tag b(Attributes attributes, HTMLFragment... content) {
		return genericTag(HTMLConstants.BOLD, attributes, content);
	}

	/** Renders a {@link HTMLConstants#LINK} tag. */
	public static Tag link() {
		return tagEmpty(HTMLConstants.LINK);
	}

	/** Renders a {@link HTMLConstants#LINK} tag using a custom CSS class. */
	public static Tag link(String cssClass) {
		return tagEmpty(HTMLConstants.LINK, cssClass);
	}

	/** Renders a {@link HTMLConstants#LINK} tag using a custom attributes. */
	public static Tag link(Attribute... attributes) {
		return tagEmpty(HTMLConstants.LINK, attributes);
	}

	/** Renders a {@link HTMLConstants#META} tag. */
	public static Tag meta() {
		return tagEmpty(HTMLConstants.META);
	}

	/** Renders a {@link HTMLConstants#META} tag using a custom CSS class. */
	public static Tag meta(String cssClass) {
		return tagEmpty(HTMLConstants.META, cssClass);
	}

	/** Renders a {@link HTMLConstants#META} tag using a custom attributes. */
	public static Tag meta(Attribute... attributes) {
		return tagEmpty(HTMLConstants.META, attributes);
	}

	/**
	 * Creates a generic plain tag without attributes.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @param content
	 *        the tag contents.
	 * @return The new tag instance.
	 */
	public static Tag tag(String tagName, HTMLFragment... content) {
		return plainTag(tagName, content);
	}

	/**
	 * Creates a generic plain tag without attributes.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @return The new tag instance.
	 */
	public static Tag tag(String tagName) {
		return plainTag(tagName);
	}

	/**
	 * @see #tag(String, HTMLFragment...)
	 */
	private static Tag plainTag(String tagName, HTMLFragment... content) {
		return new PlainTag(tagName, content);
	}

	/**
	 * Creates a generic tag with a {@link HTMLConstants#CLASS_ATTR} attribute.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @param cssClass
	 *        The CSS class value.
	 * @param content
	 *        the tag contents.
	 * @return The new tag instance.
	 */
	public static Tag tag(String tagName, String cssClass, HTMLFragment... content) {
		return cssTag(tagName, cssClass, content);
	}

	/**
	 * @see #tag(String, String, HTMLFragment...)
	 */
	private static Tag cssTag(String tagName, String cssClass, HTMLFragment... content) {
		return new CssTag(tagName, content, cssClass);
	}

	/**
	 * Creates a generic tag with a single generic {@link Attribute}.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @param attribute
	 *        The single tag attribute.
	 * @param content
	 *        the tag contents.
	 * @return The new tag instance.
	 */
	public static Tag tag(String tagName, Attribute attribute, HTMLFragment... content) {
		return singleAttributeTag(tagName, attribute, content);
	}

	/**
	 * Creates a generic tag with a single generic {@link Attribute}.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @param attribute
	 *        The single tag attribute.
	 * @return The new tag instance.
	 */
	public static Tag tag(String tagName, Attribute attribute) {
		return singleAttributeTag(tagName, attribute);
	}

	/**
	 * @see #tag(String, Attribute, HTMLFragment...)
	 */
	private static Tag singleAttributeTag(String tagName, Attribute attribute, HTMLFragment... content) {
		return new SingleAttributeTag(tagName, attribute, content);
	}

	/**
	 * Creates a generic tag with a single generic {@link Attribute}.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @param attributes
	 *        The tag attributes.
	 * @return The new tag instance.
	 */
	public static Tag tag(String tagName, Attribute... attributes) {
		return genericTag(tagName, attributes);
	}

	/**
	 * @see #tag(String, Attribute...)
	 */
	private static Tag genericTag(String tagName, Attribute... attributes) {
		return new GenericTag(tagName, new Attributes(attributes));
	}

	/**
	 * Creates a generic tag with a generic set of {@link Attributes}.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @param attributes
	 *        The attributes of this tag.
	 * @param content
	 *        the tag contents.
	 * @return The new tag instance.
	 */
	public static Tag tag(String tagName, Attributes attributes, HTMLFragment... content) {
		return genericTag(tagName, attributes, content);
	}

	/**
	 * @see #tag(String, Attributes, HTMLFragment...)
	 */
	private static Tag genericTag(String tagName, Attributes attributes, HTMLFragment... content) {
		return new GenericTag(tagName, attributes, content);
	}

	/**
	 * Constructor function for {@link Attributes}
	 */
	public static Attributes attributes(Attribute... attributes) {
		return new Attributes(attributes);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ID_ATTR}.
	 */
	public static Attribute id(String value) {
		return attribute(HTMLConstants.ID_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#NAME_ATTR}.
	 */
	public static Attribute name(String value) {
		return attribute(HTMLConstants.NAME_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#CLASS_ATTR}.
	 */
	public static Attribute css(String value) {
		return attribute(HTMLConstants.CLASS_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#BORDER_ATTR}.
	 */
	public static Attribute border(String value) {
		return attribute(HTMLConstants.BORDER_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#SUMMARY_ATTR}.
	 */
	public static Attribute summary(String value) {
		return attribute(HTMLConstants.SUMMARY_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#TYPE_ATTR}.
	 */
	public static Attribute type(String value) {
		return attribute(HTMLConstants.TYPE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#VALUE_ATTR}.
	 */
	public static Attribute value(String value) {
		return attribute(HTMLConstants.VALUE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#MAX_ATTR}.
	 */
	public static Attribute max(String value) {
		return attribute(HTMLConstants.MAX_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#SIZE_ATTR}.
	 */
	public static Attribute size(String value) {
		return attribute(HTMLConstants.SIZE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ACCEPT_ATTR}.
	 */
	public static Attribute accept(String value) {
		return attribute(HTMLConstants.ACCEPT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#MAXLENGTH_ATTR}.
	 */
	public static Attribute maxlength(String value) {
		return attribute(HTMLConstants.MAXLENGTH_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#COLS_ATTR}.
	 */
	public static Attribute cols(String value) {
		return attribute(HTMLConstants.COLS_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#COLSPAN_ATTR}.
	 */
	public static Attribute colspan(String value) {
		return attribute(HTMLConstants.COLSPAN_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ROWSPAN_ATTR}.
	 */
	public static Attribute rowspan(String value) {
		return attribute(HTMLConstants.ROWSPAN_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ROWS_ATTR}.
	 */
	public static Attribute rows(String value) {
		return attribute(HTMLConstants.ROWS_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#TABINDEX_ATTR}.
	 */
	public static Attribute tabindex(String value) {
		return attribute(HTMLConstants.TABINDEX_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#FOR_ATTR}.
	 */
	public static Attribute forAttr(String value) {
		return attribute(HTMLConstants.FOR_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ACCESSKEY_ATTR}.
	 */
	public static Attribute accesskey(String value) {
		return attribute(HTMLConstants.ACCESSKEY_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#METHOD_ATTR}.
	 */
	public static Attribute method(String value) {
		return attribute(HTMLConstants.METHOD_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ENCTYPE_ATTR}.
	 */
	public static Attribute enctype(String value) {
		return attribute(HTMLConstants.ENCTYPE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ACTION_ATTR}.
	 */
	public static Attribute action(String value) {
		return attribute(HTMLConstants.ACTION_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#TARGET_ATTR}.
	 */
	public static Attribute target(String value) {
		return attribute(HTMLConstants.TARGET_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#SRC_ATTR}.
	 */
	public static Attribute src(String value) {
		return attribute(HTMLConstants.SRC_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#HREF_ATTR}.
	 */
	public static Attribute href(String value) {
		return attribute(HTMLConstants.HREF_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSELECTSTART_ATTR}.
	 */
	public static Attribute onselectstart(String value) {
		return attribute(HTMLConstants.ONSELECTSTART_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONCLICK_ATTR}.
	 */
	public static Attribute onclick(String value) {
		return attribute(HTMLConstants.ONCLICK_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDBLCLICK_ATTR}.
	 */
	public static Attribute ondblclick(String value) {
		return attribute(HTMLConstants.ONDBLCLICK_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMOUSEOUT_ATTR}.
	 */
	public static Attribute onmouseout(String value) {
		return attribute(HTMLConstants.ONMOUSEOUT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMOUSEOVER_ATTR}.
	 */
	public static Attribute onmouseover(String value) {
		return attribute(HTMLConstants.ONMOUSEOVER_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMOUSEDOWN_ATTR}.
	 */
	public static Attribute onmousedown(String value) {
		return attribute(HTMLConstants.ONMOUSEDOWN_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMOUSEUP_ATTR}.
	 */
	public static Attribute onmouseup(String value) {
		return attribute(HTMLConstants.ONMOUSEUP_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONCHANGE_ATTR}.
	 */
	public static Attribute onchange(String value) {
		return attribute(HTMLConstants.ONCHANGE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONINPUT_ATTR}.
	 */
	public static Attribute oninput(String value) {
		return attribute(HTMLConstants.ONINPUT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONFOCUS_ATTR}.
	 */
	public static Attribute onfocus(String value) {
		return attribute(HTMLConstants.ONFOCUS_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONKEYUP_ATTR}.
	 */
	public static Attribute onkeyup(String value) {
		return attribute(HTMLConstants.ONKEYUP_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONKEYDOWN_ATTR}.
	 */
	public static Attribute onkeydown(String value) {
		return attribute(HTMLConstants.ONKEYDOWN_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONKEYPRESS_ATTR}.
	 */
	public static Attribute onkeypress(String value) {
		return attribute(HTMLConstants.ONKEYPRESS_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONBLUR_ATTR}.
	 */
	public static Attribute onblur(String value) {
		return attribute(HTMLConstants.ONBLUR_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONLOAD_ATTR}.
	 */
	public static Attribute onload(String value) {
		return attribute(HTMLConstants.ONLOAD_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONUNLOAD_ATTR}.
	 */
	public static Attribute onunload(String value) {
		return attribute(HTMLConstants.ONUNLOAD_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONCONTEXTMENU_ATTR}.
	 */
	public static Attribute oncontextmenu(String value) {
		return attribute(HTMLConstants.ONCONTEXTMENU_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMOUSEENTER_ATTR}.
	 */
	public static Attribute onmouseenter(String value) {
		return attribute(HTMLConstants.ONMOUSEENTER_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMOUSELEAVE_ATTR}.
	 */
	public static Attribute onmouseleave(String value) {
		return attribute(HTMLConstants.ONMOUSELEAVE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMOUSEMOVE_ATTR}.
	 */
	public static Attribute onmousemove(String value) {
		return attribute(HTMLConstants.ONMOUSEMOVE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONABORT_ATTR}.
	 */
	public static Attribute onabort(String value) {
		return attribute(HTMLConstants.ONABORT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONBEFOREUNLOAD_ATTR}.
	 */
	public static Attribute onbeforeunload(String value) {
		return attribute(HTMLConstants.ONBEFOREUNLOAD_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONERROR_ATTR}.
	 */
	public static Attribute onerror(String value) {
		return attribute(HTMLConstants.ONERROR_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONHASHCHANGE_ATTR}.
	 */
	public static Attribute onhashchange(String value) {
		return attribute(HTMLConstants.ONHASHCHANGE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONPAGESHOW_ATTR}.
	 */
	public static Attribute onpageshow(String value) {
		return attribute(HTMLConstants.ONPAGESHOW_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONPAGEHIDE_ATTR}.
	 */
	public static Attribute onpagehide(String value) {
		return attribute(HTMLConstants.ONPAGEHIDE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONRESIZE_ATTR}.
	 */
	public static Attribute onresize(String value) {
		return attribute(HTMLConstants.ONRESIZE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSCROLL_ATTR}.
	 */
	public static Attribute onscroll(String value) {
		return attribute(HTMLConstants.ONSCROLL_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONFOCUSIN_ATTR}.
	 */
	public static Attribute onfocusin(String value) {
		return attribute(HTMLConstants.ONFOCUSIN_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONFOCUSOUT_ATTR}.
	 */
	public static Attribute onfocusout(String value) {
		return attribute(HTMLConstants.ONFOCUSOUT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONINVALID_ATTR}.
	 */
	public static Attribute oninvalid(String value) {
		return attribute(HTMLConstants.ONINVALID_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONRESET_ATTR}.
	 */
	public static Attribute onreset(String value) {
		return attribute(HTMLConstants.ONRESET_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSEARCH_ATTR}.
	 */
	public static Attribute onsearch(String value) {
		return attribute(HTMLConstants.ONSEARCH_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSELECT_ATTR}.
	 */
	public static Attribute onselect(String value) {
		return attribute(HTMLConstants.ONSELECT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSUBMIT_ATTR}.
	 */
	public static Attribute onsubmit(String value) {
		return attribute(HTMLConstants.ONSUBMIT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#DRAGGABLE_ATTR}.
	 */
	public static Attribute draggable(String value) {
		return attribute(HTMLConstants.DRAGGABLE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDRAG_ATTR}.
	 */
	public static Attribute ondrag(String value) {
		return attribute(HTMLConstants.ONDRAG_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDRAGEND_ATTR}.
	 */
	public static Attribute ondragend(String value) {
		return attribute(HTMLConstants.ONDRAGEND_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDRAGENTER_ATTR}.
	 */
	public static Attribute ondragenter(String value) {
		return attribute(HTMLConstants.ONDRAGENTER_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDRAGLEAVE_ATTR}.
	 */
	public static Attribute ondragleave(String value) {
		return attribute(HTMLConstants.ONDRAGLEAVE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDRAGOVER_ATTR}.
	 */
	public static Attribute ondragover(String value) {
		return attribute(HTMLConstants.ONDRAGOVER_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDRAGSTART_ATTR}.
	 */
	public static Attribute ondragstart(String value) {
		return attribute(HTMLConstants.ONDRAGSTART_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDROP_ATTR}.
	 */
	public static Attribute ondrop(String value) {
		return attribute(HTMLConstants.ONDROP_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONCOPY_ATTR}.
	 */
	public static Attribute oncopy(String value) {
		return attribute(HTMLConstants.ONCOPY_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONCUT_ATTR}.
	 */
	public static Attribute oncut(String value) {
		return attribute(HTMLConstants.ONCUT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONPASTE_ATTR}.
	 */
	public static Attribute onpaste(String value) {
		return attribute(HTMLConstants.ONPASTE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONAFTERPRINT_ATTR}.
	 */
	public static Attribute onafterprint(String value) {
		return attribute(HTMLConstants.ONAFTERPRINT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONBEFOREPRINT_ATTR}.
	 */
	public static Attribute onbeforeprint(String value) {
		return attribute(HTMLConstants.ONBEFOREPRINT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONCANPLAY_ATTR}.
	 */
	public static Attribute oncanplay(String value) {
		return attribute(HTMLConstants.ONCANPLAY_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONCANPLAYTHROUGH_ATTR}.
	 */
	public static Attribute oncanplaythrough(String value) {
		return attribute(HTMLConstants.ONCANPLAYTHROUGH_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONDURATIONCHANGE_ATTR}.
	 */
	public static Attribute ondurationchange(String value) {
		return attribute(HTMLConstants.ONDURATIONCHANGE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONEMPTIED_ATTR}.
	 */
	public static Attribute onemptied(String value) {
		return attribute(HTMLConstants.ONEMPTIED_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONENDED_ATTR}.
	 */
	public static Attribute onended(String value) {
		return attribute(HTMLConstants.ONENDED_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONLOADEDDATA_ATTR}.
	 */
	public static Attribute onloadeddata(String value) {
		return attribute(HTMLConstants.ONLOADEDDATA_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONLOADEDMETADATA_ATTR}.
	 */
	public static Attribute onloadedmetadata(String value) {
		return attribute(HTMLConstants.ONLOADEDMETADATA_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONLOADSTART_ATTR}.
	 */
	public static Attribute onloadstart(String value) {
		return attribute(HTMLConstants.ONLOADSTART_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONPAUSE_ATTR}.
	 */
	public static Attribute onpause(String value) {
		return attribute(HTMLConstants.ONPAUSE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONPLAY_ATTR}.
	 */
	public static Attribute onplay(String value) {
		return attribute(HTMLConstants.ONPLAY_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONPLAYING_ATTR}.
	 */
	public static Attribute onplaying(String value) {
		return attribute(HTMLConstants.ONPLAYING_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONPROGRESS_ATTR}.
	 */
	public static Attribute onprogress(String value) {
		return attribute(HTMLConstants.ONPROGRESS_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONRATECHANGE_ATTR}.
	 */
	public static Attribute onratechange(String value) {
		return attribute(HTMLConstants.ONRATECHANGE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSEEKED_ATTR}.
	 */
	public static Attribute onseeked(String value) {
		return attribute(HTMLConstants.ONSEEKED_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSEEKING_ATTR}.
	 */
	public static Attribute onseeking(String value) {
		return attribute(HTMLConstants.ONSEEKING_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSTALLED_ATTR}.
	 */
	public static Attribute onstalled(String value) {
		return attribute(HTMLConstants.ONSTALLED_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSUSPEND_ATTR}.
	 */
	public static Attribute onsuspend(String value) {
		return attribute(HTMLConstants.ONSUSPEND_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONTIMEUPDATE_ATTR}.
	 */
	public static Attribute ontimeupdate(String value) {
		return attribute(HTMLConstants.ONTIMEUPDATE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONVOLUMECHANGE_ATTR}.
	 */
	public static Attribute onvolumechange(String value) {
		return attribute(HTMLConstants.ONVOLUMECHANGE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONWAITING_ATTR}.
	 */
	public static Attribute onwaiting(String value) {
		return attribute(HTMLConstants.ONWAITING_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ANIMATIONEND_ATTR}.
	 */
	public static Attribute animationend(String value) {
		return attribute(HTMLConstants.ANIMATIONEND_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ANIMATIONITERATION_ATTR}.
	 */
	public static Attribute animationiteration(String value) {
		return attribute(HTMLConstants.ANIMATIONITERATION_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ANIMATIONSTART_ATTR}.
	 */
	public static Attribute animationstart(String value) {
		return attribute(HTMLConstants.ANIMATIONSTART_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#TRANSITIONEND_ATTR}.
	 */
	public static Attribute transitionend(String value) {
		return attribute(HTMLConstants.TRANSITIONEND_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMESSAGE_ATTR}.
	 */
	public static Attribute onmessage(String value) {
		return attribute(HTMLConstants.ONMESSAGE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONMOUSEWHEEL_ATTR}.
	 * 
	 * @deprecated Use {@link #onwheel(String)}.
	 */
	@Deprecated
	public static Attribute onmousewheel(String value) {
		return attribute(HTMLConstants.ONMOUSEWHEEL_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONONLINE_ATTR}.
	 */
	public static Attribute ononline(String value) {
		return attribute(HTMLConstants.ONONLINE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONOFFLINE_ATTR}.
	 */
	public static Attribute onoffline(String value) {
		return attribute(HTMLConstants.ONOFFLINE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONPOPSTATE_ATTR}.
	 */
	public static Attribute onpopstate(String value) {
		return attribute(HTMLConstants.ONPOPSTATE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSHOW_ATTR}.
	 */
	public static Attribute onshow(String value) {
		return attribute(HTMLConstants.ONSHOW_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONSTORAGE_ATTR}.
	 */
	public static Attribute onstorage(String value) {
		return attribute(HTMLConstants.ONSTORAGE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONTOGGLE_ATTR}.
	 */
	public static Attribute ontoggle(String value) {
		return attribute(HTMLConstants.ONTOGGLE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ONWHEEL_ATTR}.
	 */
	public static Attribute onwheel(String value) {
		return attribute(HTMLConstants.ONWHEEL_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#WIDTH_ATTR}.
	 */
	public static Attribute width(String value) {
		return attribute(HTMLConstants.WIDTH_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#HEIGHT_ATTR}.
	 */
	public static Attribute height(String value) {
		return attribute(HTMLConstants.HEIGHT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ALT_ATTR}.
	 */
	public static Attribute alt(String value) {
		return attribute(HTMLConstants.ALT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#TITLE_ATTR}.
	 */
	public static Attribute title(String value) {
		return attribute(HTMLConstants.TITLE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#STYLE_ATTR}.
	 */
	public static Attribute style(String value) {
		return attribute(HTMLConstants.STYLE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#PLACEHOLDER_ATTR}.
	 */
	public static Attribute placeholder(String value) {
		return attribute(HTMLConstants.PLACEHOLDER_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#CHECKED_ATTR}.
	 */
	public static Attribute checked(String value) {
		return attribute(HTMLConstants.CHECKED_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#DISABLED_ATTR}.
	 */
	public static Attribute disabled(String value) {
		return attribute(HTMLConstants.DISABLED_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#READONLY_ATTR}.
	 */
	public static Attribute readonly(String value) {
		return attribute(HTMLConstants.READONLY_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#SELECTED_ATTR}.
	 */
	public static Attribute selected(String value) {
		return attribute(HTMLConstants.SELECTED_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#AUTOFOCUS_ATTR}.
	 */
	public static Attribute autofocus(String value) {
		return attribute(HTMLConstants.AUTOFOCUS_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#FORMNOVALIDATE_ATTR}.
	 */
	public static Attribute formnovalidate(String value) {
		return attribute(HTMLConstants.FORMNOVALIDATE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#NOVALIDATE_ATTR}.
	 */
	public static Attribute novalidate(String value) {
		return attribute(HTMLConstants.NOVALIDATE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#REQUIRED_ATTR}.
	 */
	public static Attribute required(String value) {
		return attribute(HTMLConstants.REQUIRED_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#MULTIPLE_ATTR}.
	 */
	public static Attribute multiple(String value) {
		return attribute(HTMLConstants.MULTIPLE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#HTTP_EQUIV_ATTR}.
	 */
	public static Attribute httpEquiv(String value) {
		return attribute(HTMLConstants.HTTP_EQUIV_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#CONTENT_ATTR}.
	 */
	public static Attribute content(String value) {
		return attribute(HTMLConstants.CONTENT_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#FRAMEBORDER_ATTR}.
	 */
	public static Attribute frameborder(String value) {
		return attribute(HTMLConstants.FRAMEBORDER_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#FRAMESPACING_ATTR}.
	 */
	public static Attribute framespacing(String value) {
		return attribute(HTMLConstants.FRAMESPACING_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#COORDS_ATTR}.
	 */
	public static Attribute coords(String value) {
		return attribute(HTMLConstants.COORDS_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#SHAPE_ATTR}.
	 */
	public static Attribute shape(String value) {
		return attribute(HTMLConstants.SHAPE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#USEMAP_ATTR}.
	 */
	public static Attribute usemap(String value) {
		return attribute(HTMLConstants.USEMAP_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#VALIGN_ATTR}.
	 */
	public static Attribute valign(String value) {
		return attribute(HTMLConstants.VALIGN_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#CELLPADDING_ATTR}.
	 */
	public static Attribute cellpadding(String value) {
		return attribute(HTMLConstants.CELLPADDING_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#CELLSPACING_ATTR}.
	 */
	public static Attribute cellspacing(String value) {
		return attribute(HTMLConstants.CELLSPACING_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#REL_ATTR}.
	 */
	public static Attribute rel(String value) {
		return attribute(HTMLConstants.REL_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#ALIGN_ATTR}.
	 */
	public static Attribute align(String value) {
		return attribute(HTMLConstants.ALIGN_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#TEXT_ALIGN_ATTR}.
	 */
	public static Attribute textAlign(String value) {
		return attribute(HTMLConstants.TEXT_ALIGN_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#SCOPE_ATTR}.
	 */
	public static Attribute scope(String value) {
		return attribute(HTMLConstants.SCOPE_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#DIR_ATTR}.
	 */
	public static Attribute dir(String value) {
		return attribute(HTMLConstants.DIR_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#LANG_ATTR}.
	 */
	public static Attribute lang(String value) {
		return attribute(HTMLConstants.LANG_ATTR, value);
	}

	/**
	 * Creates an {@link Attribute} with name {@link HTMLConstants#LONGDESC_ATTR}.
	 */
	public static Attribute longdesc(String value) {
		return attribute(HTMLConstants.LONGDESC_ATTR, value);
	}

	/**
	 * Constructor function for {@link Attribute}
	 */
	public static Attribute attribute(String name, String value) {
		return new StaticAttribute(name, value);
	}

	/**
	 * Constructor function for {@link Attribute}
	 */
	public static Attribute attribute(String name, DynamicText value) {
		return new DynamicAttribute(name, value);
	}

	private static class GenericTag implements Tag {
		private String _tagName;

		private Attributes _attributes;

		private HTMLFragment[] _content;

		GenericTag(String tagName, Attributes attributes, HTMLFragment... content) {
			_tagName = tagName;
			_attributes = attributes;
			_content = content;
		}

		@Override
		public String getTagName() {
			return _tagName;
		}

		@Override
		public boolean isEmptyTag() {
			return false;
		}

		@Override
		public Attribute[] getAttributes() {
			return _attributes.getAttributes();
		}

		@Override
		public HTMLFragment[] getContent() {
			return _content;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(_tagName);
			_attributes.write(context, out);
			out.endBeginTag();

			renderContent(context, out, _content);
			out.endTag(_tagName);
		}
	}

	private static class SingleAttributeTag implements Tag {

		private Attribute _attribute;

		private String _tagName;

		private HTMLFragment[] _content;

		SingleAttributeTag(String tagName, Attribute attribute, HTMLFragment[] content) {
			_attribute = attribute;
			_tagName = tagName;
			_content = content;
		}

		@Override
		public String getTagName() {
			return _tagName;
		}

		@Override
		public boolean isEmptyTag() {
			return false;
		}

		@Override
		public Attribute[] getAttributes() {
			return new Attribute[] { _attribute };
		}

		@Override
		public HTMLFragment[] getContent() {
			return _content;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(_tagName);
			_attribute.write(context, out);
			out.endBeginTag();

			renderContent(context, out, _content);
			out.endTag(_tagName);
		}
	}

	private static class CssTag implements Tag {
		private String _tagName;

		private HTMLFragment[] _content;

		private String _cssClass;

		CssTag(String tagName, HTMLFragment[] content, String cssClass) {
			_tagName = tagName;
			_content = content;
			_cssClass = cssClass;
		}

		@Override
		public String getTagName() {
			return _tagName;
		}

		@Override
		public boolean isEmptyTag() {
			return false;
		}

		@Override
		public Attribute[] getAttributes() {
			return new Attribute[] { attribute(HTMLConstants.CLASS_ATTR, _cssClass) };
		}

		@Override
		public HTMLFragment[] getContent() {
			return _content;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(_tagName);
			out.writeAttribute(HTMLConstants.CLASS_ATTR, _cssClass);
			out.endBeginTag();
			renderContent(context, out, _content);
			out.endTag(_tagName);
		}
	}

	/**
	 * Creates a plain empty tag without attributes.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @return The new tag instance.
	 */
	public static Tag tagEmpty(String tagName) {
		return new PlainEmptyTag(tagName, null);
	}

	/**
	 * Creates an empty tag with a single <code>class</code> attribute.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @param cssClass
	 *        The CSS class to add.
	 * @return The new tag instance.
	 */
	public static Tag tagEmpty(String tagName, String cssClass) {
		return new PlainEmptyTag(tagName, cssClass);
	}

	/**
	 * Creates a generic empty tag.
	 * 
	 * @param tagName
	 *        The tag name.
	 * @param attributes
	 *        The attributes of this tag.
	 * @return The new tag instance.
	 */
	public static Tag tagEmpty(String tagName, Attribute... attributes) {
		return new EmptyTag(tagName, attributes);
	}

	private static class EmptyTag implements Tag {
		private static final HTMLFragment[] NO_CONTENT = {};

		private Attribute[] _attributes;

		private String _tagName;

		EmptyTag(String tagName, Attribute... attributes) {
			_tagName = tagName;
			_attributes = attributes;
		}

		@Override
		public String getTagName() {
			return _tagName;
		}

		@Override
		public boolean isEmptyTag() {
			return true;
		}

		@Override
		public Attribute[] getAttributes() {
			return _attributes;
		}

		@Override
		public HTMLFragment[] getContent() {
			return NO_CONTENT;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(_tagName);
			for (Attribute attribute : _attributes) {
				attribute.write(context, out);
			}
			out.endEmptyTag();
		}
	}

	private static class PlainEmptyTag implements Tag {
		private static final HTMLFragment[] NO_CONTENT = {};

		private String _tagName;

		private String _cssClass;

		PlainEmptyTag(String tagName, String cssClass) {
			_tagName = tagName;
			_cssClass = cssClass;
		}

		@Override
		public String getTagName() {
			return _tagName;
		}

		@Override
		public boolean isEmptyTag() {
			return true;
		}

		@Override
		public Attribute[] getAttributes() {
			return new Attribute[] { attribute(HTMLConstants.CLASS_ATTR, _cssClass) };
		}

		@Override
		public HTMLFragment[] getContent() {
			return NO_CONTENT;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(_tagName);
			out.writeAttribute(HTMLConstants.CLASS_ATTR, _cssClass);
			out.endEmptyTag();
		}
	}

	private static class PlainTag implements Tag {
		private static final Attribute[] NO_ATTRIBUTES = {};

		private String _tagName;

		private HTMLFragment[] _content;

		PlainTag(String tagName, HTMLFragment[] content) {
			_tagName = tagName;
			_content = content;
		}

		@Override
		public String getTagName() {
			return _tagName;
		}

		@Override
		public boolean isEmptyTag() {
			return false;
		}

		@Override
		public Attribute[] getAttributes() {
			return NO_ATTRIBUTES;
		}

		@Override
		public HTMLFragment[] getContent() {
			return _content;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			out.beginTag(_tagName);
			renderContent(context, out, _content);
			out.endTag(_tagName);
		}
	}

	/**
	 * A collection of tag attributes.
	 * 
	 * @see #tag(String, Attributes, HTMLFragment...)
	 */
	public static class Attributes {

		private Attribute[] _attributes;

		Attributes(Attribute[] attributes) {
			_attributes = attributes;
		}

		/**
		 * The {@link Attribute} array.
		 */
		public Attribute[] getAttributes() {
			return _attributes;
		}

		/**
		 * Writes the attributes to the given {@link TagWriter}.
		 * 
		 * <p>
		 * Note: This method must not implement
		 * {@link HTMLFragment#write(DisplayContext, TagWriter)} to avoid ambiguity in
		 * {@link #tag(String, Attributes, HTMLFragment...)}.
		 * </p>
		 * 
		 * @param context
		 *        The current display context.
		 * @param out
		 *        The target writer.
		 * 
		 * @throws IOException
		 *         If writing fails.
		 */
		void write(DisplayContext context, TagWriter out) throws IOException {
			for (Attribute attribute : _attributes) {
				attribute.write(context, out);
			}
		}

	}

	/**
	 * A single tag attribute.
	 * 
	 * @see #tag(String, Attribute, HTMLFragment...)
	 */
	public abstract static class Attribute {

		private String _name;

		/**
		 * Creates a {@link Attribute}.
		 *
		 * @param name
		 *        See {@link #getName()}.
		 */
		public Attribute(String name) {
			_name = name;
		}

		/**
		 * The attribute name.
		 */
		public final String getName() {
			return _name;
		}

		/**
		 * The attribute value.
		 */
		public abstract String getValue();

		/**
		 * Writes the attribute to the given {@link TagWriter}.
		 * 
		 * <p>
		 * Note: This method must not implement
		 * {@link HTMLFragment#write(DisplayContext, TagWriter)} to avoid ambiguity in
		 * {@link #tag(String, Attribute, HTMLFragment...)}.
		 * </p>
		 * 
		 * @param context
		 *        The current display context.
		 * @param out
		 *        The target writer.
		 * 
		 * @throws IOException
		 *         If writing fails.
		 */
		abstract void write(DisplayContext context, TagWriter out) throws IOException;

	}

	/**
	 * A single tag attribute.
	 * 
	 * @see #tag(String, Attribute, HTMLFragment...)
	 */
	static class StaticAttribute extends Attribute {

		private String _value;

		StaticAttribute(String name, String value) {
			super(name);
			_value = value;
		}

		@Override
		public String getValue() {
			return _value;
		}

		@Override
		void write(DisplayContext context, TagWriter out) throws IOException {
			out.writeAttribute(getName(), _value);
		}

	}

	/**
	 * A single tag attribute that depends on the {@link DisplayContext}.
	 * 
	 * @see #tag(String, Attribute, HTMLFragment...)
	 */
	public static class DynamicAttribute extends Attribute {

		private DynamicText _value;

		DynamicAttribute(String name, DynamicText value) {
			super(name);
			_value = value;
		}

		@Override
		public String getValue() {
			return Fragments.toString(DefaultDisplayContext.getDisplayContext(), _value);
		}

		/**
		 * Writes the attribute to the given {@link TagWriter}.
		 * 
		 * <p>
		 * Note: This method must not implement
		 * {@link HTMLFragment#write(DisplayContext, TagWriter)} to avoid ambiguity in
		 * {@link #tag(String, Attribute, HTMLFragment...)}.
		 * </p>
		 * 
		 * @param context
		 *        The current display context.
		 * @param out
		 *        The target writer.
		 * 
		 * @throws IOException
		 *         If writing fails.
		 */
		@Override
		void write(DisplayContext context, TagWriter out) throws IOException {
			out.beginAttribute(getName());
			_value.append(context, out);
			out.endAttribute();
		}

	}

	static void renderContent(DisplayContext context, TagWriter out, HTMLFragment... content) throws IOException {
		for (HTMLFragment fragment : content) {
			if (fragment != null) {
				fragment.write(context, out);
			}
		}
	}

	/**
	 * Creates a variable fragment that renders the {@link HTMLFragment}
	 * {@link #templateBind(HTMLFragment, TemplateArg...) bound} to the given property in the rendering
	 * context.
	 * 
	 * @param name
	 *        The property to access during rendering, see {@link DisplayContext#get(Property)}.
	 * @return The new variable fragment.
	 * 
	 * @see #templateProperty(String)
	 */
	public static HTMLFragment templateVar(Property<HTMLFragment> name) {
		return new TemplateVar(name);
	}

	/**
	 * Utility for creating a {@link Property} for {@link #templateVar(Property)}.
	 * 
	 * <p>
	 * Note: The same {@link Property} instance must be used for {@link #templateVar(Property)} and the
	 * corresponding {@link #templateArg(Property, HTMLFragment)}.
	 * </p>
	 * 
	 * @param name
	 *        The property name, see {@link Property}.
	 * @return The new {@link Property}.
	 */
	public static Property<HTMLFragment> templateProperty(String name) {
		return TypedAnnotatable.property(HTMLFragment.class, name);
	}

	/**
	 * Creates a fragment that binds the given single argument during rendering of its template.
	 * 
	 * @see #templateBind(HTMLFragment, TemplateArg...)
	 */
	public static HTMLFragment templateBind(HTMLFragment template, Property<HTMLFragment> name, HTMLFragment value) {
		return templateBind(template, templateArg(name, value));
	}

	/**
	 * Creates a fragment that binds the given arguments during rendering of its template.
	 * 
	 * @param template
	 *        The template to render with the given arguments bound in the rendering
	 *        {@link DisplayContext}.
	 * @param args
	 *        List of arguments, see {@link #templateArg(Property, HTMLFragment)}.
	 * @return Fragment rendering a template with variables, see {@link #templateVar(Property)}.
	 * 
	 * @see #templateProperty(String)
	 */
	public static HTMLFragment templateBind(HTMLFragment template, TemplateArg... args) {
		return new TemplateBind(template, args);
	}

	/**
	 * Creates a single argument for usage in {@link #templateBind(HTMLFragment, TemplateArg...)}.
	 * 
	 * @param name
	 *        The {@link Property} to bind during rendering, see {@link #templateVar(Property)}.
	 * @param value
	 *        The value to bind this property to.
	 * @return New argument container.
	 */
	public static TemplateArg templateArg(Property<HTMLFragment> name, HTMLFragment value) {
		return new TemplateArg(name, value);
	}

	private static class TemplateVar implements HTMLFragment {

		private final Property<HTMLFragment> _name;

		public TemplateVar(Property<HTMLFragment> name) {
			_name = name;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			HTMLFragment fragment = context.get(_name);
			if (fragment == null) {
				if (context.isSet(_name)) {
					// explicitly bound to null, handle as empty.
					return;
				} else {
					throw new IllegalStateException("Variable " + _name + " is not bound.");
				}
			}

			fragment.write(context, out);
		}

	}

	private static class TemplateBind implements HTMLFragment {

		private final HTMLFragment _template;

		private final TemplateArg[] _args;

		TemplateBind(HTMLFragment template, TemplateArg[] args) {
			_template = template;
			_args = args;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			for (TemplateArg arg : _args) {
				context.set(arg.getName(), arg.getValue());
			}
			try {
				_template.write(context, out);
			} finally {
				for (TemplateArg arg : _args) {
					context.reset(arg.getName());
				}
			}
		}

	}

	/**
	 * Single argument to be bound during rendering, see
	 * {@link Fragments#templateBind(HTMLFragment, TemplateArg...)}.
	 */
	public static class TemplateArg {

		private final Property<HTMLFragment> _name;

		private final HTMLFragment _value;

		TemplateArg(Property<HTMLFragment> name, HTMLFragment value) {
			_name = name;
			_value = value;
		}

		/**
		 * The {@link Property} to bind, see {@link Fragments#templateVar(Property)}.
		 */
		public Property<HTMLFragment> getName() {
			return _name;
		}

		/**
		 * The {@link HTMLFragment} to render.
		 */
		public HTMLFragment getValue() {
			return _value;
		}

	}

	/**
	 * Whether the given {@link HTMLFragment} is visible (in case it is a {@link View}).
	 */
	public static boolean isVisible(HTMLFragment view) {
		return view instanceof View ? ((View) view).isVisible() : true;
	}

	/**
	 * Creates a static string from the given {@link DynamicText} in the given
	 * {@link DisplayContext}.
	 */
	public static String toString(DisplayContext context, DynamicText text) throws IOError {
		if (text instanceof DisplayValue) {
			return ((DisplayValue) text).get(context);
		} else {
			StringBuilder buffer = new StringBuilder();
			try {
				text.append(context, buffer);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
			return buffer.toString();
		}
	}

	/**
	 * Converts a {@link DynamicText} to a {@link HTMLFragment}.
	 */
	public static HTMLFragment toFragment(DynamicText text) throws IOError {
		if (text instanceof HTMLFragment) {
			return (HTMLFragment) text;
		} else {
			return (context, out) -> text.append(context, out);
		}
	}

	/**
	 * Converts a {@link DynamicText} to a legacy {@link DisplayValue}.
	 */
	public static DisplayValue toDisplayValue(DynamicText text) {
		if (text instanceof DisplayValue) {
			return (DisplayValue) text;
		} else {
			return new AbstractDisplayValue() {
				@Override
				public void append(DisplayContext context, Appendable out) throws IOException {
					text.append(context, out);
				}
			};
		}
	}

}
