package com.nexturn.internmanagement.ppo;

public record PpoResponseDto(
        Long id,
        Long internId,
        String internName,
        Long mentorId,
        String mentorName,
        double attendancePct,
        double trainingCompletionPct,
        double technicalScore,
        double communicationScore,
        double overallScore,
        String mentorRecommendation,
        String hrRecommendation,
        String status) {

    public static PpoResponseDto from(Ppo ppo) {
        return new PpoResponseDto(
                ppo.getId(),
                ppo.getIntern().getId(),
                ppo.getIntern().getName(),
                ppo.getMentor() == null ? null : ppo.getMentor().getId(),
                ppo.getMentor() == null ? null : ppo.getMentor().getName(),
                ppo.getAttendancePct(),
                ppo.getTrainingCompletionPct(),
                ppo.getTechnicalScore(),
                ppo.getCommunicationScore(),
                ppo.getOverallScore(),
                ppo.getMentorRecommendation() == null ? null : ppo.getMentorRecommendation().name(),
                ppo.getHrRecommendation() == null ? null : ppo.getHrRecommendation().name(),
                ppo.getStatus().name().replace('_', ' '));
    }
}
