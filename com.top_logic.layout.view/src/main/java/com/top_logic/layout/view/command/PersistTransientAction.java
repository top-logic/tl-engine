/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.security.ModelAccessRule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link ViewAction} making the transient object it receives persistent, the draft a create dialog
 * has edited.
 *
 * <p>
 * The persistent object is created the way the TL-Script
 * {@code $draft.copy(transient: false, skipTransactionDefaults: true)} creates it, with the draft's
 * values and the parts of its compositions, in a transaction of its own (a transaction nested in an
 * enclosing {@link WithTransactionAction} commits together with that one). An attribute whose
 * {@link DefaultProvider} computes its default in the creating transaction (e.g. a sequence number)
 * keeps the default computed for the persistent object; the draft, being transient, never has such
 * a default. With a {@link Config#getContainer() container}, the object is created in the context
 * of the container and, with a {@link Config#getReference() reference}, added to that reference of
 * the container, the way {@code $container.add(reference, $created)} adds it. The action results in
 * the persistent object.
 * </p>
 *
 * <p>
 * The current user must hold the right to create the object and, when it is added to a reference
 * of the container, the right to write that reference. A refusal aborts the chain with the message
 * of the refused TL-Script operation, and nothing is created.
 * </p>
 *
 * <p>
 * The action brings its executability: the command is offered only where the user may create the
 * object. The created type is the {@link Config#getType() type}, when given; otherwise the type of
 * the {@link Config#getReference() reference}, or the type of the command input - the draft, when
 * the command's input is the dialog's model channel. See {@link ModelAccessRule} for how a refusal
 * is displayed.
 * </p>
 *
 * <p>
 * Example: the Create button of a dialog creating a milestone of a project scope.
 * </p>
 *
 * <pre>
 * &lt;generic-command input="model"&gt;
 *   &lt;store-form-state/&gt;
 *   &lt;persist-transient container="container" reference="milestones"/&gt;
 *   &lt;write-channel name="selection"/&gt;
 *   &lt;close-dialog/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 *
 * @implNote The persistent object is created by a {@link CopyOperation} with security and
 *           {@link CopyOperation#skipTransactionDefaults(boolean)}; the reference of the
 *           container is checked by
 *           {@link Update#checkWritePermission(TLObject, TLStructuredTypePart)}.
 */
@InApp
public class PersistTransientAction implements ViewAction {

	/**
	 * Configuration for {@link PersistTransientAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<PersistTransientAction>, CreationContainer {

		/** Tag name of a {@link PersistTransientAction} in an action chain. */
		String TAG_NAME = "persist-transient";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		@Override
		@ClassDefault(PersistTransientAction.class)
		Class<? extends PersistTransientAction> getImplementationClass();

		/**
		 * The type of the created object, for deciding whether the command is offered.
		 *
		 * <p>
		 * When unset, it is the type of the {@link #getReference() reference}, or the type of the
		 * command input. The persistent object always has the type of the transient object the
		 * action receives.
		 * </p>
		 */
		@Name(TYPE)
		@Nullable
		TLModelPartRef getType();
	}

	private final Config _config;

	/**
	 * Creates a {@link PersistTransientAction} from configuration.
	 */
	@CalledByReflection
	public PersistTransientAction(InstantiationContext context, Config config) {
		_config = config;
		CreationContainer.checkContainer(context, config);
	}

	/**
	 * The right to create the object, in the context of the container if one is given.
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return ModelAccessRule.creation(_config.getType(), _config.getContainer(), _config.getReference());
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (input == null) {
			return null;
		}
		if (!(input instanceof TLObject draft) || !draft.tTransient()) {
			throw new IllegalArgumentException(
				"A '" + Config.TAG_NAME + "' action expects a transient object, got: " + input);
		}

		TLObject container = container(context);
		TLStructuredTypePart reference = reference(container);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			CopyOperation operation = CopyOperation.initial();
			if (container != null) {
				operation.setContext(container, reference instanceof TLReference ref ? ref : null);
			}
			operation.setTransient(Boolean.FALSE);
			operation.withSecurity(Boolean.TRUE);
			operation.skipTransactionDefaults(true);
			TLObject created = (TLObject) operation.copyReference(draft);
			operation.finish();

			if (reference != null) {
				Update.checkWritePermission(container, reference);
				if (reference.isMultiple()) {
					container.tAdd(reference, created);
				} else {
					container.tUpdate(reference, created);
				}
			}
			tx.commit();
			return created;
		}
	}

	private TLObject container(ReactContext context) {
		if (_config.getContainer() == null) {
			return null;
		}
		Object value = ((ViewContext) context).resolveChannel(_config.getContainer()).get();
		if (value == null) {
			return null;
		}
		if (!(value instanceof TLObject container)) {
			throw new IllegalArgumentException("The '" + CreationContainer.CONTAINER + "' of a '" + Config.TAG_NAME
				+ "' action holds no object: " + value);
		}
		return container;
	}

	private TLStructuredTypePart reference(TLObject container) {
		String name = _config.getReference();
		if (name == null || container == null) {
			return null;
		}
		return container.tType().getPartOrFail(name);
	}
}
