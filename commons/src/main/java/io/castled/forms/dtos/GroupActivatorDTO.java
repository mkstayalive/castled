package io.castled.forms.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupActivatorDTO {
    private String condition;
    private List<String> dependencies;

}
