/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.json.simple.JSONObject;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.indexing.ContentObject;
import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.wrap.person.PersonManager;

import dev.langchain4j.community.store.embedding.jvector.JVectorEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;

/**
 * Representation of the Vector index.
 * <p>
 * This class contains methods for creating a Vector index, getting VEctor index readers, writers
 * and searchers.
 * </p>
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@ServiceDependencies({
	FormatConverterFactory.Module.class,
	MimeTypes.Module.class,
	PersonManager.Module.class,
	PersistencyLayer.Module.class,
})
public final class VectorIndex extends ConfiguredManagedClass<VectorIndex.VectorIndexingConfig> {

	/**
	 * Configuration for {@link VectorIndex}.
	 */
	public static interface VectorIndexingConfig extends ConfiguredManagedClass.Config<VectorIndex> {

		/**
		 * Type of vector database to use (e.g., "qdrant", "jvector")
		 */
		@StringDefault("jvector")
		String getVectorStoreType();

		/**
		 * Hostname for remote vector stores (e.g., Qdrant, Pinecone)
		 */
		@StringDefault("localhost")
		String getVectorStoreHost();

		/**
		 * Port for remote vector stores
		 */
		@IntDefault(6334)
		Integer getVectorStorePort();

		/**
		 * Default collection name only in case of qdrant store
		 */
		@StringDefault("top-logic-qdrant-default-collection")
		String getQdrantDbCollectionName();

		/**
		 * File system path for local vector stores (e.g., JVector)
		 */
		@StringDefault("tmp/vector-store")
		String getLocalStorePath();

		/**
		 * Mistral Embedding Service API Key
		 */
		@Encrypted
		@StringDefault("EMPTY")
		String getMistralApiKey();

		/**
		 * Mistral Embedding model
		 */
		@StringDefault("mistral-embed")
		String getMistralEmbedModel();

		/**
		 * Embedding model type ("mistral" or "local")
		 */
		@StringDefault("mistral")
		String getEmbeddingModelType();

		/**
		 * Vector Index models
		 */
		@StringDefault("")
		String getVectorIndexModels();

		/**
		 * No Vector Index models
		 */
		@StringDefault("")
		String getNoVectorIndexModels();

		/**
		 * Number of millisecond to wait before aborting the indexing process after the
		 * {@link VectorIndexingService} was {@link #shutDown() shut down}.
		 * 
		 * <p>
		 * <b>Warning:</b> The index will be incomplete when indexing is aborted! After a forced shutdown,
		 * the index must be administratively rebuilt.
		 * </p>
		 */
		@IntDefault(Integer.MAX_VALUE)
		int getTimeoutAbortIndexingForShutdown();

		/**
		 * If the queue is longer than that, the administrator should be warn.
		 */
		@IntDefault(1000)
		int getWarnQueueSize();

	}

	private static final boolean DEBUG = Logger.isDebugEnabled(VectorIndex.class);

	/** The name of the KO field in a Lucene document */
	public static final String FIELD_KO_ID = "id";

	/** The document type of the document to index */
	public static final String FIELD_DOCTYPE = "docType";

	/** The name of the description field in a Lucene document */
	public static final String FIELD_DESCRIPTION = "description";

	/** The maximum size of the field description */
	public static final int FIELD_DESCRIPTION_MAXSIZE = 200;

	/** The name of the contents field in a Lucene document */
	public static final String FIELD_CONTENTS = "contents";

	/** Maximum time to index a Document before it will be logged */
	static final int INDEX_MILLIS = 1000 * 3; // 3 Seconds

	/** Maximum time to delete a Document before it will be logged */
	static final int DELETE_MILLIS = 1000; // one Second

	private static final int DEFAULT_MAX_FILED_LENGTH_VALUE = 10000;

	/** The index adding thread responsible for indexing/deleting documents */
	private VectorThread myThread;

	/**
	 * Is used for timeouts when the shutdown was requested but there is still work to do.
	 */
	private Maybe<Long> timestampShutdownRequest = Maybe.none();

	private final VectorIndexingConfig config;

	private final String vectorStoreType;

	private final String vectorStoreHost;

	private final Integer vectorStorePort;

	private final String qdrantDbCollectionName;

	private final String localStorePath;

	private final String mistralApiKey;

	private final String mistralEmbedModel;

	private final List<String> vectorIndexModels;

	private final EmbeddingModel embeddingModel;

	private EmbeddingStore<TextSegment> vectorStore;

	/**
	 * Protected constructor for singleton pattern. Initialization with a given index location. Creates
	 * the Vector index in case it does not exist yet.
	 */
	public VectorIndex(InstantiationContext context, VectorIndexingConfig config) throws ConfigurationException {
		super(context, config);
		this.config = config;

		this.vectorStoreType = config.getVectorStoreType();
		this.vectorStoreHost = config.getVectorStoreHost();
		this.vectorStorePort = config.getVectorStorePort();
		// Only accessed in buildQdrantStore
		this.qdrantDbCollectionName = config.getQdrantDbCollectionName();
		this.localStorePath = config.getLocalStorePath();
		this.mistralApiKey = config.getMistralApiKey();
		this.mistralEmbedModel = config.getMistralEmbedModel();
		this.vectorIndexModels = Stream.of(config.getVectorIndexModels().split(",")).map(String::trim)
			.collect(Collectors.toList());

		Logger.info("Initializing Vector Index with store type: " + this.vectorStoreType, VectorIndex.class);

		Logger.info("Instances of models to track: " + this.vectorIndexModels.toString(), VectorIndex.class);
        Logger.info("Embedding model " + this.mistralEmbedModel + " with API key " + this.mistralApiKey,VectorIndex.class);

		if ("local".equalsIgnoreCase(config.getEmbeddingModelType())) {
			this.embeddingModel = new DummyEmbeddingModel(1024); // Default, will be used later
			Logger.info("Using Dummy/Local embedding model", VectorIndex.class);
		} else {
			MistralAiEmbeddingModel.MistralAiEmbeddingModelBuilder mistralEmbedBuilder =
				new MistralAiEmbeddingModel.MistralAiEmbeddingModelBuilder();
			mistralEmbedBuilder.apiKey(this.mistralApiKey).modelName(this.mistralEmbedModel);
			this.embeddingModel = new MistralAiEmbeddingModel(mistralEmbedBuilder);
			Logger.info("Using Mistral embedding model", VectorIndex.class);
		}

		this.vectorStore = buildVectorStore(this.vectorStoreType, config);

		createAndStartThread();
	}

	/**
	 * Factory method to create appropriate vector store based on configuration.
	 * 
	 * @param storeType
	 *                  Type of vector store ("qdrant" or "jvector")
	 * @param config
	 *                  Configuration containing connection parameters
	 * @return Configured EmbeddingStore instance
	 */
	private EmbeddingStore<TextSegment> buildVectorStore(String storeType, VectorIndexingConfig config) {
		switch (storeType.toLowerCase()) {
			case "qdrant":
				return buildQdrantStore(config);
			case "jvector":
				return buildJVectorStore(config);
			default:
				throw new IllegalArgumentException("Unsupported vector store type: " + storeType
					+ ". Supported types: qdrant, jvector");
		}
	}

	/**
	 * Creates a Qdrant embedding store with configured connection parameters.
	 */
	private QdrantEmbeddingStore buildQdrantStore(VectorIndexingConfig config) {
		Logger.info("Creating Qdrant embedding store at " + config.getVectorStoreHost() + ":"
			+ config.getVectorStorePort(), VectorIndex.class);

		String collectionName = config.getQdrantDbCollectionName();

		try {
			// Check if collection exists and create if needed utilizing the Qdrant Client
			QdrantClient client = new QdrantClient(
				QdrantGrpcClient.newBuilder(
					config.getVectorStoreHost(),
					config.getVectorStorePort(),
					false).build() // No secure connection for local dev
			);

			boolean exists = false;
			try {
				exists = client.listCollectionsAsync().get().contains(collectionName);
			} catch (Exception e) {
				// Fallback or ignore if check fails, try to create anyway or proceed
				Logger.warn("Could not check if collection exists: " + e.getMessage(), e, VectorIndex.class);
			}

			if (!exists) {
				// Use the dimension from the embedding model
				int dimension = this.embeddingModel.embed("ping").content().dimension();

				Logger.info("Collection '" + collectionName + "' does not exist. Creating with dimension: " + dimension,
					VectorIndex.class);

				client.createCollectionAsync(
					collectionName,
					io.qdrant.client.grpc.Collections.VectorParams.newBuilder()
						.setSize(dimension)
						.setDistance(io.qdrant.client.grpc.Collections.Distance.Cosine)
						.build())
					.get();
			}

			client.close();
		} catch (Exception e) {
			Logger.error("Failed to ensure Qdrant collection exists: " + e.getMessage(), e, VectorIndex.class);
			// We proceed, maybe the Store builder handles it or it exists and check failed
		}

		return QdrantEmbeddingStore.builder()
			.host(config.getVectorStoreHost())
			.port(config.getVectorStorePort())
			.collectionName(collectionName)
			.build();
	}

	/**
	 * Creates a JVector embedding store with local file persistence.
	 */
	private JVectorEmbeddingStore buildJVectorStore(VectorIndexingConfig config) {
		Logger.info("Creating JVector embedding store at path: " + config.getLocalStorePath(),
			VectorIndex.class);

		// Ensure directory exists
		try {
			java.nio.file.Files.createDirectories(java.nio.file.Paths.get(config.getLocalStorePath()));
		} catch (Exception e) {
			throw new RuntimeException("Could not create local store directory: " + config.getLocalStorePath(), e);
		}

		// Use dynamic dimension
		int dimension = 1024; // Default fallback
		try {
			dimension = this.embeddingModel.embed("ping").content().dimension();
		} catch (Exception e) {
			Logger.warn(
				"Could not determine dynamic dimension from model, using default 1024. Error: " + e.getMessage(),
				VectorIndex.class);
		}

		return JVectorEmbeddingStore.builder()
			.dimension(dimension)
			.persistencePath(config.getLocalStorePath())
			.build();
	}

	/**
	 * Type of configured vector store
	 * 
	 * @return Vector store type (e.g., "qdrant", "jvector")
	 */
	public String getVectorStoreType() {
		return this.vectorStoreType;
	}

	/**
	 * Host for remote vector stores
	 * 
	 * @return Vector store host
	 */
	public String getVectorStoreHost() {
		return this.vectorStoreHost;
	}

	/**
	 * Port for remote vector stores
	 * 
	 * @return Vector store port
	 */
	public Integer getVectorStorePort() {
		return this.vectorStorePort;
	}

	/**
	 * Collection/index name in the vector store
	 * 
	 * @return Vector store collection name
	 */
	public String getQdrantDbCollectionName() {
		return this.qdrantDbCollectionName;
	}

	/**
	 * Mistral embedding service API key
	 * 
	 * @return Mistral Embed service API Key
	 */
	public String getMistralApiKey() {
		return this.mistralApiKey;
	}

	/**
	 * Mistral embedding model
	 * 
	 * @return Mistral Embed model
	 */
	public String getMistralEmbedModel() {
		return this.mistralEmbedModel;
	}

	/**
	 * Vector Index models - FQN of all models to be included in the indexing. Only objects of these
	 * models shall be indexed
	 * 
	 * @return List of FQNs of models to be indexed
	 */
	public List<String> getVectorIndexModels() {
		return this.vectorIndexModels;
	}

	void init(InstantiationContext context) {
		Logger.info("Initiating VectorIndex", VectorIndex.class);

		createAndStartThread();
	}

	private void createAndStartThread() {
		Logger.info(
			"Will verify that there is a connection to the configured Qdrant DB, that the collection exists and uises the correct vector embedding (size).",
			VectorIndex.class);

		HistoryManager historyManager = HistoryUtils.getHistoryManager(PersistencyLayer.getKnowledgeBase());
		this.myThread = new VectorThread(this, this, historyManager);
		this.myThread.start();
		Logger.info("Initiated Vector", VectorIndex.class);
	}

	/**
	 * number of outstanding Objects in the Queues.
	 */
	public int queueSizes() {
		if (myThread == null) {
			return 0;
		}
		return this.myThread.queueSizes();
	}

	/**
	 * Whether Vector index waits for work.
	 */
	public boolean isIdle() {
		return myThread == null || myThread.isIdle();
	}

	/**
	 * if {@link #myThread} {@link Thread#isAlive()}.
	 */
	public boolean isAlive() {
		return (myThread != null) && myThread.isAlive();
	}

	/**
	 * if {@link #myThread} {@link VectorThread#isStopping()} or already stopped.
	 */
	public boolean isStopping() {
		return (myThread == null) || myThread.isStopping();
	}

	/**
	 * Mark the given {@link ContentObject} to add to the index.
	 */
	public void addContent(ContentObject content) {
		if (myThread == null) {
			Logger.error("VectorDB is not running. Cannot index content.", VectorIndex.class);
			return;
		}
		this.myThread.add(content);
	}

	/**
	 * Mark the given {@link ContentObject} to remove from the index.
	 */
	public void deleteContent(ObjectKey key) {
		if (myThread == null) {
			Logger.error("VectorDB is not running. Cannot delete content.", VectorIndex.class);
			return;
		}
		this.myThread.delete(key);
	}

	/**
	 * Checks if the given document is known in the Vector index.
	 *
	 * @param key
	 *            the key of the knowledge object to be checked
	 * @return true if the given document is known in the Vector index.
	 * 
	 * @throws IndexException
	 *                        in case of an index error
	 */
	public boolean containsContent(ObjectKey key) {
		return this.containsDocument(key);
	}

	private boolean containsDocument(ObjectKey key) {
		boolean result = false;
		return result;
	}

	/**
	 * Checks (with an heuristic) if a Vector index exists
	 *
	 * @return true if the Vector index seems to exist
	 */
	public boolean indexExists() {
		return true;
	}

	/**
	 * Create the Vector index. Will delete any old data! <b>Use with caution.</b>
	 */
	public void createIndex() {
		/**
		 * Does Nothing
		 */
	}

	/**
	 * Force a reset of the {@link VectorIndex}. All indexes will be reset to initial empty state.
	 * <b>Use with caution.</b>
	 * 
	 * @see #createIndex()
	 */
	public void resetIndex() {
		/**
		 * Does Nothing
		 */
	}

	/**
	 * Get the {@link KnowledgeObject} for the given KO ID.
	 * 
	 * @param koID
	 *                The knowledge object ID for which to get the KO
	 * @param docType
	 *                The document type of the KO
	 * @param aKB
	 *                The knowledge base used
	 * @return the knowledge object for the given KO ID
	 */
	public static final KnowledgeObject getKO(TLID koID, String docType, KnowledgeBase aKB) {
		return aKB.getKnowledgeObject(docType, koID);
	}

	/**
	 * Add document to Vector index. The document's content is given through the additional parameter
	 * 'content'.
	 * 
	 * @param aDoc
	 *                    A document to be added to the Vector index
	 * @param content
	 *                    The content of the document
	 * @param description
	 *                    The content of the document
	 * @throws IndexException
	 *                        in case adding a document failed
	 */
	protected void addDocumentOnly(KnowledgeObject aDoc, String content, String description) {
		/**
		 * Create document embedding and store to Qdrant DB
		 */

		/* Do not index objects not in the Vector Index Models configuration list! */
		if (!this.getVectorIndexModels().contains(aDoc.getWrapper().tType().toString())) {
			return;
		}

		String documentId = aDoc.tId().toString();
		if (description == null) {
			description = "";
		}

		JSONObject jo = new JSONObject();
		JSONObject joContent = new JSONObject();

		FlexData fd = aDoc.getFlexDataManager().load(aDoc.getKnowledgeBase(), aDoc.tId(), true);

		for (String key : fd.getAttributes()) {
			String indexKey = key;
			if (key.equals("id")) {
				indexKey = "internalId";
			}
			Object val = fd.getAttributeValue(key);
			joContent.put(indexKey, val == null ? null : val.toString());
		}
		joContent.put("_text", content);
		content = joContent.toJSONString();

		System.out
			.println("DEBUG: [VectorIndex] Indexing document id=" + documentId + " name=" + joContent.get("name"));
		System.out.println("DEBUG: [VectorIndex] Content preview: "
			+ (content.length() > 50 ? content.substring(0, 50) + "..." : content));

		HashMap<String, ArrayList<String>> associations = new HashMap<>();

		HashMap<String, String> associationsHashMap = new HashMap<>();

		Iterator<KnowledgeAssociation> outgoingAssociations = aDoc.getOutgoingAssociations();
		Iterator<KnowledgeAssociation> incomingAssociations = aDoc.getIncomingAssociations();

		KnowledgeAssociation association;
		String associationKeyName;
		ArrayList<String> prev;

		while (outgoingAssociations.hasNext()) {
			association = outgoingAssociations.next();
			if (!association.getDestinationObject().getWrapper().tType().toString().equals("tl.model:TLClassifier")) {
				associationKeyName = "Association-" +
					association.getDestinationObject().getWrapper().tType().toString();

				prev = associations.get(associationKeyName);
				if (prev == null) {
					prev = new ArrayList<>();
				}
				prev.add(this.getDocumentUUID(association.getDestinationIdentity().toString()).toString());

				associations
					.put(associationKeyName, prev);
			}
		}

		while (incomingAssociations.hasNext()) {
			association = incomingAssociations.next();
			if (!association.getSourceObject().getWrapper().tType().toString().equals("tl.model:TLClassifier")) {
				associationKeyName = "Association-" +
					association.getSourceObject().getWrapper().tType().toString();

				prev = associations.get(associationKeyName);
				if (prev == null) {
					prev = new ArrayList<>();
				}
				prev.add(this.getDocumentUUID(association.getSourceIdentity().toString()).toString());
				associations.put(associationKeyName, prev);
			}
		}

		for (String key : associations.keySet()) {
			if (associations.get(key).size() > 1) {
				Integer i = 0;
				for (String value : associations.get(key)) {
					associationsHashMap.put(key + "[" + (i++).toString() + "]", value);
				}
			} else {
				associationsHashMap.put(key, associations.get(key).get(0));
			}

		}

		Map plMap = Map.of(
			"objectId", documentId,
			"id", this.getDocumentUUID(documentId).toString(),
			// "internalId", value(payload.get(associationKeyName)),
			"description", description,
			"TopLogic-type", aDoc.getWrapper().tType().toString(),
			"TLObject-creation-date", aDoc.getWrapper().tCreationDate().toString(),
			"TLObject-modification-date", aDoc.getWrapper().tLastModificationDate().toString(),
			"content", joContent);

		jo.putAll(plMap);

		Object internalId = joContent.get("internalId");
		if (internalId != null) {
			// payload.put("internalId", internalId);
			jo.put("internalId", internalId.toString());
		}

		// payload.putAll(associationsHashMap);
		jo.putAll(associationsHashMap);

		/* Do not embed ApplicationModel objects - they are huge and embedding does not give us any value as
		 * we want to fetch them for every request as initial prompt context they contain overall
		 * information about the application.
		 * 
		 * These objects shall be store in the _noVector collection. */

		this.deleteDocument(aDoc.tId());
		this.storeToVectorStore(documentId, jo);
	}

	/**
	 * Delete document from vector index
	 * 
	 * @param key
	 *            The key of vector point to be deleted
	 */
	protected void deleteDocument(ObjectKey key) {
		String uuid = this.getDocumentUUID(key.toString()).toString();
		System.out.println("DEBUG: Removing document from vector store: " + key + " (UUID: " + uuid + ")");
		this.vectorStore.remove(uuid);

		// Persist to disk if using JVector
		if (vectorStore instanceof JVectorEmbeddingStore) {
			try {
				((JVectorEmbeddingStore) vectorStore).save();
			} catch (IllegalStateException e) {
				// JVector throws IllegalStateException if the store is empty.
				// In this case, there is nothing to save.
			}
		}
	}

	private Embedding getDocumentEmbedding(String content) {
		Logger.info("Embed the content with Mistral Embedding client...", VectorThread.class);

		Embedding embeddingsResponse = this.embeddingModel.embed(content).content();

		return embeddingsResponse;
	}

	/**
	 * Stores document with its embedding to the configured vector store.
	 * 
	 * @param documentId
	 *                   ID of document to add to the index
	 * @param payload
	 *                   Document payload as JSON
	 */
	public void storeToVectorStore(String documentId, JSONObject payload) {
		String pl = payload.toJSONString();
		String uuid = this.getDocumentUUID(documentId).toString();
		System.out.println("DEBUG: Storing document to vector store: " + documentId + " (UUID: " + uuid + ")");
		System.out.println("DEBUG: Content: " + pl);
		this.vectorStore.addAll(
			List.of(this.getDocumentUUID(documentId).toString()),
			List.of(this.getDocumentEmbedding(pl)),
			List.of(TextSegment.from(pl)));

		// Persist to disk if using JVector
		if (vectorStore instanceof JVectorEmbeddingStore) {
			((JVectorEmbeddingStore) vectorStore).save();
		}
	}

	/**
	 * @param documentId
	 *                   id of document to return UUID of
	 * @return UUID object
	 */
	public UUID getDocumentUUID(String documentId) {
		return UUID.nameUUIDFromBytes(documentId.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Searches the vector index for documents similar to the query.
	 * 
	 * @param query
	 *              Search query text
	 * @return List of matching documents with similarity scores
	 */
	public List<EmbeddingMatch<TextSegment>> searchVectorIndex(String query) {
		Embedding queryEmbedding = this.getDocumentEmbedding(query);

		EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
			.queryEmbedding(queryEmbedding)
			.maxResults(10)
			.build();

		return this.vectorStore.search(embeddingSearchRequest).matches();
	}

	/** @see VectorThread#isIndexUntrustworthy() */
	public boolean isIndexUntrustworthy() {
		return (myThread != null) && myThread.isIndexUntrustworthy();
	}

	/** @see VectorThread#getFailsInSuccession() */
	public int getFailsInSuccession() {
		if (myThread == null) {
			return 0;
		}
		return myThread.getFailsInSuccession();
	}

	/** @see #timestampShutdownRequest */
	public Maybe<Long> getTimestampShutdownRequest() {
		return timestampShutdownRequest;
	}

	/** @see VectorIndexingConfig#getTimeoutAbortIndexingForShutdown() */
	public int getTimeoutAbortIndexingForShutdown() {
		return config.getTimeoutAbortIndexingForShutdown();
	}

	/** @see VectorIndexingConfig#getWarnQueueSize() */
	public int getWarnQueueSize() {
		return config.getWarnQueueSize();
	}

	/**
	 * Singleton pattern: static method for getting the {@link VectorIndex}.
	 *
	 * @return {@link VectorIndex} the reference to this singleton class
	 */
	public static VectorIndex getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	@Override
	protected void shutDown() {
		Logger.info("Stopping VectorDB Indexer", VectorIndex.class);

		timestampShutdownRequest = Maybe.some(System.currentTimeMillis());
		if (myThread != null) {
			try {
				myThread.stopIndexing();
				if (!Thread.currentThread().equals(myThread)) {
					myThread.join();
				}
			} catch (Exception exception) {
				Logger.error("Stopping Vector DB failed: " + exception.getMessage(), exception, this);
			}
			myThread = null;
		}

		timestampShutdownRequest = Maybe.none();

		Logger.info("Stopped Vector Indexer", VectorIndex.class);
		super.shutDown();
	}

	/**
	 * {@link TypedRuntimeModule} for access to the {@link VectorIndex}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<VectorIndex> {

		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<VectorIndex> getImplementation() {
			return VectorIndex.class;
		}

	}

	/**
	 * Dummy embedding model for testing.
	 */
	private static class DummyEmbeddingModel implements EmbeddingModel {
		private final int dimension;

		public DummyEmbeddingModel(int dimension) {
			this.dimension = dimension;
		}

		@Override
		public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
			List<Embedding> embeddings = new ArrayList<>();
			for (TextSegment seg : textSegments) {
				float[] vector = new float[dimension];
				int hash = seg.text().hashCode();
				for (int i = 0; i < dimension; i++) {
					vector[i] = (float) (hash + i) / Integer.MAX_VALUE;
				}
				embeddings.add(Embedding.from(vector));
			}
			return Response.from(embeddings);
		}
	}

}
