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

import org.apache.shiro.SecurityUtils;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static com.cloudogu.repositorytrashbin.RepositoryBinManager.MANAGE_TRASH_BIN;

@Enrich(Index.class)
@Extension
public class IndexLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public IndexLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    if (SecurityUtils.getSubject().isPermitted(MANAGE_TRASH_BIN)) {
      LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), RepositoryTrashBinResource.class);
      appender.appendLink("trashBin", linkBuilder.method("getAll").parameters().href());
    }
  }
}
