package org.http4k.lens


import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.http4k.lens.ParamMeta.StringParam

object BiDiLensContract {

    val spec = BiDiLensSpec("location", StringParam, LensGet { _: String, str: String ->
        if (str.isBlank()) emptyList() else listOf(str)
    },
        LensSet { _: String, values: List<String>, str: String -> values.fold(str, { memo, next -> memo + next }) })

    fun <T> checkContract(spec: BiDiLensSpec<String, String, T>, valueAsString: String, tValue: T) {
        val optionalLens = spec.optional("hello")
        assertThat(optionalLens(valueAsString), equalTo(tValue))
        assertThat((spec.map { it.toString() }.optional("hello"))(valueAsString), equalTo(tValue.toString()))
        assertThat(optionalLens(""), absent())
        assertThat({ optionalLens("hello") }, throws(equalTo(LensFailure(optionalLens.invalid()))))
        assertThat(optionalLens(tValue, "original"), equalTo("original" + valueAsString))

        val optionalMultiLens = spec.multi.optional("hello")
        assertThat(optionalMultiLens(valueAsString), equalTo(listOf(tValue)))
        assertThat((spec.map { it.toString() }.multi.optional("hello"))(valueAsString), equalTo(listOf(tValue.toString())))
        assertThat(optionalMultiLens(""), absent())
        assertThat({ optionalMultiLens("hello") }, throws(equalTo(LensFailure(optionalLens.invalid()))))
        assertThat(optionalMultiLens(listOf(tValue, tValue), "original"), equalTo("original" + valueAsString + valueAsString))

        val requiredLens = spec.required("hello")
        assertThat(requiredLens(valueAsString), equalTo(tValue))
        assertThat((spec.map { it.toString() }.required("hello"))(valueAsString), equalTo(tValue.toString()))
        assertThat({ requiredLens("") }, throws(equalTo(LensFailure(requiredLens.missing()))))
        assertThat({ requiredLens("hello") }, throws(equalTo(LensFailure(requiredLens.invalid()))))
        assertThat(requiredLens(tValue, "original"), equalTo("original" + valueAsString))

        val requiredMultiLens = spec.multi.required("hello")
        assertThat(requiredMultiLens(valueAsString), equalTo(listOf(tValue)))
        assertThat((spec.map { it.toString() }.multi.required("hello"))(valueAsString), equalTo(listOf(tValue.toString())))
        assertThat({ requiredMultiLens("") }, throws(equalTo(LensFailure(requiredLens.missing()))))
        assertThat({ requiredMultiLens("hello") }, throws(equalTo(LensFailure(requiredLens.invalid()))))
        assertThat(requiredMultiLens(listOf(tValue, tValue), "original"), equalTo("original" + valueAsString + valueAsString))

        val defaultedLens = spec.defaulted("hello", tValue)
        assertThat(defaultedLens(valueAsString), equalTo(tValue))
        assertThat((spec.map { it.toString() }.defaulted("hello", "world"))(valueAsString), equalTo(tValue.toString()))
        assertThat(defaultedLens(""), equalTo(tValue))
        assertThat({ defaultedLens("hello") }, throws(equalTo(LensFailure(defaultedLens.invalid()))))
        assertThat(defaultedLens(tValue, "original"), equalTo("original" + valueAsString))

        val defaultedMultiLens = spec.multi.defaulted("hello", listOf(tValue))
        assertThat(defaultedMultiLens(valueAsString), equalTo(listOf(tValue)))
        assertThat((spec.map { it.toString() }.multi.defaulted("hello", listOf(tValue.toString())))(valueAsString), equalTo(listOf(tValue.toString())))
        assertThat(defaultedMultiLens(""), equalTo(listOf(tValue)))
        assertThat({ defaultedMultiLens("hello") }, throws(equalTo(LensFailure(defaultedMultiLens.invalid()))))
        assertThat(defaultedMultiLens(listOf(tValue, tValue), "original"), equalTo("original" + valueAsString + valueAsString))
    }
}

data class MyCustomBodyType(val value: String)