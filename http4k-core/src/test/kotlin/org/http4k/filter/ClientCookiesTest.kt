package org.http4k.filter

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.Request
import org.http4k.core.Request.Companion.get
import org.http4k.core.Response
import org.http4k.core.Response.Companion.ok
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.then
import org.http4k.filter.cookie.BasicCookieStorage
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ClientCookiesTest {

    @Test
    fun `can store and send cookies across multiple calls`() {
        val server = { request: Request -> ok().counterCookie(request.counterCookie() + 1) }

        val client = ClientFilters.Cookies().then(server)

        (0..3).forEach {
            val response = client(get("/"))
            assertThat(response.header("Set-Cookie"), equalTo("""counter="${it + 1}"; """))
        }
    }

    @Test
    fun `expired cookies are removed from storage and not sent`() {
        val server = { request: Request ->
            when (request.uri.path) {
                "/set" -> ok().cookie(Cookie("foo", "bar", 5))
                else -> ok().body(request.cookie("foo")?.value ?: "gone")
            }
        }

        val cookieStorage = BasicCookieStorage()

        val clock = object : Clock() {
            var millis: Long = 0
            override fun withZone(zone: ZoneId?): Clock = TODO()
            override fun getZone(): ZoneId = ZoneId.of("GMT")
            override fun instant(): Instant = Instant.ofEpochMilli(millis)
            fun add(seconds: Int) {
                millis += seconds * 1000
            }
        }

        val client = ClientFilters.Cookies(clock, cookieStorage).then(server)

        client(get("/set"))

        assertThat(cookieStorage.retrieve().size, equalTo(1))
        assertThat(client(get("/get")).bodyString(), equalTo("bar"))

        clock.add(10)

        assertThat(client(get("/get")).bodyString(), equalTo("gone"))
    }

    fun Request.counterCookie() = cookie("counter")?.value?.toInt() ?: 0
    fun Response.counterCookie(value: Int) = cookie(Cookie("counter", value.toString()))
}
