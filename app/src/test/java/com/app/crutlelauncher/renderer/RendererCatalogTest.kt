package com.app.crutlelauncher.renderer

import com.app.crutlelauncher.renderer.renderers.SFPEWRenderer
import com.app.crutlelauncher.renderer.renderers.HolyGL4ESRenderer
import com.app.crutlelauncher.renderer.renderers.MobileGluesRenderer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RendererCatalogTest {
    @Test
    fun onlyExplicitlyDocumentedTranslationLayersReceiveThatClassification() {
        assertTrue(RendererCatalog.usesAndroidOpenGlTranslationLayer(HolyGL4ESRenderer.ID))
        assertTrue(RendererCatalog.usesAndroidOpenGlTranslationLayer(SFPEWRenderer.ID))
        assertFalse(RendererCatalog.usesAndroidOpenGlTranslationLayer(MobileGluesRenderer.ID))
        assertFalse(RendererCatalog.usesAndroidOpenGlTranslationLayer("third-party-renderer"))
    }
}
