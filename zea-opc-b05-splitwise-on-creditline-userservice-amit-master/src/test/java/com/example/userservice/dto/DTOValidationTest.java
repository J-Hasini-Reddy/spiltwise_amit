package com.example.userservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("RegisterRequest Validation Tests")
    class RegisterRequestValidationTests {

        @Test
        @DisplayName("Should pass validation for valid request")
        void validRequest() {
            RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "1234567890", 10000.00);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation for blank name")
        void blankName() {
            RegisterRequest request = new RegisterRequest("", "john@example.com", "1234567890", 10000.00);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        }

        @Test
        @DisplayName("Should fail validation for null name")
        void nullName() {
            RegisterRequest request = new RegisterRequest(null, "john@example.com", "1234567890", 10000.00);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        }

        @Test
        @DisplayName("Should fail validation for name exceeding max length")
        void nameTooLong() {
            String longName = "A".repeat(256);
            RegisterRequest request = new RegisterRequest(longName, "john@example.com", "1234567890", 10000.00);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        }

        @Test
        @DisplayName("Should pass validation for name with max length")
        void nameMaxLength() {
            String maxLengthName = "A".repeat(255);
            RegisterRequest request = new RegisterRequest(maxLengthName, "john@example.com", "1234567890", 10000.00);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("name")));
        }

        @Test
        @DisplayName("Should fail validation for invalid email format")
        void invalidEmail() {
            RegisterRequest request = new RegisterRequest("John Doe", "not-an-email", "1234567890", 10000.00);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        }

        @Test
        @DisplayName("Should fail validation for blank email")
        void blankEmail() {
            RegisterRequest request = new RegisterRequest("John Doe", "", "1234567890", 10000.00);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        }

        @Test
        @DisplayName("Should fail validation for blank phone")
        void blankPhone() {
            RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "", 10000.00);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
        }

        @Test
        @DisplayName("Should fail validation for globalCreditLimit with too many decimal places")
        void tooManyDecimalPlaces() {
            RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "1234567890", 10000.123);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("globalCreditLimit")));
        }

        @Test
        @DisplayName("Should pass validation for globalCreditLimit with two decimal places")
        void twoDecimalPlaces() {
            RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "1234567890", 10000.12);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("globalCreditLimit")));
        }
    }

    @Nested
    @DisplayName("RegisterRequest Getter/Setter Tests")
    class RegisterRequestGetterSetterTests {

        @Test
        @DisplayName("Should get and set all properties correctly")
        void gettersAndSetters() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Jane Doe");
            request.setEmail("jane@example.com");
            request.setPhone("0987654321");
            request.setGlobalCreditLimit(15000.00);

            assertEquals("Jane Doe", request.getName());
            assertEquals("jane@example.com", request.getEmail());
            assertEquals("0987654321", request.getPhone());
            assertEquals(15000.00, request.getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should create with all-args constructor")
        void allArgsConstructor() {
            RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "5555555555", 5000.00);

            assertEquals("Test User", request.getName());
            assertEquals("test@example.com", request.getEmail());
            assertEquals("5555555555", request.getPhone());
            assertEquals(5000.00, request.getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should create with no-args constructor")
        void noArgsConstructor() {
            RegisterRequest request = new RegisterRequest();

            assertNull(request.getName());
            assertNull(request.getEmail());
            assertNull(request.getPhone());
            assertEquals(0.0, request.getGlobalCreditLimit());
        }
    }

    @Nested
    @DisplayName("UserProfileResponse Tests")
    class UserProfileResponseTests {

        @Test
        @DisplayName("Should get and set all properties correctly")
        void gettersAndSetters() {
            UserProfileResponse response = new UserProfileResponse();
            response.setUserId(1L);
            response.setName("John Doe");
            response.setEmail("john@example.com");
            response.setPhone("1234567890");
            response.setGlobalCreditLimit(10000.00);

            assertEquals(1L, response.getUserId());
            assertEquals("John Doe", response.getName());
            assertEquals("john@example.com", response.getEmail());
            assertEquals("1234567890", response.getPhone());
            assertEquals(10000.00, response.getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should create with all-args constructor")
        void allArgsConstructor() {
            UserProfileResponse response = new UserProfileResponse(1L, "John Doe", "john@example.com", "1234567890", 10000.00);

            assertEquals(1L, response.getUserId());
            assertEquals("John Doe", response.getName());
            assertEquals("john@example.com", response.getEmail());
            assertEquals("1234567890", response.getPhone());
            assertEquals(10000.00, response.getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should create with no-args constructor")
        void noArgsConstructor() {
            UserProfileResponse response = new UserProfileResponse();

            assertEquals(0L, response.getUserId());
            assertNull(response.getName());
            assertNull(response.getEmail());
            assertNull(response.getPhone());
            assertEquals(0.0, response.getGlobalCreditLimit());
        }

        @Test
        @DisplayName("Should handle large userId values")
        void largeUserId() {
            UserProfileResponse response = new UserProfileResponse();
            long largeId = Long.MAX_VALUE;
            response.setUserId(largeId);

            assertEquals(largeId, response.getUserId());
        }

        @Test
        @DisplayName("Should handle zero globalCreditLimit")
        void zeroGlobalCreditLimit() {
            UserProfileResponse response = new UserProfileResponse();
            response.setGlobalCreditLimit(0.0);

            assertEquals(0.0, response.getGlobalCreditLimit());
        }
    }
}
