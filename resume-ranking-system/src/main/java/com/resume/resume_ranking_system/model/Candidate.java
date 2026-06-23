package com.resume.resume_ranking_system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "candidate")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double score;

    @Column(columnDefinition = "TEXT")
    private String resumeText;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    public Candidate() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    @Transient
    public int getScorePercent() {
        return (int) Math.round(this.score);
    }

    @Transient
    public String getScoreLabel() {
        double s = this.score;
        if (s >= 75.0) {
            return "High";
        } else if (s >= 40.0) {
            return "Medium";
        } else {
            return "Low";
        }
    }

    @Transient
    public String getPreview() {
        if (this.resumeText == null) return "";
        return this.resumeText.length() > 120 ? this.resumeText.substring(0, 120) + "..." : this.resumeText;
    }
}