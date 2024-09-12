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

package com.cloudogu.repositorytrashbin.config;

import com.cloudogu.conveyor.GenerateDto;
import com.cloudogu.conveyor.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;
import jakarta.xml.bind.annotation.XmlRootElement;

@GenerateDto
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class TrashBinConfig {
  @Include
  @Pattern(regexp = "7|14|30")
  private String retentionTime = "30";
}
