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

import com.cloudogu.repositorytrashbin.config.RepositoryTrashBinConfigAdapter;
import com.cloudogu.repositorytrashbin.config.TrashBinConfig;
import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.FullRepositoryExporter;
import sonia.scm.repository.FullRepositoryImporter;
import sonia.scm.repository.Repository;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Singleton
public class RepositoryBinManager {

  public static final String MANAGE_TRASH_BIN = "trashBin:manage";
  public static final String STORE_NAME = "trash-bin";

  private final ConfigurationEntryStoreFactory storeFactory;
  private final BlobStoreFactory blobStoreFactory;
  private final FullRepositoryImporter importer;
  private final FullRepositoryExporter exporter;
  private final RepositoryTrashBinConfigAdapter configAdapter;

  @Inject
  public RepositoryBinManager(
    ConfigurationEntryStoreFactory storeFactory,
    BlobStoreFactory blobStoreFactory,
    FullRepositoryImporter importer,
    FullRepositoryExporter exporter,
    RepositoryTrashBinConfigAdapter configAdapter) {
    this.storeFactory = storeFactory;
    this.blobStoreFactory = blobStoreFactory;
    this.importer = importer;
    this.exporter = exporter;
    this.configAdapter = configAdapter;
  }

  public Collection<TrashBinEntry> getAll() {
    checkPermission();
    return createStore().getAll().values();
  }

  public void addToTrashBin(Repository repository) {
    checkPermission();
    createStore().put(repository.getId(), new TrashBinEntry(repository, SecurityUtils.getSubject().getPrincipal().toString(), Instant.now()));
    Blob blob = createBlobStore().create(repository.getId());
    try {
      exporter.export(repository, blob.getOutputStream(), "");
    } catch (IOException e) {
      throw new RepositoryTrashBinException(
        entity(repository).build(),
        "Failed to store repository in trash bin",
        e
      );
    }
  }

  public void restore(String repositoryId) {
    checkPermission();
    ConfigurationEntryStore<TrashBinEntry> entryStore = createStore();
    BlobStore blobStore = createBlobStore();

    try {
      importer.importFromStream(entryStore.get(repositoryId).getRepository(), blobStore.get(repositoryId).getInputStream(), "");
    } catch (IOException e) {
      throw new RepositoryTrashBinException(
        entity(Repository.class, repositoryId).build(),
        "Failed to restore repository from trash bin",
        e
      );
    }
    entryStore.remove(repositoryId);
    blobStore.remove(repositoryId);
  }

  public void delete(String repositoryId) {
    checkPermission();
    createBlobStore().remove(repositoryId);
    createStore().remove(repositoryId);
  }

  public void deleteAllExpired() {
    TrashBinConfig config = configAdapter.getConfiguration();
    ArrayList<String> toBeDeleted = new ArrayList<>();
    createStore().getAll().values().forEach(entry -> {
      if (shouldDeleteEntry(config, entry)) {
        toBeDeleted.add(entry.getRepository().getId());
      }
    });
    toBeDeleted.forEach(this::delete);
  }

  private boolean shouldDeleteEntry(TrashBinConfig config, TrashBinEntry r) {
    return r.getDeletedAt().isBefore(Instant.now().minus(Integer.parseInt(config.getRetentionTime()), ChronoUnit.DAYS));
  }

  public void deleteAll() {
    ArrayList<String> toBeDeleted = new ArrayList<>();
    createStore().getAll().values().forEach(entry -> toBeDeleted.add(entry.getRepository().getId()));
    toBeDeleted.forEach(this::delete);
  }

  private ConfigurationEntryStore<TrashBinEntry> createStore() {
    return storeFactory.withType(TrashBinEntry.class).withName(STORE_NAME).build();
  }

  private BlobStore createBlobStore() {
    return blobStoreFactory.withName(STORE_NAME).build();
  }

  private void checkPermission() {
    SecurityUtils.getSubject().checkPermission(MANAGE_TRASH_BIN);
  }
}
