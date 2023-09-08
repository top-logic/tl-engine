/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.GuiInspectorPlugin;
import com.top_logic.util.Resources;

/**
 * A {@link GuiInspectorPlugin} that enables the developer/tester to record an assertion.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AssertionPlugin<M> extends GuiInspectorPlugin<M> {

	/**
	 * The group that contains all fields except the one enabling the assertion.
	 */
	protected static final String CONTENT_GROUP = "content";

	/**
	 * The field name for enabling the assertion.
	 */
	protected static final String FIELD_NAME_ADD_ASSERT = "addAssert";

	/**
	 * The field name for adding an assertion comment.
	 */
	protected static final String FIELD_NAME_COMMENT = "comment";

	private BooleanField assertItField;
	private StringField commentField;
	private final boolean assertByDefault;

	/**
	 * @param assertByDefault
	 *        Should this assert be activated by default?
	 * @param internalName
	 *        The internal name of the FormGroup, in which all UI elements of this
	 *        {@link AssertionPlugin} are contained.
	 */
	public AssertionPlugin(M model, boolean assertByDefault, String internalName) {
		super(model, internalName);
		this.assertByDefault = assertByDefault;
	}

	@Override
	protected final void initGuiElements(FormContainer group) {
		initAssertItField(group);

		final FormGroup contentGroup = new FormGroup(CONTENT_GROUP, getI18nPrefix());
		group.addMember(contentGroup);
		initAssertionContents(contentGroup);
		initCommentField(contentGroup);

		ValueListener listener = new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				contentGroup.setDisabled(!assertItField.getAsBoolean());
			}
		};
		assertItField.addValueListener(listener);

		// Init
		listener.valueChanged(null, null, null);
	}

	private void initCommentField(FormContainer group) {
		commentField = FormFactory.newStringField(FIELD_NAME_COMMENT);
		group.addMember(commentField);
		ResKey labelKey = ResKey.fallback(getI18nPrefix().key(commentField.getName()), I18NConstants.COMMENT_FIELD);
		commentField.setLabel(Resources.getInstance().getString(labelKey));
	}

	/**
	 * Sets the given value as both the {@link FormField#setValue(Object) value} and the
	 * {@link FormField#setDefaultValue(Object) default value} of the {@link FormField}. Returns the
	 * {@link FormField}.
	 */
	private void initAssertItField(FormContainer group) {
		assertItField = FormFactory.newBooleanField(FIELD_NAME_ADD_ASSERT);
		assertItField.initializeField(assertByDefault);

		group.addMember(assertItField);
	}

	/**
	 * Creates the custom assertion UI.
	 * 
	 * @param group
	 *        The group to add form elements to.
	 */
	protected abstract void initAssertionContents(FormContainer group);

	/**
	 * Has the user requested that this assert should be recorded?
	 */
	protected final boolean isAssertRequested() {
		return assertItField.getAsBoolean();
	}

	/**
	 * Returns the comment the user entered for this assertion.
	 */
	protected String getComment() {
		return commentField.getAsString();
	}

	/**
	 * Record the corresponding assertion if the user requested it (checked the check box).
	 */
	public final void recordAssertionIfRequested() {
		if (isAssertRequested()) {
			for (GuiAssertion assertion : buildAssertions()) {
				ScriptingRecorder.recordAssertion(assertion);
			}
		}
	}

	/**
	 * Builds the actual assertions to record.
	 */
	protected abstract List<? extends GuiAssertion> buildAssertions();

	/**
	 * Service method to create a singleton list containing the given assertion.
	 * 
	 * @param assertion
	 *        The {@link GuiAssertion} to create a list for.
	 * 
	 * @return A list containing the given {@link GuiAssertion}.
	 */
	protected List<? extends GuiAssertion> toList(GuiAssertion assertion) {
		return Collections.singletonList(assertion);
	}
}
