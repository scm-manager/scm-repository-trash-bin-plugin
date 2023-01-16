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
