/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.WithValueArgument;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} opening the View Designer on the view stored in an attribute of the object it
 * processes - its default argument, or the argument selected with {@code value}.
 *
 * <p>
 * The {@link Config#getAttribute() attribute} holds a view configuration, e.g. an attribute of the
 * datatype {@code tl.view:ViewDefinition}, which {@code <dynamic-view>} shows. The designer edits
 * the view like a view file, see {@link AttributeDesignSource}, and its save stores the edited view
 * in the attribute. An object without a stored view starts with an empty view. The designer window
 * of a view that is open already is brought to the front.
 * </p>
 *
 * <p>
 * Example: a button editing the view of the task in the channel {@code task}:
 * </p>
 *
 * <pre>
 * &lt;button&gt;
 *   &lt;action class="com.top_logic.layout.view.command.GenericViewCommand" inputs="task"&gt;
 *     &lt;open-view-designer attribute="my.app:Task#view"/&gt;
 *   &lt;/action&gt;
 * &lt;/button&gt;
 * </pre>
 *
 * <p>
 * The result of the action is the processed object.
 * </p>
 */
@InApp
public class OpenViewDesignerAction implements ViewAction {

	/**
	 * Configuration for {@link OpenViewDesignerAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<OpenViewDesignerAction>, WithValueArgument {

		/** Tag name of an {@link OpenViewDesignerAction} in an action chain. */
		String TAG_NAME = "open-view-designer";

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		@Override
		@ClassDefault(OpenViewDesignerAction.class)
		Class<? extends OpenViewDesignerAction> getImplementationClass();

		/**
		 * The attribute of the processed object holding the view to edit, e.g.
		 * {@code my.app:Task#view}.
		 *
		 * <p>
		 * The values of the attribute are view configurations, as for an attribute of the datatype
		 * {@code tl.view:ViewDefinition}.
		 * </p>
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		TLModelPartRef getAttribute();
	}

	private final Config _config;

	/**
	 * Creates a new {@link OpenViewDesignerAction} from configuration.
	 */
	@CalledByReflection
	public OpenViewDesignerAction(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		TLStructuredTypePart attribute = attribute();
		if (!(input instanceof TLObject object)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NO_OBJECT_TO_DESIGN__ATTRIBUTE.fill(_config.getAttribute().qualifiedName()));
		}
		AttributeDesignSource source = new AttributeDesignSource(object, attribute);
		OpenDesignerCommand.openDesigner(context, source,
			OpenDesignerCommand.DESIGNER_TITLE + ": " + source.getLabel());
		return input;
	}

	/**
	 * The configured attribute, checked to hold view configurations.
	 */
	private TLStructuredTypePart attribute() {
		TLTypePart part = _config.getAttribute().resolvePart();
		if (part instanceof TLStructuredTypePart attribute && holdsViews(attribute.getType())) {
			return attribute;
		}
		throw new TopLogicException(
			I18NConstants.ERROR_NO_VIEW_ATTRIBUTE__ATTRIBUTE.fill(_config.getAttribute().qualifiedName()));
	}

	private static boolean holdsViews(TLType type) {
		return type instanceof TLPrimitive primitive
			&& ViewElement.Config.class.isAssignableFrom(primitive.getStorageMapping().getApplicationType());
	}

	@Override
	public boolean changesViewState() {
		return true;
	}
}
