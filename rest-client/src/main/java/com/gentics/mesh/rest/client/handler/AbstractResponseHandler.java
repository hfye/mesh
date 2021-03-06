package com.gentics.mesh.rest.client.handler;

import com.gentics.mesh.rest.client.MeshResponse;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;

/**
 * Abstract implementation for various mesh response handler.
 * 
 * @param <T>
 *            Success response type
 */
public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {

	protected String uri;
	protected HttpMethod method;
	protected MeshResponse<T> future;

	public AbstractResponseHandler(HttpMethod method, String uri) {
		this.method = method;
		this.uri = uri;
		this.future = new MeshResponse<T>(Future.future());
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public HttpMethod getMethod() {
		return method;
	}

	@Override
	public MeshResponse<T> getFuture() {
		return future;
	}

}
