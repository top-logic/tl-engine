/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;


import com.top_logic.basic.CalledByReflection;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link AbstractWrapper} representing a layout stored in the database.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersistentLayoutWrapper extends AbstractWrapper {

	/**
	 * Name of the {@link MetaObject} holding the {@link KnowledgeItem}.
	 */
	public static String KO_NAME_LAYOUT_CONFIGURATIONS = "LayoutConfigurations";

	/** Database attribute containing value of {@link #getPerson()}. */
	public static String PERSON_ATTR = "person";

	/** Database attribute containing value of {@link #getLayoutKey()}. */
	public static String LAYOUT_KEY_ATTR = "layoutKey";

	/** Database attribute containing value of {@link #getConfiguration()}. */
	public static String CONFIGURATION_ATTR = "configuration";

	/**
	 * Creates a new {@link PersistentLayoutWrapper}.
	 */
	@CalledByReflection
	public PersistentLayoutWrapper(KnowledgeObject ko) {
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
	 * The key to identify {@link #getConfiguration()} for {@link #getPerson()}.
	 */
	public String getLayoutKey() {
		return getString(LAYOUT_KEY_ATTR);
	}

	/** Setter for {@link #getLayoutKey()}. */
	public void setLayoutKey(String key) {
		setString(LAYOUT_KEY_ATTR, key);
	}

	/**
	 * The {@link LayoutComponent.Config} found under {@link #getLayoutKey()} for
	 * {@link #getPerson()}.
	 */
	public LayoutComponent.Config getConfiguration()	{
		return (LayoutComponent.Config) getValue(CONFIGURATION_ATTR);
	}

	/** Setter for {@link #getConfiguration()}. */
	public void setConfiguration(LayoutComponent.Config config) {
		setValue(CONFIGURATION_ATTR, config);
	}

}

