package com.gate.model;

public class Question {

    private int id;
    private String subject;
    private String topic;
    private String question;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    private int answer;

    private String difficulty;
    private String explanation;

    public Question(int id,
                    String subject,
                    String topic,
                    String question,
                    String option1,
                    String option2,
                    String option3,
                    String option4,
                    int answer,
                    String difficulty,
                    String explanation) {

        this.id = id;
        this.subject = subject;
        this.topic = topic;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answer = answer;
        this.difficulty = difficulty;
        this.explanation = explanation;
    }

    public int getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getTopic() {
        return topic;
    }

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public int getAnswer() {
        return answer;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getExplanation() {
        return explanation;
    }
}