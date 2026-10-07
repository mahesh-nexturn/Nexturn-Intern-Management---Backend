package com.nexturn.internmanagement.evaluation;

import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.mentor.Mentor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "intern_id", nullable = false)
    private Intern intern;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    @Column(name = "technical_rating")
    private short technicalRating;

    @Column(name = "communication_rating")
    private short communicationRating;

    @Column(name = "problem_solving_rating")
    private short problemSolvingRating;

    @Column(name = "overall_rating")
    private short overallRating;

    @Column(columnDefinition = "TEXT")
    private String feedback;
}
