-- 마스터 관리자 계정 6개 생성
-- 비밀번호: admin1234! (BCrypt 암호화됨)

INSERT INTO p_user (user_id, username, password, user_role, name, nickname)
SELECT gen_random_uuid(), 'admin1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MASTER', '마스터1', 'MASTER_1'
WHERE NOT EXISTS (SELECT 1 FROM p_user WHERE username = 'admin1');

INSERT INTO p_user (user_id, username, password, user_role, name, nickname)
SELECT gen_random_uuid(), 'admin2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MASTER', '마스터2', 'MASTER_2'
WHERE NOT EXISTS (SELECT 1 FROM p_user WHERE username = 'admin2');

INSERT INTO p_user (user_id, username, password, user_role, name, nickname)
SELECT gen_random_uuid(), 'admin3', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MASTER', '마스터3', 'MASTER_3'
WHERE NOT EXISTS (SELECT 1 FROM p_user WHERE username = 'admin3');

INSERT INTO p_user (user_id, username, password, user_role, name, nickname)
SELECT gen_random_uuid(), 'admin4', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MASTER', '마스터4', 'MASTER_4'
WHERE NOT EXISTS (SELECT 1 FROM p_user WHERE username = 'admin4');

INSERT INTO p_user (user_id, username, password, user_role, name, nickname)
SELECT gen_random_uuid(), 'admin5', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MASTER', '마스터5', 'MASTER_5'
WHERE NOT EXISTS (SELECT 1 FROM p_user WHERE username = 'admin5');

INSERT INTO p_user (user_id, username, password, user_role, name, nickname)
SELECT gen_random_uuid(), 'admin6', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MASTER', '마스터6', 'MASTER_6'
WHERE NOT EXISTS (SELECT 1 FROM p_user WHERE username = 'admin6');
