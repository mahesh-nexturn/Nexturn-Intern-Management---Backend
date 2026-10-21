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

    @Column(name = "requirement_understanding")
    private Short requirementUnderstanding;

    @Column(name = "technical_knowledge")
    private Short technicalKnowledge;

    @Column(name = "work_quality")
    private Short workQuality;

    @Column(name = "task_completion")
    private Short taskCompletion;

    @Column(name = "task_demonstration")
    private Short taskDemonstration;

    @Column(name = "overall_rating")
    private short overallRating;

    @Column(name = "task_id")
    private Long taskId;

    @Column(columnDefinition = "TEXT")
    private String feedback;
}
