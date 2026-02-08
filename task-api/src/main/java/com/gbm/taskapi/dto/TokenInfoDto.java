package com.gbm.taskapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenInfoDto {
    private Long userId;
    private String email;
}
