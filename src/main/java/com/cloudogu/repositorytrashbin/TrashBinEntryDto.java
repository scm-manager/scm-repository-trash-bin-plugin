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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nullable;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TrashBinEntryDto extends HalRepresentation {
  private String namespace;
  private String name;
  private String deletedBy;
  private Instant deletedAt;

  private TrashBinEntryDto(@Nullable Links links, @Nullable Embedded embedded) {
    super(links, embedded);
  }

  public static TrashBinEntryDto from(TrashBinEntry entity) {
    return from(entity, null, null);
  }

  public static TrashBinEntryDto from(TrashBinEntry entity, @Nullable Links links) {
    return from(entity, links, null);
  }

  public static TrashBinEntryDto from(TrashBinEntry entity, @Nullable Links links,
                                      @Nullable Embedded embedded) {
    TrashBinEntryDto dto = new TrashBinEntryDto(links, embedded);
    dto.deletedBy = entity.getDeletedBy();
    dto.deletedAt = entity.getDeletedAt();
    dto.namespace = entity.getRepository().getNamespace();
    dto.name = entity.getRepository().getName();
    return dto;
  }
}
