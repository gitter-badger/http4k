package org.http4k.contract

import argo.jdom.JsonRootNode
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.ContentType
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request.Companion.get
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Argo.json
import org.http4k.format.Argo.parse
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Body
import org.http4k.lens.FormField
import org.http4k.lens.FormValidator.Strict
import org.http4k.lens.Header
import org.http4k.lens.Invalid
import org.http4k.lens.Meta
import org.http4k.lens.Missing
import org.http4k.lens.ParamMeta.StringParam
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.boolean
import org.http4k.lens.int
import org.http4k.lens.webForm
import org.junit.Test

abstract class ModuleRendererContract(private val renderer: ModuleRenderer) {
    fun name(): String = this.javaClass.simpleName

    @Test
    fun `can build 400`() {
        val response = renderer.badRequest(listOf(
            Missing(Meta(true, "location1", StringParam, "name1")),
            Invalid(Meta(false, "location2", StringParam, "name2"))))
        assertThat(response.bodyString(),
            equalTo("""{"message":"Missing/invalid parameters","params":[{"name":"name1","type":"location1","required":true,"reason":"Missing"},{"name":"name2","type":"location2","required":false,"reason":"Invalid"}]}"""))
    }

    @Test
    fun `can build 404`() {
        val response = renderer.notFound()
        assertThat(response.bodyString(),
            equalTo("""{"message":"No route found on this path. Have you used the correct HTTP verb?"}"""))
    }


    @Test
    fun `renders as expected`() {

        val customBody: BiDiBodyLens<JsonRootNode> = Body.json().required("the body of the message")
//        , Argo.obj("anObject" to Argo.obj("notAStringField" to Argo.number(123))))

        val module = RouteModule(Root / "basepath", renderer)
            .securedBy(ApiKey(Query.required("the_api_key"), { true }))
            .withRoute(
                Route("summary of this route", "some rambling description of what this thing actually does")
                    .producing(APPLICATION_JSON)
                    .header(Header.optional("header", "description of the header"))
//                    .returning(ResponseSpec.json(Status.Ok to "peachy", obj("anAnotherObject" to obj("aNumberField" to number(123)))))
//        .returning(Status.Forbidden to "no way jose")
                    .at(GET) / "echo" / Path.of("message") bind { msg -> { Response(OK).body(msg) } })
            .withRoute(
                Route("a post endpoint")
                    .consuming(ContentType.APPLICATION_XML, APPLICATION_JSON)
                    .producing(APPLICATION_JSON)
//                .returning(ResponseSpec.json(Status.Forbidden to "no way jose", obj("aString" to Argo.JsonFormat.string("a message of some kind"))))
                    .query(Query.int().required("query"))
                    .body(customBody)
                    .at(POST) / "echo" / Path.of("message") bind { msg -> { Response(OK).body(msg) } })
            .withRoute(
                Route("a friendly endpoint")
                    .query(Query.boolean().required("query", "description of the query"))
                    .body(Body.webForm(Strict, FormField.int().required("form", "description of the form")))
                    .at(GET) / "welcome" / Path.of("firstName") / "bertrand" / Path.of("secondName") bind { a, _, _ -> { Response(OK).body(a) } })
            .withRoute(
                Route("a simple endpoint")
                    .at(GET) / "simples" bind { Response(OK) })

        val expected = String(this.javaClass.getResourceAsStream("${this.javaClass.simpleName}.json").readBytes())
        val actual = module.toHttpHandler()(get("/basepath?the_api_key=somevalue")).bodyString()
        println(expected)
        println(actual)
        assertThat(parse(actual), equalTo(parse(expected)))
    }
}