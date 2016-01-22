package com.gentics.mesh.core.data;

import java.util.List;
import java.util.Map;

import com.gentics.mesh.core.rest.node.field.Field;
import com.gentics.mesh.core.rest.schema.FieldSchema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.handler.ActionContext;
import com.gentics.mesh.handler.InternalActionContext;

import rx.Observable;

/**
 * A node field container is a aggregation node that holds localized fields.
 *
 */
public interface NodeGraphFieldContainer extends GraphFieldContainer {

	/**
	 * Locate the field with the given fieldkey in this container and return an observable with the rest model for this field.
	 * 
	 * @param ac
	 * @param fieldKey
	 * @param fieldSchema
	 * @param expandField
	 * @param languageTags list of language tags
	 */
	Observable<? extends Field> getRestFieldFromGraph(InternalActionContext ac, String fieldKey, FieldSchema fieldSchema, boolean expandField, List<String> languageTags);

	/**
	 * Use the given map of rest fields and the schema information to set the data from the map to this container.
	 * TODO: This should return an observable
	 * 
	 * @param ac
	 * @param fields
	 * @param schema
	 */
	void updateFieldsFromRest(ActionContext ac, Map<String, Field> fields, Schema schema);

	/**
	 * Delete the field container. This will also delete linked elements like lists
	 */
	void delete();

	/**
	 * Return the display field value for this container.
	 * 
	 * @param schema
	 * @return
	 */
	String getDisplayFieldValue(Schema schema);

}
