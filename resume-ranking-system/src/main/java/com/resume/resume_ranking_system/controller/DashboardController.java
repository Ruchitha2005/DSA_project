package com.resume.resume_ranking_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.resume.resume_ranking_system.service.ResumeRankingService;

@Controller
public class DashboardController {

    private final ResumeRankingService resumeRankingService;

    public DashboardController(ResumeRankingService resumeRankingService) {
        this.resumeRankingService = resumeRankingService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        int total = resumeRankingService.listCandidates().size();
        model.addAttribute("totalCandidates", total);
        return "dashboard";
    }
}
