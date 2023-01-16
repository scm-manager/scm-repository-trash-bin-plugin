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
