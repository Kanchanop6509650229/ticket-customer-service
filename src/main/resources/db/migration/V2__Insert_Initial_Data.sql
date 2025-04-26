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

-- Insert sample event tickets for BNK48 Concert (assuming eventId 'event123')
-- VIP Tickets
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES 
('event123', 'vip', 5000.00, 'A', 'A1', 'AVAILABLE'),
('event123', 'vip', 5000.00, 'A', 'A2', 'AVAILABLE'),
('event123', 'vip', 5000.00, 'A', 'A3', 'AVAILABLE'),
('event123', 'vip', 5000.00, 'A', 'A4', 'AVAILABLE'),
('event123', 'vip', 5000.00, 'A', 'A5', 'AVAILABLE');

-- Regular Tickets
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES 
('event123', 'regular', 2500.00, 'B', 'B1', 'AVAILABLE'),
('event123', 'regular', 2500.00, 'B', 'B2', 'AVAILABLE'),
('event123', 'regular', 2500.00, 'B', 'B3', 'AVAILABLE'),
('event123', 'regular', 2500.00, 'B', 'B4', 'AVAILABLE'),
('event123', 'regular', 2500.00, 'B', 'B5', 'AVAILABLE');

-- Insert sample event tickets for BNK48 Handshake (assuming eventId 'event456')
INSERT INTO tickets (event_id, type, price, section, seat_number, status) VALUES 
('event456', 'standard', 1500.00, 'Main', 'M1', 'AVAILABLE'),
('event456', 'standard', 1500.00, 'Main', 'M2', 'AVAILABLE'),
('event456', 'standard', 1500.00, 'Main', 'M3', 'AVAILABLE'),
('event456', 'standard', 1500.00, 'Main', 'M4', 'AVAILABLE'),
('event456', 'standard', 1500.00, 'Main', 'M5', 'AVAILABLE');

-- Insert sample bookings
INSERT INTO bookings (user_id, event_id, total_amount, status) VALUES 
(3, 'event123', 10000.00, 'PAID');

-- Update tickets for the booking
UPDATE tickets 
SET booking_id = 1, status = 'SOLD', owner_id = 3, purchase_date = NOW() 
WHERE id IN (1, 2);

-- Insert payment for the booking
INSERT INTO payments (booking_id, user_id, amount, status, method, transaction_id) VALUES 
(1, 3, 10000.00, 'COMPLETED', 'CREDIT_CARD', 'txn_123456789');

-- Insert sample chat history
INSERT INTO chat_history (user_id, session_id, query, response, event_id, confidence) VALUES 
(3, 'session123', 'Can I get a refund for my tickets?', 'Yes, you can get a refund if you cancel at least 7 days before the event. You will receive 80% of the ticket price back.', 'event123', 0.95),
(3, 'session123', 'What time does the event start?', 'The BNK48 Concert starts at 18:00 on June 15, 2025. Doors open 1 hour before the show.', 'event123', 0.98);