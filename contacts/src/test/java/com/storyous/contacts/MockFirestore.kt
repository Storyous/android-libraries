package com.storyous.storyouspay.contacts

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.mockito.ArgumentMatchers

class MockFirestore<T>(private val clazz: Class<T>) {

    var nextException: Exception? = null
    var authException: Exception? = null
    var authenticated = true
    val state = mutableMapOf<String, T>()

    fun clear() {
        nextException = null
        state.clear()
    }

    private val snapshot = mock<QuerySnapshot> {
        on { toObjects(clazz) } doAnswer { state.values.toList() }
    }

    private val task = mock<Task<QuerySnapshot>> {
        on { isComplete } doReturn true
        on { result } doReturn snapshot
        on { exception } doAnswer { nextException }
    }

    private val setTask = mock<Task<Void>> {
        on { isComplete } doReturn true

    }

    private val collection = mock<CollectionReference> {
        on { get() } doReturn task
        on { orderBy(ArgumentMatchers.anyString()) } doReturn this.mock
        on {
            whereGreaterThanOrEqualTo(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
            )
        } doReturn this.mock
        on {
            whereLessThan(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
            )
        } doReturn this.mock
        on { limit(any()) } doReturn this.mock
        on { document(ArgumentMatchers.anyString()) } doAnswer {

            val path = it.getArgument<String>(0)
            mock {
                on { set(any()) } doAnswer {
                    nextException?.run { throw this }
                    state[path] = it.getArgument(0)
                    setTask
                }
            }
        }
    }

    val firestore = mock<FirebaseFirestore> {
        on { collection(ArgumentMatchers.anyString()) } doReturn collection
    }

    private val authResult = mock<AuthResult> {}

    private val authTask = mock<Task<AuthResult>> {
        on { isComplete } doReturn true
        on { result } doReturn authResult
        on { exception } doAnswer { nextException }
    }

    val auth = mock<FirebaseAuth> {
        on { currentUser } doAnswer {
            if (authenticated) mock {} else null
        }
        on { signInWithCustomToken(ArgumentMatchers.anyString()) } doAnswer {
            nextException?.run { throw this }
            authTask
        }
    }
}
