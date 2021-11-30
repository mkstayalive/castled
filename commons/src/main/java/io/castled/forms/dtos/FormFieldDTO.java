package io.castled.forms.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FormFieldDTO {
    private String group;
    private FormFieldProps fieldProps;
    private FieldValidations validations;
}
