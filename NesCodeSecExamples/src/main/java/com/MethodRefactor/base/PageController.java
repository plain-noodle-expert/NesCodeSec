// PageController.java
package com.example.NoteStream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    public String createFlashcardsPage() {
        return "create-flashcards";
    }

    public String studyFlashcardsPage() {
        return "study-flashcards";
    }
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