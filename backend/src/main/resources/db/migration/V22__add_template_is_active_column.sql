-- -------------------------------
-- Template 테이블에 isActive 컬럼 추가
-- -------------------------------
ALTER TABLE template
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;

-- -------------------------------
-- Template 테이블에 인덱스 추가
-- -------------------------------
CREATE INDEX idx_template_active_id ON template (is_active, id);
