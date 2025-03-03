/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.example


import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.eclipse.thingweb.protocol.LMOSContext
import org.eclipse.thingweb.protocol.LMOSThingType
import org.eclipse.thingweb.reflection.annotations.*
import org.jsoup.Jsoup
import org.springframework.stereotype.Component


@Thing(id= "scraper", title="Tool",
    description= "An HTML scraper.", type = LMOSThingType.TOOL)
@VersionInfo(instance = "1.0.0")
@Context(prefix = LMOSContext.prefix, url = LMOSContext.url)
@Component
class HtmlTool() {

    @Action(title = "Fetch Content", description = "Fetches the content from the specified URL.")
    @ActionInput(title = "url", description = "The URL to fetch content from.")
    @ActionOutput(title = "content", description = "The content fetched from the URL.")
    @WithSpan
    suspend fun fetchContent(url: String): String {
        Span.current().setAttribute("lmos.agent.scraper.input.url", url)
        return try {
            val document = Jsoup.connect(url).get()
            document.outerHtml()
        } catch (e: Exception) {
            "Error fetching content"
        }
    }
}

