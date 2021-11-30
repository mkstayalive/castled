import { CodeSnippet } from "./CodeSnippet";

export interface CodeBlock{

    title: string,
    dependencies: string[],
    snippets: CodeSnippet[];
    
}