/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model.internal;

import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.layout.form.template.model.internal.TagOutput.*;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.EmptyTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.LocalVariable;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.model.Embedd;
import com.top_logic.layout.form.template.model.MemberStyle;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.form.ReactiveFormCSS;

/**
 * Utilities for rendering a {@link HTMLTemplateFragment}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateRenderer {

	/**
	 * Returns an {@link HTMLFragment} for the given HTML template using the given properties.
	 * 
	 * @param properties
	 *        UI interface providing properties for {@link HTMLTemplateFragment}s.
	 * @param template
	 *        Template fragment thats wrapped into an {@link HTMLFragment}.
	 * 
	 * @see #toFragmentInline(WithProperties, HTMLTemplateFragment)
	 */
	public static HTMLFragment toFragment(WithProperties properties, HTMLTemplateFragment template) {
		FormMember member = (FormMember) TemplateRenderer.model(properties);
		ControlProvider contextControlProvider = TemplateRenderer.contextControlProvider(properties);
		return new TemplateControl(member, contextControlProvider,
			div(css(ReactiveFormCSS.RF_LINE), template));
	}

	/**
	 * Returns an inline {@link HTMLFragment} for the given HTML template using the given
	 * properties.
	 * 
	 * @param properties
	 *        UI interface providing properties for {@link HTMLTemplateFragment}s.
	 * @param template
	 *        Template fragment thats wrapped into an {@link HTMLFragment}.
	 * 
	 * @see #toFragment(WithProperties, HTMLTemplateFragment)
	 */
	public static HTMLFragment toFragmentInline(WithProperties properties, HTMLTemplateFragment template) {
		FormMember member = (FormMember) TemplateRenderer.model(properties);
		ControlProvider contextControlProvider = TemplateRenderer.contextControlProvider(properties);
		return new TemplateControl(member, contextControlProvider,
			div(css("rf_keepInline"), template));
	}

	/**
	 * Renders the named field in the current {@link TemplateRenderer#model(WithProperties)}.
	 */
	public static void renderFieldByName(DisplayContext displayContext, TagWriter out, String fieldName,
			MemberStyle style,
			HTMLTemplateFragment template, ControlProvider cp, WithProperties properties) throws IOException {
		FormMember field = resolveMember(properties, fieldName);

		renderField(displayContext, out, field, style, template, cp, properties);
	}

	/**
	 * Resolves the given form field name to a field instance.
	 */
	public static FormMember resolveMember(WithProperties properties, String name) {
		FormMember group = (FormMember) TemplateRenderer.model(properties);
		FormMember field = FormGroup.getMemberByRelativeName(group, name);
		return field;
	}

	/**
	 * Renders the given {@link FormMember}.
	 * 
	 * @param member
	 *        The {@link FormMember} to display.
	 * @param style
	 *        The aspect of the {@link FormMember} to render, see {@link FormTemplateConstants}.
	 * @param template
	 *        The context-provided {@link HTMLTemplateFragment} to use, <code>null</code> for using
	 *        the default {@link HTMLTemplateFragment} or {@link Control} for the given
	 *        {@link FormMember}.
	 * @param customControlProvider
	 *        The specialized {@link ControlProvider} to render the given {@link FormMember}.
	 * @param properties
	 *        Variables to access from the template.
	 */
	public static void renderField(DisplayContext displayContext, TagWriter out, FormMember member,
			MemberStyle style, HTMLTemplateFragment template,
			ControlProvider customControlProvider, WithProperties properties) throws IOException {
		if (style == MemberStyle.NONE) {
			if (template == null) {
				ControlProvider cp = activeControlProvider(member, customControlProvider);
				if (cp instanceof TemplateAnnotation) {
					// The form member has an annotated template (through a
					// TemplateControlProvider).
					// Extract the template for embedding in the current rendering process to keep
					// the
					// context ControlProvider.
					HTMLTemplateFragment annotatedTemplate = ((TemplateAnnotation) cp).getTemplate();
					renderWithTemplate(displayContext, out, member, annotatedTemplate, properties);
				} else {
					renderDirectly(displayContext, out, properties, member, style, cp);
				}
			} else {
				renderWithTemplate(displayContext, out, member, template, properties);
			}
		} else {
			renderDirectly(displayContext, out, properties, member, style, customControlProvider);
		}
	}

	private static ControlProvider activeControlProvider(FormMember member, ControlProvider customControlProvider) {
		ControlProvider cp = customControlProvider;
		if (cp != null) {
			return cp;
		}
		return member.getControlProvider();
	}

	/**
	 * Render an input element for the given {@link FormMember} as defined by the given custom
	 * {@link ControlProvider} or the {@link ControlProvider} from the template context.
	 */
	private static void renderDirectly(DisplayContext displayContext, TagWriter out, WithProperties properties,
			FormMember member, MemberStyle style, ControlProvider customControlProvider) {
		ControlProvider cp = customControlProvider;
		if (cp == null) {
			cp = TemplateRenderer.contextControlProvider(properties);
		}
		Control control = cp.createControl(member, style.getControlProviderStyle());
		if (control != null) {
			renderControl(displayContext, out, control);
		}
	}

	private static void renderWithTemplate(DisplayContext displayContext, TagWriter out, FormMember member,
			HTMLTemplateFragment template, WithProperties properties)
			throws IOException {
		if (template instanceof TagTemplate) {
			ControlProvider contextControlProvider = TemplateRenderer.contextControlProvider(properties);
			Control control = new TemplateControl(member, contextControlProvider, template);
			renderControl(displayContext, out, control);
		} else if (template instanceof Embedd) {
			Embedd embedd = (Embedd) template;
			TemplateRenderer.scope(properties).addPart(member);
			if (isEmpty(embedd.getContents())) {
				ControlProvider cp = member.getControlProvider();
				if (!(cp instanceof AbstractTemplateControlProvider)) {
					error(out,
						"Only a field with a template control provider can be rendered with an empty embedd template: "
							+ member.getQualifiedName());
					return;
				}
				AbstractTemplateControlProvider templateControlProvider = (AbstractTemplateControlProvider) cp;
				HTMLTemplateFragment annotatedTemplate = templateControlProvider.getTemplate();
				LocalVariable modelVar = new LocalVariable(TemplateControl.MODEL, member, properties);
				if (annotatedTemplate instanceof TagTemplate) {
					// Note: In case of embedding a template taken from a TemplateControlProvider, a
					// tag must be written without attributes, because writing attributes would
					// duplicate the ID in the output.
					((TagTemplate) annotatedTemplate).getContent().write(displayContext, out, modelVar);
				} else {
					annotatedTemplate.write(displayContext, out, modelVar);
				}
			} else {
				HTMLTemplateFragment contents = embedd.getContents();
				LocalVariable modelVar = new LocalVariable(TemplateControl.MODEL, member, properties);
				contents.write(displayContext, out, modelVar);
			}
		} else {
			template.write(displayContext, out, new LocalVariable(TemplateControl.MODEL, member, properties));
		}
	}

	private static boolean isEmpty(HTMLTemplateFragment template) {
		return template == null || template == EmptyTemplate.INSTANCE;
	}

	/**
	 * Renders the given {@link Control} closing a potentially open start tag before.
	 */
	public static void renderControl(DisplayContext displayContext, TagWriter out, Control control) {
		try {
			control.write(displayContext, out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Print error to log and UI, since throwing exceptions during rendering renders a UI unusable.
	 */
	private static void error(TagWriter out, String message) {
		Logger.error(message, new Exception("Stacktrace"), TemplateRenderer.class);
	
		out.beginBeginTag(HTMLConstants.SPAN);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "color: red;");
		out.writeText("ERROR: " + message);
		endTag(out);
	}

	/**
	 * Expands the given template without creating a new control.
	 * 
	 * @param out
	 *        The {@link TagWriter} to write to.
	 * @param model
	 *        The model context of the given template.
	 * @param template
	 *        The {@link HTMLTemplateFragment} to expand.
	 * @param properties
	 *        Variables to access from the template.
	 */
	public static void writeTemplate(DisplayContext displayContext, TagWriter out, Object model,
			HTMLTemplateFragment template, WithProperties properties)
			throws IOException {

		LocalVariable modelVar = new LocalVariable(TemplateControl.MODEL, properties);
		modelVar.setValue(model);
		template.write(displayContext, out, modelVar);
	}

	/**
	 * The currently rendered model.
	 * 
	 * <p>
	 * This may be different form {@link TemplateControl#getModel()} of the current
	 * {@link TemplateRenderer#scope(WithProperties)} e.g. when an {@link Embedd} template is being
	 * rendered.
	 * </p>
	 */
	public static Object model(WithProperties properties) {
		try {
			return properties.getPropertyValue(TemplateControl.MODEL);
		} catch (NoSuchPropertyException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	/**
	 * The {@link TemplateControl} whose {@link HTMLTemplateFragment} is currently rendered.
	 */
	public static TemplateControl scope(WithProperties properties) {
		try {
			return (TemplateControl) properties.getPropertyValue(TemplateControl.SCOPE);
		} catch (NoSuchPropertyException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * {@link ControlProvider} used for all model elements that have no {@link HTMLTemplateFragment}
	 * to render.
	 */
	public static ControlProvider contextControlProvider(WithProperties properties) {
		return scope(properties).getControlProvider();
	}

}
