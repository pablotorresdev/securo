package com.mb.conitrack.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UseCaseTag Tests")
class UseCaseTagTest {

    @Test
    @DisplayName("Todos los tags deben existir")
    void testAllTagsExist() {
        assertEquals(9, UseCaseTag.values().length);
        assertNotNull(UseCaseTag.CU1);
        assertNotNull(UseCaseTag.CU3);
        assertNotNull(UseCaseTag.CU4);
        assertNotNull(UseCaseTag.CU7);
        assertNotNull(UseCaseTag.CU20);
        assertNotNull(UseCaseTag.CU22);
        assertNotNull(UseCaseTag.CU23);
        assertNotNull(UseCaseTag.CU24);
        assertNotNull(UseCaseTag.CU25);
    }

    @Test
    @DisplayName("CU1 debe tener tag correcto")
    void testCU1Tag() {
        assertEquals("_CU1_", UseCaseTag.CU1.getTag());
    }

    @Test
    @DisplayName("CU3 debe tener tag correcto")
    void testCU3Tag() {
        assertEquals("_CU3_", UseCaseTag.CU3.getTag());
    }

    @Test
    @DisplayName("CU4 debe tener tag correcto")
    void testCU4Tag() {
        assertEquals("_CU4_", UseCaseTag.CU4.getTag());
    }

    @Test
    @DisplayName("CU7 debe tener tag correcto")
    void testCU7Tag() {
        assertEquals("_CU7_", UseCaseTag.CU7.getTag());
    }

    @Test
    @DisplayName("CU20 debe tener tag correcto")
    void testCU20Tag() {
        assertEquals("_CU20_", UseCaseTag.CU20.getTag());
    }

    @Test
    @DisplayName("CU22 debe tener tag correcto")
    void testCU22Tag() {
        assertEquals("_CU22_", UseCaseTag.CU22.getTag());
    }

    @Test
    @DisplayName("CU23 debe tener tag correcto")
    void testCU23Tag() {
        assertEquals("_CU23_", UseCaseTag.CU23.getTag());
    }

    @Test
    @DisplayName("CU24 debe tener tag correcto")
    void testCU24Tag() {
        assertEquals("_CU24_", UseCaseTag.CU24.getTag());
    }

    @Test
    @DisplayName("CU25 debe tener tag correcto")
    void testCU25Tag() {
        assertEquals("_CU25_", UseCaseTag.CU25.getTag());
    }

    @ParameterizedTest
    @CsvSource({
        "CU1, _CU1_",
        "CU3, _CU3_",
        "CU4, _CU4_",
        "CU7, _CU7_",
        "CU20, _CU20_",
        "CU22, _CU22_",
        "CU23, _CU23_",
        "CU24, _CU24_",
        "CU25, _CU25_"
    })
    @DisplayName("getTag debe retornar el tag correcto para cada caso de uso")
    void testGetTagParameterized(String tagName, String expectedTag) {
        UseCaseTag tag = UseCaseTag.valueOf(tagName);
        assertEquals(expectedTag, tag.getTag());
    }

    @ParameterizedTest
    @CsvSource({
        "CU1, _CU1_",
        "CU3, _CU3_",
        "CU4, _CU4_",
        "CU7, _CU7_",
        "CU20, _CU20_",
        "CU22, _CU22_",
        "CU23, _CU23_",
        "CU24, _CU24_",
        "CU25, _CU25_"
    })
    @DisplayName("toString debe retornar el mismo valor que getTag")
    void testToStringParameterized(String tagName, String expectedTag) {
        UseCaseTag tag = UseCaseTag.valueOf(tagName);
        assertEquals(expectedTag, tag.toString());
        assertEquals(tag.getTag(), tag.toString());
    }

    @Test
    @DisplayName("Todos los tags deben tener formato _CUX_")
    void testAllTagsHaveCorrectFormat() {
        for (UseCaseTag tag : UseCaseTag.values()) {
            String tagString = tag.getTag();
            assertTrue(tagString.startsWith("_CU"),
                tag + " tag debe empezar con _CU");
            assertTrue(tagString.endsWith("_"),
                tag + " tag debe terminar con _");
            assertTrue(tagString.matches("_CU\\d+_"),
                tag + " tag debe seguir el formato _CUX_ donde X es uno o más dígitos");
        }
    }

    @Test
    @DisplayName("Todos los tags deben ser únicos")
    void testAllTagsAreUnique() {
        UseCaseTag[] tags = UseCaseTag.values();
        for (int i = 0; i < tags.length; i++) {
            for (int j = i + 1; j < tags.length; j++) {
                assertNotEquals(tags[i].getTag(), tags[j].getTag(),
                    "Tags " + tags[i] + " y " + tags[j] + " deben ser únicos");
            }
        }
    }

    @Test
    @DisplayName("getTag nunca debe retornar null")
    void testGetTagNeverNull() {
        for (UseCaseTag tag : UseCaseTag.values()) {
            assertNotNull(tag.getTag(),
                tag + " getTag() no debe retornar null");
        }
    }

    @Test
    @DisplayName("toString nunca debe retornar null")
    void testToStringNeverNull() {
        for (UseCaseTag tag : UseCaseTag.values()) {
            assertNotNull(tag.toString(),
                tag + " toString() no debe retornar null");
        }
    }

    @Test
    @DisplayName("getTag y toString deben ser consistentes para todos los tags")
    void testGetTagAndToStringConsistency() {
        for (UseCaseTag tag : UseCaseTag.values()) {
            assertEquals(tag.getTag(), tag.toString(),
                tag + " getTag() y toString() deben retornar el mismo valor");
        }
    }

    @Test
    @DisplayName("valueOf debe funcionar con nombres válidos")
    void testValueOfValid() {
        assertEquals(UseCaseTag.CU1, UseCaseTag.valueOf("CU1"));
        assertEquals(UseCaseTag.CU3, UseCaseTag.valueOf("CU3"));
        assertEquals(UseCaseTag.CU4, UseCaseTag.valueOf("CU4"));
        assertEquals(UseCaseTag.CU7, UseCaseTag.valueOf("CU7"));
        assertEquals(UseCaseTag.CU20, UseCaseTag.valueOf("CU20"));
        assertEquals(UseCaseTag.CU22, UseCaseTag.valueOf("CU22"));
        assertEquals(UseCaseTag.CU23, UseCaseTag.valueOf("CU23"));
        assertEquals(UseCaseTag.CU24, UseCaseTag.valueOf("CU24"));
        assertEquals(UseCaseTag.CU25, UseCaseTag.valueOf("CU25"));
    }

    @Test
    @DisplayName("valueOf debe lanzar excepción para nombres inválidos")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> UseCaseTag.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> UseCaseTag.valueOf("CU99"));
        assertThrows(IllegalArgumentException.class, () -> UseCaseTag.valueOf("_CU1_"));
    }

    @Test
    @DisplayName("values debe retornar todos los tags en orden de declaración")
    void testValues() {
        UseCaseTag[] tags = UseCaseTag.values();

        assertEquals(9, tags.length);
        assertEquals(UseCaseTag.CU1, tags[0]);
        assertEquals(UseCaseTag.CU3, tags[1]);
        assertEquals(UseCaseTag.CU4, tags[2]);
        assertEquals(UseCaseTag.CU7, tags[3]);
        assertEquals(UseCaseTag.CU20, tags[4]);
        assertEquals(UseCaseTag.CU22, tags[5]);
        assertEquals(UseCaseTag.CU23, tags[6]);
        assertEquals(UseCaseTag.CU24, tags[7]);
        assertEquals(UseCaseTag.CU25, tags[8]);
    }
}
