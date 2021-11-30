package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormFieldOption {

    private Object value;
    private String title;
    private String description;

    public FormFieldOption(Object value) {
        this.value = value;
    }

    public FormFieldOption(Object value, String title) {
        this.value = value;
        this.title = title;
    }
}
