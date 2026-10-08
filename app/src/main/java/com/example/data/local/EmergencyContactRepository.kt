package com.example.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class EmergencyContactRepository(private val dao: EmergencyContactDao) {

    val allContacts: Flow<List<EmergencyContactEntity>> = dao.getAllContacts()

    suspend fun insert(contact: EmergencyContactEntity): Long = dao.insertContact(contact)

    suspend fun update(contact: EmergencyContactEntity) = dao.updateContact(contact)

    suspend fun delete(contact: EmergencyContactEntity) = dao.deleteContact(contact)

    suspend fun deleteById(id: Long) = dao.deleteContactById(id)

    suspend fun setPrimary(id: Long) {
        val contacts = dao.getAllContacts().first()
        contacts.forEach { c ->
            val shouldBePrimary = (c.id == id)
            if (c.isPrimary != shouldBePrimary) {
                dao.updateContact(c.copy(isPrimary = shouldBePrimary))
            }
        }
    }

    suspend fun populateDefaultsIfEmpty() {
        if (dao.getCount() == 0) {
            dao.insertContacts(
                listOf(
                    EmergencyContactEntity(
                        name = "Ramesh Sharma",
                        relation = "Father",
                        phone = "+91 98111 22334",
                        isPrimary = true,
                        isPriorityAlert = true
                    ),
                    EmergencyContactEntity(
                        name = "Sunita Sharma",
                        relation = "Mother",
                        phone = "+91 98222 33445",
                        isPrimary = false,
                        isPriorityAlert = true
                    ),
                    EmergencyContactEntity(
                        name = "Campus Security Desk",
                        relation = "Campus Emergency",
                        phone = "+91 11 2659 1000",
                        isPrimary = false,
                        isPriorityAlert = true
                    )
                )
            )
        }
    }
}
