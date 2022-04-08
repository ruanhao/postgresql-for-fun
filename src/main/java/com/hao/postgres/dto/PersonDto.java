package com.hao.postgres.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDto {

    @JsonProperty("nick-name")
    String nickName;

    @JsonProperty("first-name")
    String firstName;

    Info info;

    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static public class Info {
        String gender;
    }
}
