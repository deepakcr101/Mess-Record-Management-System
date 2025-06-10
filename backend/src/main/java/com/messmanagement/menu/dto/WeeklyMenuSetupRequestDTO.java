package com.messmanagement.menu.dto;

import lombok.Data;
// import jakarta.validation.Valid;
// import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class WeeklyMenuSetupRequestDTO {
    // @NotEmpty
    // @Valid
    private List<WeeklyMenuItemEntryDTO> menuEntries;

    // Optionally, an overall effective date for all entries in this request if not specified individually
    // private LocalDate commonEffectiveDateStart; 
    // For simplicity, we'll rely on individual effectiveDateStart in WeeklyMenuItemEntryDTO for now.
}