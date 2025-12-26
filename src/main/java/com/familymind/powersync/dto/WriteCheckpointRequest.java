package com.familymind.powersync.dto;

import java.util.List;
import java.util.Map;

public record WriteCheckpointRequest(
        List<WriteOperation> operations
) {
    public record WriteOperation(
            String op,      // "PUT", "PATCH", or "DELETE"
            String table,
            Map<String, Object> data
    ) {}
}
