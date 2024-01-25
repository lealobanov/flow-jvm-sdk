package com.nftco.flow.sdk.cadence.fields

import com.nftco.flow.sdk.cadence.Field
import com.nftco.flow.sdk.cadence.TYPE_INT
import com.nftco.flow.sdk.cadence.TYPE_STRING
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JsonCadenceBuilderFieldTest {
    @Test
    fun `Test equality for Field with the same type and value`() {
        val field1 = SampleField(TYPE_STRING, "value")
        val field2 = SampleField(TYPE_STRING, "value")
        assertTrue(field1 == field2)
        assertTrue(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with different values`() {
        val field1 = SampleField(TYPE_STRING, "value1")
        val field2 = SampleField(TYPE_STRING, "value2")
        assertFalse(field1 == field2)
        assertFalse(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with different types`() {
        val field1 = SampleField(TYPE_STRING, "value")
        val field2 = SampleField(TYPE_INT, 42)
        assertFalse(field1 == field2)
        assertFalse(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with null values`() {
        val field1 = SampleField(TYPE_STRING, null)
        val field2 = SampleField(TYPE_STRING, null)
        assertTrue(field1 == field2)
        assertTrue(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with null value and non-null value`() {
        val field1 = SampleField(TYPE_STRING, null)
        val field2 = SampleField(TYPE_STRING, "value")
        assertFalse(field1 == field2)
        assertFalse(field1.hashCode() == field2.hashCode())
    }

    @Test
    fun `Test equality for Field with different types and values`() {
        val field1 = SampleField(TYPE_STRING, "value")
        val field2 = SampleField(TYPE_INT, 42)
        assertFalse(field1 == field2)
        assertFalse(field1.hashCode() == field2.hashCode())
    }

    private class SampleField(type: String, value: Any?) : Field<Any>(type, value)
}
