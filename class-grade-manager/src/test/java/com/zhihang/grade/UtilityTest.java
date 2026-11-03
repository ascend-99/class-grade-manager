package com.zhihang.grade;

import com.zhihang.grade.util.JsonUtil;
import com.zhihang.grade.util.PasswordUtil;
import com.zhihang.grade.util.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilityTest {

    @Test
    void studentNumberValidationAcceptsExpectedFormats() {
        assertTrue(Validation.validStudentNo("20230101"));
        assertTrue(Validation.validStudentNo("S2023A1"));
        assertFalse(Validation.validStudentNo("2023 0101"));
        assertFalse(Validation.validStudentNo("abc"));
    }

    @Test
    void scoreValidationUsesClosedRange() {
        assertTrue(Validation.validScore(0));
        assertTrue(Validation.validScore(59.5));
        assertTrue(Validation.validScore(100));
        assertFalse(Validation.validScore(-0.1));
        assertFalse(Validation.validScore(100.1));
    }

    @Test
    void bcryptHashCanBeVerifiedButIsNotPlainText() {
        String hash = PasswordUtil.hash("123456");
        assertTrue(PasswordUtil.matches("123456", hash));
        assertFalse(PasswordUtil.matches("654321", hash));
        assertFalse(hash.contains("123456"));
    }

    @Test
    void jsonQuoteEscapesScriptBreakingCharacters() {
        assertEquals("\"a\\\"b\"", JsonUtil.quote("a\"b"));
        assertTrue(JsonUtil.quote("</script>").contains("\\u003c"));
        assertTrue(JsonUtil.quote("a\nb").contains("\\n"));
    }
}
