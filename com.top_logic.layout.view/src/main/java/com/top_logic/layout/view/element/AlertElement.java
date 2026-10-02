/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ButtonSize;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActions;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} showing a highlighted message in the content of a page: a hint, a warning, the
 * reason why something cannot be done right now.
 *
 * <p>
 * The message is either fixed text or computed by TL-Script functions over the values of the
 * {@link Config#getInputs() input channels}. An optional condition decides whether the message is
 * shown at all. The functions are evaluated again whenever an input channel gets a new value, and
 * when one of the input objects itself is changed - a message typically speaks about an attribute of
 * that object, which can be edited without the channel value changing. A function reaching beyond
 * the input objects states the types it navigates to in {@link Config#getObservedTypes()}.
 * </p>
 *
 * <p>
 * A {@link Config#isClosable() closable} message can be dismissed by the user. A dismissed message
 * stays closed as long as nothing changes; it is shown again as soon as an input channel gets a new
 * value, or as soon as an evaluation yields another visibility, title or message than the one the
 * user dismissed. The dismissal is not stored: a new session, which builds the view anew, shows it
 * again. Leaving the page and coming back within the same session keeps it closed.
 * </p>
 *
 * <p>
 * Example: a warning about an overdue ticket, offering to escalate it.
 * </p>
 *
 * <pre>
 * &lt;alert severity="warning" inputs="ticket"
 *     visible-if="t -&gt; $t.get(`my.app:Ticket#overdue`)"
 *     message-expr="t -&gt; #('The ticket is overdue.'@en, 'Das Ticket ist überfällig.'@de)"
 *     closable="true"&gt;
 *   &lt;generic-command input="ticket"&gt;
 *     &lt;label&gt;&lt;en&gt;Escalate&lt;/en&gt;&lt;de&gt;Eskalieren&lt;/de&gt;&lt;/label&gt;
 *     ...
 *   &lt;/generic-command&gt;
 * &lt;/alert&gt;
 * </pre>
 *
 * @implNote The control is a {@link ReactAlertControl}. The input objects are observed by a
 *           {@link ChannelObjectObserver}, the observation shared with {@link SwitchElement} and
 *           {@link TextElement}.
 */
@InApp
public class AlertElement implements UIElement {

	/**
	 * Configuration for {@link AlertElement}.
	 */
	@TagName("alert")
	public interface Config extends UIElement.Config, Inputs {

		/** Configuration name for {@link #getSeverity()}. */
		String SEVERITY = "severity";

		/** Configuration name for {@link #getVisibleIf()}. */
		String VISIBLE_IF = "visible-if";

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getTitleExpr()}. */
		String TITLE_EXPR = "title-expr";

		/** Configuration name for {@link #getMessage()}. */
		String MESSAGE = "message";

		/** Configuration name for {@link #getMessageExpr()}. */
		String MESSAGE_EXPR = "message-expr";

		/** Configuration name for {@link #isClosable()}. */
		String CLOSABLE = "closable";

		/** Configuration name for {@link #getOnDismiss()}. */
		String ON_DISMISS = "on-dismiss";

		/** Configuration name for {@link #getCommands()}. */
		String COMMANDS = "commands";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		@Override
		@ClassDefault(AlertElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * How serious the message is: an {@link Variant#INFO information} (the default), a
		 * {@link Variant#SUCCESS success}, a {@link Variant#WARNING warning} or an
		 * {@link Variant#ERROR error}.
		 *
		 * <p>
		 * The severity decides the color and the icon of the message.
		 * </p>
		 */
		@Name(SEVERITY)
		Variant getSeverity();

		/**
		 * TL-Script predicate deciding whether the message is shown; without it, the message is
		 * always shown.
		 *
		 * <p>
		 * Called with the values of the {@link #getInputs() input channels} as arguments, in
		 * declaration order; the message is shown while it returns {@code true}.
		 * </p>
		 */
		@Name(VISIBLE_IF)
		@Nullable
		Expr getVisibleIf();

		/**
		 * Fixed title shown above the message.
		 *
		 * <p>
		 * A computed title is given by {@link #getTitleExpr()} instead; at most one of both may be
		 * given. Without either, the message has no title.
		 * </p>
		 */
		@Name(TITLE)
		@Nullable
		ResKey getTitle();

		/**
		 * TL-Script function computing the title shown above the message.
		 *
		 * <p>
		 * Called with the values of the {@link #getInputs() input channels} as arguments, in
		 * declaration order. The result is shown as text: an internationalized literal in the
		 * language of the user, any other object by its label. A fixed title is given by
		 * {@link #getTitle()} instead; at most one of both may be given.
		 * </p>
		 */
		@Name(TITLE_EXPR)
		@Nullable
		Expr getTitleExpr();

		/**
		 * Fixed message text.
		 *
		 * <p>
		 * A computed message is given by {@link #getMessageExpr()} instead; exactly one of both must
		 * be given.
		 * </p>
		 */
		@Name(MESSAGE)
		@Nullable
		ResKey getMessage();

		/**
		 * TL-Script function computing the message text.
		 *
		 * <p>
		 * Called with the values of the {@link #getInputs() input channels} as arguments, in
		 * declaration order. The result is shown as text: an internationalized literal in the
		 * language of the user, any other object by its label. A fixed message is given by
		 * {@link #getMessage()} instead; exactly one of both must be given.
		 * </p>
		 */
		@Name(MESSAGE_EXPR)
		@Nullable
		Expr getMessageExpr();

		/**
		 * Whether the user can dismiss the message.
		 *
		 * <p>
		 * A dismissed message is shown again as soon as an input channel gets a new value, or an
		 * evaluation yields another visibility, title or message than the one dismissed.
		 * </p>
		 */
		@Name(CLOSABLE)
		boolean isClosable();

		/**
		 * Actions run after the user dismissed the message, each action's result becoming the input
		 * of the next one.
		 *
		 * <p>
		 * The first action receives the value of the first {@link #getInputs() input channel}, or
		 * nothing when no input is declared.
		 * </p>
		 */
		@Name(ON_DISMISS)
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getOnDismiss();

		/**
		 * Commands offered as buttons in the message, e.g. to resolve what it speaks about.
		 *
		 * @implNote Written directly as children of the {@code <alert>}.
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();

		/**
		 * Types whose object changes (create / update / delete) trigger a new evaluation of the
		 * message, in addition to the objects of the {@link #getInputs() input channels}, which are
		 * always observed.
		 *
		 * <p>
		 * Configure this only for a function that navigates beyond the input objects, e.g. one
		 * deciding by an attribute of an input's container: a change of that other object is
		 * invisible to the observation of the inputs. Empty (default) observes just the input
		 * objects.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();
	}

	/**
	 * Appearance of the buttons of the {@link Config#getCommands() commands}: they stand inside the
	 * colored area of the message and must not compete with it.
	 */
	private static final ButtonAppearance COMMAND_APPEARANCE = ButtonAppearance.GHOST;

	/**
	 * Size of the buttons of the {@link Config#getCommands() commands}, fitting the text of the
	 * message.
	 */
	private static final ButtonSize COMMAND_SIZE = ButtonSize.SMALL;

	private final Config _config;

	private final QueryExecutor _visibleIf;

	private final QueryExecutor _titleExpr;

	private final QueryExecutor _messageExpr;

	private final GenericViewCommand _onDismiss;

	private final List<ViewCommand> _commands;

	private final List<ViewCommand.Config> _commandConfigs;

	/**
	 * Creates a new {@link AlertElement} from configuration.
	 */
	@CalledByReflection
	public AlertElement(InstantiationContext context, Config config) {
		_config = config;
		_visibleIf = QueryExecutor.compileOptional(config.getVisibleIf());
		_titleExpr = QueryExecutor.compileOptional(config.getTitleExpr());
		_messageExpr = QueryExecutor.compileOptional(config.getMessageExpr());

		if (config.getTitle() != null && config.getTitleExpr() != null) {
			context.error("An <alert> states its title either as fixed text ('" + Config.TITLE
				+ "') or as a function ('" + Config.TITLE_EXPR + "'), not both.");
		}
		if (config.getMessage() != null && config.getMessageExpr() != null) {
			context.error("An <alert> states its message either as fixed text ('" + Config.MESSAGE
				+ "') or as a function ('" + Config.MESSAGE_EXPR + "'), not both.");
		} else if (config.getMessage() == null && config.getMessageExpr() == null) {
			context.error("An <alert> requires a message, either as fixed text ('" + Config.MESSAGE
				+ "') or as a function ('" + Config.MESSAGE_EXPR + "').");
		}

		List<ViewAction> onDismiss = ViewActions.instantiate(context, config.getOnDismiss());
		_onDismiss = onDismiss.isEmpty() ? null : new GenericViewCommand(onDismiss);

		_commands = new ArrayList<>();
		_commandConfigs = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewCommand> commandConfig : config.getCommands()) {
			ViewCommand command = context.getInstance(commandConfig);
			if (command != null && commandConfig instanceof ViewCommand.Config viewCommandConfig) {
				_commands.add(command);
				_commandConfigs.add(viewCommandConfig);
			}
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> inputs = ChannelInputs.resolve(context, _config.getInputs());

		ReactAlertControl alert = new ReactAlertControl(context);
		alert.setCssClass(_config.getCssClass());
		alert.setVariant(_config.getSeverity());
		alert.setClosable(_config.isClosable());
		alert.setActions(commandButtons(context, alert));

		Display display = new Display(alert, inputs);
		alert.setDismissHandler(display::dismissed);

		ChannelListener inputListener = (sender, oldValue, newValue) -> display.inputChanged();
		for (ViewChannel input : inputs) {
			input.addListener(inputListener);
		}
		alert.addCleanupAction(() -> {
			for (ViewChannel input : inputs) {
				input.removeListener(inputListener);
			}
		});

		ChannelObjectObserver observer = new ChannelObjectObserver(inputs,
			ObservedTypes.resolve(_config.getObservedTypes()), display::update);
		alert.addAttachListener(() -> observer.attach(context.getModelScope()));
		alert.addDetachListener(observer::detach);

		display.update();
		return alert;
	}

	/**
	 * The buttons of the configured commands, following their inputs while the given alert is
	 * displayed.
	 */
	private List<ReactButtonControl> commandButtons(ViewContext context, ReactAlertControl alert) {
		List<ViewCommandModel> models = ViewCommands.buildCommandModels(context, _commands, _commandConfigs);
		List<ReactButtonControl> buttons = new ArrayList<>(models.size());
		for (int n = 0; n < models.size(); n++) {
			ReactButtonControl button = new ReactButtonControl(context, models.get(n));
			if (_commandConfigs.get(n).getImage() != null) {
				button.setDisplayMode(ButtonDisplayMode.ICON_LABEL);
			}
			button.setAppearance(COMMAND_APPEARANCE);
			button.setSize(COMMAND_SIZE);
			buttons.add(button);
		}
		ViewCommands.registerLifecycle(context, models, alert);
		return buttons;
	}

	/**
	 * What the configured functions yield for the current input values.
	 *
	 * @param visible
	 *        Whether the message is to be shown.
	 * @param title
	 *        The title text, {@code null} for none; {@code null} where the message is not visible.
	 * @param message
	 *        The message text; {@code null} where the message is not visible.
	 */
	private record Content(boolean visible, String title, String message) {

		/** The content of a message that is not to be shown. */
		static final Content HIDDEN = new Content(false, null, null);
	}

	/**
	 * The state of one displayed alert: what it shows, and what the user dismissed.
	 */
	private final class Display {

		private final ReactAlertControl _alert;

		private final List<ViewChannel> _inputs;

		/**
		 * The content last evaluated.
		 */
		private Content _current = Content.HIDDEN;

		/**
		 * The content the user dismissed, {@code null} while the message is not dismissed.
		 */
		private Content _dismissed;

		Display(ReactAlertControl alert, List<ViewChannel> inputs) {
			_alert = alert;
			_inputs = inputs;
		}

		/**
		 * Reacts to a new value of an input channel: a dismissed message is shown again.
		 */
		void inputChanged() {
			_dismissed = null;
			update();
		}

		/**
		 * Evaluates the configured functions and shows the result, unless the user dismissed exactly
		 * that result.
		 */
		void update() {
			Content content = evaluate();
			_current = content;
			if (_dismissed != null && !_dismissed.equals(content)) {
				_dismissed = null;
			}
			if (content.visible() && _dismissed == null) {
				_alert.show(_config.getSeverity(), content.title(), content.message());
			} else {
				_alert.hide();
			}
		}

		/**
		 * Records the dismissal of the content shown and runs the configured
		 * {@link Config#getOnDismiss() actions}.
		 */
		HandlerResult dismissed(ReactContext context) {
			_dismissed = _current;
			if (_onDismiss == null) {
				return HandlerResult.DEFAULT_RESULT;
			}
			Object input = _inputs.isEmpty() ? null : _inputs.get(0).get();
			return _onDismiss.execute(context, input);
		}

		private Content evaluate() {
			if (_visibleIf != null && !Boolean.TRUE.equals(apply(_visibleIf))) {
				return Content.HIDDEN;
			}
			String title = text(_config.getTitle(), _titleExpr);
			String message = text(_config.getMessage(), _messageExpr);
			return new Content(true, title, Objects.requireNonNullElse(message, ""));
		}

		private String text(ResKey fixed, QueryExecutor function) {
			if (function != null) {
				return ValueLabel.label(apply(function));
			}
			if (fixed != null) {
				return Resources.getInstance().getString(fixed);
			}
			return null;
		}

		private Object apply(QueryExecutor function) {
			return function.execute(ChannelInputs.arguments(_inputs));
		}
	}

}
