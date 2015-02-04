package com.gentics.cailun.core.repository;

import org.springframework.data.neo4j.repository.GraphRepository;

import com.gentics.cailun.core.rest.model.auth.AbstractPermission;

public interface PermissionRepository extends GraphRepository<AbstractPermission> {

}
