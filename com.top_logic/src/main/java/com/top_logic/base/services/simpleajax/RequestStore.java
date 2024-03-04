/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Temporary storage for responses to requests already processed.
 * 
 * <p>
 * A response is started with {@link #createResponse(Integer)} and written to the writer returned
 * from {@link Response#open(DisplayContext)}. After
 * {@link Response#close() closing} the response, it is kept in the store until it is acknowledged
 * by the client in {@link #acknowledge(List)}.
 * </p>
 * 
 * <p>
 * A {@link Response} not yet acknowledged can be replayed in
 * {@link #replay(Integer, HttpServletResponse)}. This allows the client to simply retry failed
 * request.
 * </p>
 * 
 * @see #FACTORY
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RequestStore {

	/**
	 * Key to associate a {@link RequestStore} with the current sub-session.
	 */
	Property<RequestStore> SESSION_KEY = TypedAnnotatable.property(RequestStore.class, "requestStore");

	/**
	 * Creates a new Response handle.
	 * 
	 * @param rxSequence
	 *        The sequence number of the request.
	 * @return A new {@link Response} instance for creating the response message.
	 */
	Response createResponse(Integer rxSequence) throws IOException;

	/**
	 * Checks whether the given sequence number has a stored response.
	 * 
	 * <p>
	 * If a previously created {@link Response} exists for the request with the given sequence
	 * number, the stored response message is replayed to the given {@link HttpServletResponse}.
	 * </p>
	 * 
	 * @param rxSequence
	 *        The sequence number of the request.
	 * @param response
	 *        The underlying {@link HttpServletResponse} to create.
	 * @return Whether the request has been answered from the cache.
	 */
	boolean replay(Integer rxSequence, HttpServletResponse response) throws IOException;

	/**
	 * Marks the given sequence numbers as successfully received by the client.
	 * 
	 * <p>
	 * An acknowledged response is removed from the store and can no longer be replayed.
	 * </p>
	 * 
	 * @param txNos
	 *        Acknowledged responses.
	 */
	void acknowledge(List<Integer> txNos);

	/**
	 * Drops all pending unacknowledged requests.
	 */
	void clear();

	/**
	 * {@link Factory} for {@link RequestStore}s.
	 * 
	 * @see #NO_STORE
	 */
	Factory FACTORY = new Factory() {
		@Override
		public RequestStore create() {
			return new RequestStoreImpl();
		}
	
		class RequestStoreImpl implements RequestStore {
			Map<Integer, ResponseImpl> _unacknowledgedRequests = new HashMap<>();
	
			@Override
			public void clear() {
				_unacknowledgedRequests.clear();
			}

			@Override
			public Response createResponse(Integer rxSequence) throws IOException {
				ResponseImpl handle = new ResponseImpl();
				if (_unacknowledgedRequests.size() > 10) {
					// Something went wrong, drop old requests to avoid a memory leak.
					int limit = rxSequence.intValue() - 5;
					for (Iterator<Entry<Integer, ResponseImpl>> it = _unacknowledgedRequests.entrySet().iterator(); it
						.hasNext();) {
						Entry<Integer, ResponseImpl> entry = it.next();
						int missingTx = entry.getKey().intValue();
						if (missingTx < limit) {
							Logger.error("Dropping unacknowledged request '" + missingTx
								+ "', current sequence is '" + rxSequence + "': " + entry.getValue().toString(),
								RequestStore.class);
							it.remove();
						}
					}
				}
				_unacknowledgedRequests.put(rxSequence, handle);
				return handle;
			}
	
			@Override
			public void acknowledge(List<Integer> txNos) {
				for (Integer tx : txNos) {
					_unacknowledgedRequests.remove(tx);
				}
			}
	
			@Override
			public boolean replay(Integer rxSequence, HttpServletResponse response) throws IOException {
				ResponseImpl storedResponse = _unacknowledgedRequests.get(rxSequence);
				if (storedResponse == null) {
					return false;
				}
	
				storedResponse.applyTo(response);
				return true;
			}
	
			class ResponseImpl implements Response {
	
				private DefaultDisplayContext _context;

				private HttpServletResponse _response;

				private CharArrayWriter _buffer;

				@Override
				public TagWriter open(DisplayContext displayContext)
						throws IOException {
					_context = (DefaultDisplayContext) displayContext;

					final HttpServletRequest request = displayContext.asRequest();
					final HttpServletResponse response = displayContext.asResponse();

					_response = response;
					final CharArrayWriter buffer = new CharArrayWriter();
					_buffer = buffer;
					TagWriter out = new TagWriter(_buffer);

					HttpServletResponse bufferingResponse = new HttpServletResponseWrapper(response) {
						private PrintWriter _printer;

						@Override
						public PrintWriter getWriter() throws IOException {
							if (_printer == null) {
								_printer = new PrintWriter(buffer);
							}
							return _printer;
						}

						@Override
						public void flushBuffer() throws IOException {
							if (_printer != null) {
								_printer.flush();
							}
						}

						@Override
						public void setContentLength(int a1) {
							// Ignore.
						}

						@Override
						public void setBufferSize(int a1) {
							// Ignore.
						}

						@Override
						public int getBufferSize() {
							// Randomly choosen.
							return 4096;
						}

						@Override
						public ServletOutputStream getOutputStream() throws IOException {
							throw new UnsupportedOperationException("Cannot produce binary content in buffering mode.");
						}

						@Override
						public void setContentType(String a1) {
							// Ignore.
						}

						@Override
						public void setCharacterEncoding(String a1) {
							// Ignore.
						}
					};

					_context.setResponse(bufferingResponse);

					RequestUtil.installTagWriter(request, out);
					return out;
				}
	
				@Override
				public void close() throws IOException {
					HttpServletResponse origResponse = _response;
					_context.setResponse(origResponse);
					_response = null;
					applyTo(origResponse);
				}
	
				/**
				 * During replay, writes the cached message to the given {@link HttpServletResponse}.
				 */
				void applyTo(HttpServletResponse response) throws IOException {
					if (_response != null) {
						throw new IllegalStateException("Cannot apply incomplete response.");
					}
	
					long elapsedNanos = -System.nanoTime();
					{
						_buffer.writeTo(response.getWriter());
					}
					elapsedNanos += System.nanoTime();

					long size = _buffer.size();
					// (approx. 256 kBit/s or 4 times ISDN speed).
					int minTranferRate = 32; // simple utf-8 chars per millisecond
					long maxTransferMillis = (size / minTranferRate); // seconds
					long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

					if (elapsedMillis > maxTransferMillis) {
						float elapsedSeconds = elapsedNanos / 1E9f;
						float actualSpeed = ((8 * size) / elapsedSeconds / 1024); // kBit/s
						DebugHelper.logTiming(_context.asRequest(),
							"Flushing response of " + size + " bytes with "
								+ new DecimalFormat("#.###").format(actualSpeed) + " kBit/s",
							elapsedMillis, AJAXServlet.class);
					}
				}
	
				@Override
				public String toString() {
					return _buffer.toString();
				}
	
			}
		}
	};

	/**
	 * Handle for responses kept in a {@link RequestStore} for later replay.
	 */
	public interface Response extends AutoCloseable {

		/**
		 * Starts creating the response.
		 * 
		 * @param displayContext
		 *        The current {@link DisplayContext}.
		 * 
		 * @return A {@link TagWriter} to write the response message to.
		 */
		public TagWriter open(DisplayContext displayContext) throws IOException;


		/**
		 * Finishes creating the {@link Response} and flushes the created message to the client.
		 */
		@Override
		public void close() throws IOException;

	}

	/**
	 * Factory for {@link RequestStore}s.
	 * 
	 * @see RequestStore#FACTORY
	 * @see RequestStore#NO_STORE
	 */
	public interface Factory {
		/**
		 * Creates a new {@link RequestStore}.
		 */
		public RequestStore create();
	}

	/**
	 * Dummy store that does not store responses.
	 * 
	 * @see #FACTORY
	 */
	RequestStore NO_STORE = new RequestStore() {
	
		final Response DIRECT_RESPONSE = new Response() {
			@Override
			public TagWriter open(DisplayContext displayContext) throws IOException {
				return MainLayout.getTagWriter(displayContext.asRequest(), displayContext.asResponse());
			}
	
			@Override
			public void close() throws IOException {
				// Noting to do.
			}
		};
	
		@Override
		public void clear() {
			// Ignore.
		}

		@Override
		public boolean replay(Integer rxSequence, HttpServletResponse response) {
			return false;
		}
	
		@Override
		public void acknowledge(java.util.List<Integer> txNos) {
			// Ignore.
		}
	
		@Override
		public Response createResponse(Integer rxSequence) throws IOException {
			return DIRECT_RESPONSE;
		}
	};
}
