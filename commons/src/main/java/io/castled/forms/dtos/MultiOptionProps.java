package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class MultiOptionProps extends DisplayFieldProps {

    private List<FormFieldOption> options;

    private String optionsRef;

    public MultiOptionProps(FormFieldType type, List<FormFieldOption> options, String title, String description) {
        super(type, title, description);
        this.options = options;
    }

    public MultiOptionProps(FormFieldType type, String optionsRef, String title, String description) {
        super(type, title, description);
        this.optionsRef = optionsRef;
    }
}
