package com.resume.resume_ranking_system.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.resume.resume_ranking_system.model.Candidate;
import com.resume.resume_ranking_system.repository.CandidateRepository;

@Service
@Transactional
public class ResumeRankingService {

    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");
    private final CandidateRepository candidateRepository;

    public ResumeRankingService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    public List<Candidate> listCandidates() {
        return candidateRepository.findAll();
    }

    public Candidate createCandidate(String name, String resumeText) {
        if (resumeText == null) {
            resumeText = "";
        }

        Candidate candidate = new Candidate();
        candidate.setName(name == null || name.isBlank() ? "Anonymous Candidate" : name.trim());
        candidate.setResumeText(resumeText.trim());
        candidate.setScore(0.0);

        return candidateRepository.save(candidate);
    }

    public String readResumeFileText(MultipartFile resumeFile) throws IOException {
        if (resumeFile == null || resumeFile.isEmpty()) {
            return "";
        }

        String fileName = resumeFile.getOriginalFilename();
        if (fileName != null && !fileName.isBlank()) {
            String lowercase = fileName.toLowerCase(Locale.ROOT);
            if (!(lowercase.endsWith(".txt") || lowercase.endsWith(".md"))) {
                throw new IllegalArgumentException("Unsupported file type. Please upload a .txt or .md file.");
            }
        }

        return new String(resumeFile.getBytes(), StandardCharsets.UTF_8).trim();
    }

    public List<Candidate> rankCandidates(String jobDescription) {
        Map<String, Integer> jobTerms = tokenize(jobDescription);
        if (jobTerms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Candidate> candidates = listCandidates();
        
        // Only rank candidates that don't have a job description yet (unranked)
        List<Candidate> unrankedCandidates = candidates.stream()
            .filter(c -> c.getJobDescription() == null || c.getJobDescription().isBlank())
            .collect(Collectors.toList());
        
        for (Candidate candidate : unrankedCandidates) {
            double score = computeScore(jobTerms, tokenize(candidate.getResumeText()));
            candidate.setScore(score);
            candidate.setJobDescription(jobDescription);
        }

        unrankedCandidates.sort(Comparator.comparingDouble(Candidate::getScore).reversed());
        candidateRepository.saveAll(unrankedCandidates);
        return unrankedCandidates;
    }

    private Map<String, Integer> tokenize(String text) {
        if (text == null) {
            text = "";
        }

        return Arrays.stream(WORD_PATTERN.split(text.toLowerCase(Locale.ROOT)))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .filter(token -> token.length() > 1)
                .collect(Collectors.toMap(token -> token, token -> 1, Integer::sum));
    }

    private double computeScore(Map<String, Integer> jobTerms, Map<String, Integer> resumeTerms) {
        if (jobTerms.isEmpty()) {
            return 0.0;
        }

        double matchedCount = 0.0;
        for (Map.Entry<String, Integer> entry : jobTerms.entrySet()) {
            int resumeFrequency = resumeTerms.getOrDefault(entry.getKey(), 0);
            if (resumeFrequency > 0) {
                matchedCount += 1.0 + Math.min(2, resumeFrequency - 1);
            }
        }

        double normalized = (matchedCount / jobTerms.size()) * 100.0;
        return Math.min(100.0, Math.max(0.0, normalized));
    }
}
