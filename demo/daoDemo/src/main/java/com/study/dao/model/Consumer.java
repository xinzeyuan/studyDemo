package com.study.dao.model;

import lombok.*;

import java.io.Serializable;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Consumer implements Serializable {
    private Long id;

    private String name;

    private Integer age;

    private Integer ssoid;

    private static final long serialVersionUID = 1L;

}