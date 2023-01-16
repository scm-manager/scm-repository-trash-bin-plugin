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

import { ConfirmAlert, ErrorNotification, Icon, Level } from "@scm-manager/ui-components";
import { Repository } from "@scm-manager/ui-types";
import React, { FC, useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useDeleteRepository } from "@scm-manager/ui-api";
import { Button } from "@scm-manager/ui-buttons";
import { useQueryClient } from "react-query";

type Props = {
  repository: Repository;
};

const MoveToTrashBinButton: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  const history = useHistory();
  const queryClient = useQueryClient();
  const { isLoading, error, remove: moveToTrashBin, isDeleted } = useDeleteRepository({
    onSuccess: async () => {
      await queryClient.invalidateQueries("trashBin");
      history.push("/repos/");
    }
  });
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const confirmDelete = useCallback(() => setShowConfirmAlert(true), []);

  useEffect(() => {
    if (isDeleted) {
      setShowConfirmAlert(false);
    }
  }, [isDeleted]);

  return (
    <>
      <ErrorNotification error={error} />
      {showConfirmAlert ? (
        <ConfirmAlert
          title={t("scm-repository-trash-bin-plugin.repository.confirmAlert.title")}
          message={t("scm-repository-trash-bin-plugin.repository.confirmAlert.message")}
          buttons={[
            {
              label: t("scm-repository-trash-bin-plugin.repository.confirmAlert.submit"),
              onClick: () => moveToTrashBin(repository)
            },
            {
              className: "is-info",
              label: t("scm-repository-trash-bin-plugin.repository.confirmAlert.cancel"),
              onClick: () => null,
              autofocus: true
            }
          ]}
          close={() => setShowConfirmAlert(false)}
        />
      ) : null}
      <Level
        className="mb-5"
        left={
          <div>
            <h4 className="has-text-weight-bold">{t("scm-repository-trash-bin-plugin.repository.subtitle")}</h4>
            <p>{t("scm-repository-trash-bin-plugin.repository.description")}</p>
          </div>
        }
        right={
          <Button variant="signal" onClick={confirmDelete} isLoading={isLoading} className="is-align-self-center mt-3">
            <Icon name="times" color="inherit" className="pr-5" />
            {t("scm-repository-trash-bin-plugin.repository.deleteButton")}
          </Button>
        }
      />
    </>
  );
};

export default MoveToTrashBinButton;
