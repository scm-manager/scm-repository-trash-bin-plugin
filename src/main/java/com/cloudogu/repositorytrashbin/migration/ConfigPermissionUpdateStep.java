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

package com.cloudogu.repositorytrashbin.migration;

import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.version.Version;

import javax.inject.Inject;
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
