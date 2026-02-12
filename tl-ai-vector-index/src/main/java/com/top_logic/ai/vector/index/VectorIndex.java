/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;

//import static io.qdrant.client.PointIdFactory.*;
//import static io.qdrant.client.ValueFactory.*;
//import static io.qdrant.client.VectorsFactory.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
//import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONObject;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
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
//import com.top_logic.knowledge.searching.SearchException;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.wrap.person.PersonManager;

import dev.langchain4j.community.store.embedding.jvector.JVectorEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
//import io.qdrant.client.QdrantClient;
//import io.qdrant.client.QdrantGrpcClient;
//import io.qdrant.client.grpc.Collections.Distance;
//import io.qdrant.client.grpc.Collections.VectorParams;
//import io.qdrant.client.grpc.Common.PointId;
//import io.qdrant.client.grpc.JsonWithInt;
//import io.qdrant.client.grpc.Points.PointStruct;
//import io.qdrant.client.grpc.Points.Vectors;
//import nl.dannyj.mistral.MistralClient;
//import nl.dannyj.mistral.models.embedding.EmbeddingRequest;
//import nl.dannyj.mistral.models.embedding.EmbeddingResponse;
//import nl.dannyj.mistral.models.embedding.FloatEmbedding;

class QdrantCollection {
	public final String collectionName;
	public final Integer vectorSize;
	
	public QdrantCollection(String collectionName, Integer vectorSize) {
		this.collectionName = collectionName;
		this.vectorSize = vectorSize;
	}
}

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
		 * The Qdrant database Hostname
		 */
		@StringDefault("localhost")
		String getQdrantDbHostname();

		/**
		 * The Qdrant database port
		 */
		@IntDefault(6334)
		Integer getQdrantDbPort();

		/**
		 * The Qdrant Vector Index collection name
		 */
		@StringDefault("changeme")
		String getQdrantDbCollectionName();

		/**
		 * Mistral Embedding Service API Key
		 */
		@StringDefault("EMPTY")
		String getMistralApiKey();

		/**
		 * Mistral Embedding model
		 */
		@StringDefault("mistral-embed")
		String getMistralEmbedModel();

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
		 * <b>Warning:</b> The index will be incomplete when indexing is aborted! After a forced
		 * shutdown, the index must be administratively rebuilt.
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
    public static final String FIELD_KO_ID            = "id";
    
    /** The document type of the document to index */
    public static final String FIELD_DOCTYPE          = "docType";

    /** The name of the description field in a Lucene document */
    public static final String FIELD_DESCRIPTION      = "description";
    
    /** The maximum size of the field description */
    public static final int FIELD_DESCRIPTION_MAXSIZE = 200;

    /** The name of the contents field in a Lucene document */
    public static final String FIELD_CONTENTS         = "contents";

    /** Maximum time to index a Document before it will be logged */
    static final int INDEX_MILLIS             = 1000 * 3; // 3 Seconds

    /**  Maximum time to delete a Document before it will be logged */
    static final int DELETE_MILLIS            = 1000;     // one Second

    private static final int DEFAULT_MAX_FILED_LENGTH_VALUE = 10000;

    /** The index adding thread responsible for indexing/deleting documents */
	private VectorThread myThread;
    
    /**
     * Is used for timeouts when the shutdown was requested but there is still work to do.
     */
    private Maybe<Long> timestampShutdownRequest = Maybe.none();

	private final VectorIndexingConfig config;

	private final String qdrantDbHostname;

	private final Integer qdrantDbPort;

	private final String qdrantDbCollectionName;

	private final String qdrantDbCollectionNameNoVector;

	private final String mistralApiKey;

	private final String mistralEmbedModel;

	private final List<String> vectorIndexModels;

//	private final List<String> noVectorIndexModels;

//	private QdrantClient qdrantClient;

//	private final MistralClient mistralClient;

	private final MistralAiEmbeddingModel lc4jMistralEmbedClient;

	private EmbeddingStore<TextSegment> qdrantEmbeddingStore;

	private EmbeddingStore<TextSegment> jVectorEmbeddingStore;

	/**
	 * Protected constructor for singleton pattern. Initialization with a given index location.
	 * Creates the Vector index in case it does not exist yet.
	 */
	public VectorIndex(InstantiationContext context, VectorIndexingConfig config) throws ConfigurationException {
		super(context, config);
		this.config = config;
		
		this.qdrantDbHostname = config.getQdrantDbHostname();
		this.qdrantDbPort = config.getQdrantDbPort();
		this.qdrantDbCollectionName = config.getQdrantDbCollectionName();
		this.qdrantDbCollectionNameNoVector = this.qdrantDbCollectionName + "_noVector";
		this.mistralApiKey = config.getMistralApiKey();
		this.mistralEmbedModel = config.getMistralEmbedModel();
		this.vectorIndexModels =
			Stream.of(config.getVectorIndexModels().split(",")).map(String::trim).collect(Collectors.toList());
//		this.noVectorIndexModels =
//			Stream.of(config.getNoVectorIndexModels().split(",")).map(String::trim).collect(Collectors.toList());

		Logger.info("Create a Qdrant Client for Qdrant database at " + this.qdrantDbHostname + ":"
			+ this.qdrantDbPort, VectorThread.class);
//		try {
//			this.qdrantClient = this.getQdrandClient();

			this.qdrantEmbeddingStore =
				QdrantEmbeddingStore.builder()
					.host(config.getQdrantDbHostname())
					.port(config.getQdrantDbPort())
					.collectionName(config.getQdrantDbCollectionName())
					.build();

			this.jVectorEmbeddingStore = JVectorEmbeddingStore.builder()
				.dimension(1024)
				.persistencePath("tmp/jvector") // Base path for index files
				.build();


//		} catch (InterruptedException | ExecutionException ex) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		}

		Logger.info("Check for collection", VectorThread.class);
		this.verifyQdrantCollection();

//		Logger.info("Initiate Mistral Client", VectorThread.class);
//		this.mistralClient = new MistralClient(this.mistralApiKey);
		
		MistralAiEmbeddingModel.MistralAiEmbeddingModelBuilder mistralEmbedBuilder =
			new MistralAiEmbeddingModel.MistralAiEmbeddingModelBuilder();
		mistralEmbedBuilder.apiKey(this.mistralApiKey).modelName("mistral-embed");
		this.lc4jMistralEmbedClient = new MistralAiEmbeddingModel(mistralEmbedBuilder);

		createAndStartThread();
	}

	/**
	 * Qdrant DB hostname configuration.
	 * 
	 * @return Qdrant DB hostname
	 */
	public String getQdrantDbHostname() {
		return this.qdrantDbHostname;
	}

	/**
	 * Qdrant DB port configuration
	 * 
	 * @return Qdrant DB port
	 */
	public Integer getQdrantDbPort() {
		return this.qdrantDbPort;
	}

	/**
	 * Qdrant DB Index collection name
	 * 
	 * @return Qdrant DB Index Collection Name
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

//	/**
//	 * Vector No Index models - FQN of all models to be added as points to the Qdrant DB, but
//	 * without vectors (no embedding). These points can be used for AI model context storage.
//	 * 
//	 * @return List of FQNs of models to be indexed
//	 */
//	public List<String> getNoVectorIndexModels() {
//		return this.noVectorIndexModels;
//	}

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
    

    
//	/**
//	 * Get a Vector index searcher for searching in a Vector index.
//	 * 
//	 * @return The Vector index searcher
//	 * @throws SearchException
//	 *         if the index could not be opened
//	 */
//	public QdrantClient getSearcher() throws SearchException {
//		return new QdrantClient(QdrantGrpcClient.newBuilder(this.getQdrantDbHostname()).build());
//    }
    
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
	 *        the key of the knowledge object to be checked
	 * @return true if the given document is known in the Vector index.
	 * 
	 * @throws IndexException
	 *         in case of an index error
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
	 *        The knowledge object ID for which to get the KO
	 * @param docType
	 *        The document type of the KO
	 * @param aKB
	 *        The knowledge base used
	 * @return the knowledge object for the given KO ID
	 */
	public static final KnowledgeObject getKO(TLID koID, String docType, KnowledgeBase aKB) {
        return aKB.getKnowledgeObject(docType, koID);
    }

	/**
	 * Add document to Vector index. The document's content is given through the additional
	 * parameter 'content'.
	 * 
	 * @param aDoc
	 *        A document to be added to the Vector index
	 * @param content
	 *        The content of the document
	 * @param description
	 *        The content of the document
	 * @throws IndexException
	 *         in case adding a document failed
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
		
		for(String key: fd.getAttributes()) {
			String indexKey = key;
			if (key.equals("id")) {
				indexKey = "internalId";
			}
			joContent.put(indexKey, fd.getAttributeValue(key).toString());
		}
		joContent.put("_text", content);

		content = joContent.toJSONString();
		
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

//		HashMap<String, String> payload = new HashMap<>();

//		Map plMap = Map.of(
//			"objectId", documentId,
//			"id", this.getDocumentUUID(documentId).toString(),
//			// "internalId", value(payload.get(associationKeyName)),
//			"description", description,
//			"TopLogic-type", aDoc.getWrapper().tType().toString(),
//			"TLObject-creation-date", aDoc.getWrapper().tCreationDate().toString(),
//			"TLObject-modification-date", aDoc.getWrapper().tLastModificationDate().toString(),
//			"content", content);

//		payload.putAll(plMap);

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

		String internalId = joContent.get("internalId").toString();
		if (internalId != null) {
//			payload.put("internalId", internalId);
			jo.put("internalId", internalId);
		}

//		payload.putAll(associationsHashMap);
		jo.putAll(associationsHashMap);

//		List<FloatEmbedding> embeddings = new ArrayList<>();
//		embeddings.add(new FloatEmbedding());
//		String collectionName = this.qdrantDbCollectionNameNoVector;

		/* Do not embed ApplicationModel objects - they are huge and embedding does not give us any
		 * value as we want to fetch them for every request as initial prompt context they contain
		 * overall information about the application.
		 * 
		 * These objects shall be store in the _noVector collection. */
//		if (!this.getNoVectorIndexModels().contains(aDoc.getWrapper().tType().toString())) {
//			embeddings = this.getDocumentEmbeddings(payload.toString());
//			collectionName = this.qdrantDbCollectionName;
//		}

//		this.storeVectorIndexToQdrantDb(documentId, embeddings, payload, collectionName);
		this.storeVectorIndexToQdrantDb_(documentId, jo);
	}

//	/**
//	 * Delete document from index
//	 * 
//	 * @param key
//	 *        The key of vector point to be deleted
//	 */
//	protected void deleteDocument(ObjectKey key) {
//		try {
//			this.qdrantClient.deleteAsync(
//				this.qdrantDbCollectionName, List.of(id(this.getDocumentUUID(key.toString()))))
//				.get();
//		} catch (InterruptedException ex) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		} catch (ExecutionException ex) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		}
//	}

	/**
	 * Delete document from index
	 * 
	 * @param key
	 *        The key of vector point to be deleted
	 */
	protected void deleteDocument(ObjectKey key) {
		this.qdrantEmbeddingStore.remove(key.toString());
		this.jVectorEmbeddingStore.remove(key.toString());
	}

//	private List<FloatEmbedding> getDocumentEmbeddings(String content) {
//		Logger.info("Embed the content with Mistral Embedding client...", VectorThread.class);
//
//		List<String> contentList = List.of(content);
//
//		EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
//			.model(this.getMistralEmbedModel()) // mistral-embed is currently the only model
//												// available for embedding
//			.input(contentList)
//			.build();
//
//		EmbeddingResponse embeddingsResponse = this.mistralClient.createEmbedding(embeddingRequest);
//		// Embeddings are returned as a list of FloatEmbedding objects. FloatEmbedding objects
//		// contain a list of floats per input string.
//		// See the Mistral documentation for more information:
//		// https://docs.mistral.ai/capabilities/embeddings/
//
//		return embeddingsResponse.getData();
//
//	}

	private Embedding getDocumentEmbedding(String content) {
		Logger.info("Embed the content with Mistral Embedding client...", VectorThread.class);

		Embedding embeddingsResponse = this.lc4jMistralEmbedClient.embed(content).content();

		return embeddingsResponse;
	}


//	private void storeVectorIndexToQdrantDb(String documentId, List<FloatEmbedding> embeddings,
//			Map payload, String collectionName) {
//		PointId pointId = id(this.getDocumentUUID(documentId));
//
//		embeddings.forEach(embedding -> {
//			Vectors vectors;
//			try {
//				vectors = vectors(embedding.getEmbedding());
//				Logger.info(embedding.getEmbedding().toString(), VectorThread.class);
//			} catch (Exception ex) {
//				vectors = vectors(0);
//			}
//			try {
//				List<PointStruct> points = List.of(
//					PointStruct.newBuilder()
//						.setId(pointId)
//						.setVectors(vectors)
//						.putAllPayload(payload)
//						.build());
//
//				this.qdrantClient.upsertAsync(collectionName, points).get();
//			} catch (InterruptedException ex) {
//				// TODO Auto-generated catch block
//				ex.printStackTrace();
//			} catch (ExecutionException ex) {
//				// TODO Auto-generated catch block
//				ex.printStackTrace();
//			}
//		});
//	}

	/**
	 * @param documentId
	 *        id of document to add to the index
	 * @param payload
	 *        document payload
	 */
	public void storeVectorIndexToQdrantDb_(String documentId, JSONObject payload) {
		String pl = payload.toJSONString();
		this.qdrantEmbeddingStore.addAll(List.of(this.getDocumentUUID(documentId).toString()),
			List.of(this.getDocumentEmbedding(pl)),
			List.of(TextSegment.from(pl)));

		this.jVectorEmbeddingStore.addAll(List.of(this.getDocumentUUID(documentId).toString()),
			List.of(this.getDocumentEmbedding(pl)),
			List.of(TextSegment.from(pl)));

		((JVectorEmbeddingStore) this.jVectorEmbeddingStore).save();
	}

	/**
	 * @param documentId
	 *        id of document to return UUID of
	 * @return UUID object
	 */
	public UUID getDocumentUUID(String documentId) {
		return UUID.nameUUIDFromBytes(documentId.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * @param query
	 *        vector index search query
	 * @return list of embedding matches
	 */
	public List<EmbeddingMatch<TextSegment>> searchVectorIndex(String query) {
		Embedding queryEmbedding = this.getDocumentEmbedding(query);

		EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
			.queryEmbedding(queryEmbedding)
			.maxResults(5)
			.build();

		List<EmbeddingMatch<TextSegment>> qdrantEmbeddingStoreMatches =
			this.qdrantEmbeddingStore.search(embeddingSearchRequest).matches();
		List<EmbeddingMatch<TextSegment>> jvectorEmbeddingStoreMatches =
			this.qdrantEmbeddingStore.search(embeddingSearchRequest).matches();

		return jvectorEmbeddingStoreMatches;
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
				if ( ! Thread.currentThread().equals(myThread)) {
					myThread.join();
				}
			}
			catch (Exception exception) {
				Logger.error("Stopping Vector DB failed: " + exception.getMessage(), exception, this);
			}
			myThread = null;
		}
		
		timestampShutdownRequest = Maybe.none();
		
//		this.qdrantClient.close();

		Logger.info("Stopped Vector Indexer", VectorIndex.class);
		super.shutDown();
	}
    
    
	
	private boolean verifyQdrantCollection() {
		List.of(new QdrantCollection(this.qdrantDbCollectionName, 1024),
			new QdrantCollection(this.qdrantDbCollectionNameNoVector, 1))
			.forEach(collection -> {
//				if (!this.qdrantClient.collectionExistsAsync(collection.collectionName).get()) {
//					this.createQdrantCollection(collection.collectionName, collection.vectorSize);
//				}
			});

		return true;
	}

//	private void createQdrantCollection(String collectionName, Integer vectorSize) {
//		try {
//			if (!this.qdrantClient.collectionExistsAsync(collectionName).get()) {
//				this.qdrantClient.createCollectionAsync(
//					collectionName,
//					VectorParams.newBuilder()
//						.setDistance(Distance.Cosine)
//						.setSize(vectorSize)
//						.build())
//					.get();
//			}
//		} catch (InterruptedException ex) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		} catch (ExecutionException ex) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		}
//	}

//	private QdrantClient getQdrandClient() throws InterruptedException, ExecutionException {
//		QdrantClient client = new QdrantClient(
//			QdrantGrpcClient.newBuilder(this.qdrantDbHostname, this.qdrantDbPort, false)
//				.build());
//
//		return client;
//	}

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
	
}
