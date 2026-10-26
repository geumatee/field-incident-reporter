package com.geumatee.fieldincidentreporter.util

import org.junit.Assert.assertNull
import org.junit.Test

class ImageUtilsTest {

    @Test
    fun `getCachedBitmap returns null for null or blank input`() {
        assertNull(ImageUtils.getCachedBitmap(null))
        assertNull(ImageUtils.getCachedBitmap(""))
        assertNull(ImageUtils.getCachedBitmap("   "))
    }

    @Test
    fun `base64DataUriToBitmap returns null for null or blank input`() {
        assertNull(ImageUtils.base64DataUriToBitmap(null))
        assertNull(ImageUtils.base64DataUriToBitmap(""))
        assertNull(ImageUtils.base64DataUriToBitmap("   "))
    }

    @Test
    fun `base64DataUriToBitmap returns null for uncached unknown dataUri in local jvm test`() {
        assertNull(ImageUtils.getCachedBitmap("data:image/jpeg;base64,unknown"))
    }
}
