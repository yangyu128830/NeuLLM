package com.neusoft.edu.neullmdev.dto.classroom.response;

import java.util.List;

public record StudentScopeOptionsResponse(
        List<String> majors,
        List<String> grades,
        List<String> classNames,
        List<StudentRosterEntry> roster
) {
}
