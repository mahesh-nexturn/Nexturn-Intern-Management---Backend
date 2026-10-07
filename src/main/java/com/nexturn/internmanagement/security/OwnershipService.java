package com.nexturn.internmanagement.security;

import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.MentorRepository;
import com.nexturn.internmanagement.user.Role;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Central place for ownership-scoped authorization: MENTOR users may only
 * manage records belonging to their own interns, and INTERN users may only
 * read their own records. ADMIN always bypasses these checks.
 */
@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final MentorRepository mentorRepository;
    private final InternRepository internRepository;

    public boolean isAdmin(UserPrincipal principal) {
        return principal.getUser().getRole() == Role.ADMIN;
    }

    public boolean isMentor(UserPrincipal principal) {
        return principal.getUser().getRole() == Role.MENTOR;
    }

    public boolean isIntern(UserPrincipal principal) {
        return principal.getUser().getRole() == Role.INTERN;
    }

    /** Resolves the {@code mentors.id} linked to the given authenticated user, or null if none. */
    public Long currentMentorId(UserPrincipal principal) {
        return mentorRepository.findByUserId(principal.getId()).map(m -> m.getId()).orElse(null);
    }

    /** Resolves the {@code interns.id} linked to the given authenticated user, or null if none. */
    public Long currentInternId(UserPrincipal principal) {
        return internRepository.findByUserId(principal.getId()).map(i -> i.getId()).orElse(null);
    }

    /** Throws 403 if a MENTOR principal is trying to access a resource owned by a different mentor. */
    public void assertOwnsMentorResource(UserPrincipal principal, Long resourceMentorId) {
        if (isMentor(principal) && !Objects.equals(resourceMentorId, currentMentorId(principal))) {
            throw new AccessDeniedException("You can only manage your own interns' records");
        }
    }

    /** Throws 403 if an INTERN principal is trying to access a resource belonging to a different intern. */
    public void assertOwnsInternResource(UserPrincipal principal, Long resourceInternId) {
        if (isIntern(principal) && !Objects.equals(resourceInternId, currentInternId(principal))) {
            throw new AccessDeniedException("You can only access your own records");
        }
    }
}
