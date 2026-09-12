/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.config.ModuleSingletonConfig;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link PathElement} that resolves to a {@link TLModule#getSingleton(String) module singleton}.
 *
 * <p>
 * The reached object is independent of the base object. This is typically used to add a fixed
 * object (e.g. the security root, the default singleton of the security structure module) to the
 * security parents of a type.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SingletonPathElement extends AbstractConfiguredInstance<SingletonPathElement.Config>
		implements PathElement {

	/**
	 * Configuration of a {@link SingletonPathElement}.
	 */
	@TagName("singleton")
	public interface Config extends PolymorphicConfiguration<SingletonPathElement>, ModuleSingletonConfig {

		// The module and singleton-name properties are inherited from ModuleSingletonConfig.

	}

	private final TLModule _module;

	/**
	 * Creates a new {@link SingletonPathElement}.
	 *
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SingletonPathElement(InstantiationContext context, Config config) {
		super(context, config);
		_module = TLModelUtil.findModule(config.getModule());
	}

	private TLObject singleton() {
		return _module.getSingleton(getConfig().getSingletonName());
	}

	@Override
	public Collection<? extends TLObject> getValues(TLObject base) {
		return CollectionUtil.singletonOrEmptySet(singleton());
	}

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getSources(TLObject destination) {
		// The singleton is reached from every base object regardless of its attributes, so any
		// object of the rule's type is a potential source when the destination is the singleton.
		TLObject singleton = singleton();
		if (singleton != null && singleton.equals(destination)) {
			return BaseObjects.all();
		}
		return BaseObjects.of(Collections.emptySet());
	}

	@Override
	public Collection<TLStructuredTypePart> getRelevantParts() {
		// A singleton is constant; its value does not depend on any attribute, so a recompute is
		// never necessary.
		return Collections.emptySet();
	}

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getPathBase(TLObject element,
			TLStructuredTypePart part, Supplier<?> partValue) {
		// No relevant parts, so this is never triggered by an attribute change.
		return BaseObjects.of(Collections.emptySet());
	}

	@Override
	public void appendForTooltip(Appendable out) throws IOException {
		out.append("Singleton: ");
		out.append(TagUtil.encodeXML(getConfig().getModule()));
		out.append('#');
		out.append(TagUtil.encodeXML(getConfig().getSingletonName()));
	}

}
