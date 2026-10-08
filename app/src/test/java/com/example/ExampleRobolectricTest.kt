package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.SelrRepository
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.SelrDatabase
import com.example.model.AlertType
import com.example.model.EmergencyStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: SelrDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, SelrDatabase::class.java).build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `verify SelrViewModel instantiation`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.viewmodel.SelrViewModel(app)
    assertNotNull(vm)
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SELR", appName)
  }

  @Test
  fun `verify selr repository emergency trigger`() {
    val repository = SelrRepository()
    val alert = repository.triggerEmergency(AlertType.SOS_CRITICAL)
    assertNotNull(alert)
    assertEquals(EmergencyStatus.ACTIVE, alert.status)
    assertEquals("Aarav Sharma", alert.studentName)
  }

  @Test
  fun `verify room emergency contact insert and flow read`() = runBlocking {
    val dao = db.emergencyContactDao()
    val contact = EmergencyContactEntity(
      name = "Father",
      relation = "Parent",
      phone = "+91 99999 88888",
      isPrimary = true
    )
    val id = dao.insertContact(contact)
    assertNotNull(id)

    val contacts = dao.getAllContacts().first()
    assertEquals(1, contacts.size)
    assertEquals("Father", contacts[0].name)
    assertEquals("+91 99999 88888", contacts[0].phone)
  }

  @Test
  fun `verify high risk area detection and safe status format`() {
    val repository = SelrRepository()
    val highRiskZone = repository.highRiskZones.value.first()
    assertNotNull(highRiskZone)

    val insideLocation = com.example.model.LocationData(
      latitude = highRiskZone.latitude,
      longitude = highRiskZone.longitude,
      address = "Test Canal Area"
    )
    val detected = repository.findActiveHighRiskZone(insideLocation)
    assertNotNull(detected)
    assertEquals(highRiskZone.id, detected?.id)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationHelper = com.example.service.EmergencyNotificationHelper(context)
    val sms = notificationHelper.formatSafeStatusSms(
      studentName = "Aarav Sharma",
      studentClass = "CS-3A",
      zoneName = highRiskZone.name,
      location = insideLocation,
      nextIntervalMinutes = 30
    )
    assert(sms.contains("SAFE STATUS"))
    assert(sms.contains("30-min auto check-in"))
    assert(sms.contains(highRiskZone.name))
  }

  @Test
  fun `verify student registration and contact synchronization`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.viewmodel.SelrViewModel(app)

    val newStudent = com.example.model.StudentProfile(
      id = "stu_test_99",
      fullName = "Vikram Aditya",
      classRollNo = "ECE-4B / 19",
      schoolCollegeName = "Delhi Institute of Tech",
      bloodGroup = "AB+",
      isHosteller = true,
      mobile = "+91 98888 77777",
      emergencyContact1 = com.example.model.EmergencyContact("Aditya Senior", "Father", "+91 97777 66666"),
      emergencyContact2 = com.example.model.EmergencyContact("Campus Security", "Officer", "112")
    )

    vm.registerStudent(newStudent)
    assertEquals("Vikram Aditya", vm.studentProfile.value.fullName)
    assertEquals("AB+", vm.studentProfile.value.bloodGroup)
    assertEquals(true, vm.studentProfile.value.isHosteller)

    vm.saveEmergencyContact("Aditya Senior", "Father", "+91 97777 66666", true)
  }

  @Test
  fun `verify firestore user live location update and observation`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val repo = com.example.data.firestore.FirestoreEmergencyRepository(app)

    val liveLocation = com.example.data.firestore.FirestoreUserLiveLocation.fromCoordinates(
      userId = "test_user_42",
      studentName = "Rohan Varma",
      studentRollNo = "CS-3A",
      latitude = 28.5450,
      longitude = 77.1926,
      accuracy = 3.5f,
      provider = "gps",
      isEmergencyActive = true,
      activeEmergencyId = "alert_999"
    )

    val res = repo.updateUserLiveLocation(liveLocation)
    assert(res.isSuccess)

    val observed = repo.observeUserLiveLocation("test_user_42").first()
    assertNotNull(observed)
    assertEquals(28.5450, observed!!.latitude, 0.0001)
    assertEquals(77.1926, observed.longitude, 0.0001)
    assertEquals(true, observed.isEmergencyActive)
    assertEquals("alert_999", observed.activeEmergencyId)
  }

  @Test
  fun `verify location tracking service start stop controls`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.viewmodel.SelrViewModel(app)

    vm.startBackgroundLocationTracking(intervalSeconds = 5L)
    vm.stopBackgroundLocationTracking()
    assertEquals(false, vm.isLocationTrackingRunning.value)
  }
}

