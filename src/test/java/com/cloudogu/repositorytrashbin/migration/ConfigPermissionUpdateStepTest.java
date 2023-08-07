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
