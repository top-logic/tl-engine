/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewConfig;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.util.model.ModelService;

/**
 * The catalog of places the application displays its business objects at.
 *
 * <p>
 * A target names a model type and the views that display an object of that type: the enclosing
 * displays first, the one holding the object itself last. Each view receives the values its
 * bindings compute from the object being displayed.
 * </p>
 *
 * <pre>
 * &lt;config service-class="com.top_logic.layout.view.navigation.DisplayTargetService"&gt;
 *   &lt;instance&gt;
 *     &lt;targets&gt;
 *       &lt;target type="demo.tickets:Ticket"&gt;
 *         &lt;show view="milestones.view.xml"&gt;
 *           &lt;bind channel="milestone"
 *             expr="t -&gt; $t.get(`demo.tickets:Ticket#milestone`)"
 *           /&gt;
 *         &lt;/show&gt;
 *         &lt;show view="ticket-detail.view.xml"&gt;
 *           &lt;bind channel="ticket"/&gt;
 *         &lt;/show&gt;
 *       &lt;/target&gt;
 *     &lt;/targets&gt;
 *   &lt;/instance&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * <p>
 * Declaring the targets in one place rather than at the views displaying the objects lets an object
 * be displayed from anywhere: whoever holds an object asks this service where its type belongs, and
 * offers the object as a link exactly when a target answers.
 * </p>
 *
 * @see DisplayTargets
 */
@Label("Display targets")
@ServiceDependencies({
	ModelService.Module.class,
})
public class DisplayTargetService extends ConfiguredManagedClass<DisplayTargetService.Config> {

	/**
	 * Configuration of the {@link DisplayTargetService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<DisplayTargetService> {

		/** Configuration name for {@link #getTargets()}. */
		String TARGETS = "targets";

		/** Entry tag of {@link #getTargets()}. */
		String TARGET = "target";

		@Override
		@ClassDefault(DisplayTargetService.class)
		Class<? extends DisplayTargetService> getImplementationClass();

		/**
		 * The types whose objects can be displayed, and where.
		 *
		 * <p>
		 * Several entries may declare the same type. The one that is displayed nearest to the place
		 * the object is offered at wins, and among equally near ones the entry marked as default,
		 * then the first declared.
		 * </p>
		 */
		@Name(TARGETS)
		@EntryTag(TARGET)
		List<TargetConfig> getTargets();
	}

	/**
	 * Where the objects of one model type are displayed.
	 */
	public interface TargetConfig extends ConfigurationItem {

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #isDefault()}. */
		String DEFAULT = "default";

		/** Configuration name for {@link #getShows()}. */
		String SHOWS = "shows";

		/** Entry tag of {@link #getShows()}. */
		String SHOW = "show";

		/**
		 * The type whose objects are displayed here.
		 *
		 * <p>
		 * An object is displayed by the target of its own type; if there is none, by the target of
		 * its nearest generalization.
		 * </p>
		 */
		@Name(TYPE)
		@Mandatory
		TLModelPartRef getType();

		/**
		 * Whether to prefer this target over the other targets declared for the same type.
		 */
		@Name(DEFAULT)
		@BooleanDefault(false)
		boolean isDefault();

		/**
		 * The views displaying the object, the enclosing ones first, the one holding the object
		 * itself last.
		 *
		 * @implNote Written directly as {@code <show>} children of the {@code <target>}.
		 */
		@Name(SHOWS)
		@EntryTag(SHOW)
		@DefaultContainer
		@Mandatory
		List<ShowConfig> getShows();
	}

	/**
	 * One view displaying the object, and the values its channels receive.
	 */
	public interface ShowConfig extends ConfigurationItem {

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/** Configuration name for {@link #isDialog()}. */
		String DIALOG = "dialog";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getLabelExpr()}. */
		String LABEL_EXPR = "label-expr";

		/** Configuration name for {@link #getBindings()}. */
		String BINDINGS = "bindings";

		/** Entry tag of {@link #getBindings()}. */
		String BIND = "bind";

		/**
		 * Path of the view file to display, relative to the application's view directory.
		 */
		@Name(VIEW)
		@Mandatory
		String getView();

		/**
		 * Whether to display the view as a dialog on top of the current display, rather than within
		 * it.
		 */
		@Name(DIALOG)
		@BooleanDefault(false)
		boolean isDialog();

		/**
		 * The label the displayed view is announced with, e.g. in a breadcrumb or as a dialog
		 * title.
		 *
		 * <p>
		 * Empty leaves the naming to the display itself.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * Function computing the label of the displayed view from the object being displayed.
		 *
		 * <p>
		 * Takes precedence over the fixed label. A view that the user reaches by drilling down is
		 * announced with the name of the object it shows, so a target leading to the same place
		 * must compute the same label to arrive at the drill-down the user already has.
		 * </p>
		 */
		@Name(LABEL_EXPR)
		@Nullable
		Expr getLabelExpr();

		/**
		 * The values the channels of the displayed view receive.
		 *
		 * @implNote Written directly as {@code <bind>} children of the {@code <show>}.
		 */
		@Name(BINDINGS)
		@EntryTag(BIND)
		@DefaultContainer
		List<BindConfig> getBindings();
	}

	/**
	 * The value one channel of a displayed view receives.
	 */
	public interface BindConfig extends ConfigurationItem {

		/** Configuration name for {@link #getChannel()}. */
		String CHANNEL = "channel";

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/**
		 * Name of the channel in the displayed view that receives the value.
		 */
		@Name(CHANNEL)
		@Mandatory
		String getChannel();

		/**
		 * Function computing the channel value from the object being displayed.
		 *
		 * <p>
		 * Empty passes the displayed object itself.
		 * </p>
		 */
		@Name(EXPR)
		@Nullable
		Expr getExpr();
	}

	private DisplayTargets _targets;

	/**
	 * Creates a {@link DisplayTargetService} from configuration.
	 */
	@CalledByReflection
	public DisplayTargetService(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		List<DisplayTarget> targets = new ArrayList<>();
		for (TargetConfig target : getConfig().getTargets()) {
			DisplayTarget resolved = resolve(target);
			if (resolved != null) {
				targets.add(resolved);
			}
		}
		_targets = new DisplayTargets(targets, DisplayTargetService::applicationMounts);
		_targets.checkBindings(new LogProtocol(DisplayTargetService.class), DisplayTargetService::declaredChannels);
	}

	/**
	 * Builds the target of a configured type, or {@code null} if its type is unknown to the
	 * application model.
	 */
	private DisplayTarget resolve(TargetConfig config) {
		TLModelPartRef typeRef = config.getType();
		TLType type;
		try {
			type = typeRef.resolveType();
		} catch (RuntimeException ex) {
			Logger.error("Skipping a display target for the unresolvable type '" + typeRef.qualifiedName() + "'.", ex,
				DisplayTargetService.class);
			return null;
		}
		if (type == null) {
			Logger.error("Skipping a display target for the unknown type '" + typeRef.qualifiedName() + "'.",
				DisplayTargetService.class);
			return null;
		}

		List<ShowStep> shows = new ArrayList<>();
		for (ShowConfig show : config.getShows()) {
			List<Binding> bindings = new ArrayList<>();
			for (BindConfig binding : show.getBindings()) {
				bindings.add(new Binding(binding.getChannel(), QueryExecutor.compileOptional(binding.getExpr())));
			}
			shows.add(new ShowStep(ViewLoader.viewRef(show.getView()), show.isDialog(), show.getLabel(),
				QueryExecutor.compileOptional(show.getLabelExpr()), bindings));
		}
		return new DisplayTarget(type, config.isDefault(), shows);
	}

	/**
	 * The display targets of the application.
	 */
	public DisplayTargets getTargets() {
		return _targets;
	}

	/**
	 * Whether an object of the given type can be displayed.
	 *
	 * @see DisplayTargets#hasTarget(TLType)
	 */
	public boolean hasTarget(TLType type) {
		return _targets.hasTarget(type);
	}

	/**
	 * The targets displaying an object of the given type, best first.
	 *
	 * @see DisplayTargets#resolve(TLType, MountPath)
	 */
	public List<DisplayTarget> resolve(TLType type, MountPath nearest) {
		return _targets.resolve(type, nearest);
	}

	/**
	 * The target displaying an object of the given type, or {@code null} if there is none.
	 *
	 * @see DisplayTargets#resolveBest(TLType, MountPath)
	 */
	public DisplayTarget resolveBest(TLType type, MountPath nearest) {
		return _targets.resolveBest(type, nearest);
	}

	/**
	 * The places the application's views are displayed at, seen from its root view.
	 */
	private static ViewMounts applicationMounts() {
		return ViewMounts.forRootView(ApplicationConfig.getInstance().getConfig(ViewConfig.class).getDefaultView());
	}

	/**
	 * The channels the given view declares, or {@code null} if the view cannot be read.
	 *
	 * @implNote Reads the view's own configuration (including the overlays contributed to it), so
	 *           that a view is checked whether it is displayed within the application's root view
	 *           or reached by displaying it anew.
	 */
	private static Set<String> declaredChannels(String viewRef) {
		try {
			ViewElement.Config config = ViewLoader.getOrLoadConfig(ViewLoader.fullPath(viewRef));
			Set<String> result = new LinkedHashSet<>();
			for (ChannelConfig channel : config.getChannels()) {
				result.add(channel.getName());
			}
			return result;
		} catch (ConfigurationException | RuntimeException ex) {
			Logger.error("Cannot check the bindings of the display target displaying the view '" + viewRef
				+ "': the view could not be read.", ex, DisplayTargetService.class);
			return null;
		}
	}

	/**
	 * The {@link DisplayTargetService} singleton.
	 */
	public static DisplayTargetService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link DisplayTargetService}.
	 */
	public static final class Module extends TypedRuntimeModule<DisplayTargetService> {

		/** Singleton {@link DisplayTargetService.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<DisplayTargetService> getImplementation() {
			return DisplayTargetService.class;
		}

	}

}
