-- -------------------------------
-- TemplateWish 테이블의 distance 컬럼을 nullable로 변경
-- -------------------------------A
ALTER TABLE template_wish MODIFY COLUMN distance int NULL;
