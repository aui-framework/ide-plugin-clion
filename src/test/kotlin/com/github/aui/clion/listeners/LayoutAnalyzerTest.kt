package com.github.aui.clion.listeners

import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.junit.internal.runners.JUnit38ClassRunner
import org.junit.runner.RunWith

@RunWith(JUnit38ClassRunner::class) // TODO: drop the annotation when issue with Gradle test scanning go away
@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class LayoutAnalyzerTest : BasePlatformTestCase() {

    fun testRootElementLocator() {
        val f = myFixture.configureByFile("Layout1.cpp")

        val begin = 42 + 21
        val end = begin + 233

        for (i in begin .. end) {
            val result = AUIElementCaretListener.analyze(f, i)
            TestCase.assertNotNull("(caret $i) root should not be null at index", result.first)

            val root = result.first!!

            TestCase.assertEquals("(caret at $i) root start", begin, root.startOffset)
            TestCase.assertEquals("(caret at $i) root end", end, root.endOffset)
        }
    }
    fun testTargetElementLocator() {
        val f = myFixture.configureByFile("Layout1.cpp")

        val begin = 42 + 21
        val end = begin + 233 -1

        for (i in begin..end) {
            var expectedStart = -1
            var expectedEnd = -1

            when (i) {
                // root Vertical { ... }
                in 63..72, in 295..296 -> {
                    expectedStart = 63
                    expectedEnd = 296
                }

                // label "1"
                in 86..103 -> {
                    expectedStart = 86
                    expectedEnd = 103
                }

                // Horizontal { ... }
                in 117..129, in 250..285 -> {
                    expectedStart = 117
                    expectedEnd = 285
                }

                else -> continue
            }

            val result = AUIElementCaretListener.analyze(f, i)
            TestCase.assertNotNull("(caret $i) item should not be null at index", result.second)

            val target = result.second!!


            TestCase.assertEquals("(caret at $i) target start", expectedStart, target.startOffset)
            TestCase.assertEquals("(caret at $i) target end", expectedEnd, target.endOffset)
        }
    }

    override fun getTestDataPath() = "src/test/testData/layout"

}
