package com.nexturn.internmanagement.hr.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record HrAssignRequest(@NotNull List<Long> ids) {}
