/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Base interface for {@link ConfigurationItem}s for configuring a polymorphic
 * component hierarchy, where subclasses require specialized configuration.
 *
 * <p>
 * The type parameter names the type that is instantiated from the configuration, and its bound is
 * where the default implementation class is taken from. A configuration hierarchy therefore hands
 * the parameter on instead of binding it: the configuration of a base type keeps the parameter open
 * and bounds it by that base type, and only the configuration of a concrete implementation binds it
 * to that implementation. Binding the parameter is how a configuration states its default
 * implementation class.
 * </p>
 *
 * <p>
 * The implementation class is a {@link ConfiguredInstance}, most simply by extending
 * {@link AbstractConfiguredInstance}, so that the configuration an instance was built from stays
 * reachable from that instance.
 * </p>
 *
 * @see TypedConfiguration Generically instantiating configuration items from
 *      {@link ConfigurationItem} interface declarations.
 * @see ConfiguredInstance The implementation side of a polymorphic configuration.
 *
 * @implNote A base type and one of its concrete implementations declare their configurations like
 *           this:
 *
 *           <pre>
 * public abstract class Sender {
 *     public interface Config&lt;I extends Sender&gt; extends PolymorphicConfiguration&lt;I&gt; {
 *         String getSubject();
 *     }
 * }
 *
 * public class MailSender extends AbstractConfiguredInstance&lt;MailSender.Config&gt; {
 *     &#64;TagName("mail")
 *     public interface Config extends Sender.Config&lt;MailSender&gt; {
 *         String getRecipient();
 *     }
 * }
 *           </pre>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PolymorphicConfiguration<T> extends ConfigurationItem {

	/**
	 * Configuration option for {@link #getImplementationClass()}
	 */
	String IMPLEMENTATION_CLASS_NAME = "class";

	/**
	 * The corresponding implementation class for this {@link ConfigurationItem}.
	 *
	 * <p>
	 * In a UI, the implementation class cannot be edited. Instead, the whole configuration item
	 * must be replaced with another implementation class. Otherwise, the properties defined by the
	 * item may be inconsistent with the requirements of the implementation class.
	 * </p>
	 *
	 * @implNote As long as the type parameter is kept open, the default of this property follows
	 *           from the parameter's bound and an extending interface does not redeclare it. A
	 *           hierarchy that binds the parameter at its base leaves a specialization nothing to
	 *           bind, so every specialization has to redeclare this property with a
	 *           {@link ClassDefault}: without one, a configuration reached through its tag name
	 *           names a configuration type but no implementation class. Keeping the parameter open
	 *           is what makes those redeclarations unnecessary.
	 */
	@Name(IMPLEMENTATION_CLASS_NAME)
	@Hidden
	@ReadOnly
	Class<? extends T> getImplementationClass();

	/**
	 * @see #getImplementationClass()
	 */
	void setImplementationClass(Class<? extends T> value);

}
