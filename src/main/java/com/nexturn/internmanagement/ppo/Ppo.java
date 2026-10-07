package com.nexturn.internmanagement.ppo;

import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.mentor.Mentor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "ppo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ppo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "intern_id", nullable = false)
    private Intern intern;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;

    @Column(name = "attendance_pct")
    private double attendancePct;

    @Column(name = "training_completion_pct")
    private double trainingCompletionPct;

    @Column(name = "technical_score")
    private double technicalScore;

    @Column(name = "communication_score")
    private double communicationScore;

    @Column(name = "overall_score")
    private double overallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "mentor_recommendation", length = 5)
    private RecommendationFlag mentorRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "hr_recommendation", length = 5)
    private RecommendationFlag hrRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PpoStatus status;
}
