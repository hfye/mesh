package com.gentics.mesh.graphql.type;

import static com.gentics.mesh.graphql.type.RoleTypeProvider.ROLE_PAGE_TYPE_NAME;
import static com.gentics.mesh.graphql.type.UserTypeProvider.USER_PAGE_TYPE_NAME;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gentics.mesh.core.data.Group;
import com.gentics.mesh.graphql.context.GraphQLContext;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;

@Singleton
public class GroupTypeProvider extends AbstractTypeProvider {

	public static final String GROUP_TYPE_NAME = "Group";

	public static final String GROUP_PAGE_TYPE_NAME = "GroupsPage";

	@Inject
	public InterfaceTypeProvider interfaceTypeProvider;

	@Inject
	public GroupTypeProvider() {
	}

	public GraphQLObjectType createType() {
		Builder groupType = newObject();
		groupType.name(GROUP_TYPE_NAME);
		groupType.description("A group is a collection of users. Groups can't be nested.");
		interfaceTypeProvider.addCommonFields(groupType);

		// .name
		groupType.field(newFieldDefinition().name("name").description("The name of the group.").type(GraphQLString));

		// .roles
		groupType.field(newPagingFieldWithFetcher("roles", "Roles assigned to the group.", (env) -> {
			GraphQLContext gc = env.getContext();
			Group group = env.getSource();
			return group.getRoles(gc.getUser(), getPagingInfo(env));
		}, ROLE_PAGE_TYPE_NAME));

		// .users
		groupType.field(newPagingFieldWithFetcher("users", "Users assigned to the group.", (env) -> {
			GraphQLContext gc = env.getContext();
			Group group = env.getSource();
			return group.getVisibleUsers(gc.getUser(), getPagingInfo(env));
		}, USER_PAGE_TYPE_NAME));
		return groupType.build();
	}

}
