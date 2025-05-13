-- 1. 회원 관련 테이블

-- 1.1. 회원 인증 테이블 (Member)
CREATE TABLE IF NOT EXISTS member (
                                      member_id BIGINT NOT NULL AUTO_INCREMENT,
                                      email VARCHAR(255) DEFAULT NULL,
    password_hash VARCHAR(255) DEFAULT NULL,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id),
    UNIQUE KEY uq_email (email)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1.2. 회원 프로필 테이블 (MemberProfile)
CREATE TABLE IF NOT EXISTS member_profile (
                                              profile_id BIGINT NOT NULL AUTO_INCREMENT,
                                              member_id BIGINT NOT NULL,
                                              full_name VARCHAR(255) NOT NULL,
    default_image VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (profile_id),
    CONSTRAINT fk_memberprofile_member FOREIGN KEY (member_id)
    REFERENCES member(member_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1.3. 소셜 로그인 테이블 (SocialLogin)
CREATE TABLE IF NOT EXISTS social_login (
                                            social_id BIGINT NOT NULL AUTO_INCREMENT,
                                            member_id BIGINT NOT NULL,
                                            provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    access_token VARCHAR(512) NOT NULL,
    refresh_token VARCHAR(512) DEFAULT NULL,
    token_expires_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (social_id),
    CONSTRAINT fk_sociallogin_member FOREIGN KEY (member_id)
    REFERENCES member(member_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uq_provider_user (provider, provider_user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1.4. 회원 주소 테이블 (MemberAddress)
CREATE TABLE IF NOT EXISTS member_address (
                                              address_id BIGINT NOT NULL AUTO_INCREMENT,
                                              member_id BIGINT NOT NULL,
                                              address VARCHAR(255) NOT NULL,         -- 기본 주소
    road_address VARCHAR(255) NOT NULL,    -- 도로명 주소
    detail_address VARCHAR(255),           -- 상세 주소
    alias VARCHAR(255),                    -- 주소 별칭
    latitude DECIMAL(10,7) DEFAULT NULL,   -- 위도
    longitude DECIMAL(10,7) DEFAULT NULL,  -- 경도
    status VARCHAR(255) NOT NULL,          -- 주소 상태 (HOME, COMPANY, ETC, N:삭제)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (address_id),
    CONSTRAINT fk_memberaddress_member FOREIGN KEY (member_id)
    REFERENCES member(member_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 2. 예산 관리 테이블

-- 2.1. 월별 예산 테이블 (MonthlyBudget)
CREATE TABLE IF NOT EXISTS monthly_budget (
                                              budget_id BIGINT NOT NULL AUTO_INCREMENT,
                                              member_id BIGINT NOT NULL,
                                              year_month CHAR(7) NOT NULL,           -- "YYYY-MM" 형식 (예: "2025-04")
    monthly_limit DECIMAL(10,2) NOT NULL,  -- 한 달 목표 예산 (예: 500000.00)
    spent_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,  -- 지금까지 소비한 금액
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (budget_id),
    UNIQUE KEY uq_member_yearmonth (member_id, year_month),
    CONSTRAINT fk_monthlybudget_member FOREIGN KEY (member_id)
    REFERENCES member(member_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.2. 일별 예산 테이블 (DailyBudget)
CREATE TABLE IF NOT EXISTS daily_budget (
                                            daily_budget_id BIGINT NOT NULL AUTO_INCREMENT,
                                            member_id BIGINT NOT NULL,
                                            budget_date DATE NOT NULL,
                                            daily_limit DECIMAL(10,2) NOT NULL,    -- 일일 목표 예산 (예: 20000.00)
    spent_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,  -- 하루 동안 소비한 총액
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (daily_budget_id),
    UNIQUE KEY uq_member_date (member_id, budget_date),
    CONSTRAINT fk_dailybudget_member FOREIGN KEY (member_id)
    REFERENCES member(member_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 3. 음식점 및 음식 정보 테이블

-- 3.1. 음식점 테이블 (FoodStore)
CREATE TABLE IF NOT EXISTS food_store (
                                          food_store_id BIGINT NOT NULL AUTO_INCREMENT,
                                          name VARCHAR(255) NOT NULL,                          -- 음식점 이름
    store_type ENUM('RESTAURANT', 'SCHOOL_CAFETERIA', 'CONVENIENCE_STORE') NOT NULL,
    address VARCHAR(255) NOT NULL,                       -- 음식점 주소
    latitude DECIMAL(10,7) DEFAULT NULL,                 -- 위도
    longitude DECIMAL(10,7) DEFAULT NULL,                -- 경도
    phone VARCHAR(50) DEFAULT NULL,                      -- 음식점 전화번호
    open_time TIME DEFAULT NULL,                         -- 오픈 시간
    close_time TIME DEFAULT NULL,                        -- 마감 시간
    external_id VARCHAR(255) DEFAULT NULL,               -- 외부 음식점 ID(KAKAO)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status VARCHAR(1) NOT NULL,                          -- 음식점 상태 (Y:활성화, N:삭제)
    PRIMARY KEY (food_store_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3.2. 음식 (메뉴) 테이블 (Food)
CREATE TABLE IF NOT EXISTS food (
                                    food_id BIGINT NOT NULL AUTO_INCREMENT,
                                    food_store_id BIGINT NOT NULL,
                                    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,  -- 음식 종류
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status VARCHAR(1) NOT NULL,      -- 음식 상태 (Y:활성화, N:삭제)
    PRIMARY KEY (food_id),
    CONSTRAINT fk_food_foodstore FOREIGN KEY (food_store_id)
    REFERENCES food_store(food_store_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 4. 즐겨찾기 테이블

-- 4.1. 즐겨찾는 음식점 테이블 (FavoriteFoodStore)
CREATE TABLE IF NOT EXISTS favorite_food_store (
                                                   favorite_id BIGINT NOT NULL AUTO_INCREMENT,
                                                   member_id BIGINT NOT NULL,
                                                   food_store_id BIGINT NOT NULL,
                                                   rank_order INT DEFAULT NULL,
                                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                   PRIMARY KEY (favorite_id),
    CONSTRAINT fk_favorite_member FOREIGN KEY (member_id)
    REFERENCES member(member_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_favorite_foodstore FOREIGN KEY (food_store_id)
    REFERENCES food_store(food_store_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uq_favorite (member_id, food_store_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 5. 지출 내역 테이블

-- 5.1. 지출 내역 테이블 (Expenditure)
CREATE TABLE IF NOT EXISTS expenditure (
                                           expenditure_id BIGINT NOT NULL AUTO_INCREMENT,
                                           member_id BIGINT NOT NULL,
                                           food_store_id BIGINT DEFAULT NULL,
                                           transaction_date DATETIME NOT NULL,
                                           amount DECIMAL(10,2) NOT NULL,
    source_type ENUM('MANUAL', 'SMS', 'CAPTURE', 'RECOMMENDATION', 'IN_APP') NOT NULL,
    description TEXT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (expenditure_id),
    CONSTRAINT fk_expenditure_member FOREIGN KEY (member_id)
    REFERENCES member(member_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_expenditure_foodstore FOREIGN KEY (food_store_id)
    REFERENCES food_store(food_store_id)
                                                            ON DELETE SET NULL ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5.2. 주문서 테이블 (ExpenditureFood)
CREATE TABLE IF NOT EXISTS expenditure_food (
                                                expenditure_food_id BIGINT NOT NULL AUTO_INCREMENT,
                                                expenditure_id BIGINT NOT NULL,
                                                food_id BIGINT NOT NULL,
                                                quantity INT NOT NULL DEFAULT 1,              -- 주문한 개수
                                                unit_price DECIMAL(10,2) NOT NULL,            -- 주문 당시 단위 가격
    total_price DECIMAL(10,2) NOT NULL,           -- quantity * unit_price 계산 값
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (expenditure_food_id),
    CONSTRAINT fk_expenditurefood_expenditure FOREIGN KEY (expenditure_id)
    REFERENCES expenditure(expenditure_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_expenditurefood_food FOREIGN KEY (food_id)
    REFERENCES food(food_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 6. 사용자 음식 취향 테이블

-- 6.1. 사용자 음식 취향 선호 테이블 (MemberFoodPreference)
CREATE TABLE IF NOT EXISTS member_food_preference (
                                                      preference_id BIGINT NOT NULL AUTO_INCREMENT,
                                                      member_id BIGINT NOT NULL,
                                                      food_keyword VARCHAR(100) NOT NULL,
    food_category VARCHAR(100) NOT NULL,
    preference_order INT NOT NULL,
    is_dislike BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (preference_id),
    CONSTRAINT fk_preference_member FOREIGN KEY (member_id)
    REFERENCES member(member_id)
                                                            ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uq_member_food (member_id, food_keyword)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
