package io.castled.forms.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeBlockDTO {
    private String title;
    private List<String> dependencies;
    private List<CodeSnippetDTO> snippets;
}
