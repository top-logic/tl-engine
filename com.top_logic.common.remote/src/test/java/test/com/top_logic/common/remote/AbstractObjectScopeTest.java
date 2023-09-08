/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.common.remote;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.basic.shared.io.StringR;
import com.top_logic.basic.shared.io.StringW;
import com.top_logic.basic.shared.io.W;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.common.remote.update.Changes;

/**
 * Base class for tests with {@link ObjectScope}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractObjectScopeTest extends TestCase {

	/**
	 * The given source object in the destination scope.
	 */
	@SuppressWarnings({ "unchecked" })
	protected <T> T in(ObjectScope dest, ObjectScope src, T srcObj) {
		return (T) dest.obj(src.data(srcObj).id()).handle();
	}

	/**
	 * Assert that the corresponding base object in the destination scope equals the corresponding
	 * expected object in that scope.
	 * 
	 * <p>
	 * All objects for comparision are given in the source scope.
	 * </p>
	 */
	protected void assertRefEquals(ObjectScope dest, Object base, String ref, ObjectScope src, Object expected) {
		assertEquals(dest.obj(id(src, expected)).handle(), dest.obj(id(src, base)).getData(ref));
	}

	/**
	 * Check that the given object in the source scope also exists in the destination scope.
	 */
	protected void assertExists(ObjectScope dest, ObjectScope src, Object obj) {
		assertNotNull(dest.obj(id(src, obj)));
	}

	/**
	 * Resolves the network ID of the given object in the given scope.
	 */
	protected String id(ObjectScope scope, Object obj) {
		return scope.data(obj).id();
	}

	/**
	 * Transports changes from the source scope into the destination scope.
	 */
	protected void transport(ObjectScope src, ObjectScope dest) throws IOException {
		W buffer = new StringW();
		src.popChanges().writeTo(new JsonWriter(buffer));

		Changes changes = Changes.loadChanges(new JsonReader(new StringR(buffer.toString())));
		dest.update(changes);
	}

}
