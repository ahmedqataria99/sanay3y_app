package com.sanay3y.egy.presentation.screens.client

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ClientHomeScreenTest {

    // 1. إنشاء قاعدة اختبار لـ Compose
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clientHomeScreen_elementsAreDisplayedCorrectly() {
        // 2. تشغيل الشاشة داخل بيئة الاختبار
        composeTestRule.setContent {
            ClientHomeScreen(userId = "123")
        }

        // 3. التحقق من ظهور العناصر الأساسية في الشاشة
        composeTestRule.onNodeWithText("Sanay3y").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome").assertIsDisplayed()
        composeTestRule.onNodeWithText("Categories").assertIsDisplayed()

        // التحقق من ظهور أحد التصنيفات للتأكد من أن الـ Grid تعمل
        composeTestRule.onNodeWithText("Plumbing").assertIsDisplayed()
    }

    @Test
    fun clientHomeScreen_categoryClick_triggersCallbackWithCorrectName() {
        // متغير وهمي لتخزين النتيجة عند الضغط
        var clickedCategoryName: String? = null

        // تشغيل الشاشة وتمرير الأكشن للمتغير الوهمي
        composeTestRule.setContent {
            ClientHomeScreen(
                userId = "123",
                onCategoryClick = { categoryName ->
                    clickedCategoryName = categoryName
                }
            )
        }

        // 4. البحث عن عنصر "Plumbing" والضغط عليه
        composeTestRule.onNodeWithText("Plumbing").performClick()

        // 5. التأكد من أن الـ Callback اشتغل ورجّع الاسم الصح
        assertEquals("Plumbing", clickedCategoryName)
    }
}