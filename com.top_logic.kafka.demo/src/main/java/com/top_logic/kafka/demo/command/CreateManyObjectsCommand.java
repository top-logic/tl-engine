/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.demo.command;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Positive;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.kafka.demo.model.types.KafkaDemoFactory;
import com.top_logic.kafka.demo.model.types.Node;
import com.top_logic.kafka.demo.model.types.UntransferredNode;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.v5.transform.ModelLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Creates "many" objects at once for performance tests.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class CreateManyObjectsCommand extends AbstractCommandHandler {

	/** {@link ConfigurationItem} for the {@link CreateManyObjectsCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@StringDefault(CommandHandlerFactory.CREATE_CLIQUE)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		CommandGroupReference getGroup();

		@Override
		@BooleanDefault(true)
		boolean getConfirm();

		@Override
		@FormattedDefault("theme:KAFKA_DEMO_CREATE_MANY_OBJECTS")
		ThemeImage getImage();

		/** The number of objects to be created. */
		@Constraint(value = Positive.class)
		@IntDefault(100_000)
		int getObjectCount();

	}

	private static final String TL_BEACON_THREE = "tl.beacon.three";

	/** The progress is logged every time that many objects have been created. */
	private static final int LOG_STEP = 10_000;

	/** {@link TypedConfiguration} constructor for {@link CreateManyObjectsCommand}. */
	public CreateManyObjectsCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		int objectCount = getConfigTyped().getObjectCount();
		StopWatch watch = StopWatch.createStartedWatch();
		createAndCommit(objectCount);
		String time = watch.toString();
		InfoService.showInfo(I18NConstants.CREATED_MANY_OBJECTS__COUNT__TIME.fill(objectCount, time));
		logInfo("Creating " + objectCount + " objects took " + time + ".");
		return HandlerResult.DEFAULT_RESULT;
	}

	private void createAndCommit(int objectCount) {
		Message commitMessage = Messages.KAFKA_DEMO_CREATE_MANY_OBJECTS__COUNT.fill(objectCount);
		try (Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction(commitMessage)) {
			createAllNodes(objectCount);
			transaction.commit();
		}
	}

	private void createAllNodes(int objectCount) {
		KafkaDemoFactory factory = KafkaDemoFactory.getInstance();
		Set<TLClassifier> beacons = Set.copyOf(getBeaconEnum().getClassifiers());
		List<Node> nodes = list();
		List<TLObject> untransferredNodes = list();
		Node lastNode = null;
		for (int counter = 0; counter < objectCount; counter++) {
			if ((counter % LOG_STEP) == 0) {
				logInfo("Created " + counter + " of " + objectCount + " objects.");
			}
			Node node = createOneNodeWithData(untransferredNodes, nodes, beacons, lastNode, counter, factory);
			lastNode = node;
		}
	}

	private Node createOneNodeWithData(List<TLObject> untransferredNodes, List<Node> nodes, Set<TLClassifier> beacons,
			Node lastNode, int counter, KafkaDemoFactory factory) {
		UntransferredNode untransferredNode1 = createUntransferredNode(untransferredNodes, factory);
		UntransferredNode untransferredNode2 = createUntransferredNode(untransferredNodes, factory);
		Node node = createNode(factory, counter);
		node.setOtherNode(lastNode);
		node.setClassificationMulti(beacons);
		node.setUntransferredNode1(untransferredNode1);
		node.setUntransferredNode2(untransferredNode2);
		nodes.add(node);
		return node;
	}

	private TLEnumeration getBeaconEnum() {
		return (TLEnumeration) TLModelUtil.findType(ModelLayout.TL5_ENUM_MODULE, TL_BEACON_THREE);
	}

	private UntransferredNode createUntransferredNode(List<TLObject> untransferredNodes, KafkaDemoFactory factory) {
		UntransferredNode node = factory.createUntransferredNode();
		node.setName("Untransferred Node " + untransferredNodes.size());
		untransferredNodes.add(node);
		return node;
	}

	private Node createNode(KafkaDemoFactory factory, int counter) {
		Node node = factory.createNode();
		node.setName("Node " + counter);
		node.setNotExternalized("Value " + counter);
		return node;
	}

	private void logInfo(String message) {
		Logger.info(message, CreateManyObjectsCommand.class);
	}

	/** Correctly typed variant of {@link #getConfig()}. */
	public Config getConfigTyped() {
		return (Config) getConfig();
	}

}
