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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import jakarta.inject.Provider;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class RepositoryTrashBinResourceTest {

  @Mock
  private RepositoryBinManager binManager;
  @Mock
  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @InjectMocks
  private RepositoryTrashBinResource repositoryTrashBinResource;

  RestDispatcher restDispatcher;

  @BeforeEach
  void initDispatcher() {
    restDispatcher = new RestDispatcher();
    restDispatcher.addSingletonResource(repositoryTrashBinResource);
  }

  @Test
  @SubjectAware(value = "trillian")
  void shouldGetAllEntriesWithoutRestoreLink() throws URISyntaxException {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("scm/api/"));
    when(scmPathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);

    Repository repo = RepositoryTestData.create42Puzzle();
    when(binManager.getAll())
      .thenReturn(ImmutableList.of(new TrashBinEntry(repo, "trillian", Instant.now())));

    MockHttpRequest request = MockHttpRequest.get("/v2/trashBin");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    restDispatcher.invoke(request, response);

    JsonNode content = response.getContentAsJson();
    assertThat(content.get("_links").get("deleteAll").get("href").textValue())
      .isEqualTo("scm/api/v2/trashBin");

    JsonNode entry = content.get("_embedded").get("entries").get(0);
    assertThat(entry.get("namespace").textValue()).isEqualTo("hitchhiker");
    assertThat(entry.get("name").textValue()).isEqualTo("42Puzzle");
    assertThat(entry.get("deletedBy").textValue()).isEqualTo("trillian");
    assertThat(entry.get("_links").get("delete").get("href").textValue()).isEqualTo("scm/api/v2/trashBin/" + repo.getId());
    assertThat(entry.get("_links").get("restore")).isNull();
  }

  @Test
  @SubjectAware(value = "trillian", permissions = "repository:create")
  void shouldGetAllEntriesWithRestoreLink() throws URISyntaxException {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("scm/api/"));
    when(scmPathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);

    Repository repo = RepositoryTestData.create42Puzzle();
    when(binManager.getAll())
      .thenReturn(ImmutableList.of(new TrashBinEntry(repo, "trillian", Instant.now())));

    MockHttpRequest request = MockHttpRequest.get("/v2/trashBin");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    restDispatcher.invoke(request, response);

    JsonNode responseContentAsJson = response.getContentAsJson();
    assertThat(responseContentAsJson.get("_links").get("deleteAll").get("href").textValue())
      .isEqualTo("scm/api/v2/trashBin");

    JsonNode entry = responseContentAsJson.get("_embedded").get("entries").get(0);
    assertThat(entry.get("namespace").textValue()).isEqualTo("hitchhiker");
    assertThat(entry.get("name").textValue()).isEqualTo("42Puzzle");
    assertThat(entry.get("deletedBy").textValue()).isEqualTo("trillian");
    assertThat(entry.get("_links").get("delete").get("href").textValue()).isEqualTo("scm/api/v2/trashBin/" + repo.getId());
    assertThat(entry.get("_links").get("restore").get("href").textValue()).isEqualTo("scm/api/v2/trashBin/" + repo.getId() + "/restore");
  }

  @Test
  void shouldDeleteSingleEntry() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/v2/trashBin/id-1");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(binManager).delete("id-1");
  }

  @Test
  void shouldRestoreSingleEntry() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.post("/v2/trashBin/id-1/restore");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(binManager).restore("id-1");
  }

  @Test
  void shouldDeleteAllEntries() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/v2/trashBin");
    JsonMockHttpResponse response = new JsonMockHttpResponse();

    restDispatcher.invoke(request, response);

    verify(binManager).deleteAll();
  }
}
