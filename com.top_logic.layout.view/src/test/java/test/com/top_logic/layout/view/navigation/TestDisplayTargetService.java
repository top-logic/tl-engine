/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.navigation;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.navigation.Binding;
import com.top_logic.layout.view.navigation.DisplayTarget;
import com.top_logic.layout.view.navigation.DisplayTargetService;
import com.top_logic.layout.view.navigation.DisplayTargets;
import com.top_logic.layout.view.navigation.MountPath;
import com.top_logic.layout.view.navigation.ShowStep;
import com.top_logic.layout.view.navigation.ViewMounts;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests for the display targets of {@link DisplayTargetService}: their configuration, and the
 * {@link DisplayTargets} answering where an object of a given type is displayed.
 */
public class TestDisplayTargetService extends TestCase {

	private static final String ROOT = "mounts-root.view.xml";

	private static final String SHARED = "mounts-shared.view.xml";

	private static final String TILE = "mounts-tile.view.xml";

	private static final String ORPHAN = "mounts-orphan.view.xml";

	private TLModelImpl _model;

	private TLClass _general;

	private TLClass _special;

	private TLClass _unrelated;

	private ViewMounts _mounts;

	private Supplier<ViewMounts> _mountAccess;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_model = new TLModelImpl();
		_model.addCoreModule();
		TLModule module = TLModelUtil.addModule(_model, "test.navigation");
		_general = TLModelUtil.addClass(module, "General");
		_special = TLModelUtil.addClass(module, "Special");
		_special.getGeneralizations().add(_general);
		_unrelated = TLModelUtil.addClass(module, "Unrelated");

		_mounts = ViewMounts.scan(ROOT, new FixtureViews());
		_mountAccess = () -> _mounts;
	}

	/**
	 * The target of the object's own type precedes the one of its generalization.
	 */
	public void testExactTypeBeatsGeneralization() {
		DisplayTarget forGeneral = target(_general, false, SHARED);
		DisplayTarget forSpecial = target(_special, false, SHARED);
		DisplayTargets targets = targets(forGeneral, forSpecial);

		assertEquals(List.of(forSpecial, forGeneral), targets.resolve(_special, null));
		assertSame(forSpecial, targets.resolveBest(_special, null));
		assertEquals("The generalization is not displayed where its specialization is.",
			List.of(forGeneral), targets.resolve(_general, null));
	}

	/**
	 * Among targets of the same type, the one displayed nearest to the caller wins; one displayed
	 * nowhere ranks last, and a dialog is at the caller wherever that is.
	 */
	public void testNearestMountWins() {
		DisplayTarget viaRoot = target(_special, false, ROOT);
		DisplayTarget viaTile = target(_special, false, TILE);
		DisplayTarget viaOrphan = target(_special, false, ORPHAN);
		DisplayTarget viaDialog = new DisplayTarget(_special, false,
			List.of(new ShowStep(ORPHAN, true, null, null, List.of())));
		DisplayTargets targets = targets(viaRoot, viaTile, viaOrphan, viaDialog);

		MountPath fromShared = mount(SHARED, 0);
		assertEquals("The tile view shares the sidebar item with the caller, the root view nothing.",
			List.of(viaTile, viaRoot, viaDialog, viaOrphan), targets.resolve(_special, fromShared));

		assertEquals("Without a caller, no mount is nearer than another.",
			List.of(viaRoot, viaTile, viaDialog, viaOrphan), targets.resolve(_special, null));
	}

	/**
	 * Among equally near targets, the one marked as default wins.
	 */
	public void testDefaultBeatsDeclarationOrder() {
		DisplayTarget first = target(_special, false, SHARED);
		DisplayTarget preferred = target(_special, true, SHARED);
		DisplayTargets targets = targets(first, preferred);

		assertEquals(List.of(preferred, first), targets.resolve(_special, null));
	}

	/**
	 * Among equally preferred targets, the one declared first wins.
	 */
	public void testDeclarationOrderDecidesLast() {
		DisplayTarget first = new DisplayTarget(_special, false,
			List.of(new ShowStep(SHARED, false, null, null, List.of(new Binding("item", null)))));
		DisplayTarget second = new DisplayTarget(_special, false,
			List.of(new ShowStep(SHARED, false, null, null, List.of(new Binding("filter", null)))));
		DisplayTargets targets = targets(first, second);

		assertEquals(List.of(first, second), targets.resolve(_special, null));
		assertSame(first, targets.resolveBest(_special, null));
	}

	/**
	 * A type is displayable through its own target and through the target of a generalization,
	 * everything else is not.
	 */
	public void testHasTarget() {
		DisplayTargets targets = targets(target(_general, false, SHARED));

		assertTrue(targets.hasTarget(_general));
		assertTrue("Inherited from the generalization.", targets.hasTarget(_special));
		assertFalse(targets.hasTarget(_unrelated));
		assertFalse(targets.hasTarget(null));
		assertNull(targets.resolveBest(_unrelated, null));
	}

	/**
	 * A binding without an expression passes the displayed object itself.
	 */
	public void testBindingWithoutExpressionYieldsObject() {
		Object shown = new Object();

		assertSame(shown, new Binding("item", null).evaluate(shown));
	}

	/**
	 * A binding with an expression passes what the expression computes from the displayed object.
	 */
	public void testBindingEvaluatesExpression() {
		QueryExecutor expr = QueryExecutor.interpret(null, _model, lambda("x", isEmpty(var("x"))));
		Binding binding = new Binding("item", expr);

		assertEquals(Boolean.FALSE, binding.evaluate("displayed"));
		assertEquals(Boolean.TRUE, binding.evaluate(null));
	}

	/**
	 * A label expression names the view after the object being displayed, a fixed label always the
	 * same.
	 */
	public void testLabelIsComputedFromTheShownObject() {
		QueryExecutor identity = QueryExecutor.interpret(null, _model, lambda("x", var("x")));
		ResKey fixed = ResKey.text("Fixed");

		assertEquals(fixed, new ShowStep(SHARED, false, fixed, null, List.of()).labelFor("Shown"));
		assertEquals(ResKey.text("Shown"),
			new ShowStep(SHARED, false, fixed, identity, List.of()).labelFor("Shown"));
		assertNull(new ShowStep(SHARED, false, null, null, List.of()).labelFor("Shown"));
	}

	/**
	 * A binding addressing a channel the displayed view does not declare is reported.
	 */
	public void testMissingChannelIsReported() {
		DisplayTargets targets = targets(new DisplayTarget(_special, false,
			List.of(new ShowStep(SHARED, false, null, null,
				List.of(new Binding("item", null), new Binding("noSuchChannel", null))))));

		BufferingProtocol log = new BufferingProtocol();
		targets.checkBindings(log, _mounts::getChannelNames);

		List<String> errors = log.getErrors();
		assertEquals(errors.toString(), 1, errors.size());
		String error = errors.get(0);
		assertTrue(error, error.contains("noSuchChannel"));
		assertTrue(error, error.contains(SHARED));
		assertTrue(error, error.contains(TLModelUtil.qualifiedName(_special)));
	}

	/**
	 * A view whose declarations are unavailable leaves its bindings unchecked.
	 */
	public void testUnreadableViewIsNotChecked() {
		DisplayTargets targets = targets(new DisplayTarget(_special, false,
			List.of(new ShowStep("does-not-exist.view.xml", false, null, null, List.of(new Binding("item", null))))));

		BufferingProtocol log = new BufferingProtocol();
		targets.checkBindings(log, viewRef -> null);

		assertEquals(List.of(), log.getErrors());
	}

	/**
	 * The configuration of a target: its type, its default flag, and the views it displays with
	 * their labels and bindings.
	 */
	public void testParseConfiguration() throws Exception {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"config", TypedConfiguration.getConfigurationDescriptor(DisplayTargetService.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDisplayTargetService.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(TestDisplayTargetService.class, "display-targets.config.xml"));
		DisplayTargetService.Config config = (DisplayTargetService.Config) reader.read();
		context.checkErrors();

		assertEquals(1, config.getTargets().size());
		DisplayTargetService.TargetConfig target = config.getTargets().get(0);
		assertEquals("test.navigation:Special", target.getType().qualifiedName());
		assertTrue(target.isDefault());
		assertEquals(2, target.getShows().size());

		DisplayTargetService.ShowConfig outer = target.getShows().get(0);
		assertEquals(SHARED, outer.getView());
		assertFalse(outer.isDialog());
		assertNull(outer.getLabel());
		assertNotNull("The view is named after the object it displays.", outer.getLabelExpr());
		assertEquals(1, outer.getBindings().size());
		assertEquals("item", outer.getBindings().get(0).getChannel());
		assertNull("Without an expression, the displayed object is the value.",
			outer.getBindings().get(0).getExpr());

		DisplayTargetService.ShowConfig inner = target.getShows().get(1);
		assertEquals(ORPHAN, inner.getView());
		assertTrue(inner.isDialog());
		assertNotNull(inner.getLabel());
		assertNull("A fixed label needs no expression.", inner.getLabelExpr());
		assertNotNull(inner.getBindings().get(0).getExpr());
	}

	/**
	 * A target displaying the given view, with no bindings.
	 */
	private static DisplayTarget target(TLType type, boolean isDefault, String viewRef) {
		return new DisplayTarget(type, isDefault, List.of(new ShowStep(viewRef, false, null, null, List.of())));
	}

	private DisplayTargets targets(DisplayTarget... targets) {
		return new DisplayTargets(List.of(targets), _mountAccess);
	}

	/**
	 * One of the places the given view is displayed at, as the caller's location.
	 */
	private MountPath mount(String viewRef, int index) {
		List<MountPath> paths = _mounts.getMounts(viewRef);
		assertTrue("View '" + viewRef + "' is displayed " + paths.size() + " times.", index < paths.size());
		return paths.get(index);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestDisplayTargetService.class, TypeIndex.Module.INSTANCE);
	}
}
