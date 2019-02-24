package com.example.sardor.wifiscanner;

public class StudentInfo {
    private String studentName;
    private String studentId;

    public StudentInfo() {
    }

    public StudentInfo(String studentName, String studentId) {
        this.studentName = studentName;
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
