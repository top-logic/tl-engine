/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import static com.top_logic.layout.Resource.*;

import junit.framework.Test;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Resource;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Test case for {@link Resource}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestResource extends AbstractLayoutTest {

	private static final String LINK = "link";

	private static final String LINK_VALUE = "window.open('" + LINK + "', '_blank');";

	private static final ThemeImage ICON = ThemeImage.forTest("/test/image.png");
	private static final Object OBJ = new Object();

	private static final String LABEL_VALUE = "test.label";
	private static final ResKey LABEL = ResKey.text(LABEL_VALUE);

	private static final String TOOLTIP_VALUE = "test.tooltip";

	private static final ResKey TOOLTIP = ResKey.text(TOOLTIP_VALUE);

	public void testResourceFor1() {
		Resource resource = resourceFor(OBJ, LABEL, ICON, TOOLTIP, LINK, "type");
		assertEquals(OBJ, resource.getUserObject());
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertEquals(ICON, Resource.Provider.INSTANCE.getImage(resource, null));
		assertEquals(TOOLTIP_VALUE, Resource.Provider.INSTANCE.getTooltip(resource));
		assertEquals(LINK_VALUE, Resource.Provider.INSTANCE.getLink(null, resource));
		assertEquals("type", Resource.Provider.INSTANCE.getType(resource));
	}

	public void testResourceFor2() {
		Resource resource = resourceFor(OBJ, LABEL, ICON, TOOLTIP, LINK);
		assertEquals(OBJ, resource.getUserObject());
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertEquals(ICON, Resource.Provider.INSTANCE.getImage(resource, null));
		assertEquals(TOOLTIP_VALUE, Resource.Provider.INSTANCE.getTooltip(resource));
		assertEquals(LINK_VALUE, Resource.Provider.INSTANCE.getLink(null, resource));
		assertNull(Resource.Provider.INSTANCE.getType(resource));
	}

	public void testResourceFor3() {
		Resource resource = resourceFor(null, LABEL, ICON, TOOLTIP);
		assertEquals(null, resource.getUserObject());
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertEquals(ICON, Resource.Provider.INSTANCE.getImage(resource, null));
		assertEquals(TOOLTIP_VALUE, Resource.Provider.INSTANCE.getTooltip(resource));
		assertNull(Resource.Provider.INSTANCE.getLink(null, resource));
		assertNull(Resource.Provider.INSTANCE.getType(resource));
	}

	public void testResourceFor4() {
		Resource resource = resourceFor(null, LABEL, ICON);
		assertEquals(null, resource.getUserObject());
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertEquals(ICON, Resource.Provider.INSTANCE.getImage(resource, null));
		assertNull(Resource.Provider.INSTANCE.getTooltip(resource));
		assertNull(Resource.Provider.INSTANCE.getLink(null, resource));
		assertNull(Resource.Provider.INSTANCE.getType(resource));
	}

	public void testResourceFor5() {
		Resource resource = resourceFor(null, LABEL);
		assertEquals(null, resource.getUserObject());
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertNull(Resource.Provider.INSTANCE.getImage(resource, null));
		assertNull(Resource.Provider.INSTANCE.getTooltip(resource));
		assertNull(Resource.Provider.INSTANCE.getLink(null, resource));
		assertNull(Resource.Provider.INSTANCE.getType(resource));
	}

	public void testResource1() {
		Resource resource = resourceFor(null, LABEL, ICON, TOOLTIP, LINK, "type", "css");
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertEquals(ICON, Resource.Provider.INSTANCE.getImage(resource, null));
		assertEquals(TOOLTIP_VALUE, Resource.Provider.INSTANCE.getTooltip(resource));
		assertEquals(LINK_VALUE, Resource.Provider.INSTANCE.getLink(null, resource));
		assertEquals("type", Resource.Provider.INSTANCE.getType(resource));
		assertEquals("css", Resource.Provider.INSTANCE.getCssClass(resource));
	}

	public void testResource2() {
		Resource resource = resource(LABEL, ICON, TOOLTIP, LINK);
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertEquals(ICON, Resource.Provider.INSTANCE.getImage(resource, null));
		assertEquals(TOOLTIP_VALUE, Resource.Provider.INSTANCE.getTooltip(resource));
		assertEquals(LINK_VALUE, Resource.Provider.INSTANCE.getLink(null, resource));
		assertNull(Resource.Provider.INSTANCE.getType(resource));
		assertNull(Resource.Provider.INSTANCE.getCssClass(resource));
	}

	public void testResource3() {
		Resource resource = resource(LABEL, ICON, TOOLTIP);
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertEquals(ICON, Resource.Provider.INSTANCE.getImage(resource, null));
		assertEquals(TOOLTIP_VALUE, Resource.Provider.INSTANCE.getTooltip(resource));
		assertNull(Resource.Provider.INSTANCE.getLink(null, resource));
		assertNull(Resource.Provider.INSTANCE.getType(resource));
	}

	public void testResource4() {
		Resource resource = resource(LABEL, ICON);
		assertEquals(LABEL_VALUE, Resource.Provider.INSTANCE.getLabel(resource));
		assertEquals(ICON, Resource.Provider.INSTANCE.getImage(resource, null));
		assertNull(Resource.Provider.INSTANCE.getTooltip(resource));
		assertNull(Resource.Provider.INSTANCE.getLink(null, resource));
		assertNull(Resource.Provider.INSTANCE.getType(resource));
	}

	public static Test suite() {
		return suite(TestResource.class);
	}

}
