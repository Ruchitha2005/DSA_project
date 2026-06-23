package com.resume.resume_ranking_system.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.resume.resume_ranking_system.model.Candidate;
import com.resume.resume_ranking_system.service.ResumeRankingService;

@Controller
public class ResumeController {

    private final ResumeRankingService resumeRankingService;

    public ResumeController(ResumeRankingService resumeRankingService) {
        this.resumeRankingService = resumeRankingService;
    }

    @GetMapping("/app")
    public String index(Model model) {
        List<Candidate> candidates = resumeRankingService.listCandidates();
        
        // Group candidates by job description
        Map<String, List<Candidate>> groupedByJob = candidates.stream()
            .collect(Collectors.groupingBy(
                c -> c.getJobDescription() != null ? c.getJobDescription() : "Unranked",
                LinkedHashMap::new,
                Collectors.toList()
            ));
        
        model.addAttribute("candidates", candidates);
        model.addAttribute("candidatesByJob", groupedByJob);
        model.addAttribute("rankedCandidates", Collections.emptyList());
        model.addAttribute("rankedCandidatesByJob", new LinkedHashMap<String, List<Candidate>>());
        return "index";
    }

    @PostMapping("/upload")
    public String uploadCandidate(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestParam(name = "resumeText", required = false) String resumeText,
            RedirectAttributes redirectAttributes) {

        try {
            String text = "";
            if (resumeFile != null && !resumeFile.isEmpty()) {
                text = resumeRankingService.readResumeFileText(resumeFile);
            }

            if (text.isBlank() && resumeText != null) {
                text = resumeText.trim();
            }

            if (text.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please upload a resume file or enter resume text.");
                return "redirect:/";
            }

            if (name == null || name.isBlank()) {
                name = "Candidate " + (resumeRankingService.listCandidates().size() + 1);
            }

            resumeRankingService.createCandidate(name, text);
                redirectAttributes.addFlashAttribute("successMessage", "Resume uploaded successfully.");
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/app";
    }

    @PostMapping("/rank")
    public String rankCandidates(@RequestParam(name = "jobDescription", required = false) String jobDescription,
            Model model, RedirectAttributes redirectAttributes) {

        if (jobDescription == null || jobDescription.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a job description to rank candidates.");
            return "redirect:/app";
        }

        List<Candidate> rankedCandidates = resumeRankingService.rankCandidates(jobDescription);
        
        // Group ranked candidates by job description
        Map<String, List<Candidate>> rankedByJob = rankedCandidates.stream()
            .collect(Collectors.groupingBy(
                c -> c.getJobDescription() != null ? c.getJobDescription() : "Unranked",
                LinkedHashMap::new,
                Collectors.toList()
            ));
        
        List<Candidate> allCandidates = resumeRankingService.listCandidates();
        
        // Group all candidates by job description
        Map<String, List<Candidate>> candidatesByJob = allCandidates.stream()
            .collect(Collectors.groupingBy(
                c -> c.getJobDescription() != null ? c.getJobDescription() : "Unranked",
                LinkedHashMap::new,
                Collectors.toList()
            ));
        
        model.addAttribute("jobDescription", jobDescription);
        model.addAttribute("rankedCandidates", rankedCandidates);
        model.addAttribute("rankedCandidatesByJob", rankedByJob);
        model.addAttribute("candidates", allCandidates);
        model.addAttribute("candidatesByJob", candidatesByJob);
        return "index";
    }
}
