package org.example.project.model
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime


class UserRepository {
    private fun resultRowToUser(row: ResultRow): Users = Users(
        email = row[User.email],
        password = row[User.passwordHash],
        roles = row[User.role]
    )

    fun getAllUsers(): List<Users> = transaction {
        println("Fetching all users...")
        val result = User.selectAll().map(::resultRowToUser)
        println("Fetched ${result.size} users")
        result
    }

    fun getUserByEmail(email: String): Users? = transaction {
        User.select(User.email eq email)
            .map(::resultRowToUser)
            .singleOrNull()
    }

    // Hash the password before storing it
    fun addUser(user: Users): Boolean = transaction {
        // Check if the user already exists based on email
        if (User.select(User.email eq user.email).empty()) {
            val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
            val currentTime = LocalDateTime.now()

            User.insert {
                it[username] = user.email
                it[email] = user.email
                it[passwordHash] = hashedPassword
                it[role] = user.roles
                it[createdAt] = currentTime
            }
            true
        } else {
            false
        }
    }

    fun deleteUser(email: String): Boolean = transaction {
        User.deleteWhere { User.email eq email } > 0
    }
}
