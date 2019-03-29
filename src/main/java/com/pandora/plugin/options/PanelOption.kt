/*
 * Copyright 2019 Pandora Media, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See accompanying LICENSE file or you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pandora.plugin.options

import java.awt.GridBagConstraints

enum class PanelOption(val constraints: GridBagConstraints) {
    WRAP(GridBagConstraints().apply {
        this.anchor = GridBagConstraints.LINE_START
    }),
    FILL(GridBagConstraints().apply {
        this.anchor = GridBagConstraints.LINE_START
        this.weightx = 1.0
        this.fill = GridBagConstraints.HORIZONTAL
    })
}