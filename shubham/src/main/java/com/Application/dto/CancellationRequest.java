package com.Application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancellationRequest{
    private Long userId;
    private Long appointmentId;
}
