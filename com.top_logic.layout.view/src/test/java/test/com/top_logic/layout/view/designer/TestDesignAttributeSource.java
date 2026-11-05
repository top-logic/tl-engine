/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.configedit.ConfigChildren;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.designer.AddChildCommand;
import com.top_logic.layout.view.designer.AttributeDesignSource;
import com.top_logic.layout.view.designer.DesignSession;
import com.top_logic.layout.view.designer.DesignSource;
import com.top_logic.layout.view.designer.DesignTreeNode;
import com.top_logic.layout.view.designer.DesignTreeNodes;
import com.top_logic.layout.view.designer.FileDesignSource;
import com.top_logic.layout.view.designer.SaveDesignCommand;
import com.top_logic.layout.view.designer.ViewLinkDesignTreeNode;
import com.top_logic.layout.view.element.StackElement;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.I18NConstants;

/**
 * Tests a {@link DesignSession} on a view stored in an attribute of a persistent object, an
 * {@link AttributeDesignSource}.
 *
 * <p>
 * The view is stored in the attribute {@value #VIEW} (datatype {@code tl.view:ViewDefinition}) of
 * the task of the test model of {@link AbstractModelAccessTest}, which the responsible of its
 * project may write.
 * </p>
 */
public class TestDesignAttributeSource extends AbstractModelAccessTest {

	/** Name of the channel holding the session for the save command. */
	private static final String SESSION_CHANNEL = "designSession";

	/** A view file of the module, referenced from a stored view. */
	private static final String LINKED_FILE = "designer.view.xml";

	private TLStructuredTypePart _view;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_view = part(TASK, VIEW);
	}

	@Override
	protected void tearDown() throws Exception {
		_view = null;
		super.tearDown();
	}

	/**
	 * An object without a stored view starts with an empty view; an element added in the tree is
	 * stored in the attribute by the save.
	 */
	public void testSaveStoresEditedView() throws Exception {
		becomeUser(_responsible);
		DesignSession session = new DesignSession(new AttributeDesignSource(_task, _view));
		DesignTreeNode root = session.getCurrentTree();
		assertTrue(DesignTreeNodes.configOf(root) instanceof ViewElement.Config);
		assertNull("Nothing is stored yet.", _task.tValue(_view));

		DesignTreeNode stack = addChild(root, StackElement.class);
		assertTrue(root.isDirty());
		assertNull("Editing does not touch the attribute.", _task.tValue(_view));

		save(session);

		ViewElement.Config stored = (ViewElement.Config) _task.tValue(_view);
		assertNotNull("The edited view is stored.", stored);
		assertTrue(stored.getContent() instanceof StackElement.Config);
		assertNotSame("The attribute holds a configuration of its own, not the edited one.",
			DesignTreeNodes.configOf(root), stored);
		assertFalse("The saved tree is clean.", root.isDirty());
		assertFalse(stack.isDirty());
		assertTrue(session.getDirtyViews().isEmpty());
	}

	/**
	 * Reverting discards the edit and shows the stored view, which the edit never reached.
	 */
	public void testRevertRestoresStoredView() throws Exception {
		becomeUser(_responsible);
		store("<view><stack/></view>");
		ViewElement.Config stored = (ViewElement.Config) _task.tValue(_view);
		DesignSession session = new DesignSession(new AttributeDesignSource(_task, _view));
		DesignTreeNode root = session.getCurrentTree();
		assertNotSame("The tree edits a copy of the stored view.", stored, DesignTreeNodes.configOf(root));

		DesignTreeNode stack = root.getChildren().get(0);
		addChild(stack, StackElement.class);
		assertEquals(1, ((StackElement.Config) DesignTreeNodes.configOf(stack)).getChildren().size());

		session.revert();

		DesignTreeNode reverted = session.getCurrentTree();
		assertNotSame("The tree is built anew.", root, reverted);
		StackElement.Config revertedStack = (StackElement.Config) DesignTreeNodes.configOf(reverted.getChildren().get(0));
		assertTrue("The added element is gone.", revertedStack.getChildren().isEmpty());
		ViewElement.Config storedNow = (ViewElement.Config) _task.tValue(_view);
		assertTrue("The stored view is unchanged.",
			((StackElement.Config) storedNow.getContent()).getChildren().isEmpty());
		assertTrue(((StackElement.Config) stored.getContent()).getChildren().isEmpty());
	}

	/**
	 * A user who may not write the attribute cannot save the view.
	 */
	public void testSaveRequiresWriteRight() throws Exception {
		becomeUser(_outsider);
		DesignSession session = new DesignSession(new AttributeDesignSource(_task, _view));
		addChild(session.getCurrentTree(), StackElement.class);

		assertRefused(I18NConstants.WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, () -> save(session));

		assertNull("Nothing is stored.", _task.tValue(_view));
		assertFalse("The view is still to be saved.", session.getDirtyViews().isEmpty());
	}

	/**
	 * A file referenced from a stored view is opened as file in the same session, and the save
	 * covers the stored view while it is not shown.
	 */
	public void testStoredViewLinksToFile() throws Exception {
		becomeUser(_responsible);
		store("<view><stack><view-ref view='" + LINKED_FILE + "'/></stack></view>");
		AttributeDesignSource source = new AttributeDesignSource(_task, _view);
		DesignSession session = new DesignSession(source);
		DefaultViewChannel pathChannel = new DefaultViewChannel("path");
		session.bindPath(pathChannel);

		DesignTreeNode stack = session.getCurrentTree().getChildren().get(0);
		ViewLinkDesignTreeNode link = (ViewLinkDesignTreeNode) stack.getChildren().get(0).getChildren().get(0);
		assertEquals(source, link.getSource());
		addChild(stack, StackElement.class);

		session.open(link.getTargetSource());

		FileDesignSource file = new FileDesignSource(ViewLoader.fullPath(LINKED_FILE));
		assertEquals(List.of(source, file), session.getPath());
		assertEquals(List.of(source, file), pathChannel.get());
		assertEquals(file, session.getCurrentTree().getSource());
		assertEquals("Only the stored view is modified.", List.of(source), List.copyOf(session.getDirtyViews().keySet()));

		save(session);

		StackElement.Config storedStack = (StackElement.Config) ((ViewElement.Config) _task.tValue(_view)).getContent();
		assertEquals("The reference and the added element are stored.", 2, storedStack.getChildren().size());
		assertTrue(session.getDirtyViews().isEmpty());

		// A breadcrumb going back to the stored view.
		pathChannel.set(List.of(source));
		assertEquals(source, session.getCurrent());
	}

	/**
	 * Two sources of the same attribute of the same object are the same source.
	 */
	public void testSourceIdentity() {
		DesignSource source = new AttributeDesignSource(_task, _view);
		assertEquals(source, new AttributeDesignSource(_task, _view));
		assertEquals(source.hashCode(), new AttributeDesignSource(_task, _view).hashCode());
		assertFalse(source.equals(new AttributeDesignSource(_project, part(PROJECT, NAME))));
		assertEquals(source.getId(), new AttributeDesignSource(_task, _view).getId());
	}

	private DesignTreeNode addChild(DesignTreeNode parent, Class<?> implementation) {
		ConfigChildren container = parent.getChildContainer();
		Object option = container.allowedTypes().options().stream()
			.filter(type -> container.newElement(type) instanceof PolymorphicConfiguration<?> config
				&& config.getImplementationClass() == implementation)
			.findFirst().orElseThrow(() -> new AssertionError("Cannot add a " + implementation.getName()));
		DesignTreeNode result = AddChildCommand.execute(parent, option);
		assertNotNull(result);
		return result;
	}

	private void store(String xml) throws Exception {
		ViewElement.Config view = ViewLoader.parseConfig(List.of(CharacterContents.newContent(xml)));
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_task.tUpdate(_view, view);
			tx.commit();
		}
	}

	/**
	 * Saves the session as the Apply button of the designer does.
	 */
	private static void save(DesignSession session) {
		ViewContext context = new DefaultViewContext(null);
		DefaultViewChannel channel = new DefaultViewChannel(SESSION_CHANNEL);
		channel.set(session);
		context.registerChannel(SESSION_CHANNEL, channel);

		SaveDesignCommand.Config config = TypedConfiguration.newConfigItem(SaveDesignCommand.Config.class);
		config.update(config.descriptor().getProperty(SaveDesignCommand.Config.SESSION), new ChannelRef(SESSION_CHANNEL));
		new SaveDesignCommand(null, config).execute(context, null);
	}

	/**
	 * Test suite with the test model and its access rights.
	 */
	public static Test suite() {
		return suite(TestDesignAttributeSource.class);
	}
}
