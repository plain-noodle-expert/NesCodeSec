def build_input(file_path: str, edited_line: int) -> str:
    try:
        with open(file_path, "r") as f:
            original_code = f.read()
    
        # Split the code into lines
        lines = original_code.splitlines()
        
        # Add cursor marker at the end of the edited line
        if 0 <= edited_line < len(lines):
            lines[edited_line] = lines[edited_line] + "<|user_cursor_is_here|>"
        
        # Rejoin the lines with the cursor marker
        modified_code = '\n'.join(lines)
        
        return f"```{file_path}\n\n<|start_of_file|>\n<|editable_region_start|>\n{modified_code}\n<|editable_region_end|>\n```"
    except Exception as e:
        print(e)
        raise

if __name__ == "__main__":
    from prepare_input import replace_xml_factory
    
    example_file = "InputFactoryXxeExample_DocumentBuilderFactory.java"

    edited_line =  15  # Replace with the actual line number that was edited (0-based index)
    input_text = build_input(example_file, edited_line)
    print(input_text)