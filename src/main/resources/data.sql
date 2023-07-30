

INSERT INTO web_users(access_token, username)
VALUES ('superuser', 'Super Puper User');

INSERT INTO super_users(web_user_id)
VALUES ((SELECT web_users.id FROM web_users WHERE web_users.access_token = 'superuser'))