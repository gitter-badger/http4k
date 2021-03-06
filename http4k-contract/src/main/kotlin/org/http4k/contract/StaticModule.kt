package org.http4k.contract

import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.ResourceLoader
import org.http4k.core.StaticContent
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.then

class StaticModule(basePath: BasePath,
                   resourceLoader: ResourceLoader = ResourceLoader.Classpath("/"),
                   moduleFilter: Filter = Filter { it }) : Module {

    private val staticContent = moduleFilter.then(StaticContent(basePath.toString(), resourceLoader))

    override fun toRouter(): Router = {
        staticContent(it).let { if (it.status != NOT_FOUND) { _: Request -> it } else null }
    }
}
