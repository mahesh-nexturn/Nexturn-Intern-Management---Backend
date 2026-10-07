package com.nexturn.internmanagement.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import com.nexturn.internmanagement.mentor.MentorStatus;
import com.nexturn.internmanagement.user.Role;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shared base for full-stack integration tests: boots the real Spring context against the
 * dedicated {@code intern_management_test} PostgreSQL database (see application-test.yml),
 * wraps each test in a transaction that is rolled back afterwards, and exposes fixture helpers
 * for creating users/mentors/interns and obtaining JWT access tokens via the real /auth/login
 * endpoint (exercising the full authentication + authorization stack, not mocks).
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MentorRepository mentorRepository;

    @Autowired
    protected InternRepository internRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected static final String RAW_PASSWORD = "Test@1234";

    protected User createUser(String name, String email, Role role) {
        User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(RAW_PASSWORD))
                .role(role)
                .build();
        return userRepository.save(user);
    }

    protected Mentor createMentor(String name, String email, User linkedUser) {
        Mentor mentor = new Mentor();
        mentor.setName(name);
        mentor.setEmail(email);
        mentor.setDepartment("Engineering");
        mentor.setDesignation("SDE");
        mentor.setStatus(MentorStatus.Active);
        mentor.setUser(linkedUser);
        return mentorRepository.save(mentor);
    }

    protected Intern createIntern(String name, String email, Mentor mentor, User linkedUser) {
        Intern intern = new Intern();
        intern.setName(name);
        intern.setEmail(email);
        intern.setDepartment("Engineering");
        intern.setStatus("Active");
        intern.setMentor(mentor);
        intern.setUser(linkedUser);
        return internRepository.save(intern);
    }

    /** Logs in via the real /api/auth/login endpoint and returns the JWT access token. */
    protected String loginAndGetToken(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginPayload(email, password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.path("data").path("accessToken").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    private record LoginPayload(String email, String password) {
    }
}
