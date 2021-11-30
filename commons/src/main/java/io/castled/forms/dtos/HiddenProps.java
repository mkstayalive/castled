package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HiddenProps extends FormFieldProps {

    private final String optionsRef;

    public HiddenProps(String optionsRef) {
        super(FormFieldType.HIDDEN);
        this.optionsRef = optionsRef;
    }

}
