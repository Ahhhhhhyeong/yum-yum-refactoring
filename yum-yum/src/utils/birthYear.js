// 연도 확인용 함수
export const MIN_BIRTH_YEAR = 1900;

// 현재 연도
export function getCurrentYear() {
    return new Date().getFullYear();
}

// validation max 연도
export function getMaxYear() {
    return new Date().getFullYear() - 15;
}

// 연도를 기준으로 나이 계산
export function setYeartoAge(birthYear) {
    // 한국 나이로 계산
    return new Date().getFullYear() - birthYear;
}