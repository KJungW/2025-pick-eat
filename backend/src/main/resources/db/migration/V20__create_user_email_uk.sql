-- -------------------------------
-- 유저 테이블에 nickname 컬럼 기준 가상 필드 생성
-- -------------------------------
ALTER TABLE users
ADD COLUMN nickname_active VARCHAR(255)
GENERATED ALWAYS AS (
  CASE
    WHEN deleted_at IS NULL THEN nickname
    ELSE NULL
  END
) STORED;

-- -------------------------------
-- nickname_active 가상 필드를 통해 유니크 조건 적용
-- -------------------------------
CREATE UNIQUE INDEX uk_users_nickname_active
ON users (nickname_active);

-- -------------------------------
-- 기존 nickname 관련 인덱스 수정
-- -------------------------------
DROP INDEX uk_users_nickname ON users;
CREATE INDEX idx_users_nickname_deleted_at
ON users (nickname, deleted_at);
