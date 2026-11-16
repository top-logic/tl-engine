/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.tiles;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.model.CommittedModelChanges;
import test.com.top_logic.layout.view.model.CommittedModelChanges.ScopedReactContext;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.routing.ParamBindingParticipant;
import com.top_logic.layout.view.tiles.FrameRoute;
import com.top_logic.layout.view.tiles.FrameRouteConfig;
import com.top_logic.layout.view.tiles.ReactTileStackControl;
import com.top_logic.layout.view.tiles.ScriptedTileLabel;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.layout.view.tiles.TileLabelProvider;
import com.top_logic.layout.view.tiles.TileStackScope;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the label a {@code <frame>} declaration computes for a frame of a tile stack follows
 * the objects it is computed from: a renamed object renames its frame in the path - and so in the
 * breadcrumb and the title of the page - while the frame keeps its content.
 *
 * <p>
 * The objects are modules of the application model, renamed in committed transactions and
 * delivered by a {@link GlobalModelEventForwarder}, the {@link com.top_logic.model.listen.ModelScope}
 * of the window the stack is displayed in.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTileFrameLabelObservation extends BasicTestCase {

	/** The view displaying the stack. */
	private static final String STACK_VIEW = "label-observation-stack.view.xml";

	/** The view the stack starts with. */
	private static final String HOME_VIEW = "label-observation-home.view.xml";

	/** The view of every pushed frame. */
	private static final String FRAME_VIEW = "label-observation-frame.view.xml";

	/** The channel holding the path of the stack. */
	private static final String NAV_PATH = "navPath";

	/** The frame named by the module it is pushed for. */
	private static final String PROJECT = "project";

	/** The frame named by the module of the class it is pushed for, observing modules. */
	private static final String OBSERVED = "observed";

	/** The frame named by the module of the class it is pushed for, observing no type. */
	private static final String UNOBSERVED = "unobserved";

	/** The parameter of {@link #OBSERVED} and {@link #UNOBSERVED} frames. */
	private static final String TYPE = "type";

	/** The label naming a frame by the name of its module. */
	private static final String PROJECT_LABEL = "<label class='" + ScriptedTileLabel.class.getName() + "'"
		+ " expr='$project.get(`tl.model:TLNamedPart#name`)' inputs='project'/>";

	/** The label naming a frame by the name of the module of its class. */
	private static final String MODULE_OF_TYPE_EXPR = "$type.get(`tl.model:TLType#module`).get(`tl.model:TLNamedPart#name`)";

	/** The declarations of the frames of the stack. */
	private static final List<String> FRAMES = List.of(
		"<frame id='" + PROJECT + "' view='" + FRAME_VIEW + "'>"
			+ "<param name='" + PROJECT + "'/>"
			+ PROJECT_LABEL
			+ "</frame>",
		"<frame id='" + OBSERVED + "' view='" + FRAME_VIEW + "'>"
			+ "<param name='" + TYPE + "'/>"
			+ "<label class='" + ScriptedTileLabel.class.getName() + "' expr='" + MODULE_OF_TYPE_EXPR + "'"
			+ " inputs='" + TYPE + "' observed-types='tl.model:TLModule'/>"
			+ "</frame>",
		"<frame id='" + UNOBSERVED + "' view='" + FRAME_VIEW + "'>"
			+ "<param name='" + TYPE + "'/>"
			+ "<label class='" + ScriptedTileLabel.class.getName() + "' expr='" + MODULE_OF_TYPE_EXPR + "'"
			+ " inputs='" + TYPE + "'/>"
			+ "</frame>");

	private static int _nextModule = 1;

	private File _webapp;

	private FileManager _fileManagerBefore;

	private GlobalModelEventForwarder _modelScope;

	private ReactControl _root;

	private ViewChannel _path;

	/** Pushes frames as a navigation inside the stack does. */
	private TileStackScope _scope;

	private int _writes;

	private final List<TLModule> _modules = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_webapp = Files.createTempDirectory("tl-tile-frame-label-observation").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		write(new File(views, STACK_VIEW), "<view><channels><channel name='" + NAV_PATH + "'/></channels>"
			+ "<tile-stack initial='" + HOME_VIEW + "' path='" + NAV_PATH + "'>"
			+ String.join("", FRAMES)
			+ "</tile-stack></view>");
		write(new File(views, HOME_VIEW), "<view><panel title='Home'/></view>");
		write(new File(views, FRAME_VIEW), "<view><panel title='Frame'/></view>");
		_fileManagerBefore = FileManager.getInstanceOrNull();
		FileManager.setInstance(new DefaultFileManager(_webapp));

		_modelScope = CommittedModelChanges.modelScope();
		ViewElement view = ViewLoader.getOrLoadView(ViewLoader.fullPath(STACK_VIEW));
		ViewContext viewContext =
			new DefaultViewContext(new ScopedReactContext(_modelScope), ViewLoader.fullPath(STACK_VIEW));
		_root = (ReactControl) view.createControl(viewContext);
		_root.attach();
		_root.write(new TagWriter());

		_path = viewContext.resolveChannel(new ChannelRef(NAV_PATH));
		_scope = new TileStackScope(_path, frameRoutes());
		_writes = 0;
		_path.addListener((sender, oldValue, newValue) -> _writes++);
	}

	@Override
	protected void tearDown() throws Exception {
		_root.cleanupTree();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			for (TLModule module : _modules) {
				TLModelUtil.deleteRecursive(module);
			}
			tx.commit();
		}
		_modules.clear();
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);

		_root = null;
		_path = null;
		_scope = null;
		_modelScope = null;

		super.tearDown();
	}

	/**
	 * (a) Renaming the object a frame is named by names the frame anew on the path, and (b) leaves
	 * the content of the frame as it is.
	 */
	public void testRenameRelabelsTheFrame() {
		TLModule project = module("Alpha");
		_scope.push(PROJECT, null, Map.of(PROJECT, project));
		assertEquals(ResKey.text(project.getName()), top().getLabel());
		ReactControl content = frameControl(1);

		rename(project, unique("Beta"));

		assertEquals("The frame is named by the new name.", ResKey.text(project.getName()), top().getLabel());
		assertSame("Naming the frame anew keeps its content.", content, frameControl(1));
		assertEquals("The frame is still the one pushed.", project, top().getParams().get(PROJECT));
	}

	/**
	 * (c) A change of an object the label does not read leaves the path alone.
	 */
	public void testOtherObjectDoesNotRewriteThePath() {
		TLModule project = module("Alpha");
		TLModule other = module("Other");
		_scope.push(PROJECT, null, Map.of(PROJECT, project));
		int writes = _writes;

		rename(other, unique("Renamed"));

		assertEquals("A change of another object writes no path.", writes, _writes);
	}

	/**
	 * A change of the object that does not change the label leaves the path alone.
	 */
	public void testUnchangedLabelDoesNotRewriteThePath() {
		TLModule project = module("Alpha");
		_scope.push(PROJECT, null, Map.of(PROJECT, project));
		int writes = _writes;

		rename(project, project.getName());

		assertEquals("A label that stays the same writes no path.", writes, _writes);
	}

	/**
	 * (d) A frame popped from the path is not observed any more.
	 */
	public void testPoppedFrameIsNotObserved() {
		TLModule project = module("Alpha");
		_scope.push(PROJECT, null, Map.of(PROJECT, project));
		_scope.pop();
		int writes = _writes;

		rename(project, unique("Beta"));

		assertEquals("A popped frame is named by nobody.", writes, _writes);
		assertTrue(_scope.getPath().isEmpty());
	}

	/**
	 * A frame below the top of the path is named anew as well, and the frames above it stay.
	 */
	public void testCoveredFrameIsRelabeled() {
		TLModule first = module("First");
		TLModule second = module("Second");
		_scope.push(PROJECT, null, Map.of(PROJECT, first));
		_scope.push(PROJECT, null, Map.of(PROJECT, second));
		ReactControl top = frameControl(2);

		rename(first, unique("Renamed"));

		List<TileFrame> path = _scope.getPath();
		assertEquals(ResKey.text(first.getName()), path.get(0).getLabel());
		assertEquals(ResKey.text(second.getName()), path.get(1).getLabel());
		assertSame("The frame above stays displayed.", top, frameControl(2));

		rename(first, unique("Again"));
		assertEquals("The observation goes on after naming the frame anew.", ResKey.text(first.getName()),
			_scope.getPath().get(0).getLabel());
	}

	/**
	 * (e) A label reading an object beyond its inputs follows that object where it observes its type,
	 * and does not where it observes none.
	 */
	public void testObservedTypesRelabel() {
		TLModule module = module("Alpha");
		TLClass type = type(module);
		String originalName = module.getName();
		_scope.push(OBSERVED, null, Map.of(TYPE, type));
		_scope.push(UNOBSERVED, null, Map.of(TYPE, type));

		rename(module, unique("Beta"));

		List<TileFrame> path = _scope.getPath();
		assertEquals("The observed type names the frame anew.", ResKey.text(module.getName()), path.get(0).getLabel());
		assertEquals("Without the type observed, the frame keeps its name.", ResKey.text(originalName),
			path.get(1).getLabel());
	}

	/**
	 * (f) A label the push gives the frame stays as given.
	 */
	public void testPushedLabelStays() {
		TLModule project = module("Alpha");
		ResKey given = ResKey.text("Given");
		_scope.push(PROJECT, given, Map.of(PROJECT, project));
		int writes = _writes;

		rename(project, unique("Beta"));

		assertEquals(given, top().getLabel());
		assertEquals("A label given by the push is not observed.", writes, _writes);
	}

	/**
	 * (g) The label of a route parameter naming the page follows the object on its channel: renaming
	 * it reports a route change, which composes the title anew.
	 */
	public void testParamBindingLabelFollowsTheObject() throws ConfigurationException {
		TLModule project = module("Alpha");
		ViewContext context = new DefaultViewContext(new ScopedReactContext(_modelScope));
		DefaultViewChannel channel = new DefaultViewChannel(PROJECT);
		channel.set(project);
		context.registerChannel(PROJECT, channel);
		ParamBindingParticipant participant =
			new ParamBindingParticipant(null, PROJECT, channel, scriptedLabel(), context);
		List<RouteSegment> reported = new ArrayList<>();
		RouteChangeListener listener = (RoutingParticipant sender, RouteSegment segment) -> reported.add(segment);
		participant.addRouteChangeListener(listener);
		assertEquals(ResKey.text(project.getName()), participant.activeRouteLabel());

		rename(project, unique("Beta"));

		assertEquals("The rename is reported as a route change.", 1, reported.size());
		assertEquals(ResKey.text(project.getName()), participant.activeRouteLabel());

		participant.removeRouteChangeListener(listener);
		rename(project, unique("Gamma"));
		assertEquals("Without listeners, nothing is observed.", 1, reported.size());
	}

	private TileFrame top() {
		List<TileFrame> path = _scope.getPath();
		return path.get(path.size() - 1);
	}

	/**
	 * The control displaying the frame at the given position of the stack, the initial view being
	 * position 0.
	 */
	private ReactControl frameControl(int position) {
		return stack(_root).displayedChildren().get(position);
	}

	private static ReactTileStackControl stack(ReactControl control) {
		if (control instanceof ReactTileStackControl stack) {
			return stack;
		}
		for (ReactControl child : control.displayedChildren()) {
			ReactTileStackControl found = stack(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * A committed module of the application model, named by the given text and a number unique in
	 * the test run, see {@link #unique(String)}.
	 */
	private TLModule module(String name) {
		TLModule result = CommittedModelChanges.createModule(_modelScope, unique(name));
		_modules.add(result);
		return result;
	}

	/**
	 * A module name made of the given text and a number unique in the test run.
	 */
	private static String unique(String name) {
		return "test.labelObservation." + (_nextModule++) + "." + name;
	}

	private TLClass type(TLModule module) {
		TLClass result;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			result = TLModelUtil.addClass(module, "Type");
			tx.commit();
		}
		_modelScope.synthesizeModelEvents();
		return result;
	}

	/**
	 * Renames the given module in a committed transaction and delivers the change.
	 */
	private void rename(TLModule module, String name) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			module.setName(name);
			tx.commit();
		}
		_modelScope.synthesizeModelEvents();
	}

	/**
	 * The declarations of {@link #FRAMES} as the stack has them.
	 */
	private static List<FrameRoute> frameRoutes() throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTileFrameLabelObservation.class);
		List<FrameRouteConfig> configs = new ArrayList<>();
		for (String frame : FRAMES) {
			configs.add((FrameRouteConfig) read(context, "frame", FrameRouteConfig.class, frame));
		}
		FrameRoute root = FrameRoute.createGraph(context, HOME_VIEW, configs);
		context.checkErrors();
		return root.successors();
	}

	private static TileLabelProvider scriptedLabel() throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTileFrameLabelObservation.class);
		ScriptedTileLabel.Config config =
			(ScriptedTileLabel.Config) read(context, "label", ScriptedTileLabel.Config.class, PROJECT_LABEL);
		TileLabelProvider result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static Object read(DefaultInstantiationContext context, String tag, Class<?> type, String xml)
			throws ConfigurationException {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(type);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(tag, descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		return reader.read();
	}

	private static void write(File file, String content) throws IOException {
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The labels are TL-Script expressions over modules of the application model, which
	 *           lives in the knowledge base.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTileFrameLabelObservation.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}

}
