ALTER TABLE services ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE wallets ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE transactions ALTER COLUMN currency TYPE VARCHAR(3);

INSERT INTO services (name, price, currency) VALUES
  ('Premium Weekly Wellness Report', 99.00, 'INR'),
  ('Sleep Coaching Session', 499.00, 'INR'),
  ('Nutrition Plan', 299.00, 'INR');

INSERT INTO wallets (user_id, balance, currency)
SELECT id, 1000.00, 'INR' FROM users;