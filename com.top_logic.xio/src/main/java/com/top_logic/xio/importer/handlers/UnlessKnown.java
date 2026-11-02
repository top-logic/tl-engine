/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link NestedImportHandler} that imports the current element only if its ID is not yet known.
 * 
 * <p>
 * The {@link Config#getIdAttribute() ID attribute} of the current element is known, if it resolves
 * to an object that was imported before or that is found by an enclosing {@link ResolveExisting}.
 * </p>
 * 
 * <ul>
 * <li>If the ID is not known, the content handlers are applied.</li>
 * <li>If the ID is known and no {@link Config#getUpdate() update} is configured, the element is
 * skipped with all its content.</li>
 * <li>If the ID is known and an {@link Config#getUpdate() update} is configured, its handlers are
 * applied to the element in the context of the known object, if its {@link UpdateConfig#getCondition()
 * condition} holds. Otherwise, the element is skipped with all its content.</li>
 * </ul>
 */
public class UnlessKnown<C extends UnlessKnown.Config<?>> extends NestedImportHandler<C> {

	/**
	 * Configuration options for {@link UnlessKnown}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends UnlessKnown<?>> extends NestedImportHandler.Config<I> {

		/**
		 * Tag name of {@link UnlessKnown}.
		 */
		String TAG_NAME = "unless-known";

		/**
		 * @see #getIdAttribute()
		 */
		String ID_ATTRIBUTE = "id-attribute";

		/**
		 * @see #getUpdate()
		 */
		String UPDATE = "update";

		/**
		 * The XML attribute of the current element containing the ID to check.
		 * 
		 * <p>
		 * An element without this attribute, or with an empty value, is imported.
		 * </p>
		 */
		@Name(ID_ATTRIBUTE)
		@Mandatory
		String getIdAttribute();

		/**
		 * Handlers applied to an element whose ID is already known.
		 * 
		 * <p>
		 * Without this option, an element with a known ID is skipped with all its content.
		 * </p>
		 */
		@Name(UPDATE)
		UpdateConfig getUpdate();

	}

	/**
	 * Import of an element whose ID is already known, see {@link Config#getUpdate()}.
	 * 
	 * <p>
	 * The handlers are applied with the known object bound to {@link ImportContext#THIS_VAR} and to
	 * the {@link #getVar() variable}. Nothing is created and nothing is linked, unless the handlers
	 * do so explicitly. Content of the element that is not consumed by the handlers is skipped.
	 * </p>
	 */
	@Label("Update of a known object")
	public interface UpdateConfig extends ConfigurationItem {

		/**
		 * @see #getVar()
		 */
		String VAR = "var";

		/**
		 * @see #getCondition()
		 */
		String CONDITION = "condition";

		/**
		 * @see #getArguments()
		 */
		String ARGUMENTS = "arguments";

		/**
		 * @see #getContent()
		 */
		String CONTENT = "content";

		/**
		 * Explicit variable name in the context for the known object.
		 * 
		 * <p>
		 * Inner handlers may reference the known object with this name.
		 * </p>
		 */
		@Name(VAR)
		@Label("Variable")
		@Nullable
		String getVar();

		/**
		 * Condition (function evaluating to a boolean value) deciding whether the known object is
		 * updated.
		 * 
		 * <p>
		 * The function receives the known object as first argument, followed by the values of the
		 * {@link #getArguments() arguments}. If the condition does not hold, the element is skipped
		 * with all its content. Without a condition, the known object is always updated.
		 * </p>
		 */
		@Name(CONDITION)
		@Nullable
		Expr getCondition();

		/**
		 * Names of the variables whose values are passed to the {@link #getCondition() condition} after
		 * the known object.
		 */
		@Name(ARGUMENTS)
		@Format(CommaSeparatedStrings.class)
		List<String> getArguments();

		/**
		 * The handlers to apply to the element in the context of the known object.
		 */
		@Name(CONTENT)
		@DefaultContainer
		List<PolymorphicConfiguration<? extends Handler>> getContent();

	}

	/**
	 * Marker for a value that is not yet imported.
	 */
	private static final Object UNRESOLVED = new Object();

	private final Handler _updateHandler;

	/**
	 * Creates a {@link UnlessKnown} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UnlessKnown(InstantiationContext context, C config) {
		super(context, config);

		UpdateConfig update = config.getUpdate();
		_updateHandler = update == null ? null : buildHandlers(context, update.getContent());
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String id = ObjectImport.readIdAttribute(in, getConfig().getIdAttribute());
		if (id != null) {
			Object known = context.resolveObjectBackwards(this, in.getLocation(), id);
			if (known != null) {
				if (_updateHandler != null && shouldUpdate(context, in, known)) {
					update(context, in, known);
				} else {
					XMLStreamUtil.skipUpToMatchingEndTag(in);
				}
				return null;
			}
		}
		return importXmlInScope(context, in);
	}

	private void update(ImportContext context, XMLStreamReader in, Object known) throws XMLStreamException {
		context.withVar(ImportContext.THIS_VAR, known, in, (context1, in1) -> {
			return context1.withVar(getConfig().getUpdate().getVar(), known, in1, _updateHandler);
		});

		if (in.getEventType() == XMLStreamConstants.START_ELEMENT) {
			// The handlers did not consume the element.
			XMLStreamUtil.skipUpToMatchingEndTag(in);
		}
	}

	private boolean shouldUpdate(ImportContext context, XMLStreamReader in, Object known) {
		UpdateConfig update = getConfig().getUpdate();
		Expr condition = update.getCondition();
		if (condition == null) {
			return true;
		}

		List<String> names = update.getArguments();
		Object[] args = new Object[names.size() + 1];
		args[0] = known;
		for (int n = 0, cnt = names.size(); n < cnt; n++) {
			String name = names.get(n);
			Object[] value = { UNRESOLVED };
			context.deref(context.getVar(name), target -> value[0] = target);
			if (value[0] == UNRESOLVED) {
				context.error(in.getLocation(),
					I18NConstants.ERROR_UNRESOLVED_CONDITION_ARGUMENT__HANDLER_VAR.fill(location(), name));
				return false;
			}
			args[n + 1] = value[0];
		}
		return SearchExpression.isTrue(context.eval(condition, args));
	}

}
