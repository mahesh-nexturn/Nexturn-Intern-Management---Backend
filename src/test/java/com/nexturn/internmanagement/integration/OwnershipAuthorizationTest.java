package com.nexturn.internmanagement.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.support.BaseIntegrationTest;
import com.nexturn.internmanagement.user.Role;
import com.nexturn.internmanagement.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Verifies the ownership-scoped authorization rules added on top of role-based security:
 * MENTOR users may only see/manage their own interns' records, INTERN users may only see
 * their own records, and ADMIN is unrestricted. Exercises the real HTTP + security filter
 * chain end-to-end (JWT auth, {@code @PreAuthorize}, and manual {@code OwnershipService}
 * checks) rather than unit-testing the service in isolation.
 */
class OwnershipAuthorizationTest extends BaseIntegrationTest {

    private String mentorAToken;
    private String mentorBToken;
    private String internAToken;
    private String internBToken;
    private String adminToken;

    private Intern internA;
    private Intern internB;

    @BeforeEach
    void setUp() throws Exception {
        User adminUser = createUser("Admin", "admin-it@test.com", Role.ADMIN);
        User mentorAUser = createUser("Mentor A", "mentora-it@test.com", Role.MENTOR);
        User mentorBUser = createUser("Mentor B", "mentorb-it@test.com", Role.MENTOR);
        User internAUser = createUser("Intern A", "interna-it@test.com", Role.INTERN);
        User internBUser = createUser("Intern B", "internb-it@test.com", Role.INTERN);

        Mentor mentorA = createMentor("Mentor A", "mentora-it@test.com", mentorAUser);
        Mentor mentorB = createMentor("Mentor B", "mentorb-it@test.com", mentorBUser);
        internA = createIntern("Intern A", "interna-it@test.com", mentorA, internAUser);
        internB = createIntern("Intern B", "internb-it@test.com", mentorB, internBUser);

        adminToken = loginAndGetToken("admin-it@test.com", RAW_PASSWORD);
        mentorAToken = loginAndGetToken("mentora-it@test.com", RAW_PASSWORD);
        mentorBToken = loginAndGetToken("mentorb-it@test.com", RAW_PASSWORD);
        internAToken = loginAndGetToken("interna-it@test.com", RAW_PASSWORD);
        internBToken = loginAndGetToken("internb-it@test.com", RAW_PASSWORD);
    }

    @Test
    void mentorSeesOnlyOwnInternsInList() throws Exception {
        mockMvc.perform(get("/api/interns").header(HttpHeaders.AUTHORIZATION, bearer(mentorAToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(internA.getId()));
    }

    @Test
    void mentorCannotReadAnotherMentorsIntern() throws Exception {
        mockMvc.perform(get("/api/interns/" + internB.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(mentorAToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void internSeesOnlyOwnRecordInList() throws Exception {
        mockMvc.perform(get("/api/interns").header(HttpHeaders.AUTHORIZATION, bearer(internAToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(internA.getId()));
    }

    @Test
    void internCannotReadAnotherInternsRecord() throws Exception {
        mockMvc.perform(get("/api/interns/" + internB.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(internAToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void mentorCannotCreateTaskForAnotherMentorsIntern() throws Exception {
        String body = """
                {"internId":%d,"mentorId":%d,"title":"X","description":"d",
                 "status":"Pending","priority":"Medium","dueDate":"2026-12-31"}
                """.formatted(internB.getId(), internB.getMentor().getId());
        mockMvc.perform(post("/api/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(mentorAToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void internSeesOnlyOwnReportAndCannotExport() throws Exception {
        mockMvc.perform(get("/api/reports").header(HttpHeaders.AUTHORIZATION, bearer(internAToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].internId").value(internA.getId()));

        mockMvc.perform(get("/api/reports/export").header(HttpHeaders.AUTHORIZATION, bearer(internAToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSeesAllInternsAndCanExportReports() throws Exception {
        mockMvc.perform(get("/api/interns").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/api/reports/export").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/interns"))
                .andExpect(status().isForbidden());
    }

    @Test
    void mentorBCannotSeeMentorAsIntern() throws Exception {
        mockMvc.perform(get("/api/interns").header(HttpHeaders.AUTHORIZATION, bearer(mentorBToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(internB.getId()));
    }
}
