/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.channel.linking.ref.ComponentRelation;
import com.top_logic.layout.channel.linking.ref.NamedComponent;
import com.top_logic.layout.editor.AllChannelNames;
import com.top_logic.layout.editor.ChannelNameMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.ComponentReference;

/**
 * The model is take from a certain channel of another component.
 */
@TagName("channel")
public interface Channel extends ModelSpec {

	@Override
	@ClassDefault(DirectLinking.class)
	Class<? extends ChannelLinking> getImplementationClass();

	/**
	 * Configuration name of {@link #getName()}.
	 * 
	 * <p>
	 * Note: The property name is non-standard to keep backwards compatibility of layout files.
	 * </p>
	 * 
	 * @see #getName()
	 */
	String NAME = "name";
	
	/**
	 * @see #getComponentRef()
	 */
	String COMPONENT_REF = "component-ref";

	/**
	 * The model channel implementation kind.
	 */
	@Name(NAME)
	@Mandatory
	@DynamicMode(fun = ChannelNameMode.class, args = { @Ref(steps = { @Step(COMPONENT_REF) }) })
	@Options(fun = AllChannelNames.class, args = {
		@Ref(steps = {}),
		@Ref(steps = { @Step(COMPONENT_REF) }),
		@Ref(steps = { @Step(COMPONENT_REF), @Step(type = ComponentReference.class, value = ComponentReference.NAME) }),
		@Ref(steps = { @Step(COMPONENT_REF), @Step(type = ComponentRelation.class, value = "kind") }),
	})
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String value);

	/**
	 * Specification of the component to access the specified {@link #getName()} from.
	 */
	@Name(COMPONENT_REF)
	@Mandatory
	@DefaultContainer
	@ImplementationClassDefault(NamedComponent.class)
	ComponentRef getComponentRef();

	/**
	 * @see #getComponentRef()
	 */
	void setComponentRef(ComponentRef ref);

}