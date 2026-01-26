```<|start_of_file|>
<|editable_region_start|>
// PageController.java
package com.example.NoteStream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/flashcards/create")
    public String createFlashcardsPage() {
        return "create-flashcards";
    }

    @GetMapping("/flashcards/study")
    public String studyFlashcardsPage() {
        return "study-flashcards";
    }

    <|user_cursor_is_here|>
    public String createQuizPage() {
        return "create-quiz";
    }

    public String takeQuizPage() {
        return "take-quiz";
    }

    public boolean isUserAuthenticated() {
        // Placeholder for actual authentication check logic
        return true; // Assume user is authenticated for this example
    }
}
<|editable_region_end|>
```
