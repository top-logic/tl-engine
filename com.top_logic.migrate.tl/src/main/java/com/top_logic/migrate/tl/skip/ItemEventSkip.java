/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.NoProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.ConfiguredRewritingEventVisitor;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * {@link EventRewriter} that skips item events that matches a certain filter.
 * 
 * <p>
 * Note: If an object was skipped, this object can not be reactivated in a later commit. In such
 * case the result is undefined.
 * </p>
 * 
 * <p>
 * Note: The old values of an {@link ItemUpdate} are not updated.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ItemEventSkip extends ConfiguredRewritingEventVisitor<ItemEventSkip.Config> {

	private final Set<ObjectBranchId> skippedIdentifiers = new HashSet<>();

	private final SkipFilter skipFilter;

	private final SkippedReferenceHandle skipHandle;

	private Log _log = NoProtocol.INSTANCE;

	/**
	 * Configuration options for {@link ItemEventSkip}.
	 */
	public interface Config extends ConfiguredRewritingEventVisitor.Config<ItemEventSkip> {

		/**
		 * {@link SkipFilter} that decides about skipped events.
		 */
		@Mandatory
		@Name("filter")
		@ImplementationClassDefault(TypeSkip.class)
		PolymorphicConfiguration<? extends SkipFilter> getFilter();

		/**
		 * Setter for {@link #getFilter()}.
		 */
		void setFilter(PolymorphicConfiguration<? extends SkipFilter> filter);

		/**
		 * {@link SkippedReferenceHandle} processing references to dropped objects.
		 */
		@Name("reference-handler")
		@ItemDefault
		@ImplementationClassDefault(DefaultSkippedReferenceHandle.class)
		PolymorphicConfiguration<? extends SkippedReferenceHandle> getReferenceHandler();

		/**
		 * Setter for {@link #getReferenceHandler()}.
		 */
		void setReferenceHandler(PolymorphicConfiguration<? extends SkippedReferenceHandle> handler);

	}

	/**
	 * Creates a {@link ItemEventSkip} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ItemEventSkip(InstantiationContext context, Config config) {
		super(context, config);
		this.skipFilter = context.getInstance(config.getFilter());
		this.skipHandle = context.getInstance(config.getReferenceHandler());
		setLog(new LogProtocol(ItemEventSkip.class));
	}

	/**
	 * Sets the {@link Log} to log to.
	 */
	public void setLog(Log log) {
		this._log = log;
	}

	/**
	 * Creates an {@link ItemEventSkip} with given {@link SkipFilter} and default
	 * {@link SkippedReferenceHandle}.
	 */
	public static ItemEventSkip newItemEventSkip(PolymorphicConfiguration<? extends SkipFilter> skipFilter,
			Log log) {
		return newItemEventSkip(skipFilter, null, log);
	}

	/**
	 * Creates an {@link ItemEventSkip} with given {@link SkipFilter} and
	 * {@link SkippedReferenceHandle}.
	 */
	public static ItemEventSkip newItemEventSkip(PolymorphicConfiguration<? extends SkipFilter> skipFilter,
			PolymorphicConfiguration<? extends SkippedReferenceHandle> skipHandle, Log log) {
		ItemEventSkip.Config conf = TypedConfiguration.newConfigItem(ItemEventSkip.Config.class);
		conf.setImplementationClass(ItemEventSkip.class);
		conf.setFilter(skipFilter);
		if (skipHandle != null) {
			conf.setReferenceHandler(skipHandle);
		}
		ItemEventSkip result = (ItemEventSkip) TypedConfigUtil.createInstance(conf);
		result.setLog(log);
		return result;
	}

	@Override
	protected void processCreations(ChangeSet cs) {
		super.processCreations(cs);

		boolean referenceCreationSkipped = true;
		while (referenceCreationSkipped) {
			referenceCreationSkipped = false;

			// Check which ObjectCreation refers to an object which is skipped.
			Iterator<ObjectCreation> creations = cs.getCreations().iterator();
			creationsIterator:
			while (creations.hasNext()) {
				ObjectCreation creation = creations.next();
				List<? extends MOReference> references = MetaObjectUtils.getReferences(creation.getObjectType());
				for (MOReference reference : references) {
					ObjectKey referenceKey = (ObjectKey) creation.getValues().get(reference.getName());
					if (referenceKey == null) {
						// reference is null, no update necessary
						continue;
					}
					ObjectBranchId unversiondId = ObjectBranchId.toObjectBranchId(referenceKey);
					if (skippedIdentifiers.contains(unversiondId)) {
						// references a skipped object
						boolean skipCreation =
							skipHandle.handleSkippedReference(creation, reference, skippedIdentifiers);
						if (skipCreation) {
							addSkippedObjectKey(creation);
							_log.info("Skip replay of '" + creation + "' as created item refers to skipped item '"
								+ unversiondId + "'.");
							referenceCreationSkipped = true;
							creations.remove();
							continue creationsIterator;
						}
					}
				}
			}

		}

	}

	private boolean addSkippedObjectKey(ObjectCreation creation) {
		return skippedIdentifiers.add(creation.getObjectId());
	}

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		boolean skipCreation = skipFilter.skipEvent(event, skippedIdentifiers);
		if (skipCreation) {
			addSkippedObjectKey(event);
			_log.info("Skip replay of '" + event + "' as event is accepted by '" + skipFilter + "'", Protocol.DEBUG);
			return SKIP_EVENT;
		}
		return super.visitCreateObject(event, arg);
	}

	@Override
	public Object visitDelete(ItemDeletion event, Void arg) {
		if (skippedIdentifiers.contains(event.getObjectId())) {
			// not created no deletion
			_log.info("Skip replay of '" + event + "' as creation of deleted object was skipped", Protocol.DEBUG);
			return SKIP_EVENT;
		}
		return super.visitDelete(event, arg);
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		if (skippedIdentifiers.contains(event.getObjectId())) {
			// not created no update
			_log.info("Skip replay of '" + event + "' as creation of touched object was skipped", Protocol.DEBUG);
			return SKIP_EVENT;
		}
		boolean skipUpdate = checkUpdate(event);
		if (skipUpdate) {
			return SKIP_EVENT;
		}
		return super.visitUpdate(event, arg);
	}

	/**
	 * <code>true</code> iff event must be skipped.
	 */
	private boolean checkUpdate(ItemUpdate event) {
		Map<String, Object> values = event.getValues();
		List<? extends MOReference> references = MetaObjectUtils.getReferences(event.getObjectType());
		for (MOReference reference : references) {
			ObjectKey referenceKey = (ObjectKey) values.get(reference.getName());
			if (referenceKey == null) {
				// reference is null, no update necessary
				continue;
			}
			ObjectBranchId unversionedId = ObjectBranchId.toObjectBranchId(referenceKey);
			if (skippedIdentifiers.contains(unversionedId)) {
				// reference was set to skipped object
				boolean skipUpdate = skipHandle.handleSkippedReference(event, reference, skippedIdentifiers);
				if (skipUpdate) {
					return true;
				}
			}
		}
		return false;
	}
}
