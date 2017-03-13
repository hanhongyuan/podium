/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package org.bbmri.podium.web.rest;

import org.bbmri.podium.PodiumUaaApp;
import org.bbmri.podium.domain.Authority;
import org.bbmri.podium.domain.Role;
import org.bbmri.podium.domain.User;
import org.bbmri.podium.repository.AuthorityRepository;
import org.bbmri.podium.common.security.AuthorityConstants;
import org.bbmri.podium.service.MailService;
import org.bbmri.podium.service.UserService;
import org.bbmri.podium.service.representation.UserRepresentation;
import org.bbmri.podium.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
public class AccountResourceIntTest {

    private static final String VALID_PASSWORD = "johndoe2!";

    @Inject
    private AuthorityRepository authorityRepository;

    @Inject
    private UserService userService;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    private MockMvc restUserMockMvc;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendVerificationEmail((User) anyObject());

        AccountResource accountResource = new AccountResource();
        ReflectionTestUtils.setField(accountResource, "userService", userService);
        ReflectionTestUtils.setField(accountResource, "mailService", mockMailService);

        AccountResource accountUserMockResource = new AccountResource();
        ReflectionTestUtils.setField(accountUserMockResource, "userService", mockUserService);
        ReflectionTestUtils.setField(accountUserMockResource, "mailService", mockMailService);

        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource).build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource).build();
    }

    @Test
    public void testNonAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void testAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
                .with(request -> {
                    request.setRemoteUser("test");
                    return request;
                })
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("test"));
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        Set<Role> roles = new HashSet<>();
        Authority authority = new Authority(AuthorityConstants.PODIUM_ADMIN);
        Role role = new Role(authority);
        roles.add(role);

        User user = new User();
        user.setLogin("test");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setEmail("john.doe@bbmri-podium.com");
        user.setRoles(roles);
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.login").value("test"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("doe"))
                .andExpect(jsonPath("$.email").value("john.doe@bbmri-podium.com"))
                .andExpect(jsonPath("$.authorities").value(AuthorityConstants.PODIUM_ADMIN));
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(null);

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setId(null);
        validUser.setLogin("joe");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Joe");
        validUser.setLastName("Shmoe");
        validUser.setEmail("joe@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> user = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(user.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setId(null);
        invalidUser.setLogin("funky-log!n"); // invalid
        invalidUser.setPassword(VALID_PASSWORD);
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setLangKey("en");
        invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userService.getUserWithAuthoritiesByEmail("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidEmail() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setId(null);
        invalidUser.setLogin("bob");
        invalidUser.setPassword(VALID_PASSWORD);
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("invalid"); // invalid
        invalidUser.setLangKey("en");
        invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userService.getUserWithAuthoritiesByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidPassword() throws Exception {
        StringBuilder tooLongPassword = new StringBuilder();
        for (int i=0; i < 100; i++) {
            tooLongPassword.append("Abcdef12345%^&*");
        }
        String[] invalidPasswords = {
            null, // empty password
            "", // empty password
            "1234567", // password with less than 8 characters
            "12345678", // password with only numbers
            "abcde123", // password without special characters
            "abc&%$;.Y", // password without numbers
            "123456^&*(", // password without alphabetical symbols
            tooLongPassword.toString() // password larger than 1000 characters
        };
        for(String password: invalidPasswords) {
            ManagedUserVM invalidUser = new ManagedUserVM();
            invalidUser.setId(null);
            invalidUser.setLogin("bob");
            invalidUser.setPassword(password);
            invalidUser.setFirstName("Bob");
            invalidUser.setLastName("Green");
            invalidUser.setEmail("bob@example.com");
            invalidUser.setLangKey("en");
            invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

            restUserMockMvc.perform(
                post("/api/register")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
                .andExpect(status().isBadRequest());

            Optional<User> user = userService.getUserWithAuthoritiesByLogin("bob");
            assertThat(user.isPresent()).isFalse();
        }
    }

    private ManagedUserVM duplicateManagedUserVM(ManagedUserVM original) {
        ManagedUserVM duplicate = new ManagedUserVM();
        duplicate.setId(original.getId());
        duplicate.setLogin(original.getLogin());
        duplicate.setPassword(original.getPassword());
        duplicate.setFirstName(original.getFirstName());
        duplicate.setLastName(original.getLastName());
        duplicate.setEmail(original.getEmail());
        duplicate.setLangKey(original.getLangKey());
        duplicate.setAuthorities(original.getAuthorities());
        return duplicate;
    }

    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // Good
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setId(null);
        validUser.setLogin("alice");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Alice");
        validUser.setLastName("Something");
        validUser.setEmail("alice@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        // Duplicate login, different e-mail
        ManagedUserVM duplicatedUser = duplicateManagedUserVM(validUser);
        duplicatedUser.setEmail("alicejr@example.com");

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        // Duplicate login
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().is4xxClientError());

        Optional<User> userDup = userService.getUserWithAuthoritiesByEmail("alicejr@example.com");
        assertThat(userDup.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateEmail() throws Exception {
        // Good
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setId(null);
        validUser.setLogin("john");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("John");
        validUser.setLastName("Doe");
        validUser.setEmail("john@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        // Duplicate e-mail, different login
        ManagedUserVM duplicatedUser = duplicateManagedUserVM(validUser);
        duplicatedUser.setLogin("johnjr");

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        // Duplicate e-mail
        // Status CREATED is returned, but the account is not created. A notification
        // email is sent instead.
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().isCreated());

        verify(mockMailService).sendAccountAlreadyExists((User)anyObject());

        Optional<User> userDup = userService.getUserWithAuthoritiesByLogin("johnjr");
        assertThat(userDup.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setId(null);
        validUser.setLogin("badguy");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.PODIUM_ADMIN)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<User> userDup = userService.getUserWithAuthoritiesByLogin("badguy");
        assertThat(userDup.isPresent()).isTrue();
        assertThat(userDup.get().getAuthorityNames()).hasSize(1)
            .containsExactly(authorityRepository.findOne(AuthorityConstants.RESEARCHER).getName());
    }

    @Test
    @Transactional
    public void testVerifyUserEmail() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setId(null);
        validUser.setLogin("badguy");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.PODIUM_ADMIN)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        userService.getUserWithAuthoritiesByLogin("badguy")
            .map(user -> {
                assertThat(user.getActivationKey() != null);

                try {
                    restMvc.perform(get("/api/verify")
                        .param("key", user.getActivationKey()))
                        .andExpect(status().isOk());
                } catch (Exception ex) { }

                return user;
            });

    }

    @Test
    @Transactional
    public void testVerifyUserEmailInvalid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setId(null);
        validUser.setLogin("badguy");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.PODIUM_ADMIN)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        userService.getUserWithAuthoritiesByLogin("badguy")
            .map(user -> {
                assertThat(user.getActivationKey() != null);

                try {
                    Thread.sleep(4400);

                    MvcResult result = restMvc.perform(get("/api/verify")
                        .param("key", user.getActivationKey()))
                        .andExpect(status().is5xxServerError())
                        .andReturn();

                    assertThat(result.getResponse().getContentAsString()).isEqualTo("renew");
                } catch (Exception ex) {}

                return user;
            });

    }

    @Test
    @Transactional
    public void testSaveInvalidLogin() throws Exception {
        UserRepresentation invalidUser = new UserRepresentation();
        invalidUser.setLogin("funky-log!n");
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setLangKey("en");
        invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restUserMockMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userService.getUserWithAuthoritiesByEmail("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }
}