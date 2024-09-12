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

import sonia.scm.lifecycle.PrivilegedStartupAction;
import sonia.scm.plugin.Extension;
import sonia.scm.schedule.Scheduler;

import jakarta.inject.Inject;

@Extension
public class RepositoryBinCleanupJob implements PrivilegedStartupAction {

  private final Scheduler scheduler;
  private final RepositoryBinManager binManager;

  @Inject
  public RepositoryBinCleanupJob(Scheduler scheduler, RepositoryBinManager binManager) {
    this.scheduler = scheduler;
    this.binManager = binManager;
  }

  @Override
  public void run() {
    binManager.deleteAllExpired();
    scheduler.schedule("0 0 2 * * ?", binManager::deleteAllExpired);
  }
}
