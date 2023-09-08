/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.ArrayList;
import java.util.Iterator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomSingleSourceValueLocator;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * {@link AttributeValueLocator} that navigates a {@link KnowledgeAssociation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AssociationNavigation extends CustomSingleSourceValueLocator implements
		ConfiguredInstance<AssociationNavigation.Config<?>> {

	/**
	 * Configuration options for {@link AssociationNavigation}.
	 */
	@Abstract
	public interface Config<T extends AssociationNavigation> extends PolymorphicConfiguration<T> {
		/**
		 * Name of the {@link KnowledgeAssociation} to navigate.
		 */
		@Mandatory
		String getAssociationName();

		/**
		 * @see #getAssociationName()
		 */
		void setAssociationName(String value);
	}

	/**
	 * Create a new instance from configuration values.
	 */
	protected static PolymorphicConfiguration<? extends AttributeValueLocator> createNavigation(
			Class<? extends Config<?>> type, String associationName) {
		Config<?> config = TypedConfiguration.newConfigItem(type);
		config.setAssociationName(associationName);
		return config;
	}

	private final String _associationName;

	private final Config<?> _config;

	/**
	 * Creates a {@link AssociationNavigation} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AssociationNavigation(InstantiationContext context, Config<?> config) {
		_config = config;
		_associationName = config.getAssociationName();
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	/**
	 * The name of the {@link KnowledgeAssociation} to navigate.
	 */
	public String getAssociationName() {
		return _associationName;
	}

	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		if (!(anObject instanceof KnowledgeObject)) {
			return null;
		}
		KnowledgeObject ko = (KnowledgeObject) anObject;
		Iterator<KnowledgeAssociation> links = getLinks(ko);
		ArrayList<Object> result = new ArrayList<>();
		try {
			while (links.hasNext()) {
				KnowledgeAssociation link = links.next();
				KnowledgeObject destination = getEnd(link);
				result.add(destination);
			}
		} catch (InvalidLinkException ex) {
			// Drop.
		}

		return result;
	}

	/**
	 * Resolves the links from the given base object.
	 * 
	 * @param obj
	 *        The item to resolve the links from.
	 * @return Links to navigate.
	 */
	protected abstract Iterator<KnowledgeAssociation> getLinks(KnowledgeObject obj);

	/**
	 * Navigates the given link.
	 * 
	 * @param link
	 *        The link as reported by {@link #getLinks(KnowledgeObject)}.
	 * @return The end object associated by the link.
	 */
	protected abstract KnowledgeObject getEnd(KnowledgeAssociation link) throws InvalidLinkException;

}