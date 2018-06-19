/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.config;

import java.util.Objects;

public abstract class AbstractConfigGroup implements ConfigGroup {
  @Override
  public final int hashCode() {
    return Objects.hash(getType(), getId());
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof AbstractConfigGroup) {
      final AbstractConfigGroup cfg = (AbstractConfigGroup) obj;

      return getType().equals(cfg.getType()) && getId().equals(cfg.getId());
    }
    return false;
  }
}
