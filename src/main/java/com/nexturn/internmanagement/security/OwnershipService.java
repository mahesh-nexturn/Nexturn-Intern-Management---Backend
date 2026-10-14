package com.nexturn.internmanagement.security;

import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.mentor.MentorRepository;
import com.nexturn.internmanagement.user.Role;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

    public boolean isHr(UserPrincipal principal) {
        return principal.getUser().getRole() == Role.HR;
    }

    /** True for the roles that run the programme-wide screens (ADMIN, or HR scoped to their own mentors/interns). */
    public boolean isAdminOrHr(UserPrincipal principal) {
        return isAdmin(principal) || isHr(principal);
    }

    public boolean hrOwnsMentor(UserPrincipal principal, Long mentorId) {
        return mentorId != null && mentorRepository.findById(mentorId)
                .map(m -> m.getHr() != null && Objects.equals(m.getHr().getId(), principal.getId()))
                .orElse(false);
    }

    public boolean hrOwnsIntern(UserPrincipal principal, Long internId) {
        return internId != null && internRepository.findById(internId)
                .map(i -> i.getHr() != null && Objects.equals(i.getHr().getId(), principal.getId()))
                .orElse(false);
    }

    /** Predicate over intern ids that an HR may see; ADMIN and other roles get an allow-all predicate. */
    public Predicate<Long> internScope(UserPrincipal principal) {
        if (!isHr(principal)) {
            return id -> true;
        }
        Set<Long> ids = internRepository.findByHrId(principal.getId()).stream()
                .map(i -> i.getId()).collect(Collectors.toSet());
        return ids::contains;
    }

    /** Predicate over mentor ids that an HR may see; ADMIN and other roles get an allow-all predicate. */
    public Predicate<Long> mentorScope(UserPrincipal principal) {
        if (!isHr(principal)) {
            return id -> true;
        }
        Set<Long> ids = mentorRepository.findByHrId(principal.getId()).stream()
                .map(m -> m.getId()).collect(Collectors.toSet());
        return ids::contains;
    }

    /** Resolves the {@code mentors.id} linked to the given authenticated user, or null if none. */
    public Long currentMentorId(UserPrincipal principal) {
        return mentorRepository.findByUserId(principal.getId()).map(m -> m.getId()).orElse(null);
    }

    /** Resolves the {@code interns.id} linked to the given authenticated user, or null if none. */
    public Long currentInternId(UserPrincipal principal) {
        return internRepository.findByUserId(principal.getId()).map(i -> i.getId()).orElse(null);
    }

    /**
     * Throws 403 if a MENTOR principal is trying to access a resource owned by a different mentor. HR access
     * is governed by intern ownership ({@link #assertOwnsInternResource}), not by mentor ownership.
     */
    public void assertOwnsMentorResource(UserPrincipal principal, Long resourceMentorId) {
        if (isMentor(principal) && !Objects.equals(resourceMentorId, currentMentorId(principal))) {
            throw new AccessDeniedException("You can only manage your own interns' records");
        }
    }

    /** Throws 403 if an HR principal references a mentor that is not assigned to them (no-op for other roles). */
    public void assertHrOwnsMentor(UserPrincipal principal, Long mentorId) {
        if (isHr(principal) && mentorId != null && !hrOwnsMentor(principal, mentorId)) {
            throw new AccessDeniedException("You can only use your own mentors");
        }
    }

    /**
     * Throws 403 if an INTERN principal is trying to access a resource belonging to a different intern, or an
     * HR principal is referencing an intern that is not assigned to them.
     */
    public void assertOwnsInternResource(UserPrincipal principal, Long resourceInternId) {
        if (isIntern(principal) && !Objects.equals(resourceInternId, currentInternId(principal))) {
            throw new AccessDeniedException("You can only access your own records");
        }
        if (isHr(principal) && !hrOwnsIntern(principal, resourceInternId)) {
            throw new AccessDeniedException("You can only manage records of your own interns");
        }
    }
}
