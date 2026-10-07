package com.nexturn.internmanagement.report;

import com.nexturn.internmanagement.attendance.Attendance;
import com.nexturn.internmanagement.attendance.AttendanceRepository;
import com.nexturn.internmanagement.attendance.AttendanceStatus;
import com.nexturn.internmanagement.evaluation.Evaluation;
import com.nexturn.internmanagement.evaluation.EvaluationRepository;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.ppo.PpoRepository;
import com.nexturn.internmanagement.task.Task;
import com.nexturn.internmanagement.task.TaskRepository;
import com.nexturn.internmanagement.task.TaskStatus;
import com.nexturn.internmanagement.training.Training;
import com.nexturn.internmanagement.training.TrainingRepository;
import com.nexturn.internmanagement.training.TrainingStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InternRepository internRepository;
    private final TaskRepository taskRepository;
    private final AttendanceRepository attendanceRepository;
    private final TrainingRepository trainingRepository;
    private final EvaluationRepository evaluationRepository;
    private final PpoRepository ppoRepository;

    public List<InternReportDto> generate() {
        return internRepository.findAll().stream().map(this::buildReport).toList();
    }

    public List<InternReportDto> generateForMentor(Long mentorId) {
        return internRepository.findByMentorId(mentorId).stream().map(this::buildReport).toList();
    }

    public InternReportDto generateForIntern(Long internId) {
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new com.nexturn.internmanagement.common.ResourceNotFoundException(
                        "Intern not found: " + internId));
        return buildReport(intern);
    }

    private InternReportDto buildReport(Intern intern) {
        List<Task> tasks = taskRepository.findByInternId(intern.getId());
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.Completed).count();
        double taskCompletionPct = totalTasks == 0 ? 0.0 : (completedTasks * 100.0) / totalTasks;

        List<Attendance> attendance = attendanceRepository.findByInternId(intern.getId());
        long totalAttendanceDays = attendance.size();
        long presentDays = attendance.stream().filter(a -> a.getStatus() == AttendanceStatus.Present).count();
        double attendancePct = totalAttendanceDays == 0 ? 0.0 : (presentDays * 100.0) / totalAttendanceDays;

        List<Training> trainings = trainingRepository.findByInternId(intern.getId());
        long totalTrainings = trainings.size();
        long completedTrainings =
                trainings.stream().filter(t -> t.getStatus() == TrainingStatus.Completed).count();
        double trainingCompletionPct = totalTrainings == 0 ? 0.0 : (completedTrainings * 100.0) / totalTrainings;

        List<Evaluation> evaluations = evaluationRepository.findByInternId(intern.getId());
        Double averageEvaluationScore = evaluations.isEmpty()
                ? null
                : evaluations.stream().mapToDouble(Evaluation::getOverallRating).average().orElse(0.0);

        String ppoStatus = ppoRepository.findByInternId(intern.getId())
                .map(p -> p.getStatus().name().replace('_', ' '))
                .orElse(null);

        return new InternReportDto(
                intern.getId(),
                intern.getName(),
                intern.getDepartment(),
                intern.getMentor() == null ? null : intern.getMentor().getId(),
                intern.getMentor() == null ? null : intern.getMentor().getName(),
                totalTasks,
                completedTasks,
                taskCompletionPct,
                presentDays,
                totalAttendanceDays,
                attendancePct,
                totalTrainings,
                completedTrainings,
                trainingCompletionPct,
                averageEvaluationScore,
                ppoStatus);
    }
}
