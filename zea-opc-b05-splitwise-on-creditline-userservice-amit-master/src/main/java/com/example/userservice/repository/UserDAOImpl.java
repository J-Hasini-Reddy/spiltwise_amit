package com.example.userservice.repository;

import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.exceptions.UserUpdateRequestFailedException;
import com.example.userservice.model.UserStatus;
import in.zeta.springframework.boot.commons.postgres.GenericPostgresDAO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Repository
public class UserDAOImpl implements UserDAO {

    private final GenericPostgresDAO dao;

    private static final String INSERT_USER_SQL = "INSERT INTO users (name, email, phone, global_credit_limit, status, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?::user_status, ?, ?) RETURNING user_id, name, email, phone, global_credit_limit";
    private static final String FETCH_USER_BY_ID_SQL = "SELECT user_id, name, email, phone, global_credit_limit FROM users WHERE user_id = ?";
    private static final String UPDATE_USER_SQL = "UPDATE users SET name = ?, email = ?, phone = ?, global_credit_limit = ?, updated_at = ? WHERE user_id = ?";
    private static final String CHECK_EMAIL_OR_PHONE_EXISTS = "SELECT COUNT(*) FROM users WHERE phone = ? OR email = ?";
    private static final String CHECK_EMAIL_OR_PHONE_EXISTS_EXCLUDING_USER = "SELECT COUNT(*) FROM users WHERE (phone = ? OR email = ?) AND user_id != ?";
    private static final String UPDATE_USER_GLOBAL_LIMIT = "UPDATE users SET global_credit_limit = ?, updated_at = ? WHERE user_id = ?";

    private final RowMapper<UserProfileResponse> userRowMapper = (rs, rowNum) -> {
        UserProfileResponse user = new UserProfileResponse();
        user.setUserId(rs.getLong("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setGlobalCreditLimit(rs.getDouble("global_credit_limit"));
        return user;
    };

    public UserDAOImpl(GenericPostgresDAO dao) {
        this.dao = dao;
    }

    @Override
    public CompletionStage<UserProfileResponse> createUser(RegisterRequest registerUser) {
        return dao.queryForObject(
                INSERT_USER_SQL,
                userRowMapper,
                registerUser.getName(),
                registerUser.getEmail(),
                registerUser.getPhone(),
                registerUser.getGlobalCreditLimit(),
                UserStatus.ACTIVE.name().toUpperCase(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Override
    public CompletionStage<Optional<UserProfileResponse>> getUserById(long userId) {
        return dao.queryForOptionalObject(
                FETCH_USER_BY_ID_SQL,
                userRowMapper,
                userId
        );
    }

    @Override
    public CompletionStage<UserProfileResponse> updateUser(long userId, RegisterRequest updateUser) {
        return dao.query(FETCH_USER_BY_ID_SQL, userRowMapper, userId)
                .thenCompose(users -> {
                    if (users.isEmpty()) {
                        throw new UserNotFoundException("User with ID " + userId + " does not exist");
                    }
                    return dao.update(
                            UPDATE_USER_SQL,
                            updateUser.getName(),
                            updateUser.getEmail(),
                            updateUser.getPhone(),
                            updateUser.getGlobalCreditLimit(),
                            LocalDateTime.now(),
                            userId
                    ).thenApply(rowsAffected -> {
                        if (rowsAffected == 0) {
                            throw new UserUpdateRequestFailedException("Failed to update user with ID " + userId);
                        }
                        UserProfileResponse response = users.get(0);
                        response.setName(updateUser.getName());
                        response.setEmail(updateUser.getEmail());
                        response.setPhone(updateUser.getPhone());
                        response.setGlobalCreditLimit(updateUser.getGlobalCreditLimit());
                        return response;
                    });
                });
    }

    @Override
    public CompletionStage<Boolean> isPhoneOrEmailExists(String phone, String email) {
        return dao.queryForObject(
                CHECK_EMAIL_OR_PHONE_EXISTS,
                (rs, rowNum) -> rs.getLong(1) > 0,
                phone,
                email
        );
    }

    @Override
    public CompletionStage<Boolean> isPhoneOrEmailExistsExcludingUser(String phone, String email, long userId) {
        return dao.queryForObject(
                CHECK_EMAIL_OR_PHONE_EXISTS_EXCLUDING_USER,
                (rs, rowNum) -> rs.getLong(1) > 0,
                phone,
                email,
                userId
        );
    }

    @Override
    public CompletionStage<UserProfileResponse> updateUserGlobalLimit(long userId, double newLimit) {
        return dao.query(FETCH_USER_BY_ID_SQL, userRowMapper, userId)
                .thenCompose(users -> {
                    if (users.isEmpty()) {
                        throw new UserNotFoundException("User with ID " + userId + " does not exist");
                    }
                    return dao.update(
                            UPDATE_USER_GLOBAL_LIMIT,
                            newLimit,
                            LocalDateTime.now(),
                            userId
                    ).thenApply(rowsAffected -> {
                        if (rowsAffected == 0) {
                            throw new UserUpdateRequestFailedException("Failed to update global limit for user with ID " + userId);
                        }
                        UserProfileResponse response = users.get(0);
                        response.setGlobalCreditLimit(newLimit);
                        return response;
                    });
                });
    }
}
