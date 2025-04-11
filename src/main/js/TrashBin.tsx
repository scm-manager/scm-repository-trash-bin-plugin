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

import {
  Column,
  ConfirmAlert,
  DateFromNow,
  ErrorNotification,
  Level,
  Loading,
  Notification,
  Table,
  TextColumn,
  Title
} from "@scm-manager/ui-components";
import React, { FC, ReactElement, useState } from "react";
import { useDeleteTrashBin, useRestoreTrashBinEntry, useTrashBin } from "./hooks";
import { useTranslation } from "react-i18next";
import { TrashBinEntry, TrashBinEntryCollection } from "./types";
import { Link } from "@scm-manager/ui-types";
import { Button } from "@scm-manager/ui-buttons";
import { useDocumentTitle } from "@scm-manager/ui-core";

const TrashBin: FC<{
  link: string;
}> = ({ link }) => {
  const { data, isLoading, error: loadError } = useTrashBin(link);
  const [error, setError] = useState<Error | undefined | null>();
  const [t] = useTranslation("plugins");
  useDocumentTitle(t("scm-repository-trash-bin-plugin.navLink"));

  let content: ReactElement;
  let deleteAllButton: ReactElement | undefined;

  if (loadError) {
    content = <ErrorNotification error={loadError} />;
  } else if (isLoading) {
    content = <Loading />;
  } else if (!data?._embedded?.entries.length) {
    content = <Notification type="info">{t("scm-repository-trash-bin-plugin.trashBin.empty")}</Notification>;
  } else {
    deleteAllButton = <DeleteAllButton setError={setError} entries={data} />;
    content = (
      <div className="table-container">
        <Table data={data._embedded.entries} sortable={false}>
          <Column
            className="is-vertical-align-middle"
            header={t("scm-repository-trash-bin-plugin.trashBin.table.repository")}
          >
            {(row: TrashBinEntry) => `${row.namespace}/${row.name}`}
          </Column>
          <TextColumn
            className="is-vertical-align-middle"
            dataKey="deletedBy"
            header={t("scm-repository-trash-bin-plugin.trashBin.table.deletedBy")}
          />
          <Column
            className="is-vertical-align-middle"
            header={t("scm-repository-trash-bin-plugin.trashBin.table.deletedAt")}
          >
            {(row: TrashBinEntry) => <DateFromNow date={row.deletedAt} />}
          </Column>
          <Column header="">
            {(row: TrashBinEntry) => (
              <div className="is-flex is-justify-content-end">
                <DeleteButton setError={setError} entry={row} />
                {row._links.restore ? <RestoreButton setError={setError} entry={row} /> : null}
              </div>
            )}
          </Column>
        </Table>
      </div>
    );
  }

  return (
    <>
      <Level left={<Title title={t("scm-repository-trash-bin-plugin.trashBin.title")} />} right={deleteAllButton} />
      {error ? <ErrorNotification error={error} onClose={() => setError(null)} /> : null}
      {content}
    </>
  );
};

type ButtonProps = {
  entry: TrashBinEntry;
  setError: (error: Error) => void;
};

const DeleteButton: FC<ButtonProps> = ({ entry, setError }) => {
  const [t] = useTranslation("plugins");
  const { mutate: deleteTrash, isLoading } = useDeleteTrashBin((entry._links?.delete as Link).href, setError);
  return (
    <Button variant="secondary" isLoading={isLoading} onClick={() => deleteTrash()}>
      {t("scm-repository-trash-bin-plugin.trashBin.table.delete")}
    </Button>
  );
};

const RestoreButton: FC<ButtonProps> = ({ entry, setError }) => {
  const [t] = useTranslation("plugins");
  const { mutate: restore, isLoading } = useRestoreTrashBinEntry((entry._links?.restore as Link).href, setError);
  return (
    <Button className="ml-3" variant="primary" isLoading={isLoading} onClick={() => restore()}>
      {t("scm-repository-trash-bin-plugin.trashBin.table.restore")}
    </Button>
  );
};

const DeleteAllButton: FC<{ entries: TrashBinEntryCollection; setError: (error: Error) => void }> = ({
  entries,
  setError
}) => {
  const [t] = useTranslation("plugins");
  const { mutate: deleteAll, isLoading } = useDeleteTrashBin((entries._links?.deleteAll as Link).href, setError);
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);

  return (
    <>
      <Button variant="secondary" onClick={() => setShowConfirmAlert(true)}>
        {t("scm-repository-trash-bin-plugin.trashBin.table.deleteAll")}
      </Button>
      {showConfirmAlert ? (
        <ConfirmAlert
          title={t("scm-repository-trash-bin-plugin.trashBin.confirmAlert.title")}
          message={t("scm-repository-trash-bin-plugin.trashBin.confirmAlert.message")}
          buttons={[
            {
              label: t("scm-repository-trash-bin-plugin.trashBin.confirmAlert.submit"),
              isLoading,
              onClick: deleteAll
            },
            {
              className: "is-info",
              label: t("scm-repository-trash-bin-plugin.trashBin.confirmAlert.cancel"),
              onClick: () => null,
              autofocus: true
            }
          ]}
          close={() => setShowConfirmAlert(false)}
        />
      ) : null}
    </>
  );
};

export default TrashBin;
