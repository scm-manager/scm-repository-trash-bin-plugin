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

package com.cloudogu.repositorytrashbin.config;

import sonia.scm.api.v2.resources.ConfigurationAdapterBase;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;

@Path("v2/trashBinConfig")
@Extension
@Enrich(Index.class)
public class RepositoryTrashBinConfigAdapter extends ConfigurationAdapterBase<TrashBinConfig, TrashBinConfigDto> {

  @Inject
  public RepositoryTrashBinConfigAdapter(Provider<ScmPathInfoStore> scmPathInfoStore, ConfigurationStoreFactory storeFactory) {
    super(storeFactory, scmPathInfoStore, TrashBinConfig.class, TrashBinConfigDto.class);
  }

  @Override
  protected String getName() {
    return "trashBinConfig";
  }
}
