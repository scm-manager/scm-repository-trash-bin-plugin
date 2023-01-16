/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
