package com.example.budget_management_app.user;

import com.example.budget_management_app.account.service.AccountService;
import com.example.budget_management_app.auth.dto.RegistrationRequestDto;
import com.example.budget_management_app.category.service.CategoryService;
import com.example.budget_management_app.common.dto.ResponseMessageDto;
import com.example.budget_management_app.common.exception.*;
import com.example.budget_management_app.recurring_transaction.service.RecurringTransactionService;
import com.example.budget_management_app.security.service.TwoFactorAuthenticationService;
import com.example.budget_management_app.transaction.service.TransactionService;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import com.example.budget_management_app.user.domain.UserStatus;
import com.example.budget_management_app.user.dto.ChangePasswordRequestDto;
import com.example.budget_management_app.user.dto.TfaQRCode;
import com.example.budget_management_app.user.dto.UpdateUserRequestDto;
import com.example.budget_management_app.user.dto.UserResponseDto;
import com.example.budget_management_app.user.mapper.UserMapper;
import com.example.budget_management_app.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private TwoFactorAuthenticationService tfaService;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AccountService accountService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private RecurringTransactionService recurringTransactionService;
    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUpSelfInjection() {
        Field selfField = ReflectionUtils.findField(UserServiceImpl.class, "self");
        if (selfField != null) {
            ReflectionUtils.makeAccessible(selfField);
            ReflectionUtils.setField(selfField, userService, userService);
        } else {
            throw new RuntimeException("Not found 'self' w UserServiceImpl");
        }
    }

    @Nested
    class CreateUserTests {

        private RegistrationRequestDto dto;
        private User savedUser;
        private final String rawPassword = "rawPassword123";

        @BeforeEach
        void setUp() {
            dto = new RegistrationRequestDto(
                    "Jan",
                    "Kowalski",
                    "jan.kowalski@example.com",
                    rawPassword,
                    rawPassword
            );

            savedUser = new User();
            savedUser.setId(1L);
            savedUser.setEmail(dto.email());
        }

        @Test
        void should_create_user_successfully() {
            //given
            when(userDao.findByEmail(dto.email())).thenReturn(Optional.empty());
            String hashedPassword = "hashedPasswordXYZ";
            when(encoder.encode(rawPassword)).thenReturn(hashedPassword);
            when(userDao.save(any(User.class))).thenReturn(savedUser);
            doNothing().when(categoryService).assignInitialCategories(any(User.class));
            doNothing().when(accountService).createDefaultAccount(any(User.class));

            //when
            User result = userService.createUser(dto);

            //then
            assertThat(result).isEqualTo(savedUser);

            verify(userDao, times(1)).findByEmail(dto.email());
            verify(encoder, times(1)).encode(rawPassword);
            verify(userDao, times(1)).save(userCaptor.capture());

            User userToSave = userCaptor.getValue();
            assertThat(userToSave.getName()).isEqualTo(dto.name());
            assertThat(userToSave.getSurname()).isEqualTo(dto.surname());
            assertThat(userToSave.getEmail()).isEqualTo(dto.email());
            assertThat(userToSave.getPassword()).isEqualTo(hashedPassword);
            assertThat(userToSave.getStatus()).isEqualTo(UserStatus.PENDING_CONFIRMATION);
            assertThat(userToSave.getCreatedAt()).isNotNull();

            verify(categoryService, times(1)).assignInitialCategories(userToSave);
            verify(accountService, times(1)).createDefaultAccount(userToSave);
        }

        @Test
        void should_throw_ValidationException_when_email_already_used() {
            //given
            when(userDao.findByEmail(dto.email())).thenReturn(Optional.of(new User()));

            //when
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> userService.createUser(dto));

            //then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_USED);

            verify(encoder, never()).encode(anyString());
            verify(userDao, never()).save(any(User.class));
            verify(categoryService, never()).assignInitialCategories(any(User.class));
            verify(accountService, never()).createDefaultAccount(any(User.class));
        }
    }

    @Nested
    class UpdateUserTests {

        private Long userId = 1L;
        private User existingUser;
        private UserResponseDto responseDto;

        @BeforeEach
        void setUp() {
            existingUser = new User();
            existingUser.setId(userId);
            existingUser.setName("old name");
            existingUser.setSurname("old surname");
            existingUser.setEmail("test@example.com");
            existingUser.setStatus(UserStatus.ACTIVE);

            responseDto = mock(UserResponseDto.class);

            lenient().when(userDao.findById(userId)).thenReturn(Optional.of(existingUser));
            lenient().when(userDao.update(any(User.class))).thenReturn(existingUser);
            lenient().when(mapper.toUserResponseDto(existingUser)).thenReturn(responseDto);
        }

        @Test
        void should_throw_NotFoundException_when_user_not_found() {
            //given
            when(userDao.findById(userId)).thenReturn(Optional.empty());
            UpdateUserRequestDto dto = new UpdateUserRequestDto("new name", "new surname");

            //when
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.updateUser(userId, dto));

            //then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
            verify(userDao, times(1)).findById(userId);
            verify(userDao, never()).update(any());
            verify(mapper, never()).toUserResponseDto(any());
        }

        @Test
        void should_throw_UserStatusException_when_user_is_not_active() {
            //given
            existingUser.setStatus(UserStatus.PENDING_CONFIRMATION);
            UpdateUserRequestDto dto = new UpdateUserRequestDto("new name", "new surname");

            //when
            UserStatusException exception = assertThrows(UserStatusException.class,
                    () -> userService.updateUser(userId, dto));

            //then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_ACTIVE);
            verify(userDao, times(1)).findById(userId);
            verify(userDao, never()).update(any());
            verify(mapper, never()).toUserResponseDto(any());
        }

        @Test
        void should_update_user_name_and_surname_successfully() {
            //given
            UpdateUserRequestDto dto = new UpdateUserRequestDto("new name", "new surname");

            //when
            UserResponseDto result = userService.updateUser(userId, dto);

            //then
            assertThat(result).isEqualTo(responseDto);

            verify(userDao, times(1)).findById(userId);
            verify(userDao, times(1)).update(userCaptor.capture());
            verify(mapper, times(1)).toUserResponseDto(existingUser);

            User updatedUser = userCaptor.getValue();
            assertThat(updatedUser.getName()).isEqualTo("new name");
            assertThat(updatedUser.getSurname()).isEqualTo("new surname");
        }

        @Test
        void should_not_update_fields_when_dto_fields_are_null_or_blank() {
            //given
            UpdateUserRequestDto dto = new UpdateUserRequestDto(null, "   ");

            //when
            UserResponseDto result = userService.updateUser(userId, dto);

            //then
            assertThat(result).isEqualTo(responseDto);

            verify(userDao, times(1)).update(userCaptor.capture());
            User updatedUser = userCaptor.getValue();
            assertThat(updatedUser.getName()).isEqualTo("old name");
            assertThat(updatedUser.getSurname()).isEqualTo("old surname");
        }
    }

    @Nested
    class ActivateUserTests {

        private User user;
        private final String email = "test@example.com";
        private final Long userId = 1L;

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);
            user.setEmail(email);
            user.setStatus(UserStatus.PENDING_CONFIRMATION);

            lenient().when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        }

        @Test
        void should_activate_user_successfully() {
            //given
            doNothing().when(accountService).activateAllByUser(userId);
            doNothing().when(recurringTransactionService).activateAllByUser(userId);

            //when
            userService.activateUser(email);

            //then
            verify(accountService, times(1)).activateAllByUser(userId);
            verify(recurringTransactionService, times(1)).activateAllByUser(userId);

            verify(userDao, times(1)).update(userCaptor.capture());
            User activatedUser = userCaptor.getValue();

            assertThat(activatedUser).isEqualTo(user);
            assertThat(activatedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        void should_do_nothing_when_user_is_already_active() {
            //given
            user.setStatus(UserStatus.ACTIVE);

            //when
            userService.activateUser(email);

            //then
            verify(userDao, times(1)).findByEmail(email);
            verify(accountService, never()).activateAllByUser(anyLong());
            verify(userDao, never()).update(any(User.class));
            verify(recurringTransactionService, never()).activateAllByUser(anyLong());
        }

        @Test
        void should_throw_NotFoundException_when_email_does_not_exist() {
            //given
            when(userDao.findByEmail(email)).thenReturn(Optional.empty());

            //when
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.activateUser(email));

            //then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            verify(accountService, never()).activateAllByUser(anyLong());
            verify(userDao, never()).update(any(User.class));
        }
    }


    @Nested
    class CloseUserTests {

        private User user;
        private final Long userId = 1L;

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);
            user.setEmail("test@example.com");
            user.setStatus(UserStatus.ACTIVE);

            lenient().when(userDao.findById(userId)).thenReturn(Optional.of(user));
        }

        @Test
        void should_mark_user_for_deletion_successfully() {
            //given
            doNothing().when(accountService).deactivateAllByUser(userId);
            doNothing().when(recurringTransactionService).deactivateAllByUser(userId);

            //when
            ResponseMessageDto response = userService.closeUser(userId);

            //then
            assertThat(response).isNotNull();
            assertThat(response.message()).contains("30 days");

            verify(accountService, times(1)).deactivateAllByUser(userId);
            verify(recurringTransactionService, times(1)).deactivateAllByUser(userId);

            verify(userDao, times(1)).update(userCaptor.capture());
            User updatedUser = userCaptor.getValue();

            assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.PENDING_DELETION);
            assertThat(updatedUser.getRequestCloseAt()).isNotNull();

            assertThat(updatedUser.getRequestCloseAt()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        void should_throw_NotFoundException_when_user_not_found() {
            //given
            when(userDao.findById(userId)).thenReturn(Optional.empty());

            //when
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.closeUser(userId));

            //then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
            verify(accountService, never()).deactivateAllByUser(anyLong());
            verify(userDao, never()).update(any(User.class));
        }

        @Test
        void should_throw_UserStatusException_when_user_is_not_active() {
            //given
            user.setStatus(UserStatus.PENDING_CONFIRMATION);

            //when
            UserStatusException exception = assertThrows(UserStatusException.class,
                    () -> userService.closeUser(userId));

            //then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_ACTIVE);
            verify(accountService, never()).deactivateAllByUser(anyLong());
            verify(userDao, never()).update(any(User.class));
        }
    }

    @Nested
    class ChangePasswordTests {

        private Long userId = 1L;
        private User user;
        private final String oldPasswordHashed = "hashedOldPassword";
        private final String newPasswordRaw = "newPassword123";
        private final String newPasswordHashed = "hashedNewPassword";

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);
            user.setStatus(UserStatus.ACTIVE);
            user.setPassword(oldPasswordHashed);

            lenient().when(userDao.findById(userId)).thenReturn(Optional.of(user));
            lenient().when(encoder.matches(anyString(), anyString())).thenReturn(true);
            lenient().when(encoder.encode(newPasswordRaw)).thenReturn(newPasswordHashed);
        }

        @Test
        void should_change_password_successfully() {
            //given
            ChangePasswordRequestDto dto = new ChangePasswordRequestDto("oldPassword123", newPasswordRaw, newPasswordRaw);
            when(encoder.matches(dto.oldPassword(), oldPasswordHashed)).thenReturn(true);

            //when
            userService.changePassword(userId, dto);

            //then
            verify(userDao, times(1)).update(userCaptor.capture());
            User updatedUser = userCaptor.getValue();

            assertThat(updatedUser.getPassword()).isEqualTo(newPasswordHashed);
        }

        @Test
        void should_throw_UserStatusException_when_user_not_active() {
            //given
            user.setStatus(UserStatus.PENDING_CONFIRMATION);
            ChangePasswordRequestDto dto = new ChangePasswordRequestDto("old", "new", "new");

            //when & then
            assertThrows(UserStatusException.class,
                    () -> userService.changePassword(userId, dto));

            verify(userDao, never()).update(any());
        }

        @Test
        void should_throw_ValidationException_when_old_password_is_incorrect() {
            //given
            ChangePasswordRequestDto dto = new ChangePasswordRequestDto("wrongOldPassword", newPasswordRaw, newPasswordRaw);
            when(encoder.matches(dto.oldPassword(), oldPasswordHashed)).thenReturn(false);

            //when & then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> userService.changePassword(userId, dto));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_OLD_PASSWORD);
            verify(userDao, never()).update(any());
        }

        @Test
        void should_throw_ValidationException_when_new_passwords_do_not_match() {
            //given
            ChangePasswordRequestDto dto = new ChangePasswordRequestDto("oldPassword123", newPasswordRaw, "differentPassword");

            when(encoder.matches(dto.oldPassword(), oldPasswordHashed)).thenReturn(true);

            //when & then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> userService.changePassword(userId, dto));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORDS_NOT_MATCH);
            verify(userDao, never()).update(any());
        }
    }

    @Nested
    class TfaSetupTests {

        private Long userId = 1L;
        private User user;

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);
            user.setStatus(UserStatus.ACTIVE);

            lenient().when(userDao.findById(userId)).thenReturn(Optional.of(user));
        }

        @Test
        void should_setup_tfa_successfully() {
            //given
            String mockSecret = "MOCK_SECRET_KEY";
            String mockQrCodeUri = "data:image/png;base64,mock_qr_code";

            when(tfaService.generateSecret()).thenReturn(mockSecret);
            when(tfaService.generateQrCodeImageUri(mockSecret)).thenReturn(mockQrCodeUri);

            //when
            TfaQRCode result = userService.tfaSetup(userId);

            //then
            assertThat(result).isNotNull();
            assertThat(result.secretImageUri()).isEqualTo(mockQrCodeUri);

            verify(userDao, times(1)).update(userCaptor.capture());
            User updatedUser = userCaptor.getValue();
            assertThat(updatedUser.getTempSecret()).isEqualTo(mockSecret);
        }

        @Test
        void should_throw_UserStatusException_when_user_not_active() {
            //given
            user.setStatus(UserStatus.PENDING_DELETION);

            //when & then
            assertThrows(UserStatusException.class,
                    () -> userService.tfaSetup(userId));

            verify(userDao, never()).update(any());
            verify(tfaService, never()).generateSecret();
        }
    }

    @Nested
    class VerifyTfaSetupTests {

        private Long userId = 1L;
        private User user;
        private final String tempSecret = "MOCK_TEMP_SECRET";
        private final String validCode = "123456";

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);
            user.setStatus(UserStatus.ACTIVE);
            user.setTempSecret(tempSecret);
            user.setMfaEnabled(false);

            lenient().when(userDao.findById(userId)).thenReturn(Optional.of(user));
        }

        @Test
        void should_verify_tfa_setup_successfully() {
            //given
            when(tfaService.isOptValid(tempSecret, validCode)).thenReturn(true);

            //when
            userService.verifyTfaSetup(userId, validCode);

            //then
            verify(userDao, times(1)).update(userCaptor.capture());
            User updatedUser = userCaptor.getValue();

            assertThat(updatedUser.getSecret()).isEqualTo(tempSecret);
            assertThat(updatedUser.getTempSecret()).isNull();
            assertThat(updatedUser.isMfaEnabled()).isTrue();
        }

        @Test
        void should_throw_TfaException_when_code_is_invalid() {
            //given
            String invalidCode = "654321";
            when(tfaService.isOptValid(tempSecret, invalidCode)).thenReturn(false);

            //when & then
            assertThrows(TfaException.class,
                    () -> userService.verifyTfaSetup(userId, invalidCode));

            verify(userDao, never()).update(any());
        }

        @Test
        void should_throw_UserStatusException_when_user_not_active() {
            //given
            user.setStatus(UserStatus.PENDING_CONFIRMATION);

            //when & then
            assertThrows(UserStatusException.class,
                    () -> userService.verifyTfaSetup(userId, validCode));

            verify(tfaService, never()).isOptValid(anyString(), anyString());
            verify(userDao, never()).update(any());
        }
    }

    @Nested
    class TfaDisableTests {

        private Long userId = 1L;
        private User user;
        private final String mainSecret = "MOCK_MAIN_SECRET";
        private final String validCode = "123456";

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(userId);
            user.setStatus(UserStatus.ACTIVE);
            user.setSecret(mainSecret);
            user.setMfaEnabled(true);

            lenient().when(userDao.findById(userId)).thenReturn(Optional.of(user));
        }

        @Test
        void should_disable_tfa_successfully() {
            //given
            when(tfaService.isOptValid(mainSecret, validCode)).thenReturn(true);

            //when
            userService.tfaDisable(userId, validCode);

            //then
            verify(userDao, times(1)).update(userCaptor.capture());
            User updatedUser = userCaptor.getValue();

            assertThat(updatedUser.getSecret()).isNull();
            assertThat(updatedUser.isMfaEnabled()).isFalse();
        }

        @Test
        void should_throw_TfaException_when_code_is_invalid() {
            //given
            String invalidCode = "654321";
            when(tfaService.isOptValid(mainSecret, invalidCode)).thenReturn(false);

            //when & then
            assertThrows(TfaException.class,
                    () -> userService.tfaDisable(userId, invalidCode));

            verify(userDao, never()).update(any());
        }
    }

    @Nested
    class DeleteUsersPendingDeletionTests {

        private User user1;
        private User user2;

        @BeforeEach
        void setUp() {
            user1 = new User();
            user1.setId(1L);
            user1.setStatus(UserStatus.PENDING_DELETION);
            user1.setEmail("user1@example.com");

            user2 = new User();
            user2.setId(2L);
            user2.setStatus(UserStatus.PENDING_DELETION);
            user2.setEmail("user2@example.com");
        }

        @Test
        void should_delete_all_found_users_successfully() {
            //given
            when(userDao.findUsersForDeletion(eq(UserStatus.PENDING_DELETION), any(Instant.class)))
                    .thenReturn(List.of(user1, user2));

            when(userDao.findById(1L)).thenReturn(Optional.of(user1));
            when(userDao.findById(2L)).thenReturn(Optional.of(user2));

            doNothing().when(accountService).deleteAllByUser(anyLong());
            doNothing().when(categoryService).deleteAllByUser(anyLong());
            doNothing().when(userDao).delete(any(User.class));

            //when
            userService.deleteUsersPendingDeletion();

            //then
            verify(accountService, times(1)).deleteAllByUser(1L);
            verify(accountService, times(1)).deleteAllByUser(2L);

            verify(categoryService, times(1)).deleteAllByUser(1L);
            verify(categoryService, times(1)).deleteAllByUser(2L);

            verify(userDao, times(1)).delete(user1);
            verify(userDao, times(1)).delete(user2);
        }

        @Test
        void should_do_nothing_when_no_users_found() {
            //given
            when(userDao.findUsersForDeletion(eq(UserStatus.PENDING_DELETION), any(Instant.class)))
                    .thenReturn(List.of());

            //when
            userService.deleteUsersPendingDeletion();

            //then
            verify(userDao, never()).findById(anyLong());
            verify(accountService, never()).deleteAllByUser(anyLong());
            verify(categoryService, never()).deleteAllByUser(anyLong());
            verify(userDao, never()).delete(any());
        }

        @Test
        void should_continue_loop_if_one_user_fails_validation() {
            //given
            user1.setStatus(UserStatus.ACTIVE);

            when(userDao.findUsersForDeletion(eq(UserStatus.PENDING_DELETION), any(Instant.class)))
                    .thenReturn(List.of(user1, user2));

            when(userDao.findById(1L)).thenReturn(Optional.of(user1));
            when(userDao.findById(2L)).thenReturn(Optional.of(user2));

            doNothing().when(accountService).deleteAllByUser(2L);
            doNothing().when(categoryService).deleteAllByUser(2L);
            doNothing().when(userDao).delete(user2);

            //when
            userService.deleteUsersPendingDeletion();

            //then
            verify(accountService, never()).deleteAllByUser(1L);
            verify(categoryService, never()).deleteAllByUser(1L);
            verify(userDao, never()).delete(user1);

            verify(accountService, times(1)).deleteAllByUser(2L);
            verify(categoryService, times(1)).deleteAllByUser(2L);
            verify(userDao, times(1)).delete(user2);
        }
    }
}
