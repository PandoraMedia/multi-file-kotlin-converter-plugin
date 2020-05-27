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

enum class IntComparison(private val prettyString: String, val function: (Int, Int) -> Boolean) {
    LESS_THAN("<", { i, j -> i < j }),
    LESS_THAN_EQUAL("<=", { i, j -> i <= j }),
    EQUAL("==", { i, j -> i == j }),
    GREATER_THAN_EQUAL(">=", { i, j -> i >= j }),
    GREATER_THAN(">", { i, j -> i > j }),
    NOT_EQUAL("!=", { i, j -> i != j });

    override fun toString() = prettyString
}
