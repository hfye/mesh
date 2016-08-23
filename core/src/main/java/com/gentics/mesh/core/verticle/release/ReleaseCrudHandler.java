package com.gentics.mesh.core.verticle.release;

import static com.gentics.mesh.core.data.relationship.GraphPermission.UPDATE_PERM;
import static com.gentics.mesh.core.rest.error.Errors.error;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import javax.inject.Inject;

import org.apache.commons.lang.NotImplementedException;

import com.gentics.mesh.Mesh;
import com.gentics.mesh.context.InternalActionContext;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.Release;
import com.gentics.mesh.core.data.relationship.GraphPermission;
import com.gentics.mesh.core.data.root.MicroschemaContainerRoot;
import com.gentics.mesh.core.data.root.RootVertex;
import com.gentics.mesh.core.data.root.SchemaContainerRoot;
import com.gentics.mesh.core.data.schema.MicroschemaContainerVersion;
import com.gentics.mesh.core.data.schema.SchemaContainerVersion;
import com.gentics.mesh.core.rest.release.ReleaseResponse;
import com.gentics.mesh.core.rest.schema.MicroschemaReferenceList;
import com.gentics.mesh.core.rest.schema.SchemaReferenceList;
import com.gentics.mesh.core.verticle.handler.AbstractCrudHandler;
import com.gentics.mesh.core.verticle.node.NodeMigrationVerticle;
import com.gentics.mesh.search.index.node.NodeIndexHandler;

import io.vertx.core.eventbus.DeliveryOptions;
import rx.Observable;
import rx.Single;

/**
 * CRUD Handler for Releases
 */
public class ReleaseCrudHandler extends AbstractCrudHandler<Release, ReleaseResponse> {

	private NodeIndexHandler nodeIndexHandler;

	@Inject
	public ReleaseCrudHandler(NodeIndexHandler nodeIndexHandler) {
		this.nodeIndexHandler = nodeIndexHandler;
	}

	@Override
	public RootVertex<Release> getRootVertex(InternalActionContext ac) {
		return ac.getProject().getReleaseRoot();
	}

	@Override
	public void handleDelete(InternalActionContext ac, String uuid) {
		throw new NotImplementedException("Release can't be deleted");
	}

	/**
	 * Handle getting the schema versions of a release.
	 * 
	 * @param ac
	 * @param uuid
	 *            Uuid of release to be queried
	 */
	public void handleGetSchemaVersions(InternalActionContext ac, String uuid) {
		validateParameter(uuid, "uuid");
		db.asyncNoTx(() -> {
			return getRootVertex(ac).loadObjectByUuid(ac, uuid, GraphPermission.READ_PERM).flatMap((release) -> getSchemaVersions(release));
		}).subscribe(model -> ac.send(model, OK), ac::fail);
	}

	/**
	 * Handle assignment of schema version to a release.
	 * 
	 * @param ac
	 * @param uuid
	 *            Uuid of release
	 */
	public void handleAssignSchemaVersion(InternalActionContext ac, String uuid) {
		validateParameter(uuid, "uuid");
		db.asyncNoTx(() -> {
			RootVertex<Release> root = getRootVertex(ac);
			return root.loadObjectByUuid(ac, uuid, UPDATE_PERM).flatMap(release -> {
				SchemaReferenceList schemaReferenceList = ac.fromJson(SchemaReferenceList.class);
				Project project = ac.getProject();
				SchemaContainerRoot schemaContainerRoot = project.getSchemaContainerRoot();
				return db.tx(() -> {
					// Resolve the list of references to graph schema container versions
					Observable<SchemaContainerVersion> obs = Observable.from(schemaReferenceList)
							.flatMap(reference -> schemaContainerRoot.fromReference(reference).toObservable());

					// Invoke schema migration for each found schema version
					obs.toBlocking().forEach(version -> {
						SchemaContainerVersion assignedVersion = release.getVersion(version.getSchemaContainer());
						if (assignedVersion != null && assignedVersion.getVersion() > version.getVersion()) {
							throw error(BAD_REQUEST, "error_release_downgrade_schema_version", version.getName(),
									Integer.toString(assignedVersion.getVersion()), Integer.toString(version.getVersion()));
						}
						release.assignSchemaVersion(version);

						// Update the index type specific ES mapping
						nodeIndexHandler.updateNodeIndexMapping("node-" + project.getUuid() + "-" + release.getUuid() + "-draft",
								version.getName() + "-" + version.getVersion(), version.getSchema()).await();
						nodeIndexHandler.updateNodeIndexMapping("node-" + project.getUuid() + "-" + release.getUuid() + "-published",
								version.getName() + "-" + version.getVersion(), version.getSchema()).await();

						DeliveryOptions options = new DeliveryOptions();
						options.addHeader(NodeMigrationVerticle.PROJECT_UUID_HEADER, release.getRoot().getProject().getUuid());
						options.addHeader(NodeMigrationVerticle.RELEASE_UUID_HEADER, release.getUuid());
						options.addHeader(NodeMigrationVerticle.UUID_HEADER, version.getSchemaContainer().getUuid());
						options.addHeader(NodeMigrationVerticle.FROM_VERSION_UUID_HEADER, assignedVersion.getUuid());
						options.addHeader(NodeMigrationVerticle.TO_VERSION_UUID_HEADER, version.getUuid());
						Mesh.vertx().eventBus().send(NodeMigrationVerticle.SCHEMA_MIGRATION_ADDRESS, null, options);
					});
					return getSchemaVersions(release);
				});
			});
		}).subscribe(model -> ac.send(model, OK), ac::fail);

	}

	/**
	 * Handle getting the microschema versions of a release.
	 * 
	 * @param ac
	 * @param uuid
	 *            Uuid of release to be queried
	 */
	public void handleGetMicroschemaVersions(InternalActionContext ac, String uuid) {
		validateParameter(uuid, "uuid");
		db.asyncNoTx(() -> {
			return getRootVertex(ac).loadObjectByUuid(ac, uuid, GraphPermission.READ_PERM).flatMap((release) -> getMicroschemaVersions(release));
		}).subscribe(model -> ac.send(model, OK), ac::fail);
	}

	/**
	 * Handle assignment of microschema version to a release.
	 * 
	 * @param ac
	 * @param uuid
	 *            Uuid of release
	 */
	public void handleAssignMicroschemaVersion(InternalActionContext ac, String uuid) {
		validateParameter(uuid, "uuid");
		db.asyncNoTx(() -> {
			RootVertex<Release> root = getRootVertex(ac);
			return root.loadObjectByUuid(ac, uuid, UPDATE_PERM).flatMap(release -> {
				MicroschemaReferenceList microschemaReferenceList = ac.fromJson(MicroschemaReferenceList.class);
				MicroschemaContainerRoot microschemaContainerRoot = ac.getProject().getMicroschemaContainerRoot();

				return db.tx(() -> {
					// Transform the list of references into microschema container version vertices
					Observable<MicroschemaContainerVersion> obs = Observable.from(microschemaReferenceList)
							.flatMap(reference -> microschemaContainerRoot.fromReference(reference).toObservable());

					obs.toBlocking().forEach(version -> {
						MicroschemaContainerVersion assignedVersion = release.getVersion(version.getSchemaContainer());
						if (assignedVersion != null && assignedVersion.getVersion() > version.getVersion()) {
							throw error(BAD_REQUEST, "error_release_downgrade_microschema_version", version.getName(),
									Integer.toString(assignedVersion.getVersion()), Integer.toString(version.getVersion()));
						}
						release.assignMicroschemaVersion(version);

						// start microschema migration
						DeliveryOptions options = new DeliveryOptions();
						options.addHeader(NodeMigrationVerticle.PROJECT_UUID_HEADER, release.getRoot().getProject().getUuid());
						options.addHeader(NodeMigrationVerticle.RELEASE_UUID_HEADER, release.getUuid());
						options.addHeader(NodeMigrationVerticle.UUID_HEADER, version.getSchemaContainer().getUuid());
						options.addHeader(NodeMigrationVerticle.FROM_VERSION_UUID_HEADER, assignedVersion.getUuid());
						options.addHeader(NodeMigrationVerticle.TO_VERSION_UUID_HEADER, version.getUuid());
						Mesh.vertx().eventBus().send(NodeMigrationVerticle.MICROSCHEMA_MIGRATION_ADDRESS, null, options);
					});
					return getMicroschemaVersions(release);
				});
			});
		}).subscribe(model -> ac.send(model, OK), ac::fail);
	}

	/**
	 * Get the rest model of the schema versions of the release.
	 * 
	 * @param release
	 *            release
	 * @return single emitting the rest model
	 */
	protected Single<SchemaReferenceList> getSchemaVersions(Release release) {
		try {
			return Observable.from(release.findAllSchemaVersions()).map(SchemaContainerVersion::transformToReference).collect(() -> {
				return new SchemaReferenceList();
			}, (x, y) -> {
				x.add(y);
			}).toSingle();
		} catch (Exception e) {
			throw error(INTERNAL_SERVER_ERROR, "Unknown error while getting schema versions", e);
		}
	}

	/**
	 * Get the rest model of the microschema versions of the release
	 * 
	 * @param release
	 *            release
	 * @return single emitting the rest model
	 */
	protected Single<MicroschemaReferenceList> getMicroschemaVersions(Release release) {
		try {
			return Observable.from(release.findAllMicroschemaVersions()).map(MicroschemaContainerVersion::transformToReference).collect(() -> {
				return new MicroschemaReferenceList();
			}, (x, y) -> {
				x.add(y);
			}).toSingle();
		} catch (Exception e) {
			throw error(INTERNAL_SERVER_ERROR, "Unknown error while getting microschema versions", e);
		}
	}
}
