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

import React, { FC } from "react";
import { RETENTION_TIME_OPTIONS, TrashBinConfig } from "./types";
import { ConfigurationForm, Form } from "@scm-manager/ui-forms";
import { Title } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useDocumentTitle } from "@scm-manager/ui-core";

const TrashBinConfiguration: FC<{ link: string }> = ({ link }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitle(t("scm-repository-trash-bin-plugin.config.link"));

  return (
    <>
      <Title title={t("scm-repository-trash-bin-plugin.config.title")} />
      <ConfigurationForm<TrashBinConfig>
        link={link}
        translationPath={["plugins", "scm-repository-trash-bin-plugin.config.form"]}
      >
        <Form.Row>
          <Form.Select name="retentionTime">
            {RETENTION_TIME_OPTIONS.map(value => (
              <option value={value} key={value}>
                {value}
              </option>
            ))}
          </Form.Select>
        </Form.Row>
      </ConfigurationForm>
    </>
  );
};

export default TrashBinConfiguration;
