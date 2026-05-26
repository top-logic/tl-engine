/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * {@link PathElement} determining a {@link Group} with a configured name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GroupWithName extends AbstractConfiguredInstance<GroupWithName.Config> implements PathElement {

	/**
	 * Configuration of a {@link GroupWithName}.
	 */
	@TagName("group-with-name")
	public interface Config extends PolymorphicConfiguration<GroupWithName>, NamedConfigMandatory {

		/**
		 * The name of the group which this {@link PathElement} returns.
		 */
		@Override
		String getName();
	}

	private Set<TLStructuredTypePart> _relevantAttrs;

	/**
	 * Creates a new {@link GroupWithName}.
	 */
	public GroupWithName(InstantiationContext context, Config config) {
		super(context, config);
		TLStructuredTypePart nameAttr = Group.getGroupType().getPartOrFail(Group.NAME_ATTRIBUTE);
		Set<TLStructuredTypePart> overrides = TLModelCacheService.getOperations().getOverrides(nameAttr);
		if (overrides.isEmpty()) {
			_relevantAttrs = Collections.singleton(nameAttr);
		} else {
			HashSet<TLStructuredTypePart> relevantAttributes = new HashSet<>(overrides);
			relevantAttributes.add(nameAttr);
			_relevantAttrs = Collections.unmodifiableSet(relevantAttributes);
		}
	}

	@Override
	public Collection<TLStructuredTypePart> getRelevantParts() {
		return _relevantAttrs;
	}

	@Override
	public Collection<? extends TLObject> getValues(TLObject base) {
		return CollectionUtil.singletonOrEmptySet(Group.getGroupByName(groupName()));
	}

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getSources(TLObject destination) {
		return getPathBase(destination, () -> ((Group) destination).getName());
	}

	private String groupName() {
		return getConfig().getName();
	}

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getPathBase(TLObject element,
			TLStructuredTypePart part, Supplier<?> partValue) {
		return getPathBase(element, partValue);
	}

	private BaseObjects<? extends Collection<? extends TLObject>> getPathBase(TLObject element, Supplier<?> nameValue) {
		if (element instanceof Group) {
			if (nameValue.get().equals(groupName())) {
				return BaseObjects.all();
			}
			return BaseObjects.of(Collections.emptySet());
		}
		return BaseObjects.of(Collections.emptySet());
	}

	@Override
	public void appendId(Appendable out) throws IOException {
		out.append("group:").append(groupName());
	}

	@Override
	public void appendForTooltip(Appendable out) throws IOException {
		out.append("Group: ");
		out.append(TagUtil.encodeXML(groupName()));
	}

}

