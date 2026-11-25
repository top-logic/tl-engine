/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import junit.framework.Test;

import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests that the {@link ModelAccessPolicy} offers the discover attributes of a type to a user who
 * may only discover its objects, and that discovering offers the columns of all attributes.
 *
 * <p>
 * {@code DiscoverNamed} specializes {@code Named}. The role {@code NameReader} may read every
 * {@code Named} object, the role {@code NameKeeper} may only discover a {@code DiscoverNamed},
 * whose discover attribute is {@code name} (see <code>ModelAccessTest.config.xml</code>). The
 * {@link #_responsible} user holds {@code NameReader} on {@link #_discoverNamed}, the
 * {@link #_outsider} holds {@code NameKeeper} on it.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestDiscoverAttributesPolicy extends AbstractModelAccessTest {

	private static final String NAMED = "Named";

	private static final String DISCOVER_NAMED = "DiscoverNamed";

	private static final String ACCOUNT_A = "accountA";

	private TLObject _discoverNamed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_discoverNamed = newObject(qualified(DISCOVER_NAMED), DISCOVER_NAMED);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_discoverNamed.tUpdateByName(ACCOUNT_A, _responsible);
			_discoverNamed.tUpdateByName("accountB", _outsider);
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_discoverNamed = null;
		super.tearDown();
	}

	/**
	 * The column of every attribute is offered to a user who may only discover the objects: none of
	 * the attributes has a read grant of its own, the rows and their cells decide what the user
	 * sees.
	 */
	public void testColumns() {
		TLStructuredTypePart name = part(NAMED, NAME);
		TLStructuredTypePart accountA = part(NAMED, ACCOUNT_A);

		becomeUser(_outsider);
		assertExecutable(ModelAccessPolicy.onAttributeColumn(type(DISCOVER_NAMED), name));
		assertExecutable(ModelAccessPolicy.onAttributeColumn(type(DISCOVER_NAMED), accountA));
		assertExecutable(ModelAccessPolicy.onAttributeColumn(type(NAMED), name));
		assertExecutable(ModelAccessPolicy.onAttributeColumn(type(NAMED), accountA));
		assertExecutable(ModelAccessPolicy.onAttributeColumn(null, name));
	}

	/**
	 * On an object, the discover attribute is readable for a user who may only discover it, but not
	 * writable.
	 */
	public void testOnObject() {
		TLStructuredTypePart name = part(NAMED, NAME);

		becomeUser(_outsider);
		assertExecutable(ModelAccessPolicy.onAttribute(SimpleBoundCommandGroup.READ, _discoverNamed, name));
		assertHidden(
			ModelAccessPolicy.onAttribute(SimpleBoundCommandGroup.READ, _discoverNamed, part(NAMED, ACCOUNT_A)));
		assertHidden(ModelAccessPolicy.onAttribute(SimpleBoundCommandGroup.WRITE, _discoverNamed, name));
	}

	private static void assertHidden(ExecutableState state) {
		assertTrue("Expected hidden: " + state, state.isHidden());
	}

	private static void assertExecutable(ExecutableState state) {
		assertTrue("Expected executable: " + state, state.isExecutable());
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestDiscoverAttributesPolicy.class);
	}

}
