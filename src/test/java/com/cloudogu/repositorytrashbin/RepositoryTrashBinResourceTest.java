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

import javax.inject.Provider;
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
