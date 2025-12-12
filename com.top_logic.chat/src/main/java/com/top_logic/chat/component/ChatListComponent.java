package com.top_logic.chat.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.table.component.TableComponent;

/**
 * Component displaying a list of all chats.
 *
 * <p>
 * This component uses a {@link TableComponent} to display chats with built-in selection support.
 * The selection is automatically published via the component's selection channel and can be
 * consumed by other components.
 * </p>
 */
public class ChatListComponent extends TableComponent {

	/**
	 * Configuration for {@link ChatListComponent}.
	 */
	public interface Config extends TableComponent.Config {

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/**
		 * Icon for the chat list component.
		 */
		@Name(ICON)
		@StringDefault("css:fas fa-comments")
		String getIcon();
	}

	/**
	 * Creates a {@link ChatListComponent} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChatListComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}
}
