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

import { HalRepresentationWithEmbedded, HalRepresentation } from "@scm-manager/ui-types";

export const RETENTION_TIME_OPTIONS = ["7", "14", "30"] as const;

type RetentionTime = typeof RETENTION_TIME_OPTIONS[number];

export type TrashBinConfig = HalRepresentation & {
  retentionTime: RetentionTime;
};

export type TrashBinEntry = HalRepresentation & {
  namespace: string;
  name: string;
  deletedAt: string;
  deletedBy: string;
};

export type TrashBinEntryCollection = HalRepresentationWithEmbedded<{ entries: TrashBinEntry[] }>;
