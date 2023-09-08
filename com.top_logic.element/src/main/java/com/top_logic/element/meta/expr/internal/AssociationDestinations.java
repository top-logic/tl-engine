/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.Iterator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * {@link AttributeValueLocator} that navigates over a {@link KnowledgeAssociation} in forwards
 * direction.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class AssociationDestinations extends AssociationNavigation {

	/**
	 * Configuration options for {@link AssociationDestinations}.
	 */
	@TagName("association-destinations")
	public interface Config extends AssociationNavigation.Config<AssociationDestinations> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link AssociationDestinations} configuration.
	 * 
	 * @param associationName
	 *        the {@link KnowledgeAssociation} name.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> newInstance(String associationName) {
		return createNavigation(Config.class, associationName);
	}

	/**
	 * Creates a {@link AssociationDestinations} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AssociationDestinations(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected KnowledgeObject getEnd(KnowledgeAssociation link) throws InvalidLinkException {
		return link.getDestinationObject();
	}

	@Override
	protected Iterator<KnowledgeAssociation> getLinks(KnowledgeObject ko) {
		return ko.getOutgoingAssociations(getAssociationName());
	}
}