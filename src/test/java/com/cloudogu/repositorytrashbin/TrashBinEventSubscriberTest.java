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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrashBinEventSubscriberTest {

  @Mock
  private RepositoryBinManager binManager;

  @InjectMocks
  private TrashBinEventSubscriber subscriber;

  @Test
  void shouldDoNothing() {
    subscriber.onEvent(new RepositoryEvent(HandlerEventType.CREATE, RepositoryTestData.create42Puzzle()));

    verify(binManager, never()).addToTrashBin(any());
  }

  @Test
  void shouldMoveRepoToTrash() {
    Repository repo = RepositoryTestData.create42Puzzle();
    subscriber.onEvent(new RepositoryEvent(HandlerEventType.BEFORE_DELETE, repo));

    verify(binManager).addToTrashBin(repo);
  }
}
