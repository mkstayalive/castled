package io.castled.forms.dtos;

import io.castled.forms.FormFieldType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DropDownProps extends MultiOptionProps {

    public DropDownProps(List<FormFieldOption> selectOptions, String title, String description) {
        super(FormFieldType.DROP_DOWN, selectOptions, title, description);
    }

    public DropDownProps(String optionsReference, String title, String description) {
        super(FormFieldType.DROP_DOWN, optionsReference, title, description);
    }

}
