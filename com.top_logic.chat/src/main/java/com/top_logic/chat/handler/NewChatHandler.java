package com.top_logic.chat.handler;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.chat.model.Chat;
import com.top_logic.chat.model.ChatRoot;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.model.ModelService;

/**
 * Command handler for creating a new chat.
 *
 * <p>
 * Creates a new {@link Chat} instance with a unique ID and adds it to the {@link ChatRoot}.
 * The new chat is then selected in the chat list.
 * </p>
 */
public class NewChatHandler extends AbstractCommandHandler {

	/** Command ID for this handler. */
	public static final String COMMAND_ID = "newChat";

	/**
	 * Creates a {@link NewChatHandler} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NewChatHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		try {
			// Get ChatRoot singleton
			ChatRoot root = getChatRoot();
			if (root == null) {
				return HandlerResult.error("Chat system not initialized.");
			}

			// Create new Chat instance
			Chat newChat = (Chat) TLModelUtil.createObject(getChatType());
			newChat.setChatId(UUID.randomUUID().toString());
			newChat.setCreatedAt(new Date());

			// Add to ChatRoot
			root.getChats().add(newChat);

			tx.commit();

			// Select the new chat in the table component
			if (aComponent instanceof TableComponent) {
				((TableComponent) aComponent).setSelected(newChat);
			}

			return HandlerResult.DEFAULT_RESULT;

		} catch (Exception ex) {
			return HandlerResult.error("Failed to create new chat: " + ex.getMessage());
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Get the {@link ChatRoot} singleton instance.
	 */
	private ChatRoot getChatRoot() {
		TLModel model = ModelService.getApplicationModel();
		TLClass chatRootType = (TLClass) model.getType("Chat:ChatRoot");
		if (chatRootType == null) {
			return null;
		}

		Object singleton = TLModelUtil.getSingleton(chatRootType);
		if (singleton instanceof ChatRoot) {
			return (ChatRoot) singleton;
		}

		return null;
	}

	/**
	 * Get the {@link Chat} TLClass.
	 */
	private TLClass getChatType() {
		TLModel model = ModelService.getApplicationModel();
		return (TLClass) model.getType("Chat:Chat");
	}
}
