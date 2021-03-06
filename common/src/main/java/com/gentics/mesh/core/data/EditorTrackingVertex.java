package com.gentics.mesh.core.data;

import static com.gentics.mesh.core.data.relationship.GraphRelationships.HAS_EDITOR;

import com.gentics.mesh.util.DateUtils;

/**
 * Interface for vertices that track their editor and editing time
 */
public interface EditorTrackingVertex extends MeshVertex {

	String LAST_EDIT_TIMESTAMP_PROPERTY_KEY = "last_edited_timestamp";

	/**
	 * Return the editor of the vertex.
	 * 
	 * @return Editor
	 */
	User getEditor();

	/**
	 * Set the editor of the vertex.
	 * 
	 * @param user
	 *            Editor
	 */
	default void setEditor(User user) {
		setLinkOut(user, HAS_EDITOR);
	}

	/**
	 * Return the timestamp on which the vertex was last updated.
	 * 
	 * @return Edit timestamp
	 */
	default Long getLastEditedTimestamp() {
		return getProperty(LAST_EDIT_TIMESTAMP_PROPERTY_KEY);
	}

	/**
	 * Set the timestamp on which the vertex was last updated.
	 * 
	 * @param timestamp
	 *            Edit timestamp
	 */
	default void setLastEditedTimestamp(long timestamp) {
		setProperty(LAST_EDIT_TIMESTAMP_PROPERTY_KEY, timestamp);
	}

	/**
	 * Update the last edit timestamp using the current time.
	 */
	default void setLastEditedTimestamp() {
		setLastEditedTimestamp(System.currentTimeMillis());
	}

	/**
	 * Return the ISO8601 formatted edited date.
	 * 
	 * @return
	 */
	default String getLastEditedDate() {
		return DateUtils.toISO8601(getLastEditedTimestamp(), 0);
	}

}
