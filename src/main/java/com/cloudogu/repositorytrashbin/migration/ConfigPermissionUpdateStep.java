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

package com.cloudogu.repositorytrashbin.migration;

import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.version.Version;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Extension
public class ConfigPermissionUpdateStep implements UpdateStep {

  private final ConfigurationEntryStoreFactory storeFactory;

  @Inject
  public ConfigPermissionUpdateStep(ConfigurationEntryStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate() {
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();
    Map<String, AssignedPermission> allPermissions = securityStore.getAll();

    Set<Map.Entry<String, AssignedPermission>> invalidPermissions = allPermissions.entrySet().stream()
      .filter(e -> e.getValue().getPermission().getValue().contains(":trashBin") && !e.getValue().getPermission().getValue().contains("trashBinConfig"))
      .collect(Collectors.toSet());

    invalidPermissions.forEach(e -> {
      AssignedPermission oldPermission = e.getValue();

      AssignedPermission newPermission = new AssignedPermission(
        oldPermission.getName(),
        oldPermission.isGroupPermission(),
        new PermissionDescriptor(oldPermission.getPermission().getValue().replace(":trashBin", ":trashBinConfig")));
      securityStore.put(e.getKey(), newPermission);
    });
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("1.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin.trashBinConfigPermission";
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return storeFactory.withType(AssignedPermission.class).withName("security").build();
  }
}
