package com.minicommerceapi.minicommerce.unit;

import com.minicommerceapi.minicommerce.util.SlugUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlugUtilTest {

    @Test
    void slugify_basic() {
        assertEquals("winter-jacket", SlugUtil.slugify("Winter Jacket"));
    }
}
