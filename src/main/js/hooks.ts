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

import { useMutation, useQuery, useQueryClient, UseMutationOptions } from "react-query";
import { apiClient } from "@scm-manager/ui-api";
import { TrashBinEntryCollection } from "./types";

export const useTrashBin = (link: string) =>
  useQuery<TrashBinEntryCollection, Error>("trashBin", () => apiClient.get(link).then(r => r.json()));

export const useRestoreTrashBinEntry = (link: string, onError: UseMutationOptions<unknown, Error>["onError"]) => {
  const queryClient = useQueryClient();
  return useMutation<unknown, Error>({
    mutationFn: () => apiClient.post(link),
    onSuccess: () => queryClient.invalidateQueries("trashBin"),
    onError
  });
};

export const useDeleteTrashBin = (link: string, onError: UseMutationOptions<unknown, Error>["onError"]) => {
  const queryClient = useQueryClient();
  return useMutation<unknown, Error>({
    mutationFn: () => apiClient.delete(link),
    onSuccess: () => queryClient.invalidateQueries("trashBin"),
    onError
  });
};
