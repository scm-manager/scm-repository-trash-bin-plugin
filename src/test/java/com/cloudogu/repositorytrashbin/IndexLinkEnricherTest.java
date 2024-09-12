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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import jakarta.inject.Provider;
import java.net.URI;

import static com.cloudogu.repositorytrashbin.RepositoryBinManager.MANAGE_TRASH_BIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class IndexLinkEnricherTest {

  @Mock
  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Mock
  private HalAppender appender;

  @Mock
  private HalEnricherContext context;

  @InjectMocks
  private IndexLinkEnricher enricher;

  @Test
  void shouldNotAppendLink() {
    enricher.enrich(context, appender);

    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  @SubjectAware(value = "trillian", permissions = MANAGE_TRASH_BIN)
  void shouldAppendTrashBinLink() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("scm/api/"));
    when(scmPathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);

    enricher.enrich(context, appender);

    verify(appender).appendLink("trashBin", "scm/api/v2/trashBin");
  }
}
