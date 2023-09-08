/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link AbstractWrapper} representing a template layout stored in the database.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class PersistentTemplateLayoutWrapper extends AbstractWrapper {

	/**
	 * Name of the {@link MetaObject} holding the {@link KnowledgeItem}.
	 */
	public static String KO_NAME_TEMPLATE_LAYOUTS = "TemplateLayouts";

	/** Database attribute containing value of {@link #getPerson()}. */
	public static String PERSON_ATTR = "person";

	/** Database attribute containing value of {@link #getLayoutKey()}. */
	public static String LAYOUT_KEY_ATTR = "layoutKey";

	/** Database attribute containing value of {@link #getArguments()}. */
	public static String ARGUMENTS_ATTR = "arguments";

	/** Database attribute containing value of {@link #getTemplate()}. */
	public static String TEMPLATE_ATTR = "template";

	/**
	 * Creates a new {@link PersistentTemplateLayoutWrapper}.
	 */
	public PersistentTemplateLayoutWrapper(KnowledgeObject ko) {
		super(ko);
	}

	/**
	 * The {@link Person} for which the layout is stored.
	 * 
	 * @return May be <code>null</code>. In such case the configuration is used for all persons,
	 *         which have no specialisation.
	 */
	public Person getPerson() {
		return getReference(Person.class, PERSON_ATTR);
	}

	/** Setter for {@link #getPerson()}. */
	public void setPerson(Person p) {
		setReference(PERSON_ATTR, p);
	}

	/**
	 * The key to identify {@link #getTemplate()} and {@link #getArguments()} for
	 * {@link #getPerson()}.
	 */
	public String getLayoutKey() {
		return getString(LAYOUT_KEY_ATTR);
	}

	/** Setter for {@link #getLayoutKey()}. */
	public void setLayoutKey(String key) {
		setString(LAYOUT_KEY_ATTR, key);
	}

	/**
	 * Layout template used for expansion.
	 */
	public String getTemplate() {
		return getString(TEMPLATE_ATTR);
	}

	/** Setter for {@link #getLayoutKey()}. */
	public void setTemplate(String template) {
		setString(TEMPLATE_ATTR, template);
	}

	/**
	 * Arguments for the template call.
	 */
	public String getArguments() {
		return getString(ARGUMENTS_ATTR);
	}

	/** Setter for {@link #getArguments()}. */
	public void setArguments(String arguments) {
		setString(ARGUMENTS_ATTR, arguments);
	}

}
