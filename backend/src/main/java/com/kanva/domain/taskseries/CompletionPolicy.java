package com.kanva.domain.taskseries;

/**
 * 반복 Task 완료 시 동작 정책
 *
 * - PER_OCCURRENCE: 인스턴스 완료는 해당 날짜만 완료, 시리즈는 계속 생성 (습관/알고리즘)
 * - COMPLETE_STOPS_SERIES: 인스턴스 완료 시 시리즈 stopDate 설정, 이후 생성 중단 (프로젝트/마일스톤)
 */
public enum CompletionPolicy {

    PER_OCCURRENCE("인스턴스별 완료"),
    COMPLETE_STOPS_SERIES("완료 시 시리즈 중단");

    private final String description;

    CompletionPolicy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
