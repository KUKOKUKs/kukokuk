package com.kukokuk.domain.twenty.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoom {
    private int groupNo;
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    @NotBlank(message = "정답을 입력해주세요.")
    private String correct;
}
