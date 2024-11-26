-- Drop existing tables if they exist
DROP TABLE IF EXISTS LostTickets;
DROP TABLE IF EXISTS Feedback;
DROP TABLE IF EXISTS CarWashRequests;
DROP TABLE IF EXISTS ParkingViolations;
DROP TABLE IF EXISTS ParkingHistory;
DROP TABLE IF EXISTS Reservations;
DROP TABLE IF EXISTS ParkingCards;
DROP TABLE IF EXISTS ParkingSpots;
DROP TABLE IF EXISTS Users;

-- Create Users table
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create ParkingCards table
CREATE TABLE ParkingCards (
    card_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    card_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    issued_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Create ParkingSpots table
CREATE TABLE ParkingSpots (
    spot_id INT PRIMARY KEY AUTO_INCREMENT,
    spot_number VARCHAR(10) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    floor_number INT NOT NULL,
    type VARCHAR(20) DEFAULT 'STANDARD',
    reserved_by INT,
    FOREIGN KEY (reserved_by) REFERENCES Users(user_id)
);

-- Create Reservations table
CREATE TABLE Reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    spot_id INT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    reservation_start TIMESTAMP NOT NULL,
    reservation_end TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    extension_count INT DEFAULT 0,
    charges DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (spot_id) REFERENCES ParkingSpots(spot_id)
);

-- Create ParkingHistory table
CREATE TABLE ParkingHistory (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    spot_id INT NOT NULL,
    reservation_id INT,
    vehicle_number VARCHAR(20) NOT NULL,
    check_in TIMESTAMP NOT NULL,
    check_out TIMESTAMP,
    duration INT DEFAULT 0,
    charges DECIMAL(10,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    card_number VARCHAR(50) NOT NULL,
    plate_number VARCHAR(20) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (spot_id) REFERENCES ParkingSpots(spot_id),
    FOREIGN KEY (reservation_id) REFERENCES Reservations(reservation_id)
);

-- Create ParkingViolations table
CREATE TABLE ParkingViolations (
    violation_id INT PRIMARY KEY AUTO_INCREMENT,
    history_id INT NOT NULL,
    user_id INT NOT NULL,
    violation_type VARCHAR(50) NOT NULL,
    description TEXT,
    fine_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    disputed BOOLEAN DEFAULT FALSE,
    dispute_reason TEXT,
    FOREIGN KEY (history_id) REFERENCES ParkingHistory(history_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Create CarWashRequests table
CREATE TABLE CarWashRequests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    history_id INT NOT NULL,
    user_id INT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    requested_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scheduled_time TIMESTAMP NOT NULL,
    completed_time TIMESTAMP,
    charges DECIMAL(10,2) DEFAULT 25.00,
    FOREIGN KEY (history_id) REFERENCES ParkingHistory(history_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Create Feedback table
CREATE TABLE Feedback (
    feedback_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    feedback_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    acknowledged_by INT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (acknowledged_by) REFERENCES Users(user_id)
);

-- Create LostTickets table
CREATE TABLE LostTickets (
    lost_ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    session_id INT NOT NULL,
    user_id INT NOT NULL,
    reported_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fine DECIMAL(10,2) DEFAULT 50.00,
    verified BOOLEAN DEFAULT FALSE,
    verification_method VARCHAR(50),
    resolved_time TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    FOREIGN KEY (session_id) REFERENCES ParkingHistory(history_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Insert sample users with hashed passwords (in reality, these would be properly hashed)
INSERT INTO Users (username, password_hash, email, phone_number, role, status) VALUES
('admin', 'admin123', 'admin@pms.com', '1234567890', 'ADMIN', 'ACTIVE'),
('john_doe', 'pass123', 'john@example.com', '9876543210', 'USER', 'ACTIVE'),
('jane_smith', 'pass456', 'jane@example.com', '5555555555', 'USER', 'ACTIVE'),
('bob_wilson', 'pass789', 'bob@example.com', '4444444444', 'USER', 'PENDING'),
('alice_brown', 'passabc', 'alice@example.com', '3333333333', 'USER', 'ACTIVE');

-- Insert parking cards
INSERT INTO ParkingCards (user_id, card_number, expiry_date) VALUES
(2, 'PMS-001-2024', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 YEAR)),
(3, 'PMS-002-2024', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 YEAR)),
(5, 'PMS-003-2024', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 YEAR));

-- Insert parking spots (multiple floors and types)
INSERT INTO ParkingSpots (spot_number, floor_number, type, status) VALUES
('A1-01', 1, 'STANDARD', 'AVAILABLE'),
('A1-02', 1, 'STANDARD', 'OCCUPIED'),
('A1-03', 1, 'STANDARD', 'AVAILABLE'),
('B1-01', 1, 'PREMIUM', 'AVAILABLE'),
('B1-02', 1, 'PREMIUM', 'RESERVED'),
('C2-01', 2, 'STANDARD', 'AVAILABLE'),
('C2-02', 2, 'STANDARD', 'MAINTENANCE'),
('D2-01', 2, 'PREMIUM', 'AVAILABLE'),
('D2-02', 2, 'PREMIUM', 'AVAILABLE'),
('E3-01', 3, 'STANDARD', 'AVAILABLE');

-- Insert reservations (including some historical ones)
INSERT INTO Reservations (user_id, spot_id, vehicle_number, reservation_start, reservation_end, status, charges) VALUES
(2, 4, 'ABC123', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 'ACTIVE', 25.00),
(3, 5, 'XYZ789', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 'PENDING', 25.00),
(5, 8, 'DEF456', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -1 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 'COMPLETED', 50.00);

-- Insert parking history
INSERT INTO ParkingHistory (user_id, spot_id, reservation_id, vehicle_number, check_in, check_out, duration, charges, status, receipt_number, card_number, plate_number) VALUES
(2, 1, NULL, 'ABC123', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 HOUR), NULL, 180, 15.00, 'ACTIVE', 'RCP-001-2024', 'PMS-001-2024', 'ABC123'),
(3, 2, NULL, 'XYZ789', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 23 HOUR), 60, 10.00, 'COMPLETED', 'RCP-002-2024', 'PMS-002-2024', 'XYZ789'),
(5, 3, NULL, 'DEF456', CURRENT_TIMESTAMP, NULL, 0, 0.00, 'ACTIVE', 'RCP-003-2024', 'PMS-003-2024', 'DEF456');

-- Insert violations
INSERT INTO ParkingViolations (history_id, user_id, violation_type, description, fine_amount, status) VALUES
(2, 3, 'OVERTIME_PARKING', 'Vehicle parked beyond permitted time', 50.00, 'PENDING'),
(1, 2, 'WRONG_SPOT', 'Vehicle parked in reserved spot', 75.00, 'DISPUTED'),
(3, 5, 'IMPROPER_PARKING', 'Vehicle not parked within designated lines', 25.00, 'RESOLVED');

-- Insert car wash requests
INSERT INTO CarWashRequests (history_id, user_id, vehicle_number, status, scheduled_time) VALUES
(1, 2, 'ABC123', 'PENDING', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),
(2, 3, 'XYZ789', 'COMPLETED', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 HOUR)),
(3, 5, 'DEF456', 'SCHEDULED', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 4 HOUR));

-- Insert feedback
INSERT INTO Feedback (user_id, feedback_type, content, rating, status) VALUES
(2, 'SERVICE', 'Great parking experience!', 5, 'PENDING'),
(3, 'FACILITY', 'Car wash service was excellent', 4, 'ACKNOWLEDGED'),
(5, 'COMPLAINT', 'Parking spot was difficult to find', 2, 'PENDING');

-- Insert lost tickets
INSERT INTO LostTickets (session_id, user_id, status, fine) VALUES
(1, 2, 'PENDING', 50.00),
(2, 3, 'RESOLVED', 50.00);

-- Create indexes for better query performance
CREATE INDEX idx_user_status ON Users(status);
CREATE INDEX idx_card_number ON ParkingCards(card_number);
CREATE INDEX idx_spot_status ON ParkingSpots(status);
CREATE INDEX idx_reservations_dates ON Reservations(reservation_start, reservation_end);
CREATE INDEX idx_history_dates ON ParkingHistory(check_in, check_out);
CREATE INDEX idx_violations_status ON ParkingViolations(status);
CREATE INDEX idx_feedback_status ON Feedback(status);
CREATE INDEX idx_carwash_status ON CarWashRequests(status);
CREATE INDEX idx_lost_tickets_status ON LostTickets(status);