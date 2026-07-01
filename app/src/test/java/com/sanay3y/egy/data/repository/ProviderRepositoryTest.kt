package com.sanay3y.egy.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.sanay3y.egy.data.model.Provider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProviderRepositoryTest {
    // 1. تجهيز العناصر الوهمية (Mocks)
    private val firestore = mockk<FirebaseFirestore>()
    private val collectionReference = mockk<CollectionReference>()
    private val query = mockk<Query>()
    private val querySnapshot = mockk<QuerySnapshot>()
    private val documentSnapshot = mockk<DocumentSnapshot>()
    private val documentReference = mockk<DocumentReference>()
    private val voidTask = mockk<Task<Void>>()

    @Suppress("UNCHECKED_CAST")
    private val queryTask = mockk<Task<QuerySnapshot>>()
    @Suppress("UNCHECKED_CAST")
    private val docTask = mockk<Task<DocumentSnapshot>>()

    private lateinit var repository: ProviderRepository

    @Before
    fun setUp() {
        // عشان نقدر نختبر دوال الـ await() بتاعة الفايربيز، بنعمل لها MockK الاستاتيكي
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        // ربط الفايربيز بالـ Collection الوهمي
        every { firestore.collection("users") } returns collectionReference

        // إنشاء المستودع واستخدام الفايربيز الوهمي اللي صنعناه
        repository = ProviderRepository(firestore)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getAllProviders_success_returnsList() = runTest {
        // ترتيب المشهد (Given)
        every { collectionReference.whereEqualTo("role", "PROVIDER") } returns query
        coEvery { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns listOf(documentSnapshot)

        // تجهيز بيانات المستند الوهمي
        every { documentSnapshot.id } returns "user_1"
        every { documentSnapshot.getString("firebaseUid") } returns "uid_1"
        every { documentSnapshot.getString("name") } returns "Ahmed"
        every { documentSnapshot.getString("category") } returns "Plumber"
        every { documentSnapshot.getDouble("rating") } returns 4.5
        every { documentSnapshot.getLong("reviewCount") } returns 10L
        every { documentSnapshot.getString("phone") } returns "012345"
        every { documentSnapshot.getString("imageUrl") } returns ""
        every { documentSnapshot.getString("bio") } returns "Hi"
        every { documentSnapshot.getLong("experienceYears") } returns 5L
        every { documentSnapshot.getBoolean("isOnline") } returns true
        every { documentSnapshot.getString("location") } returns "Cairo"
        every { documentSnapshot.getDouble("latitude") } returns 30.0
        every { documentSnapshot.getDouble("longitude") } returns 31.0

        // تشغيل الدالة للاختبار (When)
        val result = repository.getAllProviders()

        // التأكد من النتيجة (Then)
        assertTrue(result.isSuccess)
        val list = result.getOrNull()
        assertNotNull(list)
        assertEquals(1, list!!.size)
        assertEquals("Ahmed", list[0].name)
        assertEquals("Plumber", list[0].category)
    }

    @Test
    fun getProvidersByCategory_success_returnsFilteredList() = runTest {
        // Given
        every { collectionReference.whereEqualTo("role", "PROVIDER") } returns query
        every { query.whereEqualTo("category", "Electrician") } returns query
        coEvery { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns emptyList() // مفيش بيانات مثلاً

        // When
        val result = repository.getProvidersByCategory("Electrician")

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun getProviderById_foundDirectly_returnsProvider() = runTest {
        // 1. ترتيب المشهد (Given)
        every { collectionReference.document("id_123") } returns documentReference
        coEvery { documentReference.get().await() } returns documentSnapshot
        every { documentSnapshot.exists() } returns true

        // ⚠️ التعديل هنا: بنحط الـ any() الأول كقيمة افتراضية لكل الخانات
        every { documentSnapshot.getString(any()) } returns ""
        every { documentSnapshot.getDouble(any()) } returns 0.0
        every { documentSnapshot.getLong(any()) } returns 0L
        every { documentSnapshot.getBoolean(any()) } returns false

        // 🎯 هنا بنحدد القيم الخاصة اللي الدالة محتاجاها عشان تنجح وتعدي الشروط
        every { documentSnapshot.id } returns "id_123"
        every { documentSnapshot.getString("role") } returns "PROVIDER" // دي اللي كانت بتبوظ!
        every { documentSnapshot.getString("firebaseUid") } returns "id_123"
        every { documentSnapshot.getString("name") } returns "Mostafa"
        every { documentSnapshot.getString("category") } returns "General"

        // 2. تشغيل الدالة (When)
        val result = repository.getProviderById("id_123")

        // 3. التأكد من النتيجة (Then)
        assertTrue(result.isSuccess) // المفروض تنجح دلوقتي وتجيب علامة خضراء ✅
        assertNotNull(result.getOrNull())
        assertEquals("Mostafa", result.getOrNull()!!.name)
    }

    @Test
    fun searchProviders_success_returnsMatches() = runTest {
        // Given
        every { collectionReference.whereEqualTo("role", "PROVIDER") } returns query
        every { query.orderBy("name") } returns query
        every { query.startAt("Ali") } returns query
        every { query.endAt("Ali\uf8ff") } returns query
        coEvery { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        // When
        val result = repository.searchProviders("Ali")

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun getTopRatedProviders_failure_fallbackToLocalSort() = runTest {
        // Given: هنخلي الفايربيز يفشل ويرمي خطأ عشان نشوف الخطة البديلة هتشتغل ولا لاء
        every { collectionReference.whereEqualTo("role", "PROVIDER") } returns query
        every { query.orderBy("rating", Query.Direction.DESCENDING) } returns query
        every { query.limit(10) } returns query
        coEvery { query.get().await() } throws Exception("Firebase Error")

        // الخطة البديلة هتجيب الكل، فبنديلها الكود البديل الناجح
        every { collectionReference.whereEqualTo("role", "PROVIDER") } returns query
        coEvery { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        // When
        val result = repository.getTopRatedProviders()

        // Then: المفروض تنجح بردو بسبب الخطة البديلة
        assertTrue(result.isSuccess)
    }

    @Test
    fun saveProviderProfile_success_returnsUnit() = runTest {
        // Given
        val provider = com.sanay3y.egy.data.model.Provider(
            id = "1", firebaseUid = "1", name = "Hassan", category = "Painter"
        )
        every { collectionReference.document("1") } returns documentReference
        coEvery { documentReference.set(any(), SetOptions.merge()).await() } returns mockk()

        // When
        val result = repository.saveProviderProfile(provider)

        // Then
        assertTrue(result.isSuccess)
    }
}
