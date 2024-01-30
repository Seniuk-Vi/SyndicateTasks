package com.task06.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAudit {
    private String id;
    private String itemKey;
    private String modificationTime;
    private String updatedAttribute;
    private String oldValue;
    private String newValue;
}
