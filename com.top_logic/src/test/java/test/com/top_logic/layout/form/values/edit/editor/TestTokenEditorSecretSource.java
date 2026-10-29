/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit.editor;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.TokenEditor;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;

/**
 * Test case for a {@link TokenEditor} on a property holding a {@link SecretSource} and for the
 * preselected source of a plain {@link SecretSource} property.
 */
@SuppressWarnings("javadoc")
public class TestTokenEditorSecretSource extends TestCase {

	/** Name of the token opener command of the {@link TokenEditor}. */
	private static final String OPENER = "opener";

	/** Name of the clear command of the {@link TokenEditor}. */
	private static final String CLEAR = "clear";

	public interface SecretProperties extends ConfigurationItem {

		String TOKEN = "token";

		@Name(TOKEN)
		@Mandatory
		@Format(SecretSourceFormat.class)
		@ImplementationClassDefault(LiteralSecretSource.class)
		@PropertyEditor(TokenEditor.class)
		PolymorphicConfiguration<? extends SecretSource> getToken();

		void setToken(PolymorphicConfiguration<? extends SecretSource> value);

	}

	public interface PlainSecretProperties extends ConfigurationItem {

		String SECRET = "secret";

		@Name(SECRET)
		@Mandatory
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getSecret();

	}

	public void testPlainSecretPreselectsNamed() {
		PlainSecretProperties model = TypedConfiguration.newConfigItem(PlainSecretProperties.class);
		FormGroup group = build(model);

		assertTrue("The named source is preselected: " + model.getSecret(),
			model.getSecret() instanceof NamedSecretSource.Config);
		assertNotNull("The name of the secret is edited.", findMember(group, NamedSecretSource.Config.NAME));
		assertNull("No token generation without token editor.", findMember(group, OPENER));
	}

	public void testLiteralPreselectedWithTokenCommands() {
		SecretProperties model = TypedConfiguration.newConfigItem(SecretProperties.class);
		FormGroup group = build(model);

		assertTrue("The literal source is preselected: " + model.getToken(),
			model.getToken() instanceof LiteralSecretSource.Config);
		assertNotNull("The literal value is edited as token.", findMember(group, OPENER));
		assertNotNull("The literal value can be cleared.", findMember(group, CLEAR));
	}

	public void testNamedSourceShowsReference() {
		SecretProperties model = TypedConfiguration.newConfigItem(SecretProperties.class);
		FormGroup group = build(model);

		model.setToken(TypedConfiguration.newConfigItem(NamedSecretSource.Config.class));

		assertNull("No token generation for a reference.", findMember(group, OPENER));
		assertNotNull("The name of the secret is edited.", findMember(group, NamedSecretSource.Config.NAME));
	}

	public void testConfiguredSourceKept() {
		SecretProperties model = TypedConfiguration.newConfigItem(SecretProperties.class);
		EnvironmentSecretSource.Config environment =
			TypedConfiguration.newConfigItem(EnvironmentSecretSource.Config.class);
		model.setToken(environment);
		FormGroup group = build(model);

		assertSame("A configured source is not replaced.", environment, model.getToken());
		assertNull(findMember(group, OPENER));
	}

	private static FormGroup build(ConfigurationItem model) {
		FormGroup group = new FormGroup("group", null);
		EditorFactory editorFactory = new EditorFactory(
			ApplicationConfig.getInstance().getConfig(EditorFactory.Config.class), new InitializerIndex());
		InitializerUtil.initAll(editorFactory.getInitializer(), model);
		editorFactory.initEditorGroup(group, model);
		return group;
	}

	private static FormMember findMember(FormMember member, String name) {
		if (name.equals(member.getName())) {
			return member;
		}
		if (member instanceof FormContainer container) {
			for (Iterator<? extends FormMember> it = container.getMembers(); it.hasNext();) {
				FormMember result = findMember(it.next(), name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestTokenEditorSecretSource.class);
	}

}
