UPDATE member SET email = lower(email);

ALTER TABLE member ADD CONSTRAINT member_email_lowercase_check CHECK (email = lower(email));
