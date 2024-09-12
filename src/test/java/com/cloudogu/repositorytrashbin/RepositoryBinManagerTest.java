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

import com.cloudogu.repositorytrashbin.config.RepositoryTrashBinConfigAdapter;
import com.cloudogu.repositorytrashbin.config.TrashBinConfig;
import org.apache.shiro.authz.AuthorizationException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.FullRepositoryExporter;
import sonia.scm.repository.FullRepositoryImporter;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryBlobStore;
import sonia.scm.store.InMemoryBlobStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static com.cloudogu.repositorytrashbin.RepositoryBinManager.MANAGE_TRASH_BIN;
import static com.cloudogu.repositorytrashbin.RepositoryBinManager.STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class RepositoryBinManagerTest {

  private ConfigurationEntryStoreFactory storeFactory;
  private final BlobStore blobStore = new InMemoryBlobStore();
  private BlobStoreFactory blobStoreFactory;
  @Mock
  private FullRepositoryImporter importer;
  @Mock
  private FullRepositoryExporter exporter;
  @Mock
  private RepositoryTrashBinConfigAdapter configAdapter;

  @Test
  void shouldThrowAuthorizationExceptionOnGetAll() {
    assertThrows(AuthorizationException.class, () -> createBinManager().getAll());
  }
  @Test
  void shouldThrowAuthorizationExceptionOnMove() {
    assertThrows(AuthorizationException.class, () -> createBinManager().addToTrashBin(RepositoryTestData.create42Puzzle()));
  }

  @Test
  void shouldThrowAuthorizationExceptionOnRestore() {
    assertThrows(AuthorizationException.class, () -> createBinManager().restore(RepositoryTestData.create42Puzzle().getId()));
  }

  @Test
  void shouldThrowAuthorizationExceptionOnDelete() {
    assertThrows(AuthorizationException.class, () -> createBinManager().delete(RepositoryTestData.create42Puzzle().getId()));
  }

  @Test
  @SubjectAware(permissions = "repository:delete:id-1")
  void shouldMoveRepoToTrash() {
    blobStoreFactory = new InMemoryBlobStoreFactory(blobStore);
    storeFactory = new InMemoryConfigurationEntryStoreFactory();
    Repository puzzle = RepositoryTestData.create42Puzzle();
    puzzle.setId("id-1");

    createBinManager().addToTrashBin(puzzle);

    verify(exporter).export(eq(puzzle), any(), eq(""));
    assertThat(storeFactory.withType(TrashBinEntry.class).withName(STORE_NAME).build().get(puzzle.getId())).isNotNull();
    assertThat(blobStore.get(puzzle.getId())).isNotNull();
  }

  @Nested
  @SubjectAware(permissions = {MANAGE_TRASH_BIN, "repository:delete:id-1"})
  class WithPermission {
    Repository puzzle = RepositoryTestData.create42Puzzle();
    RepositoryBinManager binManager;

    @BeforeEach
    void init() {
      blobStoreFactory = new InMemoryBlobStoreFactory(blobStore);
      storeFactory = new InMemoryConfigurationEntryStoreFactory();
      puzzle.setId("id-1");
      binManager = createBinManager();
    }

    @Test
    @SubjectAware(permissions = {"repository:delete:id-2"})
    void shouldGetAllTrashedRepos() {
      binManager.addToTrashBin(puzzle);

      Repository verticalPeopleTransporter = RepositoryTestData.createHappyVerticalPeopleTransporter();
      verticalPeopleTransporter.setId("id-2");
      binManager.addToTrashBin(verticalPeopleTransporter);

      Collection<TrashBinEntry> entries = binManager.getAll();
      assertThat(entries).hasSize(2);
      assertThat(entries.stream().map(TrashBinEntry::getRepository)).contains(puzzle, verticalPeopleTransporter);
    }

    @Test
    void shouldRestoreTrashedRepository() {
      binManager.addToTrashBin(puzzle);

      binManager.restore(puzzle.getId());

      verify(importer).importFromStream(eq(puzzle), any(), eq(""));
      assertThat(storeFactory.withType(TrashBinEntry.class).withName(STORE_NAME).build().get(puzzle.getId())).isNull();
      assertThat(blobStore.get(puzzle.getId())).isNull();
    }

    @Test
    void shouldDeleteTrashedRepository() {
      binManager.addToTrashBin(puzzle);

      binManager.delete(puzzle.getId());

      assertThat(storeFactory.withType(TrashBinEntry.class).withName(STORE_NAME).build().get(puzzle.getId())).isNull();
      assertThat(blobStore.get(puzzle.getId())).isNull();
    }

    @Test
    void shouldDeleteAllTrashedExpiredRepositories() {
      when(configAdapter.getConfiguration()).thenReturn(new TrashBinConfig());
      ConfigurationEntryStore<TrashBinEntry> store = storeFactory.withType(TrashBinEntry.class).withName(STORE_NAME).build();

      store.put(puzzle.getId(), new TrashBinEntry(puzzle, "trillian", Instant.now()));
      blobStore.create(puzzle.getId());

      Repository heartOfGold = RepositoryTestData.createHeartOfGold();
      store.put(heartOfGold.getId(), new TrashBinEntry(heartOfGold, "zaphod", Instant.now().minus(31, ChronoUnit.DAYS)));
      blobStore.create(heartOfGold.getId());

      Repository verticalPeopleTransporter = RepositoryTestData.createHappyVerticalPeopleTransporter();
      store.put(verticalPeopleTransporter.getId(), new TrashBinEntry(verticalPeopleTransporter, "zaphod", Instant.now().minus(42, ChronoUnit.DAYS)));
      blobStore.create(verticalPeopleTransporter.getId());

      binManager.deleteAllExpired();

      assertThat(blobStore.getAll()).hasSize(1);
      assertThat(blobStore.get(puzzle.getId())).isNotNull();
      assertThat(store.getAll().values()).hasSize(1);
      assertThat(store.get(puzzle.getId())).isNotNull();
    }

    @Test
    void shouldDeleteAllTrashedRepositories() {
      ConfigurationEntryStore<TrashBinEntry> store = storeFactory.withType(TrashBinEntry.class).withName(STORE_NAME).build();

      Repository puzzle = RepositoryTestData.create42Puzzle();
      store.put(puzzle.getId(), new TrashBinEntry(puzzle, "trillian", Instant.now()));
      blobStore.create(puzzle.getId());

      Repository heartOfGold = RepositoryTestData.createHeartOfGold();
      store.put(heartOfGold.getId(), new TrashBinEntry(heartOfGold, "zaphod", Instant.now().plus(31, ChronoUnit.DAYS)));
      blobStore.create(heartOfGold.getId());

      Repository verticalPeopleTransporter = RepositoryTestData.createHappyVerticalPeopleTransporter();
      store.put(verticalPeopleTransporter.getId(), new TrashBinEntry(verticalPeopleTransporter, "zaphod", Instant.now().plus(42, ChronoUnit.DAYS)));
      blobStore.create(verticalPeopleTransporter.getId());

      binManager.deleteAll();

      assertThat(blobStore.getAll()).isEmpty();
      assertThat(store.getAll().values()).isEmpty();
    }
  }

  private RepositoryBinManager createBinManager() {
    return new RepositoryBinManager(storeFactory, blobStoreFactory, importer, exporter, configAdapter);
  }
}
