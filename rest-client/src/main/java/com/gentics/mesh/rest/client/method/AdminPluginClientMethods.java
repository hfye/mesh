package com.gentics.mesh.rest.client.method;

import com.gentics.mesh.core.rest.common.GenericMessageResponse;
import com.gentics.mesh.core.rest.plugin.PluginDeploymentRequest;
import com.gentics.mesh.core.rest.plugin.PluginListResponse;
import com.gentics.mesh.core.rest.plugin.PluginResponse;
import com.gentics.mesh.parameter.ParameterProvider;
import com.gentics.mesh.rest.client.MeshRequest;

/**
 * Admin methods for plugin management.
 */
public interface AdminPluginClientMethods {

	/**
	 * Deploy the plugin using the given request.
	 * 
	 * @param request
	 * @return
	 */
	MeshRequest<PluginResponse> deployPlugin(PluginDeploymentRequest request);

	/**
	 * Return the list of deployed plugins.
	 * 
	 * @param parameters
	 * @return
	 */
	MeshRequest<PluginListResponse> findPlugins(ParameterProvider... parameters);

	/**
	 * Load the information for the plugin with the given uuid.
	 * 
	 * @param uuid
	 * @return
	 */
	MeshRequest<PluginResponse> findPlugin(String uuid);

	/**
	 * Undeploy the plugin with the given deployment uuid.
	 * 
	 * @param uuid
	 * @return
	 */
	MeshRequest<GenericMessageResponse> undeployPlugin(String uuid);
}
