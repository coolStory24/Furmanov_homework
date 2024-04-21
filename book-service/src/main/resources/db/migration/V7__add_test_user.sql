-- Insert test users

INSERT INTO users (username, password, roles) VALUES (
    'test_admin',
    '$2a$12$b6hiVWqQGT/qbeGEkis/8updinwkcEegoFMn/rZ3Mb9jTyWBTBiKO',
    '["ADMIN", "READER"]'
), (
     'test_user',
     '$2a$12$b6hiVWqQGT/qbeGEkis/8updinwkcEegoFMn/rZ3Mb9jTyWBTBiKO',
     '["READER"]'
);
