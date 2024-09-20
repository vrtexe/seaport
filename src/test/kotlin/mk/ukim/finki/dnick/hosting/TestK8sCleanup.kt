package mk.ukim.finki.dnick.hosting

import mk.ukim.finki.dnick.hosting.builder.cleanupK8sName
import org.junit.jupiter.api.Test

class TestK8sCleanup {

    @Test
    fun testCleanup() {
        val result = "image-builder-TestMeBro-1.0.0".cleanupK8sName();
        println(result)
    }
}