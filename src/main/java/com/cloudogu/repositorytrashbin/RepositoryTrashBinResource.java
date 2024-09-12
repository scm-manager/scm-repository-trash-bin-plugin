/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.repositorytrashbin;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("v2/trashBin")
@OpenAPIDefinition(tags = {
  @Tag(name = "Repository Trash Bin", description = "Repository trash bin related endpoints")
})
public class RepositoryTrashBinResource {

  private final RepositoryBinManager binManager;
  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public RepositoryTrashBinResource(RepositoryBinManager binManager, Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.binManager = binManager;
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  @GET
  @Path("")
  @Produces(APPLICATION_JSON)
  @Operation(summary = "Get trash bin entries", description = "Returns a list of repository trash bin entries.", tags = "Repository Trash Bin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"manageTrashBin\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public HalRepresentation getAll() {
    return mapEntries(binManager.getAll());
  }

  @POST
  @Path("{repositoryId}/restore")
  @Operation(summary = "Restore single trash bin entry", description = "Restores a deleted repository from the trash bin.", tags = "Repository Trash Bin")
  @ApiResponse(
    responseCode = "204",
    description = "no content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"manageTrashBin\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response restore(@PathParam("repositoryId") String id) {
    binManager.restore(id);
    return Response.noContent().build();
  }

  @DELETE
  @Path("{repositoryId}")
  @Operation(summary = "Deletes single trash bin entry", description = "Deletes a single repository from the trash bin.", tags = "Repository Trash Bin")
  @ApiResponse(
    responseCode = "204",
    description = "no content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"manageTrashBin\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response delete(@PathParam("repositoryId") String id) {
    binManager.delete(id);
    return Response.noContent().build();
  }

  @DELETE
  @Path("")
  @Operation(summary = "Deletes all trash bin entries", description = "Deletes all repository trash bin entries.", tags = "Repository Trash Bin")
  @ApiResponse(
    responseCode = "204",
    description = "no content"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"manageTrashBin\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response deleteAll() {
    binManager.deleteAll();
    return Response.noContent().build();
  }

  private HalRepresentation mapEntries(Collection<TrashBinEntry> entries) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), RepositoryTrashBinResource.class);
    List<TrashBinEntryDto> mappedEntries = entries.stream().map(e -> {
      String deleteLink = linkBuilder.method("delete").parameters(e.getRepository().getId()).href();
      String restoreLink = linkBuilder.method("restore").parameters(e.getRepository().getId()).href();
      Links.Builder linksBuilder = Links.linkingTo()
        .single(Link.link("delete", deleteLink));

      if (RepositoryPermissions.create().isPermitted()) {
        linksBuilder.single(Link.link("restore", restoreLink));
      }

      return TrashBinEntryDto.from(e, linksBuilder.build());
    }).collect(Collectors.toList());
    String deleteAllLink = linkBuilder.method("deleteAll").parameters().href();

    return new HalRepresentation(Links.linkingTo().single(Link.link("deleteAll", deleteAllLink)).build(), Embedded.embeddedBuilder().with("entries", mappedEntries).build());
  }
}
