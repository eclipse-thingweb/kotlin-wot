/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


fun main(args: Array<String>) {
    runApplication<ThingAgentApplication>(*args)
}

@SpringBootApplication
class ThingAgentApplication {

}
