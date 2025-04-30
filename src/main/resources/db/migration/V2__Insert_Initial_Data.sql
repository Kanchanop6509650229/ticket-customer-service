-- Insert roles
INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN'),
('ROLE_ORGANIZER');

-- Insert admin user (password: admin123)
INSERT INTO users (username, password, name, email, phone) VALUES
('admin', '$2a$10$b0MjPqQYqGY7QRK5Qc.1NuHPnq3W4JpMnKXYjT.k1t0lDjqaBgdT.', 'Admin User', 'admin@eventticket.com', '0812345678');

-- Insert organizer user (password: organizer123)
INSERT INTO users (username, password, name, email, phone) VALUES
('organizer', '$2a$10$b0MjPqQYqGY7QRK5Qc.1NuHPnq3W4JpMnKXYjT.k1t0lDjqaBgdT.', 'Organizer User', 'organizer@eventticket.com', '0823456789');

-- Insert regular user (password: user123)
INSERT INTO users (username, password, name, email, phone) VALUES
('user', '$2a$10$b0MjPqQYqGY7QRK5Qc.1NuHPnq3W4JpMnKXYjT.k1t0lDjqaBgdT.', 'Regular User', 'user@eventticket.com', '0834567890');

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 2), -- admin has ROLE_ADMIN
(2, 3), -- organizer has ROLE_ORGANIZER
(3, 1); -- user has ROLE_USER

-- Insert sample event tickets for BNK48 Concert (Event ID 1)
-- VIP Tickets
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES
('1', 'VIP', 5000.00, 'A', 'A1', 'AVAILABLE'),
('1', 'VIP', 5000.00, 'A', 'A2', 'AVAILABLE'),
('1', 'VIP', 5000.00, 'A', 'A3', 'AVAILABLE'),
('1', 'VIP', 5000.00, 'A', 'A4', 'AVAILABLE'),
('1', 'VIP', 5000.00, 'A', 'A5', 'AVAILABLE');

-- Regular Tickets
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES
('1', 'Regular', 2500.00, 'B', 'B1', 'AVAILABLE'),
('1', 'Regular', 2500.00, 'B', 'B2', 'AVAILABLE'),
('1', 'Regular', 2500.00, 'B', 'B3', 'AVAILABLE'),
('1', 'Regular', 2500.00, 'B', 'B4', 'AVAILABLE'),
('1', 'Regular', 2500.00, 'B', 'B5', 'AVAILABLE');

-- Economy Tickets
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES
('1', 'Economy', 1500.00, 'C', 'C1', 'AVAILABLE'),
('1', 'Economy', 1500.00, 'C', 'C2', 'AVAILABLE'),
('1', 'Economy', 1500.00, 'C', 'C3', 'AVAILABLE'),
('1', 'Economy', 1500.00, 'C', 'C4', 'AVAILABLE'),
('1', 'Economy', 1500.00, 'C', 'C5', 'AVAILABLE');

-- Insert sample event tickets for BNK48 Handshake Event (Event ID 2)
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES
('2', 'Standard', 1500.00, 'Main', 'M1', 'AVAILABLE'),
('2', 'Standard', 1500.00, 'Main', 'M2', 'AVAILABLE'),
('2', 'Standard', 1500.00, 'Main', 'M3', 'AVAILABLE'),
('2', 'Standard', 1500.00, 'Main', 'M4', 'AVAILABLE'),
('2', 'Standard', 1500.00, 'Main', 'M5', 'AVAILABLE');

-- Insert sample event tickets for Slot Machine Live (Event ID 3)
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES
('3', 'VIP', 3500.00, 'A', 'A1', 'AVAILABLE'),
('3', 'VIP', 3500.00, 'A', 'A2', 'AVAILABLE'),
('3', 'VIP', 3500.00, 'A', 'A3', 'AVAILABLE'),
('3', 'Regular', 2000.00, 'B', 'B1', 'AVAILABLE'),
('3', 'Regular', 2000.00, 'B', 'B2', 'AVAILABLE'),
('3', 'Regular', 2000.00, 'B', 'B3', 'AVAILABLE');

-- Insert sample event tickets for Thai Rock Festival (Event ID 4)
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES
('4', 'VIP', 4500.00, 'A', 'A1', 'AVAILABLE'),
('4', 'VIP', 4500.00, 'A', 'A2', 'AVAILABLE'),
('4', 'Regular', 2500.00, 'B', 'B1', 'AVAILABLE'),
('4', 'Regular', 2500.00, 'B', 'B2', 'AVAILABLE'),
('4', 'Standing', 1800.00, 'C', 'C1', 'AVAILABLE'),
('4', 'Standing', 1800.00, 'C', 'C2', 'AVAILABLE');

-- Insert sample bookings for different events
INSERT INTO bookings (user_id, event_id, total_amount, status) VALUES
(3, '1', 10000.00, 'PAID'),
(3, '2', 3000.00, 'PAID'),
(2, '3', 7000.00, 'PAID'),
(3, '4', 9000.00, 'CONFIRMED');

-- Update tickets for the bookings
UPDATE tickets
SET booking_id = 1, status = 'SOLD', owner_id = 3, purchase_date = NOW()
WHERE id IN (1, 2);

UPDATE tickets
SET booking_id = 2, status = 'SOLD', owner_id = 3, purchase_date = NOW()
WHERE id IN (52, 53);

UPDATE tickets
SET booking_id = 3, status = 'SOLD', owner_id = 2, purchase_date = NOW()
WHERE id IN (60, 61);

UPDATE tickets
SET booking_id = 4, status = 'RESERVED', owner_id = 3, purchase_date = NOW()
WHERE id IN (69, 70);

-- Insert payments for the bookings
INSERT INTO payments (booking_id, user_id, amount, status, method, transaction_id) VALUES
(1, 3, 10000.00, 'COMPLETED', 'CREDIT_CARD', 'txn_123456789'),
(2, 3, 3000.00, 'COMPLETED', 'MOBILE_PAYMENT', 'txn_987654321'),
(3, 2, 7000.00, 'COMPLETED', 'BANK_TRANSFER', 'txn_456789123');

-- Insert sample chat history
INSERT INTO chat_history (user_id, session_id, query, response, event_id, confidence) VALUES
(3, 'session123', 'Can I get a refund for my tickets?', 'Yes, you can get a refund if you cancel at least 7 days before the event. You will receive 80% of the ticket price back.', '1', 0.95),
(3, 'session123', 'What time does the event start?', 'The BNK48 Concert starts at 18:00 on June 15, 2025. Doors open 1 hour before the show.', '1', 0.98),
(3, 'session456', 'How do I get to the venue?', 'Impact Arena is located at Popular Road, Pak Kret, Bangkok. You can take the BTS to Mo Chit station and then a taxi, or use the shuttle service from Central Ladprao.', '1', 0.92),
(3, 'session456', 'Is there parking available?', 'Yes, Impact Arena has extensive parking facilities. Parking fee is 50 baht per entry.', '1', 0.97),
(2, 'session789', 'What are the benefits of VIP tickets?', 'VIP tickets for Slot Machine Live include Meet & Greet, Exclusive Merchandise, and Priority Entry.', '3', 0.96),
(3, 'session789', 'Can I bring food to the event?', 'Outside food and beverages are not allowed in the Thai Rock Festival. There will be food vendors inside the venue.', '4', 0.93);