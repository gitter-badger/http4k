package org.reekwest.http.contract

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.junit.Test
import org.reekwest.http.core.Method.GET
import org.reekwest.http.core.Request
import org.reekwest.http.core.Uri.Companion.uri
import org.reekwest.http.core.contract.*

class QueryTest {
    private val request = withQueryOf("/?hello=world&hello=world2")

    @Test
    fun `value present`() {
        assertThat(request[Query.optional("hello")], equalTo("world"))
        assertThat(request[Query.required("hello")], equalTo("world"))
        assertThat(request[Query.map { it.length }.required("hello")], equalTo(5))
        assertThat(request[Query.map { it.length }.optional("hello")], equalTo(5))

        val expected: List<String?> = listOf("world", "world2")
        assertThat(request[Query.multi.required("hello")], equalTo(expected))
        assertThat(request[Query.multi.optional("hello")], equalTo(expected))
    }

    @Test
    fun `value missing`() {
        assertThat(request[Query.optional("world")], absent())
        assertThat({ request[Query.required("world")] }, throws<Missing>())

        assertThat(request[Query.multi.optional("world")], equalTo(emptyList()))
        assertThat({ request[Query.multi.required("world")] }, throws<Missing>())
    }

    @Test
    fun `invalid value`() {
        assertThat({ request[Query.map(String::toInt).required("hello")] }, throws<Invalid>())
        assertThat({ request[Query.map(String::toInt).optional("hello")] }, throws<Invalid>())

        assertThat({ request[Query.map(String::toInt).multi.required("hello")] }, throws<Invalid>())
        assertThat({ request[Query.map(String::toInt).optional("hello")] }, throws<Invalid>())
    }

    @Test
    fun `int`() {
        assertThat(withQueryOf("/?hello=123")[Query.int().optional("hello")], equalTo(123))
        assertThat(withQueryOf("/")[Query.int().optional("world")], absent())
        val badRequest = withQueryOf("/?hello=notAnumber")
        assertThat({ badRequest[Query.int().optional("hello")] }, throws<Invalid>())
    }

    private fun withQueryOf(value: String) = Request(GET, uri(value))
//
//    @Test
//    fun `toString is ok`() {
//        assertThat(Query.required("hello").toString(), equalTo("Required query 'hello'"))
//        assertThat(Query.optional("hello").toString(), equalTo("Optional query 'hello'"))
//        assertThat(Query.multi.required("hello").toString(), equalTo("Required query 'hello'"))
//        assertThat(Query.multi.optional("hello").toString(), equalTo("Optional query 'hello'"))
//    }
}