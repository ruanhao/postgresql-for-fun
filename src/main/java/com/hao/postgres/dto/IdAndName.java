package com.hao.postgres.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@AllArgsConstructor(onConstructor_ = { @JsonCreator}) // don't want setters and make sure json deserialization is ok
                                                      // https://cloud.tencent.com/developer/article/1704523
public class IdAndName {

    long id;

    String name;

}
