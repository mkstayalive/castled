package io.castled.forms.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormFieldsDTO {

    private Map<String, FormFieldDTO> fields;
    private CodeBlockDTO codeBlock;
    private Map<String, GroupActivatorDTO> groupActivators;
}
