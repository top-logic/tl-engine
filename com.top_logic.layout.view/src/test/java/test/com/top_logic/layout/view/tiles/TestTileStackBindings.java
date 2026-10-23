/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.tiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.tiles.TileStackElement;
import com.top_logic.layout.view.tiles.TileStackScope;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests the {@link TileStackElement.Config#getBindings() bindings} of a tile stack: a bound channel
 * of the enclosing view is the same live channel in every frame, a channel that is not bound stays
 * private to each view declaring it.
 */
public class TestTileStackBindings extends TestCase {

	/** The view displaying the tile stack. */
	private static final String STACK_VIEW = "bindings-stack.view.xml";

	/** The view of every frame, the initial one and the pushed ones. */
	private static final String FRAME_VIEW = "bindings-frame.view.xml";

	/** The channel holding the path of the stack. */
	private static final String PATH = "navPath";

	/** The channel the stack binds into its frames. */
	private static final String SHARED = "shared";

	/** A channel declared by the enclosing view and the frames alike, but not bound. */
	private static final String PRIVATE = "private";

	/**
	 * {@link UIElement} recording the context it is displayed in, so that the test can reach the
	 * channels of a frame.
	 */
	public static class ContextProbe implements UIElement {

		/** Configuration for {@link ContextProbe}. */
		public interface Config extends UIElement.Config {
			@Override
			@ClassDefault(ContextProbe.class)
			Class<? extends UIElement> getImplementationClass();
		}

		/** The contexts the probes were displayed in, in creation order. */
		static final List<ViewContext> CONTEXTS = new ArrayList<>();

		/** Creates a {@link ContextProbe} from configuration. */
		@CalledByReflection
		public ContextProbe(InstantiationContext context, Config config) {
			// No configuration.
		}

		@Override
		public IReactControl createControl(ViewContext context) {
			CONTEXTS.add(context);
			return new ReactTextControl(context, "probe");
		}
	}

	private File _webapp;

	private FileManager _fileManagerBefore;

	private SSEUpdateQueue _sseQueue;

	private ViewContext _outer;

	private TileStackScope _scope;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ContextProbe.CONTEXTS.clear();

		_webapp = Files.createTempDirectory("tl-tile-stack-bindings").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		for (String fixture : List.of(STACK_VIEW, FRAME_VIEW)) {
			copyFixture(fixture, new File(views, fixture));
		}
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_sseQueue = new SSEUpdateQueue();
		DefaultReactContext reactContext =
			new DefaultReactContext("", "test", _sseQueue, new ReactWindowRegistry("test")) {
				@Override
				public ModelScope getModelScope() {
					return null;
				}
			};
		ViewElement view = ViewLoader.getOrLoadView(ViewLoader.fullPath(STACK_VIEW));
		_outer = new DefaultViewContext(reactContext, ViewLoader.fullPath(STACK_VIEW));
		ReactControl root = (ReactControl) view.createControl(_outer);
		root.attach();
		root.write(new TagWriter());

		_scope = new TileStackScope(channel(_outer, PATH));
	}

	@Override
	protected void tearDown() throws Exception {
		_sseQueue.shutdown();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);
		super.tearDown();
	}

	/**
	 * A bound channel is the channel of the enclosing view itself, in the initial frame and in a
	 * pushed one.
	 */
	public void testBoundChannelIsTheEnclosingOne() {
		_scope.push(FRAME_VIEW, ResKey.text("pushed"), Map.of());
		assertEquals("The initial and the pushed frame are displayed.", 2, ContextProbe.CONTEXTS.size());

		ViewChannel outer = channel(_outer, SHARED);
		for (ViewContext frame : ContextProbe.CONTEXTS) {
			assertSame(outer, channel(frame, SHARED));
		}
	}

	/**
	 * A value a pushed frame writes to a bound channel is seen by the enclosing view and by the
	 * frame it covers, and a value written outside reaches the frames.
	 */
	public void testWritesAreSharedLive() {
		ViewContext initial = ContextProbe.CONTEXTS.get(0);
		channel(_outer, SHARED).set("outside");
		assertEquals("outside", channel(initial, SHARED).get());

		_scope.push(FRAME_VIEW, ResKey.text("pushed"), Map.of());
		ViewContext pushed = ContextProbe.CONTEXTS.get(1);
		assertEquals("A pushed frame sees the current value.", "outside", channel(pushed, SHARED).get());

		channel(pushed, SHARED).set("from frame");

		assertEquals("from frame", channel(_outer, SHARED).get());
		assertEquals("The covered frame sees the write.", "from frame", channel(initial, SHARED).get());
	}

	/** A channel that is not bound is a channel of its own in every view declaring it. */
	public void testUnboundChannelIsNotShared() {
		_scope.push(FRAME_VIEW, ResKey.text("pushed"), Map.of());
		ViewContext initial = ContextProbe.CONTEXTS.get(0);
		ViewContext pushed = ContextProbe.CONTEXTS.get(1);
		ViewChannel outer = channel(_outer, PRIVATE);
		assertNotSame(outer, channel(initial, PRIVATE));
		assertNotSame(outer, channel(pushed, PRIVATE));
		assertNotSame(channel(initial, PRIVATE), channel(pushed, PRIVATE));

		channel(pushed, PRIVATE).set("from frame");

		assertNull(outer.get());
		assertNull(channel(initial, PRIVATE).get());
	}

	private static ViewChannel channel(ViewContext context, String name) {
		return context.resolveChannel(new ChannelRef(name));
	}

	private static void copyFixture(String name, File target) throws IOException {
		try (InputStream in = TestTileStackBindings.class.getResourceAsStream(name)) {
			assertNotNull("Missing fixture: " + name, in);
			Files.copy(in, Path.of(target.toURI()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex}, plus the {@link SchedulerService} the event
	 * stream's heartbeat is scheduled on.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTileStackBindings.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}

}
