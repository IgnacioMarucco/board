package com.board.util;

import com.board.entity.enums.BoardTemplate;

import java.util.List;

/**
 * Default column names for each board template.
 */
public final class BoardTemplateDefaults {

    private BoardTemplateDefaults() {
        // Utility class
    }

    /**
     * Returns the ordered column names for the given template.
     *
     * @param template the board template
     * @return list of column names in order
     */
    public static List<String> getColumns(BoardTemplate template) {
        return switch (template) {
            case SCRUM_BASIC -> List.of("Todo", "In Progress", "Review", "Done");
            case DEVELOPMENT -> List.of("Backlog", "Design", "Development", "Testing", "Deploy", "Done");
            case SUPPORT -> List.of("New", "Triaged", "In Progress", "Waiting", "Resolved");
            case KANBAN_SIMPLE -> List.of("Todo", "Doing", "Done");
            case CONTENT_MARKETING -> List.of("Ideas", "Draft", "Review", "Approved", "Published");
        };
    }
}
