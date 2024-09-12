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
