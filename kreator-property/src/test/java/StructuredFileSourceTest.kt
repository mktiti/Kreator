import hu.mktiti.kreator.property.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class StructuredFileSourceTest {

    // Property parse
    @Test
    fun `parse empty property`() {
        assertEquals(null, parseProperty(""))
    }

    @Test
    fun `parse only name property`() {
        assertEquals(null, parseProperty("property"))
    }

    @Test
    fun `parse key pair property`() {
        assertEquals("key" to "value", parseProperty("key: value"))
    }

    @Test
    fun `parse multi separator invalid property`() {
        assertEquals(null, parseProperty("key: value:rest"))
    }

    @Test
    fun `parse blank key property`() {
        assertEquals(null, parseProperty(" : value"))
    }

    @Test
    fun `parse blank value property`() {
        assertEquals(null, parseProperty("key : "))
    }

    // Block parse
    @Test
    fun `parse empty block`() {
        assertThrows(PropertyConfigException::class.java) { parseBlock("") }
    }

    @Test
    fun `parse simple property block`() {
        val node = parseBlock("key : value")
        assertEquals(NamedNode("key", RadixNode("value")), node)
    }

    @Test
    fun `parse empty named block`() {
        val node = parseBlock("key {}")
        assertEquals(NamedNode("key", RadixNode(null)), node)
    }

    @Test
    fun `parse empty named block with base value`() {
        val node = parseBlock("key : value {}")
        assertEquals(NamedNode("key", RadixNode("value")), node)
    }

    @Test
    fun `parse named block with property`() {
        val node = parseBlock("""
            key {
                property: value
            }
        """)
        assertEquals(NamedNode("key", RadixNode(null, mapOf("property" to RadixNode("value")))), node)
    }

    @Test
    fun `parse named block inlined properties`() {
        val node = parseBlock("""
            key { a: b, c: d, e: f }
        """)
        assertEquals(NamedNode("key", RadixNode(null, mapOf(
                "a" to RadixNode("b"),
                "c" to RadixNode("d"),
                "e" to RadixNode("f")
        ))), node)
    }

    @Test
    fun `parse named block with property base value`() {
        val node = parseBlock("""
            key : base-value {
                property: value
            }
        """)
        assertEquals(NamedNode("key", RadixNode("base-value", mapOf("property" to RadixNode("value")))), node)
    }

    @Test
    fun `parse named block with multi property`() {
        val node = parseBlock("""
            key {
                property1: value1
                property2: value2
                property3: value3
            }
        """)

        val expectedNode = RadixNode<String, String>(null, mapOf(
                "property1" to RadixNode("value1"),
                "property2" to RadixNode("value2"),
                "property3" to RadixNode("value3")

        ))

        assertEquals(NamedNode("key", expectedNode), node)
    }

    @Test
    fun `parse named block with multi property base value`() {
        val node = parseBlock("""
            key: base-value {
                property1: value1
                property2: value2
                property3: value3
            }
        """)

        val expectedNode = RadixNode("base-value", mapOf(
                "property1" to RadixNode("value1"),
                "property2" to RadixNode("value2"),
                "property3" to RadixNode("value3")

        ))

        assertEquals(NamedNode("key", expectedNode), node)
    }

    // Root elements
    @Test
    fun `parse root empty`() {
        val root = parseContentList("")
        assertEquals(emptyList<String>(), root)
    }

    @Test
    fun `parse root single property`() {
        val root = parseContentList("key: value")
        assertEquals(listOf(NamedNode("key", RadixNode("value"))), root)
    }

    @Test
    fun `parse root multi property`() {
        val root = parseContentList("""
            key1: val1
            key2: val2
            key3: val3
        """)

        assertEquals(listOf(
                NamedNode("key1", RadixNode("val1")),
                NamedNode("key2", RadixNode("val2")),
                NamedNode("key3", RadixNode("val3"))
        ), root)
    }

    @Test
    fun `parse root complex`() {
        val root = parseContentList("""
            single: value

            prefix {
                a : b
                c : d { inlined1: val1, inlined2: val2 }
            }

            outer {
                inner : in {
                    x : y
                }
            }

            chain : 1 {
                chain : 2 {
                    chain : 3 {
                        chain : 4 {
                            chain : 5 {
                                key: prop
                            }
                        }
                    }
                }
            }
        """)

        val list = listOf(
                NamedNode("single", RadixNode("value")),
                NamedNode("prefix", RadixNode(null, mapOf(
                        "a" to RadixNode("b"),
                        "c" to RadixNode("d", mapOf(
                                "inlined1" to RadixNode("val1"),
                                "inlined2" to RadixNode("val2")
                        ))
                ))),
                NamedNode("outer", RadixNode(null, mapOf(
                        "inner" to RadixNode("in", mapOf(
                            "x" to RadixNode("y")
                        ))
                ))),
                NamedNode("chain", RadixNode("1", mapOf(
                        "chain" to RadixNode("2", mapOf(
                                "chain" to RadixNode("3", mapOf(
                                        "chain" to RadixNode("4", mapOf(
                                                "chain" to RadixNode("5", mapOf(
                                                        "key" to RadixNode("prop")
                                                ))
                                        ))
                                ))
                        ))
                )))
        )

        assertEquals(list, root)
    }
}