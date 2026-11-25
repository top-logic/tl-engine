/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.accesstoken;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokenPermissions;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The owner of an {@link AccessToken} reads the attributes the list of their own tokens shows,
 * with the shipped security configuration of the type; another account does not.
 *
 * <p>
 * A table withholds the column of an attribute only when the attribute has a read grant of its
 * own listing no role the user holds, see {@link ModelAccessPolicy#onAttributeColumn}; the cells
 * are decided by the attribute on each row, see {@link ModelAccessPolicy#onAttribute}.
 * </p>
 */
public class TestAccessTokenReadRights extends AbstractSearchExpressionTest {

	/** The attributes of the list of the own tokens, <code>access-tokens.view.xml</code>. */
	private static final List<String> LISTED = List.of(AccessToken.NAME_ATTRIBUTE, AccessToken.HINT_ATTR,
		AccessToken.PERMISSIONS_ATTR, AccessToken.EXPIRES_AT_ATTR);

	private final List<Person> _persons = new ArrayList<>();

	private Person _owner;

	private Person _stranger;

	private AccessToken _token;

	@Override
	protected boolean checksSecurity() {
		return true;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = person("accessTokenReader");
		_stranger = person("accessTokenStranger");
		_token = AccessTokens.create(_owner, "reader", null, List.of("test-plain"), null).token();
	}

	@Override
	protected void tearDown() throws Exception {
		if (_token != null && _token.tValid()) {
			AccessTokens.revoke(_token);
		}
		for (Person person : _persons) {
			TestPerson.deletePersonAndUser(person);
		}
		_persons.clear();
		super.tearDown();
	}

	/**
	 * The owner sees the columns of the listed attributes and reads them on their token.
	 */
	public void testOwnerReadsListedAttributes() {
		becomeUser(_owner);
		TLClass type = AccessTokens.accessTokenType();
		for (String name : LISTED) {
			TLStructuredTypePart part = type.getPart(name);
			assertTrue("Column of " + name, ModelAccessPolicy.onAttributeColumn(type, part).isExecutable());
			assertTrue("Cell of " + name, ModelAccessPolicy
				.onAttribute(SimpleBoundCommandGroup.READ, _token, part).isExecutable());
		}
	}

	/**
	 * Another account does not read the token of the owner, but is offered the columns of the
	 * listed attributes: the token is no row of its table.
	 */
	public void testStrangerReadsNothing() {
		becomeUser(_stranger);
		TLClass type = AccessTokens.accessTokenType();
		for (String name : LISTED) {
			TLStructuredTypePart part = type.getPart(name);
			assertTrue("Column of " + name, ModelAccessPolicy.onAttributeColumn(type, part).isExecutable());
			assertFalse("Cell of " + name, ModelAccessPolicy
				.onAttribute(SimpleBoundCommandGroup.READ, _token, part).isExecutable());
		}
	}

	private Person person(String name) {
		Person result = TestPerson.createPerson(name);
		_persons.add(result);
		return result;
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		BasicRuntimeModule<?>[] modules = getModules(
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE,
			AccessTokenPermissions.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(TestAccessTokenReadRights.class,
			ServiceTestSetup.createStarterFactoryForModules(modules));
		String configFile = CustomPropertiesDecorator.createFileName(TestAccessTokenReadRights.class);
		return TLTestSetup.createTLTestSetup(TestUtils.doNotMerge(new CustomPropertiesSetup(kbTest, configFile, true)));
	}

}
