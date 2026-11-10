package com.example

import com.example.data.ProjectRepository
import com.example.model.AuditCategory
import com.example.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun verify_offline_git_commits() {
        val repository = ProjectRepository()
        val initialCount = repository.gitState.value.commitHistory.size

        repository.stageAll()
        val success = repository.commitChanges("feat(mobile): verified offline commits on mobile", isOffline = true)

        assertTrue(success)
        assertEquals(initialCount + 1, repository.gitState.value.commitHistory.size)
        assertEquals(1, repository.gitState.value.pendingOfflineCommits)
        assertTrue(repository.gitState.value.commitHistory.first().isOffline)

        repository.syncWithGitHub()
        assertEquals(0, repository.gitState.value.pendingOfflineCommits)
    }

    @Test
    fun verify_rbac_roles_and_audit() {
        val repository = ProjectRepository()
        assertEquals(UserRole.ADMIN, repository.currentUser.value.role)

        assertTrue(repository.currentUser.value.role.canEditCode())
        assertTrue(repository.currentUser.value.role.canDeploy())

        repository.switchUserRole(UserRole.VIEWER)
        assertEquals(UserRole.VIEWER, repository.currentUser.value.role)
        assertTrue(!repository.currentUser.value.role.canEditCode())
        assertTrue(!repository.currentUser.value.role.canDeploy())

        val roleChangeLog = repository.auditLogs.value.firstOrNull { it.category == AuditCategory.ROLE_CHANGE }
        assertNotNull(roleChangeLog)
    }
}
