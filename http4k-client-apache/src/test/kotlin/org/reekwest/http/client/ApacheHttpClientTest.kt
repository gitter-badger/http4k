package org.http4k.http.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.http4k.http.core.Request.Companion.get
import org.http4k.http.core.Status.Companion.OK

class ApacheHttpClientTest {
    val client = ApacheHttpClient()

    @Test
    fun basic_request() {
        val request = get("http://httpbin.org/get").query("name", "John Doe")
        val response = client(request)
        assertThat(response.status, equalTo(OK))
        assertThat(response.bodyString(), containsSubstring("John Doe"))
    }
}
