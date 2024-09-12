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

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigPermissionUpdateStepTest {

  @Test
  void shouldMigrateInvalidPermission() {
    InMemoryConfigurationEntryStoreFactory storeFactory = new InMemoryConfigurationEntryStoreFactory();
    InMemoryConfigurationEntryStore<AssignedPermission> securityStore = storeFactory.get("security");
    securityStore.put("test", new AssignedPermission("trillian", "configure:read,write:trashBin"));
    securityStore.put("test2", new AssignedPermission("dent", "configure:read,write:trashBinConfig"));

    new ConfigPermissionUpdateStep(storeFactory).doUpdate();

    assertThat(securityStore.get("test").getPermission().getValue()).isEqualTo("configure:read,write:trashBinConfig");
    assertThat(securityStore.get("test2").getPermission().getValue()).isEqualTo("configure:read,write:trashBinConfig");
  }

  @Test
  void shouldDoNothingIfNoInvalidPermissionFound() {
    ConfigurationEntryStoreFactory storeFactory = mock(ConfigurationEntryStoreFactory.class, RETURNS_DEEP_STUBS);
    ConfigurationEntryStore<AssignedPermission> entryStore = mock(ConfigurationEntryStore.class);
    when(storeFactory.withType(AssignedPermission.class).withName("security").build()).thenReturn(entryStore);

    HashMap<String, AssignedPermission> permissions = new HashMap<>();
    permissions.put("test", new AssignedPermission("dent", "configure:read,write:trashBinConfig"));
    when(entryStore.getAll()).thenReturn(permissions);

    new ConfigPermissionUpdateStep(storeFactory).doUpdate();

    verify(entryStore, never()).put(any(), any());
  }
}
