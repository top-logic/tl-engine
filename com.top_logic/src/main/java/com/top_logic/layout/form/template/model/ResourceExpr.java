/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.util.Resources;

/**
 * {@link TemplateExpression} interpreting a resource {@link #getKey()} as
 * {@link TemplateExpression} .
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResourceExpr extends AbstractResourceTemplate {

	private static final Pattern MEMBER_PATTERN = Pattern.compile(
		"\\{" +
		// Member name
				"(" + "[^\\}:]*" + ")" +
				"(?:" + ":" +
				// Member style
				"(" + "[^\\}:]*" + ")" +
				")?" +
				"\\}");

	ResourceExpr(ResKey key) {
		super(key);
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		Resources resources = displayContext.getResources();

		ResKey key = getKey();
		String src = resources.getStringOptional(key);
		if (src == null) {
			// Trigger missing key warning.
			out.writeText(resources.getString(key));
			out.writeText(": ");

			// Dump all members to have at least a functional UI.
			for (Iterator<? extends FormMember> members = ((FormContainer) TemplateRenderer.model(properties)).getMembers(); members
				.hasNext();) {
				FormMember member = members.next();
				TemplateRenderer.renderField(displayContext, out, member, MemberStyle.NONE, null, null, properties);
			}
			return;
		}

		Matcher matcher = MEMBER_PATTERN.matcher(src);
		int pos = 0;
		while (matcher.find(pos)) {
			int next = matcher.start();

			out.writeText(src.subSequence(pos, next));

			String name = matcher.group(1);
			String styleValue = matcher.group(2);

			MemberStyle style = MemberStyle.byExternalName(styleValue);
			if (style == null) {
				String msg = "No such style '" + styleValue + "' in template '" + src + "' for key '" + key + "'.";
				out.writeText("[" + msg + "]");
				Logger.error(msg, TemplateRenderer.class);
				return;
			}

			TemplateRenderer.renderFieldByName(displayContext, out, name, style, null, null, properties);

			pos = matcher.end();
		}
		out.writeText(src.subSequence(pos, src.length()));
	}

}
