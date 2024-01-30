package com.task06.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewAudit {
    private String id;
    private String itemKey;
    private String modificationTime;
    private Configuration newValue;
}
