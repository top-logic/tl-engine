package com.top_logic.chat.builder;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;

import com.top_logic.chat.model.Chat;
import com.top_logic.chat.model.ChatRoot;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link ListModelBuilder} that provides all {@link Chat}s from the {@link ChatRoot} singleton,
 * sorted by most recent message first.
 */
public class AllChatsModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link AllChatsModelBuilder} instance.
	 */
	public static final AllChatsModelBuilder INSTANCE = new AllChatsModelBuilder();

	private AllChatsModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		ChatRoot root = getChatRoot();
		if (root == null) {
			return java.util.Collections.emptyList();
		}

		// Sort chats by lastMessageAt descending (most recent first)
		return root.getChats().stream()
			.sorted(Comparator.comparing(
				(Chat chat) -> {
					Date lastMsg = chat.getLastMessageAt();
					return lastMsg != null ? lastMsg : chat.getCreatedAt();
				},
				Comparator.nullsLast(Comparator.reverseOrder())
			))
			.collect(Collectors.toList());
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		// Model can be null (will fetch ChatRoot singleton)
		// or can be ChatRoot itself
		return model == null || model instanceof ChatRoot;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object listElement) {
		// When a chat is selected, the component model remains ChatRoot
		return getChatRoot();
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

		// Get singleton instance
		Object singleton = TLModelUtil.getSingleton(chatRootType);
		if (singleton instanceof ChatRoot) {
			return (ChatRoot) singleton;
		}

		return null;
	}
}
