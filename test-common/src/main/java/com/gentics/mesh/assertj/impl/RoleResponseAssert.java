package com.gentics.mesh.assertj.impl;

import static com.gentics.mesh.assertj.MeshAssertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.gentics.mesh.assertj.AbstractMeshAssert;
import com.gentics.mesh.core.data.Role;
import com.gentics.mesh.core.rest.role.RoleCreateRequest;
import com.gentics.mesh.core.rest.role.RoleResponse;

public class RoleResponseAssert extends AbstractMeshAssert<RoleResponseAssert, RoleResponse> {

	public RoleResponseAssert(RoleResponse actual) {
		super(actual, RoleResponseAssert.class);
	}

	public RoleResponseAssert matches(Role role) {
		assertGenericNode(role, actual);
		assertEquals(role.getName(), actual.getName());
		assertNotNull(actual.getGroups());
		return this;
	}

	public RoleResponseAssert matches(RoleCreateRequest request) {
		assertNotNull(request);
		assertNotNull(actual);
		assertEquals(request.getName(), actual.getName());
		assertNotNull(actual.getUuid());
		assertNotNull(actual.getGroups());
		return this;
	}

	public RoleResponseAssert hasName(String name) {
		assertThat(actual.getName()).as("Role name").isEqualTo(name);
		return this;
	}

	public RoleResponseAssert hasUuid(String uuid) {
		assertThat(actual.getUuid()).as("Role uuid").isEqualTo(uuid);
		return this;
	}
}
